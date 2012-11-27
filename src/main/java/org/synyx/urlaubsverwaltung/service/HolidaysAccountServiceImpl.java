package org.synyx.urlaubsverwaltung.service;

import org.apache.log4j.Logger;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;
import org.joda.time.Months;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.transaction.annotation.Transactional;

import org.synyx.urlaubsverwaltung.calendar.OwnCalendarService;
import org.synyx.urlaubsverwaltung.dao.AccountDAO;
import org.synyx.urlaubsverwaltung.domain.Account;
import org.synyx.urlaubsverwaltung.domain.Person;

import java.math.BigDecimal;
import java.math.RoundingMode;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Transactional
public class HolidaysAccountServiceImpl implements HolidaysAccountService {

    private static final Logger LOG = Logger.getLogger("audit");
    private AccountDAO accountDAO;
    private OwnCalendarService calendarService;

    @Autowired
    public HolidaysAccountServiceImpl(AccountDAO accountDAO, OwnCalendarService calendarService) {

        this.accountDAO = accountDAO;
        this.calendarService = calendarService;
    }

    @Override
    public Account getHolidaysAccount(int year, Person person) {

        return accountDAO.getHolidaysAccountByYearAndPerson(year, person);
    }


    @Override
    public void createHolidaysAccount(Person person, DateMidnight validFrom, DateMidnight validTo, BigDecimal days,
        BigDecimal remaining, boolean remainingDaysExpire) {

        Account account = new Account(person, validFrom.toDate(), validTo.toDate(), days, remaining,
                remainingDaysExpire);
        BigDecimal vacationDays = calculateActualVacationDays(account);
        account.setVacationDays(vacationDays);
        accountDAO.save(account);

        LOG.info("Created holidays account for " + person.getLoginName() + " with following values: " + "{validFrom: "
            + validFrom.toString("dd.MM.yyyy") + ", validTo: " + validTo.toString("dd.MM.yyyy")
            + ", annualVacationDays: " + days + ", remainingDays: " + remaining + ", remainingDaysExpiring: "
            + remainingDaysExpire + "}");
    }


    @Override
    public void editHolidaysAccount(Account account, DateMidnight validFrom, DateMidnight validTo, BigDecimal days,
        BigDecimal remaining, boolean remainingDaysExpire) {

        account.setValidFrom(validFrom);
        account.setValidTo(validTo);
        account.setAnnualVacationDays(days);
        account.setRemainingVacationDays(remaining);
        account.setRemainingVacationDaysExpire(remainingDaysExpire);

        BigDecimal vacationDays = calculateActualVacationDays(account);
        account.setVacationDays(vacationDays);

        accountDAO.save(account);

        LOG.info("Edited holidays account of " + account.getPerson().getLoginName() + " with following values: "
            + "{validFrom: " + validFrom.toString("dd.MM.yyyy") + ", validTo: " + validTo.toString("dd.MM.yyyy")
            + ", annualVacationDays: " + days + ", remainingDays: " + remaining + ", remainingDaysExpiring: "
            + remainingDaysExpire + "}");
    }


