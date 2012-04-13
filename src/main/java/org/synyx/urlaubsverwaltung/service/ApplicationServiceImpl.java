package org.synyx.urlaubsverwaltung.service;

import org.apache.log4j.Logger;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;
import org.joda.time.Interval;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.transaction.annotation.Transactional;

import org.synyx.urlaubsverwaltung.calendar.OwnCalendarService;
import org.synyx.urlaubsverwaltung.dao.ApplicationDAO;
import org.synyx.urlaubsverwaltung.domain.Application;
import org.synyx.urlaubsverwaltung.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.domain.DayLength;
import org.synyx.urlaubsverwaltung.domain.HolidaysAccount;
import org.synyx.urlaubsverwaltung.domain.Person;
import org.synyx.urlaubsverwaltung.domain.VacationType;
import org.synyx.urlaubsverwaltung.util.CalcUtil;
import org.synyx.urlaubsverwaltung.util.DateUtil;

import java.math.BigDecimal;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * implementation of the applicationdata-access-service.
 *
 * @author  Johannes Reuter
 * @author  Aljona Murygina
 */
@Transactional
public class ApplicationServiceImpl implements ApplicationService {

    // audit logger: logs nontechnically occurences like 'user x applied for leave' or 'subtracted n days from
    // holidays account y'
    private static final Logger LOG = Logger.getLogger("audit");

    // sign logger: logs possible occurent errors relating to private and public keys of users
    private static final Logger LOG_SIGN = Logger.getLogger("sign");

    private static final String DATE_FORMAT = "dd.MM.yyyy";

    private ApplicationDAO applicationDAO;
    private HolidaysAccountService accountService;
    private CryptoService cryptoService;
    private OwnCalendarService calendarService;
    private CalculationService calculationService;
    private MailService mailService;

    @Autowired
    public ApplicationServiceImpl(ApplicationDAO applicationDAO, HolidaysAccountService accountService,
        CryptoService cryptoService, OwnCalendarService calendarService, CalculationService calculationService,
        MailService mailService) {

        this.applicationDAO = applicationDAO;
        this.accountService = accountService;
        this.cryptoService = cryptoService;
        this.calendarService = calendarService;
        this.calculationService = calculationService;
        this.mailService = mailService;
    }

    @Override
    public Application getApplicationById(Integer id) {

        return applicationDAO.findOne(id);
    }


    /**
     * @see  ApplicationService#getApplicationsByStateAndYear(org.synyx.urlaubsverwaltung.domain.ApplicationStatus, int)
     */
    @Override
    public List<Application> getApplicationsByStateAndYear(ApplicationStatus state, int year) {

        DateMidnight firstDayOfYear = new DateMidnight(year, DateTimeConstants.JANUARY, 1);
        DateMidnight lastDayOfYear = new DateMidnight(year, DateTimeConstants.DECEMBER, 31);

        if (state == ApplicationStatus.CANCELLED) {
            return applicationDAO.getCancelledApplicationsByYear(state, firstDayOfYear.toDate(),
                    lastDayOfYear.toDate());
        } else {
            return applicationDAO.getApplicationsByStateAndYear(state, firstDayOfYear.toDate(), lastDayOfYear.toDate());
        }
    }


    /**
     * @see  ApplicationService#getUsedVacationDaysOfPersonForYear(org.synyx.urlaubsverwaltung.domain.Person, int)
     */
    @Override
    public List<Application> getSupplementalApplicationsByPersonAndYear(Person person, int year) {

        DateMidnight firstDayOfYear = new DateMidnight(year, DateTimeConstants.JANUARY, 1);
        DateMidnight lastDayOfYear = new DateMidnight(year, DateTimeConstants.DECEMBER, 31);

        return applicationDAO.getSupplementalApplicationsByPersonAndYear(person, firstDayOfYear.toDate(),
                lastDayOfYear.toDate());
    }


    /**
     * @see  ApplicationService#getApplicationsForACertainTime(org.joda.time.DateMidnight, org.joda.time.DateMidnight)
     */
    @Override
    public List<Application> getApplicationsForACertainTime(DateMidnight startDate, DateMidnight endDate) {

        return applicationDAO.getApplicationsForACertainTime(startDate.toDate(), endDate.toDate());
    }


