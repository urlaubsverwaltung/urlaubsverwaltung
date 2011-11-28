/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.service;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;

import org.synyx.urlaubsverwaltung.calendar.OwnCalendarService;
import org.synyx.urlaubsverwaltung.domain.Application;
import org.synyx.urlaubsverwaltung.domain.HolidaysAccount;

import java.math.BigDecimal;


/**
 * @author  Aljona Murygina
 */
public class CalculationService {

    private static final int LAST_DAY = 31;
    private static final int FIRST_DAY = 1;

    private OwnCalendarService calendarService;

    public CalculationService(OwnCalendarService calendarService) {

        this.calendarService = calendarService;
    }

    /**
     * Calculating vacation days, holiday account is updated, special case April is noticed, i.e. it has to be noticed
     * that remaining vacation days may expire
     *
     * @param  application
     * @param  account
     */
    public void noticeApril(Application application, HolidaysAccount account) {

        DateMidnight start = application.getStartDate();
        DateMidnight end = application.getEndDate();

        // if application is before April
        if (application.getEndDate().getMonthOfYear() < DateTimeConstants.APRIL) {
            // how many remaining vacation days would be remain?
            BigDecimal newRemainingVacDays = (account.getRemainingVacationDays().subtract(
                        calendarService.getVacationDays(application, start, end)));

            if (newRemainingVacDays.compareTo(BigDecimal.ZERO) == -1) {
                // subtract difference from account
                account.setVacationDays(account.getVacationDays().add(newRemainingVacDays));

                // set remaining vacation days to 0
                account.setRemainingVacationDays(BigDecimal.ZERO);
            } else {
                // just save new remaining vacation days
                account.setRemainingVacationDays(newRemainingVacDays);
            }
        } else if (application.getStartDate().getMonthOfYear() >= DateTimeConstants.APRIL) {
            // if application is after April, there are no remaining vacation days
            account.setVacationDays(account.getVacationDays().subtract(
                    calendarService.getVacationDays(application, start, end)));
        } else {
            // if application is before AND after April
            BigDecimal beforeApril = calendarService.getVacationDays(application, application.getStartDate(),
                    new DateMidnight(start.getYear(), DateTimeConstants.MARCH, LAST_DAY));
            BigDecimal afterApril = calendarService.getVacationDays(application,
                    new DateMidnight(end.getYear(), DateTimeConstants.APRIL, FIRST_DAY), application.getEndDate());

            // how many remaining vacation days would be remain?
            BigDecimal newRemainingVacDays = account.getRemainingVacationDays().subtract(beforeApril);
            BigDecimal newVacDays = account.getVacationDays();

            if (newRemainingVacDays.compareTo(BigDecimal.ZERO) == -1) {
                // subtract difference from vacation days
                newVacDays = account.getVacationDays().add(newRemainingVacDays);

                // set remaining vacation days to 0
                newRemainingVacDays = BigDecimal.ZERO;
            }

            account.setRemainingVacationDays(newRemainingVacDays);
            account.setVacationDays(newVacDays.subtract(afterApril));
        }
    }


    /**
     * Calculating vacation days, holiday account is updated, special case January is noticed, i.e. two holiday accounts
     * are used
     *
     * @param  application
     * @param  accountCurrentYear
     * @param  accountNextYear
     */
    public void noticeJanuary(Application application, HolidaysAccount accountCurrentYear,
        HolidaysAccount accountNextYear) {

        DateMidnight start = application.getStartDate();
        DateMidnight end = application.getEndDate();

        BigDecimal beforeJan = calendarService.getVacationDays(application, application.getStartDate(),
                new DateMidnight(start.getYear(), DateTimeConstants.DECEMBER, LAST_DAY));
        BigDecimal afterJan = calendarService.getVacationDays(application,
                new DateMidnight(end.getYear(), DateTimeConstants.JANUARY, FIRST_DAY), application.getEndDate());

        // account of past year = just subtract days before January
        accountCurrentYear.setVacationDays(accountCurrentYear.getVacationDays().subtract(beforeJan));

        // remaining vacation days of new year = just substract days after 1.1.
        BigDecimal newRemainingVacDays = accountNextYear.getRemainingVacationDays().subtract(afterJan);
        BigDecimal newVacDays = accountNextYear.getVacationDays();

        if (newRemainingVacDays.compareTo(BigDecimal.ZERO) == -1) {
            // if remaining vacation days < 0 subtract difference from vacation days
            newVacDays = newVacDays.add(newRemainingVacDays);

            // set remaining vacation days to 0
            newRemainingVacDays = BigDecimal.ZERO;
        }

        accountNextYear.setRemainingVacationDays(newRemainingVacDays);
        accountNextYear.setVacationDays(newVacDays);
    }


