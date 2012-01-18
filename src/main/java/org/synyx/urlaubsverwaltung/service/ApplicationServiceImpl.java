package org.synyx.urlaubsverwaltung.service;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;
import org.joda.time.Interval;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.transaction.annotation.Transactional;

import org.synyx.urlaubsverwaltung.calendar.OwnCalendarService;
import org.synyx.urlaubsverwaltung.dao.ApplicationDAO;
import org.synyx.urlaubsverwaltung.domain.Application;
import org.synyx.urlaubsverwaltung.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.domain.HolidaysAccount;
import org.synyx.urlaubsverwaltung.domain.Person;
import org.synyx.urlaubsverwaltung.util.CalcUtil;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

import java.util.ArrayList;
import java.util.List;


/**
 * implementation of the applicationdata-access-service.
 *
 * @author  Johannes Reuter
 * @author  Aljona Murygina
 */
@Transactional
public class ApplicationServiceImpl implements ApplicationService {

    private ApplicationDAO applicationDAO;
    private HolidaysAccountService accountService;
    private CryptoService cryptoService;
    private OwnCalendarService calendarService;
    private MailService mailService;
    private CalculationService calculationService;

    @Autowired
    public ApplicationServiceImpl(ApplicationDAO applicationDAO, HolidaysAccountService accountService,
        CryptoService cryptoService, OwnCalendarService calendarService, MailService mailService,
        CalculationService calculationService) {

        this.applicationDAO = applicationDAO;
        this.accountService = accountService;
        this.cryptoService = cryptoService;
        this.calendarService = calendarService;
        this.mailService = mailService;
        this.calculationService = calculationService;
    }

    @Override
    public Application getApplicationById(Integer id) {

        return applicationDAO.findOne(id);
    }


    /**
     * @see  ApplicationService#getApplicationsByPerson(org.synyx.urlaubsverwaltung.domain.Person)
     */
    @Override
    public List<Application> getApplicationsByPerson(Person person) {

        return applicationDAO.getApplicationsByPerson(person);
    }


    /**
     * @see  ApplicationService#getApplicationsByState(org.synyx.urlaubsverwaltung.domain.ApplicationStatus)
     */
    @Override
    public List<Application> getApplicationsByState(ApplicationStatus state) {

        return applicationDAO.getApplicationsByState(state);
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

        // set state on allowed
        application.setStatus(ApplicationStatus.ALLOWED);

        // sign application and save it
        signApplicationByBoss(application, boss);

        simpleSave(application);

        mailService.sendAllowedNotification(application);
    }


    /**
     * @see  ApplicationService#save(org.synyx.urlaubsverwaltung.domain.Application)
     */
    @Override
    public void save(Application application) {

        // if check is successful, application is saved

        application.setStatus(ApplicationStatus.WAITING);

        // set number of used days
        application.setDays(calendarService.getVacationDays(application, application.getStartDate(),
                application.getEndDate()));

        List<HolidaysAccount> accounts = calculationService.subtractVacationDays(application);

        accountService.saveHolidaysAccount(accounts.get(0));

        if (accounts.size() > 1) {
            accountService.saveHolidaysAccount(accounts.get(1));
        }

        // save changed application in the end
        applicationDAO.save(application);

        // mail to applicant
        mailService.sendConfirmation(application);
    }


    /**
     * @see  ApplicationService#reject(org.synyx.urlaubsverwaltung.domain.Application,org.synyx.urlaubsverwaltung.domain.Person,
     *       java.lang.String)
     */
    @Override
    public void reject(Application application, Person boss) {

        application.setStatus(ApplicationStatus.REJECTED);

        application.setBoss(boss);

        rollback(application);

        applicationDAO.save(application);

        mailService.sendRejectedNotification(application);
    }


    /**
     * @see  ApplicationService#cancel(org.synyx.urlaubsverwaltung.domain.Application)
     */
    @Override
    public void cancel(Application application) {

        rollback(application);

        if (application.getStatus() == ApplicationStatus.WAITING) {
            application.setStatus(ApplicationStatus.CANCELLED);
            applicationDAO.save(application);

            // if application has been waiting, chefs get email
            mailService.sendCancelledNotification(application, true);
        } else if (application.getStatus() == ApplicationStatus.ALLOWED) {
            application.setStatus(ApplicationStatus.CANCELLED);
            applicationDAO.save(application);

            // if application has been allowed, office gets email
            mailService.sendCancelledNotification(application, false);
        }
    }