    /**
     * @see  ApplicationService#allow(org.synyx.urlaubsverwaltung.domain.Application,org.synyx.urlaubsverwaltung.domain.Person)
     */
    @Override
    public void allow(Application application, Person boss) {

        application.setBoss(boss);
        application.setEditedDate(DateMidnight.now());

        // set state on allowed
        application.setStatus(ApplicationStatus.ALLOWED);

        // sign application and save it
        signApplicationByBoss(application, boss);

        simpleSave(application);
    }


    /**
     * @see  ApplicationService#save(org.synyx.urlaubsverwaltung.domain.Application)
     */
    @Override
    public void save(Application application) {

        // get number of used days
        BigDecimal days = calendarService.getVacationDays(application, application.getStartDate(),
                application.getEndDate());

        // if check is successful, application is saved

        application.setStatus(ApplicationStatus.WAITING);

        // set number of used days
        application.setDays(days);

        // save changed application in the end
        simpleSave(application);

        LOG.info("Antrag-Id " + application.getId() + ": Im Zeitraum von "
            + application.getStartDate().toString(DATE_FORMAT) + " bis "
            + application.getEndDate().toString(DATE_FORMAT) + " liegen " + days + " Arbeitstage.");

        if (application.getVacationType() == VacationType.HOLIDAY) {
            List<HolidaysAccount> accounts = calculationService.subtractVacationDays(application);

            if (application.getStartDate().getYear() != application.getEndDate().getYear()) {
                Application decemberApplication = calculationService.createSupplementalApplication(application, true); // application for current year
                simpleSave(decemberApplication);

                Application januaryApplication = calculationService.createSupplementalApplication(application, false); // application for new year
                simpleSave(januaryApplication);
            }

            accountService.saveHolidaysAccount(accounts.get(0));

            if (accounts.size() > 1) {
                accountService.saveHolidaysAccount(accounts.get(1));
            }
        } else {
            HolidaysAccount account = accountService.getAccountOrCreateOne(application.getStartDate().getYear(),
                    application.getPerson());

            if (application.getVacationType() == VacationType.SPECIALLEAVE) {
                if (account.getSpecialLeave() != null) {
                    account.setSpecialLeave(account.getSpecialLeave().add(days));
                } else {
                    account.setSpecialLeave(days);
                }
            } else if (application.getVacationType() == VacationType.UNPAIDLEAVE) {
                if (account.getUnpaidLeave() != null) {
                    account.setUnpaidLeave(account.getUnpaidLeave().add(days));
                } else {
                    account.setUnpaidLeave(days);
                }
            } else if (application.getVacationType() == VacationType.OVERTIME) {
                if (account.getOvertime() != null) {
                    account.setOvertime(account.getOvertime().add(days));
                } else {
                    account.setOvertime(days);
                }
            }

            accountService.saveHolidaysAccount(account);
        }
    }


    /**
     * @see  ApplicationService#reject(org.synyx.urlaubsverwaltung.domain.Application,org.synyx.urlaubsverwaltung.domain.Person)
     */
    @Override
    public void reject(Application application, Person boss) {

        application.setStatus(ApplicationStatus.REJECTED);

        // are there any supplemental applications?
        // change their status too
        setStatusOfSupplementalApplications(application, ApplicationStatus.REJECTED);

        application.setBoss(boss);
        application.setEditedDate(DateMidnight.now());

        rollback(application);

        simpleSave(application);
    }


    /**
     * @see  ApplicationService#cancel(org.synyx.urlaubsverwaltung.domain.Application)
     */
    @Override
    public void cancel(Application application) {

        if (application.getVacationType() == VacationType.HOLIDAY) {
            rollback(application);
        }

        // are there any supplemental applications?
        // change their status too
        setStatusOfSupplementalApplications(application, ApplicationStatus.CANCELLED);

        application.setStatus(ApplicationStatus.CANCELLED);
        application.setCancelDate(DateMidnight.now());
        simpleSave(application);
    }


    /**
     * If an application that spans December and January is cancelled or rejected, the supplemental applications of this
     * application have to get the new status too.
     *
     * @param  application
     * @param  state
     */
    private void setStatusOfSupplementalApplications(Application application, ApplicationStatus state) {

        // are there any supplemental applications?
        if (application.getStartDate().getYear() != application.getEndDate().getYear()) {
            // if an application spans December and January, it has to own two supplementary applications

            List<Application> sApps = applicationDAO.getSupplementalApplicationsForApplication(application.getId());

            // edit status of supplemental applications too
            for (Application sa : sApps) {
                sa.setStatus(state);
                simpleSave(sa);
            }
        }
    }


