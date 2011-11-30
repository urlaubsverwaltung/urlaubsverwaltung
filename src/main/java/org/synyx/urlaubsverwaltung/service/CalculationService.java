
package org.synyx.urlaubsverwaltung.service;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;

import org.synyx.urlaubsverwaltung.calendar.OwnCalendarService;
import org.synyx.urlaubsverwaltung.domain.Application;
import org.synyx.urlaubsverwaltung.domain.HolidaysAccount;
import org.synyx.urlaubsverwaltung.domain.Person;

import java.math.BigDecimal;


/**
 * this class contains all methods to calculate with vacation days e.g. checking if an application is valid (are there
 * enough vacation days on person's HolidaysAccount) or noticing the special cases January and April for
 * calculation/updating HolidaysAccount(s)
 *
 * @author  Aljona Murygina
 */
public class CalculationService {

    private static final int LAST_DAY = 31;
    private static final int FIRST_DAY = 1;

    private OwnCalendarService calendarService;
    private HolidaysAccountService accountService;

    public CalculationService(OwnCalendarService calendarService, HolidaysAccountService accountService) {

        this.calendarService = calendarService;
        this.accountService = accountService;
    }

    /**
     * checks if given month is before April
     *
     * @param  month
     *
     * @return
     */
    private boolean isBeforeApril(int month) {

        if (month < DateTimeConstants.APRIL) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * checks if given month is after April
     *
     * @param  month
     *
     * @return
     */
    private boolean isAfterApril(int month) {

        if (month >= DateTimeConstants.APRIL) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * checks if the two given months are between March and April
     *
     * @param  monthStart
     * @param  monthEnd
     *
     * @return
     */
    private boolean isBetweenMarchAndApril(int monthStart, int monthEnd) {

        if (monthStart <= DateTimeConstants.MARCH && monthEnd >= DateTimeConstants.APRIL) {
            return true;
        } else {
            return false;
        }
    }


    private BigDecimal getDays(Application application, DateMidnight date, int month, boolean isLastDay) {

        int day;

        if (isLastDay) {
            day = LAST_DAY;
        } else {
            day = FIRST_DAY;
        }

        return calendarService.getVacationDays(application, date, new DateMidnight(date.getYear(), month, day));
    }


    /**
     * checks if application is valid in case of start date and end date of holiday are in the same year
     *
     * @param  application
     *
     * @return
     */
    public boolean checkApplicationOneYear(Application application) {

        BigDecimal days;
        DateMidnight start = application.getStartDate();
        DateMidnight end = application.getEndDate();
        Person person = application.getPerson();

        HolidaysAccount account = accountService.getAccountAndIfNotExistentCreateOne(start.getYear(), person);

        // if application is before April
        if (isBeforeApril(end.getMonthOfYear())) {
            days = calendarService.getVacationDays(application, start, end);

            // compareTo >= 0 means: greater than or equal (because it may be 0 or 1)
            if (((account.getRemainingVacationDays().add(account.getVacationDays())).subtract(days)).compareTo(
                        BigDecimal.valueOf(0.0)) >= 0) {
                return true;
            }
        } else if (isAfterApril(start.getMonthOfYear())) {
            // if application is after April, there are no remaining vacation days

            days = calendarService.getVacationDays(application, start, end);

            // compareTo >= 0 means: greater than or equal (because it may be 0 or 1)
            if ((account.getVacationDays().subtract(days)).compareTo(BigDecimal.valueOf(0.0)) >= 0) {
                return true;
            }
        } else {
            // if period of holiday is before AND after april
            BigDecimal beforeApril = getDays(application, start, DateTimeConstants.MARCH, true);
            BigDecimal afterApril = getDays(application, end, DateTimeConstants.APRIL, false);

            // subtract beforeApril from remaining vacation days
            BigDecimal result = account.getRemainingVacationDays().subtract(beforeApril);

            // if beforeApril is greater than remaining vacation days
            // (remaining vacation days minus beforeApril) has to be subtracted from vacation days
            if (result.compareTo(BigDecimal.ZERO) == 1) {
                beforeApril = (account.getRemainingVacationDays().subtract(beforeApril)).negate();
            } else {
                beforeApril = BigDecimal.ZERO;
            }

            if ((account.getVacationDays().subtract(beforeApril.add(afterApril))).compareTo(BigDecimal.ZERO) >= 0) {
                return true;
            }
        }

        return false;
    }


    /**
     * checks if application is valid in case of start date and end date of holiday are NOT in the same year
     *
     * @param  application
     *
     * @return
     */
    public boolean checkApplicationTwoYears(Application application) {

        Person person = application.getPerson();
        DateMidnight start = application.getStartDate();
        DateMidnight end = application.getEndDate();

        HolidaysAccount accountCurrentYear = accountService.getAccountAndIfNotExistentCreateOne(start.getYear(),
                person);
        HolidaysAccount accountNextYear = accountService.getAccountAndIfNotExistentCreateOne(end.getYear(), person);

        BigDecimal beforeJan = getDays(application, start, DateTimeConstants.DECEMBER, true);
        BigDecimal afterJan = getDays(application, end, DateTimeConstants.JANUARY, false);

        if (((accountCurrentYear.getVacationDays().subtract(beforeJan)).compareTo(BigDecimal.ZERO) >= 0)
                && (((accountNextYear.getVacationDays().add(accountNextYear.getRemainingVacationDays())).subtract(
                            afterJan)).compareTo(BigDecimal.ZERO) >= 0)) {
            return true;
        }

        return false;
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
        if (isBeforeApril(end.getMonthOfYear())) {
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
        } else if (isAfterApril(start.getMonthOfYear())) {
            // if application is after April, there are no remaining vacation days
            account.setVacationDays(account.getVacationDays().subtract(
                    calendarService.getVacationDays(application, start, end)));
        } else {
            // if application is before AND after April
            BigDecimal beforeApril = getDays(application, start, DateTimeConstants.MARCH, true);
            BigDecimal afterApril = getDays(application, end, DateTimeConstants.APRIL, false);

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

        BigDecimal beforeJan = getDays(application, start, DateTimeConstants.DECEMBER, true);
        BigDecimal afterJan = getDays(application, end, DateTimeConstants.JANUARY, false);

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

        BigDecimal beforeJan = getDays(application, start, DateTimeConstants.DECEMBER, true);
        BigDecimal afterJan = getDays(application, end, DateTimeConstants.JANUARY, false);

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
        if (isAfterApril(start.getMonthOfYear())) {
            BigDecimal gut = calendarService.getVacationDays(application, start, end);
            account.setVacationDays(gut.add(account.getVacationDays()));
        } else if (isBeforeApril(end.getMonthOfYear())) {
            // if holiday is before April, just add remaining vacation days if necessary
            BigDecimal gut = calendarService.getVacationDays(application, start, end).add(account.getVacationDays());

            if (gut.compareTo(entitlement) == 1) {
                account.setRemainingVacationDays(account.getRemainingVacationDays().add(gut.subtract(entitlement)));
                account.setVacationDays(entitlement);
            } else {
                account.setVacationDays(gut);
            }
        } else if (isBetweenMarchAndApril(start.getMonthOfYear(), end.getMonthOfYear())) {
            rollbackOverApril(application, account, entitlement, start, end);
        }
    }


    /**
     * Is used if application is cancelled or rejected. Used vacation days have to be added to leave account. This
     * method notices the special case April, i.e. it must be noticed that remaining vacation days may expire, but the
     * days before April have to be considered.
     *
     * @param  application
     * @param  account
     * @param  entitlement
     * @param  start
     * @param  end
     */
    public void rollbackOverApril(Application application, HolidaysAccount account, BigDecimal entitlement,
        DateMidnight start, DateMidnight end) {

        BigDecimal beforeApr = getDays(application, start, DateTimeConstants.MARCH, true);
        BigDecimal afterApr = getDays(application, end, DateTimeConstants.APRIL, false);

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
