
package org.synyx.urlaubsverwaltung.core.application.service;

import org.apache.log4j.Logger;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import org.synyx.urlaubsverwaltung.DateFormat;
import org.synyx.urlaubsverwaltung.core.application.dao.ApplicationDAO;
import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.core.application.domain.Comment;
import org.synyx.urlaubsverwaltung.core.calendar.OwnCalendarService;
import org.synyx.urlaubsverwaltung.core.mail.MailService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.security.CryptoUtil;

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
@Service
class ApplicationServiceImpl implements ApplicationService {

    // sign logger: logs possible occurent errors relating to private and public keys of users
    private static final Logger LOG_SIGN = Logger.getLogger("sign");
    private static final Logger LOG = Logger.getLogger("audit");

    private ApplicationDAO applicationDAO;
    private MailService mailService;
    private OwnCalendarService calendarService;
    private CommentService commentService;

    @Autowired
    public ApplicationServiceImpl(ApplicationDAO applicationDAO, MailService mailService,
        OwnCalendarService calendarService, CommentService commentService) {

        this.applicationDAO = applicationDAO;
        this.mailService = mailService;
        this.calendarService = calendarService;
        this.commentService = commentService;
    }

    /**
     * @see  ApplicationService#getIdOfLatestApplication(org.synyx.urlaubsverwaltung.core.person.Person, org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus)
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
     * @see  ApplicationService#save(org.synyx.urlaubsverwaltung.core.application.domain.Application)
     */
    @Override
    public void save(Application application) {

        applicationDAO.save(application);
    }


    /**
     * @see  ApplicationService#apply(org.synyx.urlaubsverwaltung.core.application.domain.Application, org.synyx.urlaubsverwaltung.core.person.Person,
     *       org.synyx.urlaubsverwaltung.core.person.Person)
     */
    @Override
    public Application apply(Application application, Person person, Person applier) {

        BigDecimal days = calendarService.getWorkDays(application.getHowLong(), application.getStartDate(),
                application.getEndDate(), person);

        application.setStatus(ApplicationStatus.WAITING);
        application.setDays(days);
        application.setPerson(person);
        application.setApplier(applier);
        application.setApplicationDate(DateMidnight.now());

        return application;
    }


    /**
     * @see  ApplicationService#allow(org.synyx.urlaubsverwaltung.core.application.domain.Application, org.synyx.urlaubsverwaltung.core.person.Person,
     *       org.synyx.urlaubsverwaltung.core.application.domain.Comment)
     */
    @Override
    public void allow(Application application, Person boss, Comment comment) {

        application.setBoss(boss);
        application.setEditedDate(DateMidnight.now());

        // set state on allowed
        application.setStatus(ApplicationStatus.ALLOWED);

        // sign application and save it
        signApplicationByBoss(application, boss);

        save(application);

        LOG.info(application.getApplicationDate() + " ID: " + application.getId() + "Der Antrag von "
            + application.getPerson().getNiceName()
            + " wurde am " + DateMidnight.now().toString(DateFormat.PATTERN) + " von " + boss.getNiceName()
            + " genehmigt.");

        commentService.saveComment(comment, boss, application);

        mailService.sendAllowedNotification(application, comment);

        if (application.getRep() != null) {
            mailService.notifyRepresentative(application);
        }
    }


    /**
     * @see  ApplicationService#reject(org.synyx.urlaubsverwaltung.core.application.domain.Application, org.synyx.urlaubsverwaltung.core.person.Person)
     */
    @Override
    public void reject(Application application, Person boss) {

        application.setStatus(ApplicationStatus.REJECTED);

        application.setBoss(boss);
        application.setEditedDate(DateMidnight.now());

        save(application);
    }


    /**
     * @see  ApplicationService#cancel(org.synyx.urlaubsverwaltung.core.application.domain.Application)
     */
    @Override
    public void cancel(Application application) {

        application.setStatus(ApplicationStatus.CANCELLED);
        application.setCancelDate(DateMidnight.now());

        save(application);
    }


    /**
     * @see  ApplicationService#signApplicationByUser(org.synyx.urlaubsverwaltung.core.application.domain.Application, org.synyx.urlaubsverwaltung.core.person.Person)
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
     * @see  ApplicationService#signApplicationByBoss(org.synyx.urlaubsverwaltung.core.application.domain.Application, org.synyx.urlaubsverwaltung.core.person.Person)
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
            PrivateKey privKey = CryptoUtil.getPrivateKeyByBytes(person.getPrivateKey());

            StringBuilder build = new StringBuilder();

            build.append(application.getPerson().getLastName());
            build.append(application.getApplicationDate().toString());
            build.append(application.getVacationType().toString());

            byte[] data = build.toString().getBytes();

            data = CryptoUtil.sign(privKey, data);

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


    @Override
    public List<Application> getAllAllowedApplicationsOfAPersonForAMonth(Person person, int month, int year) {

        return applicationDAO.getAllAllowedApplicationsOfAPersonForMonth(person, month, year);
    }


    /**
     * @see  ApplicationService#getApplicationsByStateAndYear(org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus,
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
     * @see  ApplicationService#getAllApplicationsByPersonAndYear(org.synyx.urlaubsverwaltung.core.person.Person, int)
     */
    @Override
    public List<Application> getAllApplicationsByPersonAndYear(Person person, int year) {

        DateMidnight firstDayOfYear = new DateMidnight(year, DateTimeConstants.JANUARY, 1);
        DateMidnight lastDayOfYear = new DateMidnight(year, DateTimeConstants.DECEMBER, 31);

        return applicationDAO.getAllApplicationsByPersonAndYear(person, firstDayOfYear.toDate(),
                lastDayOfYear.toDate());
    }


    @Override
    public List<Application> getAllApplicationsByPersonAndYearAndState(Person person, int year,
        ApplicationStatus state) {

        return applicationDAO.getApplicationsByPersonAndYearAndState(person, year, state);
    }
}
