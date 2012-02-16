
package org.synyx.urlaubsverwaltung.service;

import org.apache.log4j.Logger;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;

import org.synyx.urlaubsverwaltung.calendar.OwnCalendarService;
import org.synyx.urlaubsverwaltung.domain.Application;
import org.synyx.urlaubsverwaltung.domain.HolidayEntitlement;
import org.synyx.urlaubsverwaltung.domain.HolidaysAccount;
import org.synyx.urlaubsverwaltung.domain.Person;
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

    private static final Logger LOG = Logger.getLogger(CalculationService.class);

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

        HolidaysAccount account = accountService.getAccountOrCreateOne(application.getStartDate().getYear(),
                application.getPerson());

        return doCalculation(application, account, false);
    }


    /**
     * This method works alike the method subtractVacationDays except that this method doesn't manipulate the real
     * account(s) but uses copies of the available account(s) days. Method is used by ApplicationServiceImpl's method
     * checkApplication, i.e. real account(s) is/are not modified but only copies of it.
     *
     * @param  application
     *
     * @return  list with copies of holidays accounts that only have the attributes vacationDays and
     *          remainingVacationDays;
     */
    public List<HolidaysAccount> subtractForCheck(Application application) {

        HolidaysAccount account = accountService.getAccountOrCreateOne(application.getStartDate().getYear(),
                application.getPerson());

        HolidaysAccount accountCopy = new HolidaysAccount();
        accountCopy.setRemainingVacationDays(account.getRemainingVacationDays());
        accountCopy.setVacationDays(account.getVacationDays());

        return doCalculation(application, accountCopy, true);
    }


    /**
     * This method contains the calculation resp. it decides which method has to be used for calculation (dependent on
     * cases: which month resp. are remaining vacation days of holidays account expiring or not)
     *
     * @param  application
     * @param  account
     * @param  isCheck
     *
     * @return  list of updated holidays account
     */
    private List<HolidaysAccount> doCalculation(Application application, HolidaysAccount account, boolean isCheck) {

        List<HolidaysAccount> accounts = new ArrayList<HolidaysAccount>();

        if (account.isRemainingVacationDaysExpire()) {
            int startMonth = application.getStartDate().getMonthOfYear();
            int endMonth = application.getEndDate().getMonthOfYear();

            // there are four possible cases for period of holiday
            // 1. between January and March
            // 2. between April and December
            // 3. between March and April
            // 4. between December and January

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
                HolidaysAccount accountNextYear = accountService.getAccountOrCreateOne(application.getEndDate()
                        .getYear(), application.getPerson());

                if (isCheck) {
                    HolidaysAccount accountNextYearCopy = new HolidaysAccount();
                    accountNextYearCopy.setRemainingVacationDays(accountNextYear.getRemainingVacationDays());
                    accountNextYearCopy.setVacationDays(accountNextYear.getVacationDays());
                    accounts = subtractCaseBetweenJanuary(application, account, accountNextYearCopy);
                } else {
                    accounts = subtractCaseBetweenJanuary(application, account, accountNextYear);
                }
            }
        } else {
            // if remaining vacation days don't expire you can calculate as if it's always before April,
            // i.e. remaining vacation days are used before vacation days
            account = subtractCaseBeforeApril(application, account);
            accounts.add(account);
        }

        return accounts;
    }


    /**
     * This method is used if application's field 'sickDays' has not been null, but already filled. Then you have to
     * subtract the old value of sick days from holidays account.
     *
     * @param  account
     * @param  sickDays
     *
     * @return  updated holidays account
     */
    public HolidaysAccount subtractSickDays(Person person, BigDecimal sickDays, DateMidnight date) {

        HolidaysAccount account = accountService.getHolidaysAccount(date.getYear(), person);

        // subtract remaining vacation days at first
        if (DateUtil.isBeforeApril(date) || (account.isRemainingVacationDaysExpire() == false)) {
            BigDecimal diff = account.getRemainingVacationDays().subtract(sickDays);

            // example:
            // 3 (remaining vacation days) - 5 (sick days) = -2
            // so you have to add -2 to holidays account's vacation days
            if (CalcUtil.isNegative(diff)) {
                account.setRemainingVacationDays(BigDecimal.ZERO);
                account.setVacationDays(account.getVacationDays().add(diff));
            } else {
                account.setRemainingVacationDays(diff);
            }
        } else if (DateUtil.isAfterApril(date)) {
            // do not subtract remaining vacation days
            account.setVacationDays(account.getVacationDays().subtract(sickDays));
        }

        return account;
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

        if (DateUtil.isBeforeApril(startMonth, endMonth) || (account.isRemainingVacationDaysExpire() == false)) {
            // if the vacation is before April, at first remaining vacation days must be filled and then vacation days
            days = getNumberOfVacationDays(application);
            account = addDaysBeforeApril(account, days);
            accounts.add(account);
        } else if (DateUtil.isAfterApril(startMonth, endMonth)) {
            // if the vacation is after April, only vacation days are filled (not the remaining vacation days)
            days = getNumberOfVacationDays(application);
            account = addDaysAfterApril(account, days);
            accounts.add(account);
        } else if (DateUtil.spansMarchAndApril(startMonth, endMonth)) {
            // if the vacations spans March and April, the number of days in March are added to holidays
            // account's remaining vacation days (and if this is not enough: to vacation days too)
            // and the number of days in April are added only to vacation days
            BigDecimal beforeApr = getDaysBeforeLastOfGivenMonth(application, DateTimeConstants.MARCH);
            BigDecimal afterApr = getDaysAfterFirstOfGivenMonth(application, DateTimeConstants.APRIL);
            account = addDaysBeforeApril(account, beforeApr);
            account = addDaysAfterApril(account, afterApr);
            accounts.add(account);
        } else if (DateUtil.spansDecemberAndJanuary(startMonth, endMonth)) {
            HolidaysAccount accountNextYear = accountService.getAccountOrCreateOne(application.getEndDate().getYear(),
                    application.getPerson());
            BigDecimal beforeJan = getDaysBeforeLastOfGivenMonth(application, DateTimeConstants.DECEMBER);
            BigDecimal afterJan = getDaysAfterFirstOfGivenMonth(application, DateTimeConstants.JANUARY);

            BigDecimal remainingDays = account.getVacationDays().add(beforeJan);
            accountNextYear.setRemainingVacationDays(remainingDays);
            addDaysAfterApril(accountNextYear, afterJan);
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

        if (DateUtil.isBeforeApril(application.getDateOfAddingSickDays())
                || (account.isRemainingVacationDaysExpire() == false)) {
            addDaysBeforeApril(account, application.getSickDays());
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
     * If it is after April use this method to update a given holiday account by adding given number of days. At first
     * the vacation days are filled and then the remaining vacation days of the account.
     *
     * @param  account
     * @param  days
     *
     * @return  modified holidays account
     */
    protected HolidaysAccount addDaysAfterApril(HolidaysAccount account, BigDecimal days) {

        BigDecimal sum = account.getVacationDays().add(days);

        HolidayEntitlement entitlement = accountService.getHolidayEntitlement(account.getYear(), account.getPerson());

        // if sum is greater than number of entitlement's vacation days, the account's vacation days are set to
        // entitlement's vacation days and the difference between sum and the number of entitlement's vacation days is
        // added to account's remaining vacation days
        if (sum.compareTo(entitlement.getVacationDays()) == 1) {
            account.setVacationDays(entitlement.getVacationDays());
            account.setRemainingVacationDays(account.getRemainingVacationDays().add(
                    sum.subtract(entitlement.getVacationDays())));
        } else {
            // if sum is equal or smaller than number of entitlement's vacation days, the sum is added to number of
            // account's vacation days
            account.setVacationDays(sum);
        }

        LOG.info("Urlaubskonto-Id " + account.getId() + ": aufgrund von Krankheitstagen wurden " + days
            + " aufaddiert.");

        return account;
    }


    /**
     * If it is before April use this method to update a given holiday account by adding given number of days. At first
     * the remaining vacation days are filled and then the vacation days of the account.
     *
     * @param  account
     * @param  days
     *
     * @return  modified holidays account
     */
    protected HolidaysAccount addDaysBeforeApril(HolidaysAccount account, BigDecimal days) {

        BigDecimal sum = account.getRemainingVacationDays().add(days);

        HolidayEntitlement entitlement = accountService.getHolidayEntitlement(account.getYear(), account.getPerson());

        // if sum is greater than number of entitlement's remaining vacation days, the account's number of remaining
        // vacation days is set to number of entitlement's remaining vacation days. The difference between sum
        // and the number of entitlement's remaining vacation days is added to number of account's vacation days.
        if (sum.compareTo(entitlement.getRemainingVacationDays()) == 1) {
            account.setRemainingVacationDays(entitlement.getRemainingVacationDays());

            account.setVacationDays(account.getVacationDays().add(
                    sum.subtract(entitlement.getRemainingVacationDays())));
        } else {
            // if sum is equal or smaller than number of entitlement's remaining vacation days, the number of the
            // given days is added to number of account's remaining vacation days
            account.setRemainingVacationDays(sum);
        }

        LOG.info("Urlaubskonto-Id " + account.getId() + ": aufgrund von Krankheitstagen wurden " + days
            + " aufaddiert.");

        return account;
    }


    /**
     * calculation of holiday account in case of holiday before April.
     *
     * @param  application
     * @param  account
     *
     * @return  modified holidays account
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

        LOG.info("Urlaubskonto-Id " + account.getId() + ": es wurden " + days
            + " abgezogen durch den Antrag mit der Id " + application.getId());

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

        LOG.info("Urlaubskonto-Id " + account.getId() + ": es wurden " + days
            + " abgezogen durch den Antrag mit der Id " + application.getId());

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

        LOG.info("Urlaubskonto-Id " + account.getId() + ": es wurden " + (daysBeforeApril.add(daysAfterApril))
            + " abgezogen durch den Antrag mit der Id " + application.getId());

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
// TODO
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

        LOG.info("Urlaubskonto-Id " + accountCurrentYear.getId() + ": es wurden " + daysBeforeJan
            + " abgezogen durch den Antrag mit der Id " + application.getId());

        LOG.info("Urlaubskonto-Id " + accountNextYear.getId() + ": es wurden " + daysAfterJan
            + " abgezogen durch den Antrag mit der Id " + application.getId());

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
