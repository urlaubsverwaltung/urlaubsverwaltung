package org.synyx.urlaubsverwaltung.core.application.service;

import org.apache.log4j.Logger;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.core.application.domain.Comment;
import org.synyx.urlaubsverwaltung.core.calendar.OwnCalendarService;
import org.synyx.urlaubsverwaltung.core.mail.MailService;
import org.synyx.urlaubsverwaltung.core.person.Person;

import java.math.BigDecimal;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Service
@Transactional
public class ApplicationInteractionServiceImpl implements ApplicationInteractionService {

    private static final Logger LOG = Logger.getLogger(ApplicationInteractionServiceImpl.class);

    private final ApplicationService applicationService;
    private final OwnCalendarService calendarService;
    private final SignService signService;
    private final CommentService commentService;
    private final MailService mailService;

    @Autowired
    public ApplicationInteractionServiceImpl(ApplicationService applicationService, OwnCalendarService calendarService,
        SignService signService, CommentService commentService, MailService mailService) {

        this.applicationService = applicationService;
        this.calendarService = calendarService;
        this.signService = signService;
        this.commentService = commentService;
        this.mailService = mailService;
    }

    @Override
    public Application apply(Application application, Person applier) {

        Person person = application.getPerson();

        BigDecimal days = calendarService.getWorkDays(application.getHowLong(), application.getStartDate(),
                application.getEndDate(), person);

        application.setStatus(ApplicationStatus.WAITING);
        application.setDays(days);
        application.setApplier(applier);
        application.setApplicationDate(DateMidnight.now());

        signService.signApplicationByUser(application, applier);

        Application savedApplication = applicationService.save(application);

        LOG.info("Created application for leave: " + application.toString());

        // COMMENT

        Comment comment = new Comment();

        if (application.getComment() != null) {
            comment.setReason(application.getComment());
        }

        commentService.saveComment(comment, applier, application);

        // EMAILS

        // person himself applies for leave
        if (person.equals(applier)) {
            // person gets a confirmation email with the data of the application for leave
            mailService.sendConfirmation(application);
        }
        // someone else (normally the office) applies for leave on behalf of the person
        else {
            // person gets an email that someone else has applied for leave on behalf
            mailService.sendAppliedForLeaveByOfficeNotification(application);
        }

        // bosses gets email that a new application for leave has been created
        mailService.sendNewApplicationNotification(application);

        return savedApplication;
    }


    @Override
    public Application allow(Application application, Person boss, Comment comment) {

        application.setStatus(ApplicationStatus.ALLOWED);
        application.setBoss(boss);
        application.setEditedDate(DateMidnight.now());

        signService.signApplicationByBoss(application, boss);

        Application allowedApplication = applicationService.save(application);

        LOG.info("Allowed application for leave: " + application.toString());

        commentService.saveComment(comment, boss, application);

        mailService.sendAllowedNotification(application, comment);

        if (application.getRep() != null) {
            mailService.notifyRepresentative(application);
        }

        return allowedApplication;
    }


    @Override
    public Application reject(Application application, Person boss, Comment comment) {

        application.setStatus(ApplicationStatus.REJECTED);
        application.setBoss(boss);
        application.setEditedDate(DateMidnight.now());

        signService.signApplicationByBoss(application, boss);

        Application rejectedApplication = applicationService.save(application);

        LOG.info("Rejected application for leave: " + application.toString());

        commentService.saveComment(comment, boss, application);

        mailService.sendRejectedNotification(application, comment);

        return rejectedApplication;
    }


    @Override
    public Application cancel(Application application, Person canceller, Comment comment) {

        boolean cancellingAllowedApplication = application.getStatus().equals(ApplicationStatus.ALLOWED);

        application.setStatus(ApplicationStatus.CANCELLED);
        application.setCanceller(canceller);
        application.setCancelDate(DateMidnight.now());

        if (cancellingAllowedApplication) {
            application.setFormerlyAllowed(true);
        }

        Application cancelledApplication = applicationService.save(application);

        LOG.info("Cancelled application for leave: " + application);

        commentService.saveComment(comment, canceller, application);

        if (cancellingAllowedApplication) {
            // if allowed application has been cancelled, office and bosses get an email
            mailService.sendCancelledNotification(application, false, comment);
        }

        if (!application.getPerson().equals(canceller)) {
            // if application has been cancelled for someone on behalf,
            // the person gets an email regardless of application status
            mailService.sendCancelledNotification(application, true, comment);
        }

        return cancelledApplication;
    }
}