    /**
     * @see  ApplicationService#signApplicationByBoss(org.synyx.urlaubsverwaltung.domain.Application, org.synyx.urlaubsverwaltung.domain.Person)
     */
    @Override
    public void signApplicationByBoss(Application application, Person boss) {

        byte[] data = signApplication(application, boss);

        if (data != null) {
            application.setSignatureBoss(data);
            applicationDAO.save(application);
        }
    }


    /**
     * @see  ApplicationService#signApplicationByUser(org.synyx.urlaubsverwaltung.domain.Application, org.synyx.urlaubsverwaltung.domain.Person)
     */
    @Override
    public void signApplicationByUser(Application application, Person user) {

        byte[] data = signApplication(application, user);

        if (data != null) {
            application.setSignaturePerson(data);
            applicationDAO.save(application);
        }
    }


    // exceptions have to be catched!
    /**
     * generates signature (byte[]) by private key of person
     *
     * @param  application
     * @param  person
     *
     * @return  data (=signature) if using cryptoService was successful or null if there was any mistake
     */
    private byte[] signApplication(Application application, Person person) {

        try {
            PrivateKey privKey = cryptoService.getPrivateKeyByBytes(person.getPrivateKey());

            StringBuilder build = new StringBuilder();

            build.append(application.getPerson().getLastName());
            build.append(application.getApplicationDate().toString());
            build.append(application.getVacationType().toString());

            byte[] data = build.toString().getBytes();

            data = cryptoService.sign(privKey, data);

            return data;
        } catch (InvalidKeyException ex) {
            logSignException(application.getId(), ex);
        } catch (SignatureException ex) {
            logSignException(application.getId(), ex);
        } catch (NoSuchAlgorithmException ex) {
            logSignException(application.getId(), ex);
        } catch (InvalidKeySpecException ex) {
            logSignException(application.getId(), ex);
        }

        return null;
    }


    /**
     * This method logs exception's details and sends an email to inform the tool manager that an error occured while
     * signing the application.
     *
     * @param  applicationId
     * @param  ex
     */
    private void logSignException(Integer applicationId, Exception ex) {

        LOG_SIGN.error("An error occured during signing application with id " + applicationId, ex);
        mailService.sendSignErrorNotification(applicationId, ex.getMessage());
    }


    /**
     * @see  ApplicationService#checkApplication(org.synyx.urlaubsverwaltung.domain.Application)
     */
    @Override
    public boolean checkApplication(Application application) {

        // get number of used days
        BigDecimal days = calendarService.getVacationDays(application, application.getStartDate(),
                application.getEndDate());

        application.setDays(days);

        List<HolidaysAccount> accounts = calculationService.subtractForCheck(application);
        HolidaysAccount account = accounts.get(0);

        if (accounts.size() == 1) {
            if (CalcUtil.isEqualOrGreaterThanZero(account.getVacationDays())) {
                return true;
            }
        } else if (accounts.size() > 1) {
            HolidaysAccount accountNextYear = accounts.get(1);

            if ((CalcUtil.isEqualOrGreaterThanZero(account.getVacationDays()))
                    && (CalcUtil.isEqualOrGreaterThanZero(accountNextYear.getVacationDays()))) {
                return true;
            }
        }

        return false;
    }