    /**
     * Is used if application is cancelled or rejected. Used vacation days have to be added to leave account. This
     * method notices the special case January, i.e. if an application's start date and end date are not in the same
     * year
     *
     * @param  application
     * @param  accountCurrentYear
     * @param  accountNextYear
     * @param  entitlementCurrentYear
     * @param  entitlementNextYear
     */
    public void rollbackNoticeJanuary(Application application, HolidaysAccount accountCurrentYear,
        HolidaysAccount accountNextYear, BigDecimal entitlementCurrentYear, BigDecimal entitlementNextYear) {

        DateMidnight start = application.getStartDate();
        DateMidnight end = application.getEndDate();

        BigDecimal beforeJan = calendarService.getVacationDays(application, start,
                new DateMidnight(start.getYear(), DateTimeConstants.DECEMBER, LAST_DAY));
        BigDecimal afterJan = calendarService.getVacationDays(application,
                new DateMidnight(end.getYear(), DateTimeConstants.JANUARY, FIRST_DAY), end);

        BigDecimal oldVacationDays = accountCurrentYear.getVacationDays().add(beforeJan);

        // beforeJan fills account of year 1
        // account1.setVacationDays(account1.getVacationDays() + beforeJan);

        // if HolidaysAccount is greater than holiday entitlement, remaining vacation days have to be filled
        if (oldVacationDays.compareTo(entitlementCurrentYear) == 1) {
            accountCurrentYear.setRemainingVacationDays((accountCurrentYear.getRemainingVacationDays()).add(
                    oldVacationDays.subtract(entitlementCurrentYear)));
            accountCurrentYear.setVacationDays(oldVacationDays.subtract(
                    (oldVacationDays.subtract(entitlementCurrentYear))));
        } else {
            accountCurrentYear.setVacationDays(oldVacationDays);
        }

        // afterJan fills account of year 2
        // account2.setVacationDays(account2.getVacationDays() + afterJan);

        BigDecimal newVacationDays = accountNextYear.getVacationDays().add(afterJan);

        // if HolidaysAccount is greater than holiday entitlement, remaining vacation days have to be filled
        if (newVacationDays.compareTo(entitlementNextYear) == 1) {
            accountCurrentYear.setRemainingVacationDays((accountNextYear.getRemainingVacationDays()).add(
                    newVacationDays.subtract(entitlementNextYear)));
            accountNextYear.setVacationDays(newVacationDays.subtract(newVacationDays.subtract(entitlementNextYear)));
        } else {
            accountNextYear.setVacationDays(newVacationDays);
        }
    }


    /**
     * Is used if application is cancelled or rejected. Used vacation days have to be added to leave account. This
     * method notices the special case April, i.e. it must be noticed that remaining vacation days may expire.
     *
     * @param  application
     * @param  account
     * @param  entitlement
     */
    public void rollbackNoticeApril(Application application, HolidaysAccount account, BigDecimal entitlement) {

        DateMidnight start = application.getStartDate();
        DateMidnight end = application.getEndDate();

        // if holiday is after April, there are no remaining vacation days and haven't to be calculated/noticed
        if (start.getMonthOfYear() >= DateTimeConstants.APRIL) {
            BigDecimal gut = calendarService.getVacationDays(application, start, end);
            account.setVacationDays(gut.add(account.getVacationDays()));
        } else if (end.getMonthOfYear() < DateTimeConstants.APRIL) {
            // if holiday is before April, just add remaining vacation days if necessary
            BigDecimal gut = calendarService.getVacationDays(application, start, end).add(account.getVacationDays());

            if (gut.compareTo(entitlement) == 1) {
                account.setRemainingVacationDays(account.getRemainingVacationDays().add(gut.subtract(entitlement)));
                account.setVacationDays(entitlement);
            } else {
                account.setVacationDays(gut);
            }
        } else if (start.getMonthOfYear() <= DateTimeConstants.MARCH
                && end.getMonthOfYear() >= DateTimeConstants.APRIL) {
            rollbackOverApril(application, account, entitlement, start, end);
        }
    }


    public void rollbackOverApril(Application application, HolidaysAccount account, BigDecimal entitlement,
        DateMidnight start, DateMidnight end) {

        BigDecimal beforeApr = calendarService.getVacationDays(application, start,
                new DateMidnight(start.getYear(), DateTimeConstants.MARCH, LAST_DAY));
        BigDecimal afterApr = calendarService.getVacationDays(application,
                new DateMidnight(start.getYear(), DateTimeConstants.APRIL, FIRST_DAY), end);

        // at first: fill account with days after April
        // account1.setVacationDays(account1.getVacationDays() + afterApr);

        BigDecimal newVacationDays = account.getVacationDays().add(afterApr);

        // if account is equals entitlement
        // fill remaining vacation days with number of days before April
        if (newVacationDays.equals(entitlement)) {
            account.setRemainingVacationDays(account.getRemainingVacationDays().add(beforeApr));
            account.setVacationDays(newVacationDays);
        } else if (account.getVacationDays().compareTo(entitlement) == -1) {
            // if filling account with number of days after April doesn't result in: account < entitlement
            // add beforeApr to account
            // account1.setVacationDays(account1.getVacationDays() + beforeApr);
            newVacationDays = newVacationDays.add(beforeApr);

            // if account is now greater than entitlement
            // account > entitlement
            if (newVacationDays.compareTo(entitlement) >= 0) {
                // remaning vacation days are filled with remainder
                account.setRemainingVacationDays(account.getRemainingVacationDays().add(
                        newVacationDays.subtract(entitlement)));
                account.setVacationDays(entitlement);
            } else {
                account.setVacationDays(newVacationDays);
            }
        }
    }
}