    /**
     * Method to calculate the actual vacation days: (months * annual vacation days) / months per year e.g.: (5 months *
     * 28 days)/12 = 11.6666 = 12
     *
     * <p>Please notice following rounding rules: 11.1 --> 11.0 11.3 --> 11.5 11.6 --> 12.0</p>
     */
    @Override
    public BigDecimal calculateActualVacationDays(Account account) {

        DateMidnight start = account.getValidFrom();
        DateMidnight end = account.getValidTo();

        DateMidnight firstDayOfStartDatesMonth = start.dayOfMonth().withMinimumValue();
        DateMidnight lastDayOfEndDatesMonth = end.dayOfMonth().withMaximumValue();

        double unroundedVacationDays = 0.0;

        // if validity period is not from 1st to last day of month, e.g. 15. May - 31. December
        if (!start.isEqual(firstDayOfStartDatesMonth) || !end.isEqual(lastDayOfEndDatesMonth)) {
            /* 21 work days per month - public holidays are handled like normal work days
             * (28 : 12) : 21 = 0.1111 vacation days per work day
             *
             * so you have to calculate (0.1111 * work days) to get the actual vacation days
             */

            double entitlementPerMonth = 28.0 / 12.0;
            double entitlementPerDay = entitlementPerMonth / 21.0;
            double workDays = 0.0;

            DateMidnight startForMonthCalc = start;
            DateMidnight endForMonthCalc = end;

            if (!start.isEqual(firstDayOfStartDatesMonth)) {
                workDays += calendarService.getWorkDays(start, start.dayOfMonth().withMaximumValue());
                startForMonthCalc = start.plusMonths(1).dayOfMonth().withMinimumValue();
            }

            if (!end.isEqual(lastDayOfEndDatesMonth)) {
                workDays += calendarService.getWorkDays(end.dayOfMonth().withMinimumValue(), end);
                endForMonthCalc = end.minusMonths(1).dayOfMonth().withMaximumValue();
            }

            unroundedVacationDays += entitlementPerDay * workDays;

            int fullMonths = getNumberOfMonthsForPeriod(startForMonthCalc, endForMonthCalc);
            unroundedVacationDays += (fullMonths * account.getAnnualVacationDays().doubleValue()) / 12;
        } else {
            // that's the simple case
            int months = getNumberOfMonthsForPeriod(start, end);
            unroundedVacationDays += (months * account.getAnnualVacationDays().doubleValue()) / 12;
        }

        return roundItTheSpecialWay(unroundedVacationDays);
    }


    private int getNumberOfMonthsForPeriod(DateMidnight start, DateMidnight end) {

        return Months.monthsBetween(start, end).getMonths() + 1;
    }


    private BigDecimal roundItTheSpecialWay(double unroundedVacationDays) {

        BigDecimal bd = new BigDecimal(unroundedVacationDays).setScale(2, RoundingMode.HALF_UP);

        String bdString = bd.toString();
        bdString = bdString.split("\\.")[1];

        Integer referenceValue = Integer.parseInt(bdString);
        BigDecimal days;

        // please notice: bd.intValue() is an Integer, e.g. 11
        int bdIntValue = bd.intValue();

        if (referenceValue > 0 && referenceValue < 30) {
            days = new BigDecimal(bdIntValue);
        } else if (referenceValue >= 30 && referenceValue < 50) {
            days = new BigDecimal(bdIntValue + 0.5);
        } else if (referenceValue >= 50) {
            days = new BigDecimal(bdIntValue + 1);
        } else {
            // default fallback because I'm a scaredy cat
            days = new BigDecimal(unroundedVacationDays).setScale(2);
        }

        return days;
    }


    @Override
    public Account getOrCreateNewAccount(int year, Person person) {

        Account account = getHolidaysAccount(year, person);

        if (account == null) {
            Account lastYearsAccount = getHolidaysAccount(year - 1, person);

            if (lastYearsAccount != null) {
                createHolidaysAccount(person, new DateMidnight(year, DateTimeConstants.JANUARY, 1),
                    new DateMidnight(year, DateTimeConstants.DECEMBER, 31), lastYearsAccount.getAnnualVacationDays(),
                    BigDecimal.ZERO, lastYearsAccount.isRemainingVacationDaysExpire());
            } else {
                // maybe user tries to apply for leave for past year --> no account information
                // in this case just create an account for this year with the data of the current year's account
                // if a user is able to apply for leave, it's for sure that there is a current year's account
                Account currentYearAccount = getHolidaysAccount(DateMidnight.now().getYear(), person);

                createHolidaysAccount(person, new DateMidnight(year, DateTimeConstants.JANUARY, 1),
                    new DateMidnight(year, DateTimeConstants.DECEMBER, 31), currentYearAccount.getAnnualVacationDays(),
                    BigDecimal.ZERO, true);
            }

            account = getHolidaysAccount(year, person);
        }

        return account;
    }


    @Override
    public void save(Account account) {

        accountDAO.save(account);
    }
}
