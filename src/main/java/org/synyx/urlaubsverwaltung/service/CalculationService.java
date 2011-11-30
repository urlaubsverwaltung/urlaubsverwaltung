
package org.synyx.urlaubsverwaltung.service;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;

import org.synyx.urlaubsverwaltung.calendar.OwnCalendarService;
import org.synyx.urlaubsverwaltung.domain.Application;
import org.synyx.urlaubsverwaltung.domain.HolidaysAccount;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.List;


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


    private List<HolidaysAccount> getHolidaysAccountByCase(Application application) {

        List<HolidaysAccount> accounts = new ArrayList<HolidaysAccount>();

        HolidaysAccount account = accountService.getAccountOrCreateOne(application.getStartDate().getYear(),
                application.getPerson());

        // there are four possible cases for period of holiday
        // 1. between January and March
        // 2. between April and December
        // 3. between March and April
        // 4. between December and January

        int startMonth = application.getStartDate().getMonthOfYear();
        int endMonth = application.getEndDate().getMonthOfYear();

        if (startMonth >= DateTimeConstants.JANUARY && endMonth <= DateTimeConstants.MARCH) {
            account = caseBeforeApril(application, account);
            accounts.add(account);
        } else if (startMonth >= DateTimeConstants.APRIL && endMonth <= DateTimeConstants.DECEMBER) {
            account = caseAfterApril(application, account);
            accounts.add(account);
        } else if (startMonth <= DateTimeConstants.MARCH && endMonth >= DateTimeConstants.APRIL) {
            account = caseBetweenApril(application, account);
            accounts.add(account);
        } else if (startMonth <= DateTimeConstants.DECEMBER && endMonth >= DateTimeConstants.JANUARY) {
            HolidaysAccount accountNextYear = accountService.getAccountOrCreateOne(application.getEndDate().getYear(),
                    application.getPerson());
            accounts = caseBetweenJanuary(application, account, accountNextYear);
        }

        return accounts;
    }


    private HolidaysAccount caseBeforeApril(Application application, HolidaysAccount account) {

        BigDecimal days;

        days = calendarService.getVacationDays(application, application.getStartDate(), application.getEndDate());

        BigDecimal result = account.getRemainingVacationDays().subtract(days);

        if (result.compareTo(BigDecimal.ZERO) == -1) {
            account.setRemainingVacationDays(BigDecimal.ZERO);
            account.setVacationDays(account.getVacationDays().add(result));
        }

        return account;
    }


    private HolidaysAccount caseAfterApril(Application application, HolidaysAccount account) {

        BigDecimal days;

        days = calendarService.getVacationDays(application, application.getStartDate(), application.getEndDate());

        account.setVacationDays(account.getVacationDays().subtract(days));

        return account;
    }


    private HolidaysAccount caseBetweenApril(Application application, HolidaysAccount account) {

        BigDecimal daysBeforeApril = getDaysBeforeSpecialMonth(application, application.getStartDate(),
                DateTimeConstants.MARCH);
        BigDecimal daysAfterApril = getDaysAfterSpecialMonth(application, application.getEndDate(),
                DateTimeConstants.APRIL);

        BigDecimal result = account.getRemainingVacationDays().subtract(daysBeforeApril);

        if (result.compareTo(BigDecimal.ZERO) == -1) {
            account.setVacationDays(account.getVacationDays().add(result));
        }

        account.setRemainingVacationDays(BigDecimal.ZERO);
        account.setVacationDays(account.getVacationDays().subtract(daysAfterApril));

        return account;
    }


    private List<HolidaysAccount> caseBetweenJanuary(Application application, HolidaysAccount accountCurrentYear,
        HolidaysAccount accountNextYear) {

        BigDecimal daysBeforeJan = getDaysBeforeSpecialMonth(application, application.getStartDate(),
                DateTimeConstants.DECEMBER);
        BigDecimal daysAfterJan = getDaysAfterSpecialMonth(application, application.getEndDate(),
                DateTimeConstants.JANUARY);

        accountCurrentYear.setVacationDays(accountCurrentYear.getVacationDays().subtract(daysBeforeJan));

        if (accountCurrentYear.getVacationDays().compareTo(BigDecimal.ZERO) == 1) {
            accountNextYear.setRemainingVacationDays(accountCurrentYear.getVacationDays());
        }

        if (accountNextYear.getRemainingVacationDays().compareTo(BigDecimal.ZERO) == 1) {
            BigDecimal result = accountNextYear.getRemainingVacationDays().subtract(daysAfterJan);

            if (result.compareTo(BigDecimal.ZERO) == -1) {
                accountNextYear.setRemainingVacationDays(BigDecimal.ZERO);
                accountNextYear.setVacationDays(accountNextYear.getVacationDays().add(result));
            } else {
                accountNextYear.setRemainingVacationDays(accountNextYear.getRemainingVacationDays().subtract(
                        daysAfterJan));
            }
        }

        List<HolidaysAccount> accounts = new ArrayList<HolidaysAccount>();
        accounts.add(accountCurrentYear);
        accounts.add(accountNextYear);

        return accounts;
    }


    private boolean isNegative(BigDecimal b) {

        if (b.signum() == -1) {
            return true;
        } else {
            return false;
        }
    }


    private BigDecimal getDaysBeforeSpecialMonth(Application application, DateMidnight start, int month) {

        return calendarService.getVacationDays(application, start, new DateMidnight(start.getYear(), month, LAST_DAY));
    }


    private BigDecimal getDaysAfterSpecialMonth(Application application, DateMidnight end, int month) {

        return calendarService.getVacationDays(application, new DateMidnight(end.getYear(), month, FIRST_DAY), end);
    }


    /**
     * checks if application is valid in case of start date and end date of holiday are in the same year
     *
     * @param  application
     *
     * @return
     */
    public boolean checkApplicationOneYear(Application application) {

        List<HolidaysAccount> accounts = getHolidaysAccountByCase(application);

        if (accounts.size() == 1) {
            HolidaysAccount account = accounts.get(0);

            if (isNegative(account.getVacationDays())) {
                return false;
            } else {
                return true;
            }
        } else if (accounts.size() == 2) {
            HolidaysAccount accountCurrentYear = accounts.get(0);
            HolidaysAccount accountNextYear = accounts.get(1);

            if (!(isNegative(accountCurrentYear.getVacationDays()))
                    && !(isNegative(accountNextYear.getVacationDays()))) {
                return true;
            }
        }

        // else
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
            BigDecimal beforeApril = getDaysBeforeSpecialMonth(application, start, DateTimeConstants.MARCH);
            BigDecimal afterApril = getDaysAfterSpecialMonth(application, end, DateTimeConstants.APRIL);

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

        BigDecimal beforeJan = getDaysBeforeSpecialMonth(application, start, DateTimeConstants.DECEMBER);
        BigDecimal afterJan = getDaysAfterSpecialMonth(application, end, DateTimeConstants.JANUARY);

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

        BigDecimal beforeJan = getDaysBeforeSpecialMonth(application, start, DateTimeConstants.DECEMBER);
        BigDecimal afterJan = getDaysAfterSpecialMonth(application, end, DateTimeConstants.JANUARY);

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

        BigDecimal beforeApr = getDaysBeforeSpecialMonth(application, start, DateTimeConstants.MARCH);
        BigDecimal afterApr = getDaysAfterSpecialMonth(application, end, DateTimeConstants.APRIL);

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