    /**
     * if holiday is cancelled or rejected, calculation in HolidaysAccount has to be reversed
     *
     * @param  application
     */
    protected void rollback(Application application) {

        // for adding vacation days you have to distinguish between following cases
        // 1. period is before April - is equivalent to - account's remaining vacation days don't expire on 1st April
        // 2. period is after April
        // 3. period spans March and April
        // 4. period is in future (current year plus 1), i.e. after 1st January of the new year
        // 5. period spans December and January AND cancelling date is before 1st January
        // 6. period spans December and January AND cancelling date is after 1st January

        Person person = application.getPerson();

        BigDecimal days;

        List<HolidaysAccount> accounts = new ArrayList<HolidaysAccount>();

        HolidaysAccount account = accountService.getAccountOrCreateOne(application.getStartDate().getYear(), person);

        int startMonth = application.getStartDate().getMonthOfYear();
        int endMonth = application.getEndDate().getMonthOfYear();

        // the order of following methods is reallly, reeeeeeally(!) important if you change it, you risk that not the
        // right method is used e.g. an application that spans December and January shall be rollbacked, this means that
        // both of the first following conditions would be true, but only the first contains the right method for such a
        // case

        if (DateUtil.spansDecemberAndJanuary(startMonth, endMonth)) {
            BigDecimal usedDaysCurrentYear = getUsedVacationDaysOfPersonForYear(person,
                    application.getStartDate().getYear());
            BigDecimal usedDaysNextYear = getUsedVacationDaysOfPersonForYear(person,
                    application.getEndDate().getYear());
            accounts = calculationService.addDaysOfApplicationSpanningDecemberAndJanuary(application, account,
                    usedDaysCurrentYear, usedDaysNextYear);
        }
        // period is in future (current year plus 1), i.e. after 1st January of the new year
        else if (calculationService.getCurrentYear() != application.getStartDate().getYear()) {
            days = application.getDays();

            HolidaysAccount accountNextYear = accountService.getAccountOrCreateOne(application.getEndDate().getYear(),
                    person);
            accountNextYear.setVacationDays(accountNextYear.getVacationDays().add(days));
            accounts.add(accountNextYear);
        }
        // period is before April - is equivalent to - account's remaining vacation days don't expire on 1st April
        else if (DateUtil.isBeforeApril(startMonth, endMonth) || (account.isRemainingVacationDaysExpire() == false)) {
            BigDecimal usedDaysBeforeApril = getUsedVacationDaysBeforeAprilOfPerson(person, account.getYear());

            days = application.getDays();
            account = calculationService.addDaysBeforeApril(account, days, usedDaysBeforeApril);
            accounts.add(account);
        } // period is after April
        else if (DateUtil.isAfterApril(startMonth, endMonth)) {
            // if the vacation is after April, only vacation days are filled (not the remaining vacation days)
            days = application.getDays();
            account = calculationService.addDaysAfterApril(account, days);
            accounts.add(account);
        } // period spans March and April
        else if (DateUtil.spansMarchAndApril(startMonth, endMonth)) {
            // if the vacations spans March and April, the number of days in March are added to holidays
            // account's remaining vacation days (and if this is not enough: to vacation days too)
            // and the number of days in April are added only to vacation days
            BigDecimal usedDaysBeforeApril = getUsedVacationDaysBeforeAprilOfPerson(person, account.getYear());
            account = calculationService.addDaysOfApplicationSpanningMarchAndApril(application, account,
                    usedDaysBeforeApril);
            accounts.add(account);
        }

        accountService.saveHolidaysAccount(accounts.get(0));

        if (accounts.size() > 1) {
            accountService.saveHolidaysAccount(accounts.get(1));
        }
    }


    /**
     * @see  ApplicationService#getUsedVacationDaysOfPersonForYear(org.synyx.urlaubsverwaltung.domain.Person, int)
     */
    @Override
    public BigDecimal getUsedVacationDaysOfPersonForYear(Person person, int year) {

        BigDecimal numberOfVacationDays = BigDecimal.ZERO;

        // get all non cancelled applications of person for the given year
        List<Application> applications = getNotCancelledApplicationsByPersonAndYear(person, year);

        // get the supplemental applications of person for the given year
        List<Application> supplementalApplications = getSupplementalApplicationsByPersonAndYear(person, year);

        // put the supplemental applications that have status waiting or allowed in the list of applications
        for (Application a : supplementalApplications) {
            if (a.getStatus() == ApplicationStatus.WAITING || a.getStatus() == ApplicationStatus.ALLOWED) {
                applications.add(a);
            }
        }

        // calculate number of vacation days
        for (Application a : applications) {
            // use only the waiting or allowed applications for calculation
            if (a.getStatus() == ApplicationStatus.WAITING || a.getStatus() == ApplicationStatus.ALLOWED) {
                // use only the applications that do not span December and January
                if (a.getStartDate().getYear() == a.getEndDate().getYear()) {
                    numberOfVacationDays = numberOfVacationDays.add(a.getDays());
                }
            }
        }

        return numberOfVacationDays;
    }