    /**
     * @see  ApplicationService#addSickDaysOnHolidaysAccount(org.synyx.urlaubsverwaltung.domain.Application, double)
     */
    @Override
    public void addSickDaysOnHolidaysAccount(Application application) {

        application.setDays(application.getDays().subtract(application.getSickDays()));

        HolidaysAccount account = accountService.getHolidaysAccount(application.getDateOfAddingSickDays().getYear(),
                application.getPerson());

        account = calculationService.addSickDaysOnHolidaysAccount(application, account);

        accountService.saveHolidaysAccount(account);
        applicationDAO.save(application);
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
    public byte[] signApplication(Application application, Person person) {

        try {
            PrivateKey privKey = cryptoService.getPrivateKeyByBytes(person.getPrivateKey());

            StringBuilder build = new StringBuilder();

            build.append(application.getPerson().getLastName());
            build.append(application.getApplicationDate().toString());
            build.append(application.getVacationType().toString());

            byte[] data = build.toString().getBytes();

            data = cryptoService.sign(privKey, data);

            return data;
        } // TODO Logging, catchen von Exceptions
        catch (InvalidKeyException ex) {
        } catch (SignatureException ex) {
        } catch (NoSuchAlgorithmException ex) {
        } catch (InvalidKeySpecException ex) {
        }

        return null;
    }


    /**
     * @see  ApplicationService#checkApplication(org.synyx.urlaubsverwaltung.domain.Application)
     */
    @Override
    public boolean checkApplication(Application application) {

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
    private void rollback(Application application) {

        List<HolidaysAccount> accounts = calculationService.addVacationDays(application);

        accountService.saveHolidaysAccount(accounts.get(0));

        if (accounts.size() > 1) {
            accountService.saveHolidaysAccount(accounts.get(1));
        }
    }


    /**
     * @see  ApplicationService#getApplicationsByPersonAndYear(org.synyx.urlaubsverwaltung.domain.Person, int)
     */
    @Override
    public List<Application> getApplicationsByPersonAndYear(Person person, int year) {

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


    protected List<Application> getApplicationsByPersonForACertainTime(Application application) {

        return applicationDAO.getApplicationsByPersonForACertainTime(application.getStartDate().toDate(),
                application.getEndDate().toDate(), application.getPerson());
    }


    @Override
    public int checkOverlap(Application application) {

        DateMidnight startDate = application.getStartDate();
        DateMidnight endDate = application.getEndDate();

        Interval interval = new Interval(startDate, endDate);
        List<Interval> listOfOverlaps = new ArrayList<Interval>();

        // list ordered by startDate of applications
        List<Application> apps = getApplicationsByPersonForACertainTime(application);

        // case (1): no overlap at all
        if (apps.isEmpty()) {
            /* (1) The
             * period of the new application has no overlap at all with existent applications; i.e. you can calculate
             * the normal way and save the application if there are enough vacation days on person's holidays account.
             */
            return 1;
        } else {
            // case (2) or (3): overlap

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

            List<Interval> listOfGaps = new ArrayList<Interval>();

            // check start and end points

            DateMidnight firstOverlapStart = listOfOverlaps.get(0).getStart().toDateMidnight();
            DateMidnight lastOverlapEnd = listOfOverlaps.get(listOfOverlaps.size() - 1).getEnd().toDateMidnight();

            if (startDate.isBefore(firstOverlapStart)) {
                Interval gapStart = new Interval(startDate, firstOverlapStart);
                listOfGaps.add(gapStart);
            }

            if (endDate.isAfter(lastOverlapEnd)) {
                Interval gapEnd = new Interval(lastOverlapEnd, endDate);
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

            // gaps between the intervals mean that you can apply vacation for this periods
            // this is case (3)
            if (listOfGaps.size() > 0) {
                /* (3) The period of the new application is part
                 * of an existent application's period, but for a part of it you could apply new vacation; i.e. user
                 * must be asked if he wants to apply for leave for the not overlapping period of the new application.
                 */
                return 3;
            } else {
                // no gaps mean that period of application is element of other periods of applications
                // i.e. you have no free periods to apply vacation for
                // this is case (2)

                /* (2) The period of
                 * the new application is element of an existent application's period; i.e. the new application is not
                 * necessary because there is already an existent application for this period.
                 */
                return 2;
            }
        }
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
}
