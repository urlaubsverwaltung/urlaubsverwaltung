
package org.synyx.urlaubsverwaltung.service;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;

import org.synyx.urlaubsverwaltung.calendar.OwnCalendarService;
import org.synyx.urlaubsverwaltung.domain.Application;
import org.synyx.urlaubsverwaltung.domain.HolidayEntitlement;
import org.synyx.urlaubsverwaltung.domain.HolidaysAccount;
import org.synyx.urlaubsverwaltung.util.CalcUtil;
import org.synyx.urlaubsverwaltung.util.DateUtil;

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
     * This method returns updated holiday account after calculating and setting vacation days and remaining vacation
     * days. Method is used by ApplicationServiceImpl's method save, i.e. account(s) is/are modified.
     *
     * @param  application
     *
     * @return  updated holiday account (list contains one element) respectively holiday accounts (list contains two
     *          elements) if holiday is between December and January
     */
    public List<HolidaysAccount> subtractVacationDays(Application application) {

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

        if (DateUtil.isBeforeApril(startMonth, endMonth)) {
            account = subtractCaseBeforeApril(application, account);
            accounts.add(account);
        } else if (DateUtil.isAfterApril(startMonth, endMonth)) {
            account = subtractCaseAfterApril(application, account);
            accounts.add(account);
        } else if (DateUtil.spansMarchAndApril(startMonth, endMonth)) {
            account = subtractCaseBetweenApril(application, account);
            accounts.add(account);
        } else if (DateUtil.spansDecemberAndJanuary(startMonth, endMonth)) {
            HolidaysAccount accountNextYear = accountService.getAccountOrCreateOne(application.getEndDate().getYear(),
                    application.getPerson());
            accounts = subtractCaseBetweenJanuary(application, account, accountNextYear);
        }

        return accounts;
    }


    /**
     * This method works alike the method subtractVacationDays excpet that this method doesn't manipulate the real
     * account(s) but uses copies of the available account(s) days. Method is used by ApplicationServiceImpl's method
     * checkApplication, i.e. real account(s) is/are not modified but only copies of it.
     *
     * @param  application
     *
     * @return  list with copies of holidays accounts that only have the attributes vacationDays and
     *          remainingVacationDays;
     */
    public List<HolidaysAccount> subtractForCheck(Application application) {

        List<HolidaysAccount> accounts = new ArrayList<HolidaysAccount>();

        HolidaysAccount account = accountService.getAccountOrCreateOne(application.getStartDate().getYear(),
                application.getPerson());

        HolidaysAccount accountCopy = new HolidaysAccount();
        accountCopy.setRemainingVacationDays(account.getRemainingVacationDays());
        accountCopy.setVacationDays(account.getVacationDays());

        // there are four possible cases for period of holiday
        // 1. between January and March
        // 2. between April and December
        // 3. between March and April
        // 4. between December and January

        int startMonth = application.getStartDate().getMonthOfYear();
        int endMonth = application.getEndDate().getMonthOfYear();

        if (DateUtil.isBeforeApril(startMonth, endMonth)) {
            accountCopy = subtractCaseBeforeApril(application, accountCopy);
            accounts.add(accountCopy);
        } else if (DateUtil.isAfterApril(startMonth, endMonth)) {
            accountCopy = subtractCaseAfterApril(application, accountCopy);
            accounts.add(accountCopy);
        } else if (DateUtil.spansMarchAndApril(startMonth, endMonth)) {
            accountCopy = subtractCaseBetweenApril(application, accountCopy);
            accounts.add(accountCopy);
        } else if (DateUtil.spansDecemberAndJanuary(startMonth, endMonth)) {
            HolidaysAccount accountNextYear = accountService.getAccountOrCreateOne(application.getEndDate().getYear(),
                    application.getPerson());
            HolidaysAccount accountNextYearCopy = new HolidaysAccount();
            accountNextYearCopy.setRemainingVacationDays(accountNextYear.getRemainingVacationDays());
            accountNextYearCopy.setVacationDays(accountNextYear.getVacationDays());
            accounts = subtractCaseBetweenJanuary(application, accountCopy, accountNextYearCopy);
        }

        return accounts;
    }


    /**
     * This method returns updated holiday account after calculating and setting vacation days and remaining vacation
     * days. Method is used by ApplicationServiceImpl's methods of cancel and rollback holiday.
     *
     * @param  application
     * @param  days
     *
     * @return  updated holiday account (list contains one element) respectively holiday accounts (list contains two
     *          elements) if holiday is between December and January
     */
    public List<HolidaysAccount> addVacationDays(Application application) {

        BigDecimal days;

        List<HolidaysAccount> accounts = new ArrayList<HolidaysAccount>();

        HolidaysAccount account = accountService.getAccountOrCreateOne(application.getStartDate().getYear(),
                application.getPerson());

        int startMonth = application.getStartDate().getMonthOfYear();
        int endMonth = application.getEndDate().getMonthOfYear();

        if (DateUtil.isBeforeApril(startMonth, endMonth)) {
            days = getNumberOfVacationDays(application);
            account = addDaysToAccount(account, days);
            accounts.add(account);
        } else if (DateUtil.isAfterApril(startMonth, endMonth)) {
            days = getNumberOfVacationDays(application);
            account.setVacationDays(account.getVacationDays().add(days));
            accounts.add(account);
        } else if (DateUtil.spansMarchAndApril(startMonth, endMonth)) {
            BigDecimal beforeApr = getDaysBeforeLastOfGivenMonth(application, DateTimeConstants.MARCH);
            BigDecimal afterApr = getDaysAfterFirstOfGivenMonth(application, DateTimeConstants.APRIL);
            account.setVacationDays(account.getVacationDays().add(afterApr));
            account = addDaysToAccount(account, beforeApr);
            accounts.add(account);
        } else if (DateUtil.spansDecemberAndJanuary(startMonth, endMonth)) {
            HolidaysAccount accountNextYear = accountService.getAccountOrCreateOne(application.getEndDate().getYear(),
                    application.getPerson());
            BigDecimal beforeJan = getDaysBeforeLastOfGivenMonth(application, DateTimeConstants.DECEMBER);
            BigDecimal afterJan = getDaysAfterFirstOfGivenMonth(application, DateTimeConstants.JANUARY);

            BigDecimal remainingDays = account.getVacationDays().add(beforeJan);
            accountNextYear.setRemainingVacationDays(remainingDays);
            addDaysToAccount(accountNextYear, afterJan);
            accounts.add(account);
            accounts.add(accountNextYear);
        }

        return accounts;
    }


    /**
     * This method updates holiday account by adding sick days to vacation days.
     *
     * <p>date of adding sick days to application is the important thing dependent on this date, it is decided if
     * remaining vacation days are filled or not if date of adding sick days is before April (January - March),
     * remaining vacation days are filled if date of adding sick days is after April (April - December), remaining
     * vacation days are ignored case between March and April is handled like after April case between December and
     * January is handled like before April</p>
     *
     * @param  application
     * @param  account
     * @param  sickDays
     *
     * @return  updated holiday account
     */
    public HolidaysAccount addSickDaysOnHolidaysAccount(Application application, HolidaysAccount account) {

        if (DateUtil.isBeforeApril(application.getDateOfAddingSickDays())) {
            addDaysToAccount(account, application.getSickDays());
        } else if (DateUtil.isAfterApril(application.getDateOfAddingSickDays())) {
            HolidayEntitlement entitlement = accountService.getHolidayEntitlement(account.getYear(),
                    account.getPerson());
            BigDecimal sum = account.getVacationDays().add(application.getSickDays());

            if (sum.compareTo(entitlement.getVacationDays()) == 1) {
                account.setVacationDays(entitlement.getVacationDays());
            } else {
                account.setVacationDays(account.getVacationDays().add(application.getSickDays()));
            }
        }

        return account;
    }


    /**
     * method that updates a given holiday account by adding given number of days.
     *
     * @param  account
     * @param  days
     *
     * @return
     */
    private HolidaysAccount addDaysToAccount(HolidaysAccount account, BigDecimal days) {

        BigDecimal sum = account.getVacationDays().add(days);

        HolidayEntitlement entitlement = accountService.getHolidayEntitlement(account.getYear(), account.getPerson());

        if (sum.compareTo(entitlement.getVacationDays()) == 1) {
            account.setVacationDays(entitlement.getVacationDays());
            account.setRemainingVacationDays(account.getRemainingVacationDays().add(
                    sum.subtract(entitlement.getVacationDays())));
        }

        return account;
    }


    /**
     * calculation of holiday account in case of holiday before April.
     *
     * @param  application
     * @param  account
     *
     * @return  updated account
     */
    private HolidaysAccount subtractCaseBeforeApril(Application application, HolidaysAccount account) {

        BigDecimal days = getNumberOfVacationDays(application);

        BigDecimal result = account.getRemainingVacationDays().subtract(days);

        // three cases:
        // result is 0
        // result is greater than 0
        // result is negative

        if (result.compareTo(BigDecimal.ZERO) == 0) {
            account.setRemainingVacationDays(BigDecimal.ZERO);
        } else if (CalcUtil.isGreaterThanZero(result)) {
            account.setRemainingVacationDays(result);
        } else if (CalcUtil.isNegative(result)) {
            account.setRemainingVacationDays(BigDecimal.ZERO);
            account.setVacationDays(account.getVacationDays().add(result)); // result is negative so that you add it to
                                                                            // vacation days instead of subtract it
        }

        return account;
    }


    /**
     * calculation of holiday account in case of holiday after April.
     *
     * @param  application
     * @param  account
     *
     * @return  updated holiday account
     */
    private HolidaysAccount subtractCaseAfterApril(Application application, HolidaysAccount account) {

        BigDecimal days = getNumberOfVacationDays(application);

        account.setVacationDays(account.getVacationDays().subtract(days));

        return account;
    }


    /**
     * calculation of holiday account in case of holiday between March and April.
     *
     * @param  application
     * @param  account
     *
     * @return  updated holiday account
     */
    private HolidaysAccount subtractCaseBetweenApril(Application application, HolidaysAccount account) {

        BigDecimal daysBeforeApril = getDaysBeforeLastOfGivenMonth(application, DateTimeConstants.MARCH);
        BigDecimal daysAfterApril = getDaysAfterFirstOfGivenMonth(application, DateTimeConstants.APRIL);

        BigDecimal result = account.getRemainingVacationDays().subtract(daysBeforeApril);

        // three cases:
        // result is 0
        // result is greater than 0
        // result is negative

        if (result.compareTo(BigDecimal.ZERO) == 0) {
            account.setRemainingVacationDays(BigDecimal.ZERO);
        } else if (CalcUtil.isGreaterThanZero(result)) {
            account.setRemainingVacationDays(result);
        } else if (CalcUtil.isNegative(result)) {
            account.setRemainingVacationDays(BigDecimal.ZERO);
            account.setVacationDays(account.getVacationDays().add(result)); // result is negative so that you add it to
                                                                            // vacation days instead of subtract it
        }

        account.setVacationDays(account.getVacationDays().subtract(daysAfterApril));

        return account;
    }


    /**
     * calculation of holiday account in case of holiday between December and January.
     *
     * @param  application
     * @param  accountCurrentYear
     * @param  accountNextYear
     *
     * @return  updated accounts: account of current year and account of next year
     */
    private List<HolidaysAccount> subtractCaseBetweenJanuary(Application application,
        HolidaysAccount accountCurrentYear, HolidaysAccount accountNextYear) {

        BigDecimal daysBeforeJan = getDaysBeforeLastOfGivenMonth(application, DateTimeConstants.DECEMBER);
        BigDecimal daysAfterJan = getDaysAfterFirstOfGivenMonth(application, DateTimeConstants.JANUARY);

        accountCurrentYear.setVacationDays(accountCurrentYear.getVacationDays().subtract(daysBeforeJan));
        accountNextYear.setVacationDays(accountNextYear.getVacationDays().subtract(daysAfterJan));
//
//        if (CalcUtil.isGreaterThanZero(accountCurrentYear.getVacationDays())) {
//            accountNextYear.setRemainingVacationDays(accountCurrentYear.getVacationDays());
//        }
//
//        if (accountNextYear.getRemainingVacationDays().compareTo(BigDecimal.ZERO) == 1) {
//            BigDecimal result = accountNextYear.getRemainingVacationDays().subtract(daysAfterJan);
//
//            if (result.compareTo(BigDecimal.ZERO) == -1) {
//                accountNextYear.setRemainingVacationDays(BigDecimal.ZERO);
//                accountNextYear.setVacationDays(accountNextYear.getVacationDays().add(result));
//            } else {
//                accountNextYear.setRemainingVacationDays(accountNextYear.getRemainingVacationDays().subtract(
//                        daysAfterJan));
//            }
//        }

        List<HolidaysAccount> accounts = new ArrayList<HolidaysAccount>();
        accounts.add(accountCurrentYear);
        accounts.add(accountNextYear);

        return accounts;
    }


    /**
     * calculates how many work days are between date of application and last day of given month
     *
     * @param  application
     * @param  month
     *
     * @return
     */
    private BigDecimal getDaysBeforeLastOfGivenMonth(Application application, int month) {

        return calendarService.getVacationDays(application, application.getStartDate(),
                new DateMidnight(application.getStartDate().getYear(), month, LAST_DAY));
    }


    /**
     * calculates how many work days are between date of application and first day of given month
     *
     * @param  application
     * @param  month
     *
     * @return
     */
    private BigDecimal getDaysAfterFirstOfGivenMonth(Application application, int month) {

        return calendarService.getVacationDays(application,
                new DateMidnight(application.getEndDate().getYear(), month, FIRST_DAY), application.getEndDate());
    }


    /**
     * calculates how many work days are between start date and end date of an application
     *
     * @param  application
     *
     * @return
     */
    private BigDecimal getNumberOfVacationDays(Application application) {

        return calendarService.getVacationDays(application, application.getStartDate(), application.getEndDate());
    }
}