    /**
     * @see  ApplicationService#getUsedVacationDaysBeforeAprilOfPerson(org.synyx.urlaubsverwaltung.domain.Person, int)
     */
    @Override
    public BigDecimal getUsedVacationDaysBeforeAprilOfPerson(Person person, int year) {

        BigDecimal numberOfVacationDays = BigDecimal.ZERO;

        // get all applications of person for the given year before April
        List<Application> applications = getApplicationsBeforeAprilByPersonAndYear(person, year);

        // calculate number of vacation days
        for (Application a : applications) {
            // use only the waiting or allowed applications for calculation
            if (a.getStatus() == ApplicationStatus.WAITING || a.getStatus() == ApplicationStatus.ALLOWED) {
                // if application doesn't span March and April, just add application's days to number of used vacation
                // days
                if (a.getEndDate().isBefore(new DateMidnight(year, DateTimeConstants.APRIL, 1))) {
                    numberOfVacationDays = numberOfVacationDays.add(a.getDays());
                } else {
                    // if application spans March and April, add number of days from application start date to 31st
                    // March
                    BigDecimal days = calendarService.getVacationDays(a, a.getStartDate(),
                            new DateMidnight(year, DateTimeConstants.MARCH, 31));
                    numberOfVacationDays = numberOfVacationDays.add(days);
                }
            }
        }

        return numberOfVacationDays;
    }


    /**
     * @see  ApplicationService#getNotCancelledApplicationsByPersonAndYear(org.synyx.urlaubsverwaltung.domain.Person, int)
     */
    @Override
    public List<Application> getNotCancelledApplicationsByPersonAndYear(Person person, int year) {

        DateMidnight firstDayOfYear = new DateMidnight(year, DateTimeConstants.JANUARY, 1);
        DateMidnight lastDayOfYear = new DateMidnight(year, DateTimeConstants.DECEMBER, 31);

        return applicationDAO.getNotCancelledApplicationsByPersonAndYear(ApplicationStatus.CANCELLED, person,
                firstDayOfYear.toDate(), lastDayOfYear.toDate());
    }


    /**
     * @see  ApplicationService#simpleSave(org.synyx.urlaubsverwaltung.domain.Application)
     */
    @Override
    public void simpleSave(Application application) {

        applicationDAO.save(application);
    }


    /**
     * With this method you get a list of existent applications that overlap with the given period (information about
     * person and period in application) and have the given day length.
     *
     * @param  Application  app
     * @param  DayLength  length
     *
     * @return  List<Application> applications overlapping with the period of the given application
     */
    private List<Application> getApplicationsByPeriodAndDayLength(Application app, DayLength length) {

        if (length == DayLength.MORNING) {
            return applicationDAO.getApplicationsByPeriodAndDayLength(app.getStartDate().toDate(),
                    app.getEndDate().toDate(), app.getPerson(), DayLength.MORNING);
        } else if (length == DayLength.NOON) {
            return applicationDAO.getApplicationsByPeriodAndDayLength(app.getStartDate().toDate(),
                    app.getEndDate().toDate(), app.getPerson(), DayLength.NOON);
        } else {
            return applicationDAO.getApplicationsByPeriodAndDayLength(app.getStartDate().toDate(),
                    app.getEndDate().toDate(), app.getPerson(), DayLength.FULL);
        }
    }


    /**
     * @see  ApplicationService#checkOverlap(org.synyx.urlaubsverwaltung.domain.Application)
     */
    @Override
    public OverlapCase checkOverlap(Application application) {

        if (application.getHowLong() == DayLength.MORNING) {
            return checkOverlapForMorning(application);
        } else if (application.getHowLong() == DayLength.NOON) {
            return checkOverlapForNoon(application);
        } else {
            // check if there are existent ANY applications (full day and half day)
            List<Application> apps = applicationDAO.getApplicationsByPeriodForEveryDayLength(application.getStartDate()
                    .toDate(), application.getEndDate().toDate(), application.getPerson());

            return getCaseOfOverlap(application, apps);
        }
    }


    /**
     * Method to check if the given application with day length "FULL" may be applied or not. (are there existent
     * applications for this period or not?)
     *
     * @param  application
     *
     * @return  int 1 for check is alright: application for leave is valid. 2 or 3 for invalid application for leave.
     */
    protected OverlapCase checkOverlapForFullDay(Application application) {

        // check if there are existent ANY applications (full day and half day)
        List<Application> apps = getApplicationsByPeriodAndDayLength(application, DayLength.FULL);

        return getCaseOfOverlap(application, apps);
    }


