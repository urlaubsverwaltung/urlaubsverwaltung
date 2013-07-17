
package org.synyx.urlaubsverwaltung.application.service;

import org.apache.log4j.Logger;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;

import org.springframework.beans.factory.annotation.Autowired;

import org.synyx.urlaubsverwaltung.application.dao.ApplicationDAO;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.calendar.OwnCalendarService;
import org.synyx.urlaubsverwaltung.mail.MailService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.security.CryptoService;

import java.math.BigDecimal;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

import java.util.List;


/**
 * Implementation of interface {@link ApplicationService}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
class ApplicationServiceImpl implements ApplicationService {

    // sign logger: logs possible occurent errors relating to private and public keys of users
    private static final Logger LOG_SIGN = Logger.getLogger("sign");

    private ApplicationDAO applicationDAO;

    private CryptoService cryptoService;
    private MailService mailService;
    private OwnCalendarService calendarService;

    @Autowired
    public ApplicationServiceImpl(ApplicationDAO applicationDAO, CryptoService cryptoService, MailService mailService,
        OwnCalendarService calendarService) {

        this.applicationDAO = applicationDAO;
        this.cryptoService = cryptoService;
        this.mailService = mailService;
        this.calendarService = calendarService;
    }

    /**
     * @see  ApplicationService#getIdOfLatestApplication(org.synyx.urlaubsverwaltung.person.Person, org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus)
     */
    @Override
    public int getIdOfLatestApplication(Person person, ApplicationStatus status) {

        return applicationDAO.getIdOfLatestApplication(person, status);
    }


    /**
     * @see  ApplicationService#getApplicationById(Integer)
     */
    @Override
    public Application getApplicationById(Integer id) {

        return applicationDAO.findOne(id);
    }


    /**
     * @see  ApplicationService#save(org.synyx.urlaubsverwaltung.application.domain.Application)
     */
    @Override
    public void save(Application application) {

        applicationDAO.save(application);
    }


    /**
     * @see  ApplicationService#apply(org.synyx.urlaubsverwaltung.application.domain.Application,org.synyx.urlaubsverwaltung.person.Person,
     *       org.synyx.urlaubsverwaltung.person.Person)
     */
    @Override
    public Application apply(Application application, Person person, Person applier) {

        BigDecimal days = calendarService.getVacationDays(application.getHowLong(), application.getStartDate(),
                application.getEndDate());

        application.setStatus(ApplicationStatus.WAITING);
        application.setDays(days);
        application.setPerson(person);
        application.setApplier(applier);
        application.setApplicationDate(DateMidnight.now());

        return application;
    }


    /**
     * @see  ApplicationService#allow(org.synyx.urlaubsverwaltung.application.domain.Application, org.synyx.urlaubsverwaltung.person.Person)
     */
    @Override
    public void allow(Application application, Person boss) {

        application.setBoss(boss);
        application.setEditedDate(DateMidnight.now());

        // set state on allowed
        application.setStatus(ApplicationStatus.ALLOWED);

        // sign application and save it
        signApplicationByBoss(application, boss);

        save(application);
    }


    /**
     * @see  ApplicationService#reject(org.synyx.urlaubsverwaltung.application.domain.Application, org.synyx.urlaubsverwaltung.person.Person)
     */
    @Override
    public void reject(Application application, Person boss) {

        application.setStatus(ApplicationStatus.REJECTED);

        // are there any supplemental applications?
        // change their status too
        setStatusOfSupplementalApplications(application, ApplicationStatus.REJECTED);

        application.setBoss(boss);
        application.setEditedDate(DateMidnight.now());

        save(application);
    }


    /**
     * @see  ApplicationService#cancel(org.synyx.urlaubsverwaltung.application.domain.Application)
     */
    @Override
    public void cancel(Application application) {

        // are there any supplemental applications?
        // change their status too
        setStatusOfSupplementalApplications(application, ApplicationStatus.CANCELLED);

        application.setStatus(ApplicationStatus.CANCELLED);
        application.setCancelDate(DateMidnight.now());

        save(application);
    }


    /**
     * @see  ApplicationService#signApplicationByUser(org.synyx.urlaubsverwaltung.application.domain.Application, org.synyx.urlaubsverwaltung.person.Person)
     */
    @Override
    public void signApplicationByUser(Application application, Person user) {

        byte[] data = signApplication(application, user);

        if (data != null) {
            application.setSignaturePerson(data);
            applicationDAO.save(application);
        }
    }


    /**
     * @see  ApplicationService#signApplicationByBoss(org.synyx.urlaubsverwaltung.application.domain.Application, org.synyx.urlaubsverwaltung.person.Person)
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
     * Generates signature (byte[]) by private key of {@link Person}.
     *
     * @param  application {@link Application}
     * @param  person {@link Person}
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
     * @param  applicationId  Integer
     * @param  ex  Exception
     */
    private void logSignException(Integer applicationId, Exception ex) {

        LOG_SIGN.error("An error occured during signing application with id " + applicationId, ex);
        mailService.sendSignErrorNotification(applicationId, ex.getMessage());
    }


    /**
     * If an {@link Application} that spans December and January is cancelled or rejected, the supplemental applications
     * of this {@link Application} have to get the new status too.
     *
     * @param  application {@link Application}
     * @param  state {@link ApplicationStatus}
     */
    private void setStatusOfSupplementalApplications(Application application, ApplicationStatus state) {

        // are there any supplemental applications?
        if (application.getStartDate().getYear() != application.getEndDate().getYear()) {
            // if an application spans December and January, it has to own two supplementary applications

            List<Application> sApps = applicationDAO.getSupplementalApplicationsForApplication(application.getId());

            // edit status of supplemental applications too
            for (Application sa : sApps) {
                sa.setStatus(state);
                save(sa);
            }
        }
    }


    /**
     * @see  ApplicationService#getAllowedApplicationsForACertainPeriod(org.joda.time.DateMidnight, org.joda.time.DateMidnight)
     */
    @Override
    public List<Application> getAllowedApplicationsForACertainPeriod(DateMidnight startDate, DateMidnight endDate) {

        return applicationDAO.getApplicationsForACertainTimeAndState(startDate.toDate(), endDate.toDate(),
                ApplicationStatus.ALLOWED);
    }


    /**
     * @see  ApplicationService#getApplicationsForACertainPeriod(org.joda.time.DateMidnight, org.joda.time.DateMidnight)
     */
    @Override
    public List<Application> getApplicationsForACertainPeriod(DateMidnight startDate, DateMidnight endDate) {

        return applicationDAO.getApplicationsForACertainTime(startDate.toDate(), endDate.toDate());
    }


    /**
     * @see  ApplicationService#getApplicationsByStateAndYear(org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus,
     *       int)
     */
    @Override
    public List<Application> getApplicationsByStateAndYear(ApplicationStatus state, int year) {

        DateMidnight firstDayOfYear = new DateMidnight(year, DateTimeConstants.JANUARY, 1);
        DateMidnight lastDayOfYear = new DateMidnight(year, DateTimeConstants.DECEMBER, 31);

        if (state == ApplicationStatus.CANCELLED) {
            return applicationDAO.getCancelledApplicationsByYearThatHaveBeenAllowedFormerly(state,
                    firstDayOfYear.toDate(), lastDayOfYear.toDate());
        } else {
            return applicationDAO.getApplicationsByStateAndYear(state, firstDayOfYear.toDate(), lastDayOfYear.toDate());
        }
    }


    /**
     * @see  ApplicationService#getCancelledApplicationsByYearFormerlyAllowed(int)
     */
    @Override
    public List<Application> getCancelledApplicationsByYearFormerlyAllowed(int year) {

        DateMidnight firstDayOfYear = new DateMidnight(year, DateTimeConstants.JANUARY, 1);
        DateMidnight lastDayOfYear = new DateMidnight(year, DateTimeConstants.DECEMBER, 31);

        return applicationDAO.getCancelledApplicationsByYearThatHaveBeenAllowedFormerly(ApplicationStatus.CANCELLED,
                firstDayOfYear.toDate(), lastDayOfYear.toDate());
    }


    /**
     * @see  ApplicationService#getAllApplicationsByPersonAndYear(org.synyx.urlaubsverwaltung.person.Person, int)
     */
    @Override
    public List<Application> getAllApplicationsByPersonAndYear(Person person, int year) {

        DateMidnight firstDayOfYear = new DateMidnight(year, DateTimeConstants.JANUARY, 1);
        DateMidnight lastDayOfYear = new DateMidnight(year, DateTimeConstants.DECEMBER, 31);

        return applicationDAO.getAllApplicationsByPersonAndYear(person, firstDayOfYear.toDate(),
                lastDayOfYear.toDate());
    }
}
