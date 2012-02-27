
package org.synyx.urlaubsverwaltung.service;

import org.apache.log4j.Logger;

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
 * This class contains all methods to calculate with vacation days e.g. checking if an application is valid (are there
 * enough vacation days on person's HolidaysAccount) or noticing the special cases January and April for
 * calculation/updating HolidaysAccount(s)
 *
 * @author  Aljona Murygina
 */
public class CalculationService {

    // audit logger: logs nontechnically occurences like 'user x applied for leave' or 'subtracted n days from
    // holidays account y'
    private static final Logger LOG = Logger.getLogger("audit");

    private static final int LAST_DAY = 31; // last day of month
    private static final int FIRST_DAY = 1; // first day of month

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

        HolidaysAccount accountCopy = createCopyOfHolidaysAccount(account);

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

        // for calculation you have to distinguish between five cases
        // 1. period spans December and January
        // 2. period is in future (current year plus 1)
        // 3. period is before April - is equivalent to - account's remaining vacation days don't expire on 1st April
        // 4. period is after April
        // 5. period spans March and April

        List<HolidaysAccount> accounts;

        int startMonth = application.getStartDate().getMonthOfYear();
        int endMonth = application.getEndDate().getMonthOfYear();

        // if application spans December and January, it's the very special case!
        // two supplementary applications are created and saved
        // calculation of holidays account's values occurs
        if (application.getStartDate().getYear() != application.getEndDate().getYear()) {
            accounts = subtractCaseSpanningDecemberAndJanuary(application, account, isCheck);
        } else {
            accounts = new ArrayList<HolidaysAccount>();

            // check if holiday's period is in future: now.getYear() + 1 ?
            if (getCurrentYear() != application.getStartDate().getYear()) {
                HolidaysAccount accountNextYear = subtractCaseFutureYear(application, isCheck);
                accounts.add(accountNextYear);
            } else {
                if (DateUtil.isBeforeApril(startMonth, endMonth) || !account.isRemainingVacationDaysExpire()) {
                    // if remaining vacation days don't expire you can calculate as if it's always before April,
                    // i.e. remaining vacation days are used before vacation days
                    account = subtractCaseBeforeApril(application, account);
                    accounts.add(account);
                } else if (DateUtil.isAfterApril(startMonth, endMonth)) {
                    // remaining vacation days are not used for calculation
                    account = subtractCaseAfterApril(application, account);
                    accounts.add(account);
                } else if (DateUtil.spansMarchAndApril(startMonth, endMonth)) {
                    // partly remaining vacation days are used for calculation, partly not
                    account = subtractCaseBetweenApril(application, account);
                    accounts.add(account);
                }
            }
        }

        return accounts;
    }


    /**
     * calculation of holiday account in case of holiday before April.
     *
     * @param  application
     * @param  account
     *
     * @return  modified holidays account
     */
    protected HolidaysAccount subtractCaseBeforeApril(Application application, HolidaysAccount account) {

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

        if (account.getId() != null) {
            LOG.info("Urlaubskonto-Id " + account.getId() + ": es wurden " + days
                + " abgezogen durch den Antrag mit der Id " + application.getId());
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
    protected HolidaysAccount subtractCaseAfterApril(Application application, HolidaysAccount account) {

        BigDecimal days = getNumberOfVacationDays(application);

        account.setVacationDays(account.getVacationDays().subtract(days));

        if (account.getId() != null) {
            LOG.info("Urlaubskonto-Id " + account.getId() + ": es wurden " + days
                + " abgezogen durch den Antrag mit der Id " + application.getId());
        }

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
    protected HolidaysAccount subtractCaseBetweenApril(Application application, HolidaysAccount account) {

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

        if (account.getId() != null) {
            LOG.info("Urlaubskonto-Id " + account.getId() + ": es wurden " + (daysBeforeApril.add(daysAfterApril))
                + " abgezogen durch den Antrag mit der Id " + application.getId());
        }

        return account;
    }


    /**
     * calculation of holiday account in case of holiday between December and January.
     *
     * @param  application
     * @param  accountCurrentYear
     *
     * @return  updated accounts: account of current year and account of next year
     */
    protected List<HolidaysAccount> subtractCaseSpanningDecemberAndJanuary(Application application,
        HolidaysAccount accountCurrentYear, boolean isCheck) {

        List<HolidaysAccount> accounts = new ArrayList<HolidaysAccount>();

        BigDecimal daysBeforeJan = getDaysBeforeLastOfGivenMonth(application, DateTimeConstants.DECEMBER);
        BigDecimal daysAfterJan = getDaysAfterFirstOfGivenMonth(application, DateTimeConstants.JANUARY);

        // create two new applications - they are only for calculation, so the flag onlyForCalculation is true

        Application decemberApplication = createSupplementalApplication(application, true); // application for
                                                                                            // current year

        Application januaryApplication = createSupplementalApplication(application, false); // application for
                                                                                            // current year

        if (accountCurrentYear.isRemainingVacationDaysExpire()) {
            subtractCaseAfterApril(decemberApplication, accountCurrentYear);
        } else {
            subtractCaseBeforeApril(decemberApplication, accountCurrentYear);
        }

        accounts.add(accountCurrentYear);

        HolidaysAccount accountNextYear = accountService.getAccountOrCreateOne(application.getEndDate().getYear(),
                accountCurrentYear.getPerson());

        HolidaysAccount copyAccountNextYear = createCopyOfHolidaysAccount(accountNextYear); // only needed for check

        // date of applying for leave is in the same year like the application's start date, i.e. applying occur
        // scheduled
        if (getCurrentYear() == application.getStartDate().getYear()) {
            if (isCheck) {
                copyAccountNextYear.setVacationDays(copyAccountNextYear.getVacationDays().subtract(daysAfterJan));
                accounts.add(copyAccountNextYear);
            } else {
                accountNextYear.setVacationDays(accountNextYear.getVacationDays().subtract(daysAfterJan));
                accounts.add(accountNextYear);
            }
        } // date of applying for leave is NOT in the same year like the application's start date, i.e. applying occurs
          // subsequently
        else {
            if (isCheck) {
                copyAccountNextYear.setRemainingVacationDays(copyAccountNextYear.getRemainingVacationDays().subtract(
                        daysBeforeJan));
                subtractCaseBeforeApril(januaryApplication, copyAccountNextYear);
                accounts.add(copyAccountNextYear);
            } else {
                HolidayEntitlement ent = accountService.getHolidayEntitlement(application.getEndDate().getYear(),
                        application.getPerson());
                ent.setRemainingVacationDays(ent.getRemainingVacationDays().subtract(daysBeforeJan));
                accountService.saveHolidayEntitlement(ent);

                accountNextYear.setRemainingVacationDays(accountNextYear.getRemainingVacationDays().subtract(
                        (daysBeforeJan)));
                subtractCaseBeforeApril(januaryApplication, accountNextYear);
                accounts.add(accountNextYear);
            }
        }

        if (accountCurrentYear.getId() != null) {
            LOG.info("Urlaubskonto-Id " + accountCurrentYear.getId() + ": es wurden " + daysBeforeJan
                + " abgezogen durch den Antrag mit der Id " + application.getId());
        }

        if (accountNextYear.getId() != null) {
            LOG.info("Urlaubskonto-Id " + accountNextYear.getId() + ": es wurden " + daysAfterJan
                + " abgezogen durch den Antrag mit der Id " + application.getId());
        }

        return accounts;
    }


    protected int getCurrentYear() {

        return DateMidnight.now().getYear();
    }


    /**
     * This method creates and saves the supplemental applications by using data of the original application.
     *
     * @param  originalApplication
     * @param  forNewYear  true if it's a supplemental application for December or January, false if it's a supplemental
     *                     application for March or April
     * @param  isBeforeTheGivenMonth  true if from application's start date to last day in month, false if from first
     *                                day of month to application's end date
     *
     * @return  generated application
     */
    public Application createSupplementalApplication(Application originalApplication, boolean isForEndOfMonth) {

        Application app = new Application();

        app.setHowLong(originalApplication.getHowLong());
        app.setPerson(originalApplication.getPerson());
        app.setStatus(originalApplication.getStatus());
        app.setVacationType(originalApplication.getVacationType());

        app.setSupplementaryApplication(true);
        app.setIdOfApplication(originalApplication.getId());

        if (isForEndOfMonth) {
            // use the days from original application's start date to last day in month
            BigDecimal days = getDaysBeforeLastOfGivenMonth(originalApplication, DateTimeConstants.DECEMBER);

            app.setStartDate(originalApplication.getStartDate());
            app.setEndDate(new DateMidnight(originalApplication.getStartDate().getYear(), DateTimeConstants.DECEMBER,
                    LAST_DAY));
            app.setDays(days);
        } else {
            // use the days from first day in month to original application's end date

            BigDecimal days = getDaysAfterFirstOfGivenMonth(originalApplication, DateTimeConstants.JANUARY);

            app.setStartDate(new DateMidnight(originalApplication.getEndDate().getYear(), DateTimeConstants.JANUARY,
                    FIRST_DAY));
            app.setEndDate(originalApplication.getEndDate());
            app.setDays(days);
        }

        return app;
    }


    /**
     * calculation of holidays account if date of application is not in the same year as the holiday's period
     *
     * @param  application
     *
     * @return  updated holidays account of next year
     */
    protected HolidaysAccount subtractCaseFutureYear(Application application, boolean isCheck) {

        HolidaysAccount accountNextYear = accountService.getAccountOrCreateOne(application.getEndDate().getYear(),
                application.getPerson());

        if (isCheck) {
            HolidaysAccount copyAccountNextYear = createCopyOfHolidaysAccount(accountNextYear);
            copyAccountNextYear.setVacationDays(copyAccountNextYear.getVacationDays().subtract(application.getDays()));

            return copyAccountNextYear;
        } else {
            accountNextYear.setVacationDays(accountNextYear.getVacationDays().subtract(application.getDays()));

            return accountNextYear;
        }
    }


    /**
     * If the vacations spans March and April, the number of days in March are added to holidays account's remaining
     * vacation days (and if this is not enough: to vacation days too) and the number of days in April are added only to
     * vacation days
     *
     * @param  application
     * @param  account
     *
     * @return  updated holidays account
     */
    protected HolidaysAccount addDaysOfApplicationSpanningMarchAndApril(Application application,
        HolidaysAccount account, BigDecimal usedDaysBeforeApril) {

        BigDecimal beforeApr = getDaysBeforeLastOfGivenMonth(application, DateTimeConstants.MARCH);
        BigDecimal afterApr = getDaysAfterFirstOfGivenMonth(application, DateTimeConstants.APRIL);
        account = addDaysBeforeApril(account, beforeApr, usedDaysBeforeApril);
        account = addDaysAfterApril(account, afterApr);

        return account;
    }


    /**
     * This method add vacation days on holidays accounts of last year and new year dependent on cancelling date. (if
     * it's before or after 1st January)
     *
     * @param  application
     * @param  account
     *
     * @return  list of updated holidays accounts: new year's account and last year's account
     */
    protected List<HolidaysAccount> addDaysOfApplicationSpanningDecemberAndJanuary(Application application,
        HolidaysAccount account, BigDecimal usedDaysCurrentYear, BigDecimal usedDaysNextYear) {

        List<HolidaysAccount> accounts = new ArrayList<HolidaysAccount>();

        BigDecimal beforeJan = getDaysBeforeLastOfGivenMonth(application, DateTimeConstants.DECEMBER);
        BigDecimal afterJan = getDaysAfterFirstOfGivenMonth(application, DateTimeConstants.JANUARY);

        HolidaysAccount accountNextYear = accountService.getAccountOrCreateOne(application.getEndDate().getYear(),
                application.getPerson());

        // if date of cancelling is before 1st January
        if (getCurrentYear() == application.getStartDate().getYear()) {
            if (account.isRemainingVacationDaysExpire()) {
                account.setVacationDays(account.getVacationDays().add(beforeJan));
            } else {
                account = addDaysBeforeApril(account, beforeJan, usedDaysCurrentYear); // fill at first remaining
                                                                                       // vacation days before filling
                                                                                       // vacation days
            }

            accountNextYear.setVacationDays(accountNextYear.getVacationDays().add(afterJan));
        } else {
            // date of cancelling is after 1st January i.e. the new year's holidays account contains the right values
            // (remaining vacation days AND vacation days) after being updated on 1st January

            // the remaining vacation days of the new year's holidays account and entitlement are the values of the last
            // year's vacation days i.e. add days before January on last year's account vacation days and on new year's
            // account's and entitlement's remaining vacation days
            account.setVacationDays(account.getVacationDays().add(beforeJan));

            HolidayEntitlement ent = accountService.getHolidayEntitlement(application.getEndDate().getYear(),
                    account.getPerson());
            ent.setRemainingVacationDays(ent.getRemainingVacationDays().add(beforeJan));
            accountService.saveHolidayEntitlement(ent);

            accountNextYear.setRemainingVacationDays(accountNextYear.getRemainingVacationDays().add(beforeJan));

            BigDecimal sum = accountNextYear.getRemainingVacationDays().add(afterJan);

            // if sum is greater than number of entitlement's remaining vacation days, the account's number of remaining
            // vacation days is set to number of entitlement's remaining vacation days. The difference between sum and
            // the number of entitlement's remaining vacation days is added to number of account's vacation days.
            if (sum.compareTo(ent.getRemainingVacationDays()) == 1) {
                accountNextYear.setRemainingVacationDays(ent.getRemainingVacationDays());

                accountNextYear.setVacationDays(accountNextYear.getVacationDays().add(
                        sum.subtract(ent.getRemainingVacationDays())));
            } else {
                // if sum is equal or smaller than number of entitlement's remaining vacation days, the number of the
                // given days is added to number of account's remaining vacation days
                accountNextYear.setRemainingVacationDays(sum);
            }
        }

        accounts.add(account);
        accounts.add(accountNextYear);

        return accounts;
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

        LOG.info("Urlaubskonto-Id " + account.getId() + ": es wurden " + days
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
    protected HolidaysAccount addDaysBeforeApril(HolidaysAccount account, BigDecimal days,
        BigDecimal usedDaysBeforeApril) {

        HolidayEntitlement entitlement = accountService.getHolidayEntitlement(account.getYear(), account.getPerson());

        // total number minus number of this application's days
        usedDaysBeforeApril = usedDaysBeforeApril.subtract(days);

        if (usedDaysBeforeApril.compareTo(BigDecimal.ZERO) == 1) {
            // if number of used vacation days is greater than or equals entitlement's remaining vacation days
            // vacation days have to be filled
            if (usedDaysBeforeApril.compareTo(entitlement.getRemainingVacationDays()) >= 0) {
                account.setVacationDays(account.getVacationDays().add(days));
            } else {
                BigDecimal sum = account.getRemainingVacationDays().add(days).add(usedDaysBeforeApril);

                // fill remaining vacation days and vacation days
                if (sum.compareTo(entitlement.getRemainingVacationDays()) == 1) {
                    BigDecimal setRemaining = entitlement.getRemainingVacationDays().subtract(usedDaysBeforeApril);
                    account.setRemainingVacationDays(setRemaining);

                    BigDecimal addVacationDays = days.subtract(setRemaining);
                    account.setVacationDays(account.getVacationDays().add(addVacationDays));
                } else {
                    // fill only vacation days
                    account.setRemainingVacationDays(account.getRemainingVacationDays().add(days));
                }
            }
        } else {
            // if no used days yet
            BigDecimal sum = account.getRemainingVacationDays().add(days);

            // if sum is greater than number of entitlement's remaining vacation days, the account's number of remaining
            // vacation days is set to number of entitlement's remaining vacation days. The difference between sum and
            // the number of entitlement's remaining vacation days is added to number of account's vacation days.
            if (sum.compareTo(entitlement.getRemainingVacationDays()) == 1) {
                account.setRemainingVacationDays(entitlement.getRemainingVacationDays());

                account.setVacationDays(account.getVacationDays().add(
                        sum.subtract(entitlement.getRemainingVacationDays())));
            } else {
                // if sum is equal or smaller than number of entitlement's remaining vacation days, the number of the
                // given days is added to number of account's remaining vacation days
                account.setRemainingVacationDays(sum);
            }
        }

        LOG.info("Urlaubskonto-Id " + account.getId() + ": es wurden " + days
            + " aufaddiert.");

        return account;
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


    /**
     * This method creates a copy of the given holidays account. If subtraction is performed only for check, you must
     * not use the real holidays accounts, but copies of them.
     *
     * @param  account  holidays account that shall be copied
     *
     * @return  HolidaysAccount copy of the given holidays account
     */
    private HolidaysAccount createCopyOfHolidaysAccount(HolidaysAccount account) {

        HolidaysAccount accountCopy = new HolidaysAccount();
        accountCopy.setRemainingVacationDaysExpire(account.isRemainingVacationDaysExpire());
        accountCopy.setRemainingVacationDays(account.getRemainingVacationDays());
        accountCopy.setVacationDays(account.getVacationDays());
        accountCopy.setPerson(account.getPerson());
        accountCopy.setYear(account.getYear());

        return accountCopy;
    }
}
