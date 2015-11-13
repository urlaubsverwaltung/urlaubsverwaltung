package org.synyx.urlaubsverwaltung.core.account.service;

import org.apache.log4j.Logger;

import org.joda.time.DateMidnight;
import org.joda.time.Months;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import org.synyx.urlaubsverwaltung.core.account.domain.Account;
import org.synyx.urlaubsverwaltung.core.calendar.WorkDaysService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.util.DateUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;

import java.util.Optional;


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
    private static final int ROUNDING_LOWER_BOUND = 30;
    private static final int ROUNDING_UPPER_BOUND = 50;

    private final AccountService accountService;
    private final WorkDaysService calendarService;
    private final VacationDaysService vacationDaysService;

    @Autowired
    AccountInteractionServiceImpl(AccountService accountService, WorkDaysService calendarService,
        VacationDaysService vacationDaysService) {

        this.accountService = accountService;
        this.calendarService = calendarService;
        this.vacationDaysService = vacationDaysService;
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

        LOG.info("Updated holidays account: " + account);

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
     * @param  notRoundedVacationDays  double
     *
     * @return  {@link BigDecimal} rounded value
     */
    private BigDecimal round(double notRoundedVacationDays) {

        BigDecimal bd = new BigDecimal(notRoundedVacationDays).setScale(2, RoundingMode.HALF_UP);

        String bdString = bd.toString();
        bdString = bdString.split("\\.")[1];

        Integer referenceValue = Integer.parseInt(bdString);
        BigDecimal days;

        // please notice: bd.intValue() is an Integer, e.g. 11
        int bdIntValue = bd.intValue();

        if (referenceValue > 0 && referenceValue < ROUNDING_LOWER_BOUND) {
            days = new BigDecimal(bdIntValue);
        } else {
            if (referenceValue >= ROUNDING_LOWER_BOUND && referenceValue < ROUNDING_UPPER_BOUND) {
                days = new BigDecimal(bdIntValue + 0.5);
            } else if (referenceValue > ROUNDING_UPPER_BOUND) {
                days = new BigDecimal(bdIntValue + 1);
            } else {
                // default fallback because I'm a chicken
                days = new BigDecimal(notRoundedVacationDays).setScale(2);
            }
        }

        return days;
    }


    @Override
    public void updateRemainingVacationDays(int year, Person person) {

        int startYear = year;

        boolean hasNextAccount = true;

        while (hasNextAccount) {
            Optional<Account> nextYearsHolidaysAccountOptional = accountService.getHolidaysAccount(startYear + 1,
                    person);

            if (nextYearsHolidaysAccountOptional.isPresent()) {
                Account changedHolidaysAccount = accountService.getHolidaysAccount(startYear, person).get();
                Account nextYearsHolidaysAccount = nextYearsHolidaysAccountOptional.get();

                BigDecimal leftVacationDays = vacationDaysService.calculateTotalLeftVacationDays(
                        changedHolidaysAccount);
                nextYearsHolidaysAccount.setRemainingVacationDays(leftVacationDays);

                // number of not expiring remaining vacation days is greater than remaining vacation days
                if (nextYearsHolidaysAccount.getRemainingVacationDaysNotExpiring().compareTo(leftVacationDays) == 1) {
                    nextYearsHolidaysAccount.setRemainingVacationDaysNotExpiring(leftVacationDays);
                }

                accountService.save(nextYearsHolidaysAccount);

                startYear++;
            } else {
                hasNextAccount = false;
            }
        }
    }


    @Override
    public Account autoCreateHolidaysAccount(Account referenceAccount) {

        int nextYear = referenceAccount.getYear() + 1;

        BigDecimal leftVacationDays = vacationDaysService.calculateTotalLeftVacationDays(referenceAccount);

        return createHolidaysAccount(referenceAccount.getPerson(), DateUtil.getFirstDayOfYear(nextYear),
                DateUtil.getLastDayOfYear(nextYear), referenceAccount.getAnnualVacationDays(), leftVacationDays,
                BigDecimal.ZERO);
    }
}
