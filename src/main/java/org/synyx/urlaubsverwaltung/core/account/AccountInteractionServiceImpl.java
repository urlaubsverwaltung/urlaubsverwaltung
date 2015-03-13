package org.synyx.urlaubsverwaltung.core.account;

import org.apache.log4j.Logger;

import org.joda.time.DateMidnight;
import org.joda.time.Months;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import org.synyx.urlaubsverwaltung.core.application.service.CalculationService;
import org.synyx.urlaubsverwaltung.core.calendar.OwnCalendarService;
import org.synyx.urlaubsverwaltung.core.person.Person;

import java.math.BigDecimal;
import java.math.RoundingMode;


/**
 * Implementation of interface {@link AccountInteractionService}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Service
@Transactional
class AccountInteractionServiceImpl implements AccountInteractionService {

    private static final Logger LOG = Logger.getLogger(AccountInteractionServiceImpl.class);

    private static final int MONTHS_PER_YEAR = 12;
    private static final int WEEKDAYS_PER_MONTH = 21;

    private final AccountService accountService;
    private final OwnCalendarService calendarService;
    private final CalculationService calculationService;

    @Autowired
    AccountInteractionServiceImpl(AccountService accountService, OwnCalendarService calendarService,
        CalculationService calculationService) {

        this.accountService = accountService;
        this.calendarService = calendarService;
        this.calculationService = calculationService;
    }

    @Override
    public Account createHolidaysAccount(Person person, DateMidnight validFrom, DateMidnight validTo, BigDecimal days,
        BigDecimal remainingDays, BigDecimal remainingDaysNotExpiring) {

        Account account = new Account(person, validFrom.toDate(), validTo.toDate(), days, remainingDays,
                remainingDaysNotExpiring);
        BigDecimal vacationDays = calculateActualVacationDays(account);
        account.setVacationDays(vacationDays);

        accountService.save(account);

        LOG.info("Created holidays account: " + account);

        return account;
    }


    @Override
    public Account editHolidaysAccount(Account account, DateMidnight validFrom, DateMidnight validTo, BigDecimal days,
        BigDecimal remainingDays, BigDecimal remainingDaysNotExpiring) {

        account.setValidFrom(validFrom);
        account.setValidTo(validTo);
        account.setAnnualVacationDays(days);
        account.setRemainingVacationDays(remainingDays);
        account.setRemainingVacationDaysNotExpiring(remainingDaysNotExpiring);

        BigDecimal vacationDays = calculateActualVacationDays(account);
        account.setVacationDays(vacationDays);

        accountService.save(account);

        LOG.info("Edited holidays account: " + account);

        return account;
    }


    BigDecimal calculateActualVacationDays(Account account) {

        DateMidnight start = account.getValidFrom();
        DateMidnight end = account.getValidTo();

        DateMidnight firstDayOfStartDatesMonth = start.dayOfMonth().withMinimumValue();
        DateMidnight lastDayOfEndDatesMonth = end.dayOfMonth().withMaximumValue();

        /**
         * NOTE: The actual vacation days are calculated the following way:
         *
         * (months * annual vacation days) / months per year
         *
         * Example:
         * (5 months * 28 days)/12 = 11.6666 = 12
         *
         * Please notice following rounding rules: 11.1 --> 11.0 11.3 --> 11.5 11.6 --> 12.0
         */
        double unroundedVacationDays = 0.0;

        // if validity period is not from 1st to last day of month, e.g. 15. May - 31. December
        if (!start.isEqual(firstDayOfStartDatesMonth) || !end.isEqual(lastDayOfEndDatesMonth)) {
            /* 21 work days per month - public holidays are handled like normal work days
             * (28 : 12) : 21 = 0.1111 vacation days per work day
             *
             * so you have to calculate (0.1111 * work days) to get the actual vacation days
             */

            double entitlementPerYear = account.getAnnualVacationDays().doubleValue();
            double entitlementPerMonth = entitlementPerYear / MONTHS_PER_YEAR;
            double entitlementPerDay = entitlementPerMonth / WEEKDAYS_PER_MONTH;
            double workDays = 0.0;

            DateMidnight startForMonthCalc = start;
            DateMidnight endForMonthCalc = end;

            if (!start.isEqual(firstDayOfStartDatesMonth)) {
                workDays += calendarService.getWeekDays(start, start.dayOfMonth().withMaximumValue());
                startForMonthCalc = start.plusMonths(1).dayOfMonth().withMinimumValue();
            }

            if (!end.isEqual(lastDayOfEndDatesMonth)) {
                workDays += calendarService.getWeekDays(end.dayOfMonth().withMinimumValue(), end);
                endForMonthCalc = end.minusMonths(1).dayOfMonth().withMaximumValue();
            }

            unroundedVacationDays += entitlementPerDay * workDays;

            int fullMonths = getNumberOfMonthsForPeriod(startForMonthCalc, endForMonthCalc);

            if (fullMonths > 1) {
                fullMonths = (fullMonths * account.getAnnualVacationDays().intValue()) / MONTHS_PER_YEAR;
                unroundedVacationDays += fullMonths;
            }
        } else {
            // that's the simple case
            int months = getNumberOfMonthsForPeriod(start, end);
            unroundedVacationDays += (months * account.getAnnualVacationDays().doubleValue()) / MONTHS_PER_YEAR;
        }

        return round(unroundedVacationDays);
    }


    private int getNumberOfMonthsForPeriod(DateMidnight start, DateMidnight end) {

        return Months.monthsBetween(start, end).getMonths() + 1;
    }


    /**
     * Rounds the given value in a special way, rounding rules are: 11.1 --> 11.0 11.3 --> 11.5 11.6 --> 12.0
     *
     * @param  unroundedVacationDays  double
     *
     * @return  {@link BigDecimal} rounded value
     */
    private BigDecimal round(double unroundedVacationDays) {

        BigDecimal bd = new BigDecimal(unroundedVacationDays).setScale(2, RoundingMode.HALF_UP);

        String bdString = bd.toString();
        bdString = bdString.split("\\.")[1];

        Integer referenceValue = Integer.parseInt(bdString);
        BigDecimal days;

        // please notice: bd.intValue() is an Integer, e.g. 11
        int bdIntValue = bd.intValue();

        int lowerBound = 30;
        int upperBound = 50;

        if (referenceValue > 0 && referenceValue < lowerBound) {
            days = new BigDecimal(bdIntValue);
        } else {
            if (referenceValue >= lowerBound && referenceValue < upperBound) {
                days = new BigDecimal(bdIntValue + 0.5);
            } else if (referenceValue > upperBound) {
                days = new BigDecimal(bdIntValue + 1);
            } else {
                // default fallback because I'm a chicken
                days = new BigDecimal(unroundedVacationDays).setScale(2);
            }
        }

        return days;
    }


    @Override
    public void updateRemainingVacationDays(int year, Person person) {

        int startYear = year;
        int currentYear = DateMidnight.now().getYear();

        while (startYear <= currentYear) {
            Account holidaysAccount = accountService.getHolidaysAccount(startYear, person);

            if (holidaysAccount != null) {
                BigDecimal leftVacationDays = calculationService.calculateTotalLeftVacationDays(holidaysAccount);
                holidaysAccount.setRemainingVacationDays(leftVacationDays);

                // number of not expiring remaining vacation days is greater than remaining vacation days
                if (holidaysAccount.getRemainingVacationDaysNotExpiring().compareTo(leftVacationDays) == 1) {
                    holidaysAccount.setRemainingVacationDaysNotExpiring(leftVacationDays);
                }

                accountService.save(holidaysAccount);
            }

            startYear++;
        }
    }
}