    /**
     * Method to check if the given application with day length "MORNING" may be applied or not. (are there existent
     * applications for this period or not?)
     *
     * @param  application
     *
     * @return  int 1 for check is alright: application for leave is valid. 2 or 3 for invalid application for leave.
     */
    protected OverlapCase checkOverlapForMorning(Application application) {

        // check if there are overlaps with full day periods
        if (checkOverlapForFullDay(application) == OverlapCase.NO_OVERLAPPING) {
            // if there are no overlaps with full day periods, you have to check if there are overlaps with half day
            // (MORNING) periods
            List<Application> apps = getApplicationsByPeriodAndDayLength(application, DayLength.MORNING);

            return getCaseOfOverlap(application, apps);
        } else {
            return checkOverlapForFullDay(application);
        }
    }


    /**
     * Method to check if the given application with day length "NOON" may be applied or not. (are there existent
     * applications for this period or not?)
     *
     * @param  application
     *
     * @return  int 1 for check is alright: application for leave is valid. 2 or 3 for invalid application for leave.
     */
    protected OverlapCase checkOverlapForNoon(Application application) {

        // check if there are overlaps with full day periods
        if (checkOverlapForFullDay(application) == OverlapCase.NO_OVERLAPPING) {
            // if there are no overlaps with full day periods, you have to check if there are overlaps with half day
            // (NOON) periods
            List<Application> apps = getApplicationsByPeriodAndDayLength(application, DayLength.NOON);

            return getCaseOfOverlap(application, apps);
        } else {
            return checkOverlapForFullDay(application);
        }
    }


    /**
     * This method contains the logic how to check if there are existent overlapping applications for the given period;
     * use this method only for full day applications.
     *
     * @param  application
     *
     * @return  1 if there is no overlap at all - 2 if the given period is element of (an) existent application(s) - 3
     *          if the new application is part of an existent application's period, but for a part of it you could apply
     *          new vacation
     */
    private OverlapCase getCaseOfOverlap(Application application, List<Application> apps) {

        // case (1): no overlap at all
        if (apps.isEmpty()) {
            /* (1) The
             * period of the new application has no overlap at all with existent applications; i.e. you can calculate
             * the normal way and save the application if there are enough vacation days on person's holidays account.
             */
            return OverlapCase.NO_OVERLAPPING;
        } else {
            // case (2) or (3): overlap

            List<Interval> listOfOverlaps = getListOfOverlaps(application, apps);

            if (application.getHowLong() == DayLength.FULL) {
                List<Interval> listOfGaps = getListOfGaps(application, listOfOverlaps);

                // gaps between the intervals mean that you can apply vacation for this periods
                // this is case (3)
                if (listOfGaps.size() > 0) {
                    /* (3) The period of the new application is part
                     * of an existent application's period, but for a part of it you could apply new vacation; i.e. user
                     * must be asked if he wants to apply for leave for the not overlapping period of the new
                     * application.
                     */
                    return OverlapCase.FULLY_OVERLAPPING.PARTLY_OVERLAPPING;
                }
            }
            // no gaps mean that period of application is element of other periods of applications
            // i.e. you have no free periods to apply vacation for
            // this is case (2)

            /* (2) The period of
             * the new application is element of an existent application's period; i.e. the new application is not
             * necessary because there is already an existent application for this period.
             */
            return OverlapCase.FULLY_OVERLAPPING;
        }
    }


    /**
     * This method gets a list of applications that overlap with the period of the given application; all overlapping
     * intervals are put in this list for further checking (e.g. if there are gaps) and for getting the case of overlap
     * (1, 2 or 3)
     *
     * @param  application
     * @param  apps
     *
     * @return  List<Interval> list of overlaps
     */
    private List<Interval> getListOfOverlaps(Application application, List<Application> apps) {

        Interval interval = new Interval(application.getStartDate(), application.getEndDate());

        List<Interval> listOfOverlaps = new ArrayList<Interval>();

        for (Application a : apps) {
            Interval inti = new Interval(a.getStartDate(), a.getEndDate());
            Interval overlap = inti.overlap(interval);

            // because intervals are inclusive of the start instant, but exclusive of the end instant
            // you have to check if end of interval a is start of interval b

            if (inti.getEnd().equals(interval.getStart())) {
                overlap = new Interval(interval.getStart(), interval.getStart());
            }

            if (inti.getStart().equals(interval.getEnd())) {
                overlap = new Interval(interval.getEnd(), interval.getEnd());
            }

            // check if they really overlap, else value of overlap would be null
            if (overlap != null) {
                listOfOverlaps.add(overlap);
            }
        }

        return listOfOverlaps;
    }


    /**
     * This method gets a list of overlaps and checks with the given application if there are any gaps where a user
     * could apply for leave (these gaps are not yet applied for leave) - may be a feature in later version.
     *
     * @param  application
     * @param  listOfOverlaps
     *
     * @return  List<Interval> list of gaps
     */
    private List<Interval> getListOfGaps(Application application, List<Interval> listOfOverlaps) {

        List<Interval> listOfGaps = new ArrayList<Interval>();

        // check start and end points

        DateMidnight firstOverlapStart = listOfOverlaps.get(0).getStart().toDateMidnight();
        DateMidnight lastOverlapEnd = listOfOverlaps.get(listOfOverlaps.size() - 1).getEnd().toDateMidnight();

        if (application.getStartDate().isBefore(firstOverlapStart)) {
            Interval gapStart = new Interval(application.getStartDate(), firstOverlapStart);
            listOfGaps.add(gapStart);
        }

        if (application.getEndDate().isAfter(lastOverlapEnd)) {
            Interval gapEnd = new Interval(lastOverlapEnd, application.getEndDate());
            listOfGaps.add(gapEnd);
        }

        // check if intervals abut or gap
        for (int i = 0; (i + 1) < listOfOverlaps.size(); i++) {
            // if they don't abut, you can calculate the gap
            // test if end of interval is equals resp. one day plus of start of other interval
            // e.g. if period 1: 16.-18. and period 2: 19.-20 --> they abut
            // e.g. if period 1: 16.-18. and period 2: 20.-22 --> they have a gap
            if (intervalsHaveGap(listOfOverlaps.get(i), listOfOverlaps.get(i + 1))) {
                Interval gap = listOfOverlaps.get(i).gap(listOfOverlaps.get(i + 1));
                listOfGaps.add(gap);
            }
        }

        return listOfGaps;
    }


    /**
     * This method checks if the two given intervals have a gap or if they abut. Some examples: (1) if period 1: 16.-18.
     * and period 2: 19.-20 --> they abut (2) if period 1: 16.-18. and period 2: 20.-22 --> they have a gap
     *
     * @param  i1
     * @param  i2
     *
     * @return  true if they have a gap between or false if they have no gap
     */
    private boolean intervalsHaveGap(Interval i1, Interval i2) {

        // test if end of interval is equals resp. one day plus of start of other interval
        if (!(i1.getEnd().toDateMidnight().equals(i2.getStart().toDateMidnight())
                    || i1.getEnd().toDateMidnight().plusDays(1).equals(i2.getStart().toDateMidnight()))) {
            return true;
        } else {
            return false;
        }
    }


    @Override
    public List<Application> getApplicationsBeforeAprilByPersonAndYear(Person person, int year) {

        Date firstJanuary = new DateMidnight(year, DateTimeConstants.JANUARY, 1).toDate();
        Date lastOfMarch = new DateMidnight(year, DateTimeConstants.MARCH, 31).toDate();

        return applicationDAO.getApplicationsBeforeAprilByPersonAndYear(person, firstJanuary, lastOfMarch);
    }

    @Override
    public List<Application> getAllApplicationsByPersonAndYear(Person person, int year) {
        DateMidnight firstDayOfYear = new DateMidnight(year, DateTimeConstants.JANUARY, 1);
        DateMidnight lastDayOfYear = new DateMidnight(year, DateTimeConstants.DECEMBER, 31);
        
        return applicationDAO.getAllApplicationsByPersonAndYear(person,
                firstDayOfYear.toDate(), lastDayOfYear.toDate());
    }

    @Override
    public int getIdOfLatestApplication(Person person, ApplicationStatus status) {
        return applicationDAO.getIdOfLatestApplication(person, status);
    }

    @Override
    public List<Application> getCancelledApplicationsByYearFormerlyAllowed(int year) {
        DateMidnight firstDayOfYear = new DateMidnight(year, DateTimeConstants.JANUARY, 1);
        DateMidnight lastDayOfYear = new DateMidnight(year, DateTimeConstants.DECEMBER, 31);
        return applicationDAO.getCancelledApplicationsByYear(ApplicationStatus.CANCELLED, firstDayOfYear.toDate(), lastDayOfYear.toDate());
    }
}
