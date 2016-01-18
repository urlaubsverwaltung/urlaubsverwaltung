package org.synyx.urlaubsverwaltung.core.application.service;

import org.apache.log4j.Logger;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import org.synyx.urlaubsverwaltung.core.account.service.AccountInteractionService;
import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationAction;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationComment;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.core.application.service.exception.ImpatientAboutApplicationForLeaveProcessException;
import org.synyx.urlaubsverwaltung.core.application.service.exception.RemindAlreadySentException;
import org.synyx.urlaubsverwaltung.core.mail.MailService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.settings.CalendarSettings;
import org.synyx.urlaubsverwaltung.core.settings.SettingsService;
import org.synyx.urlaubsverwaltung.core.sync.CalendarSyncService;
import org.synyx.urlaubsverwaltung.core.sync.absence.*;

import java.util.Optional;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Service
@Transactional
public class ApplicationInteractionServiceImpl implements ApplicationInteractionService {

    private static final Logger LOG = Logger.getLogger(ApplicationInteractionServiceImpl.class);

    private static final int MIN_DAYS_LEFT_BEFORE_REMINDING_IS_POSSIBLE = 2;

    private final ApplicationService applicationService;
    private final AccountInteractionService accountInteractionService;
    private final SignService signService;
    private final ApplicationCommentService commentService;
    private final MailService mailService;
    private final CalendarSyncService calendarSyncService;
    private final AbsenceMappingService absenceMappingService;
    private final SettingsService settingsService;

    @Autowired
    public ApplicationInteractionServiceImpl(ApplicationService applicationService,
        ApplicationCommentService commentService, AccountInteractionService accountInteractionService,
        SignService signService, MailService mailService, CalendarSyncService calendarSyncService,
        AbsenceMappingService absenceMappingService, SettingsService settingsService) {

        this.applicationService = applicationService;
        this.commentService = commentService;
        this.accountInteractionService = accountInteractionService;
        this.signService = signService;
        this.mailService = mailService;
        this.calendarSyncService = calendarSyncService;
        this.absenceMappingService = absenceMappingService;
        this.settingsService = settingsService;
    }

    @Override
    public Application apply(Application application, Person applier, Optional<String> comment) {

        Person person = application.getPerson();

        application.setStatus(ApplicationStatus.WAITING);
        application.setApplier(applier);
        application.setApplicationDate(DateMidnight.now());

        signService.signApplicationByUser(application, applier);

        applicationService.save(application);

        LOG.info("Created application for leave: " + application.toString());

        // COMMENT
        ApplicationComment createdComment = commentService.create(application, ApplicationAction.APPLIED, comment,
                applier);

        // EMAILS

        // person himself applies for leave
        if (person.equals(applier)) {
            // person gets a confirmation email with the data of the application for leave
            mailService.sendConfirmation(application, createdComment);
        }
        // someone else (normally the office) applies for leave on behalf of the person
        else {
            // person gets an email that someone else has applied for leave on behalf
            mailService.sendAppliedForLeaveByOfficeNotification(application, createdComment);
        }

        // bosses gets email that a new application for leave has been created
        mailService.sendNewApplicationNotification(application, createdComment);

        // update remaining vacation days (if there is already a holidays account for next year)
        accountInteractionService.updateRemainingVacationDays(application.getStartDate().getYear(), person);

        CalendarSettings calendarSettings = settingsService.getSettings().getCalendarSettings();
        AbsenceTimeConfiguration timeConfiguration = new AbsenceTimeConfiguration(calendarSettings);

        Optional<String> eventId = calendarSyncService.addAbsence(new Absence(application.getPerson(),
                    application.getPeriod(), EventType.WAITING_APPLICATION, timeConfiguration));

        if (eventId.isPresent()) {
            absenceMappingService.create(application.getId(), AbsenceType.VACATION, eventId.get());
        }

        return application;
    }


    @Override
    public Application allow(Application application, Person boss, Optional<String> comment) {

        application.setStatus(ApplicationStatus.ALLOWED);
        application.setBoss(boss);
        application.setEditedDate(DateMidnight.now());

        signService.signApplicationByBoss(application, boss);

        applicationService.save(application);

        LOG.info("Allowed application for leave: " + application.toString());

        ApplicationComment createdComment = commentService.create(application, ApplicationAction.ALLOWED, comment,
                boss);

        mailService.sendAllowedNotification(application, createdComment);

        if (application.getHolidayReplacement() != null) {
            mailService.notifyHolidayReplacement(application);
        }

        Optional<AbsenceMapping> absenceMapping = absenceMappingService.getAbsenceByIdAndType(application.getId(),
                AbsenceType.VACATION);

        if (absenceMapping.isPresent()) {
            CalendarSettings calendarSettings = settingsService.getSettings().getCalendarSettings();
            AbsenceTimeConfiguration timeConfiguration = new AbsenceTimeConfiguration(calendarSettings);
            calendarSyncService.update(new Absence(application.getPerson(), application.getPeriod(),
                    EventType.ALLOWED_APPLICATION, timeConfiguration), absenceMapping.get().getEventId());
        }

        return application;
    }


    @Override
    public Application reject(Application application, Person boss, Optional<String> comment) {

        application.setStatus(ApplicationStatus.REJECTED);
        application.setBoss(boss);
        application.setEditedDate(DateMidnight.now());

        signService.signApplicationByBoss(application, boss);

        applicationService.save(application);

        LOG.info("Rejected application for leave: " + application.toString());

        ApplicationComment createdComment = commentService.create(application, ApplicationAction.REJECTED, comment,
                boss);

        mailService.sendRejectedNotification(application, createdComment);

        Optional<AbsenceMapping> absenceMapping = absenceMappingService.getAbsenceByIdAndType(application.getId(),
                AbsenceType.VACATION);

        if (absenceMapping.isPresent()) {
            calendarSyncService.deleteAbsence(absenceMapping.get().getEventId());
            absenceMappingService.delete(absenceMapping.get());
        }

        return application;
    }


    @Override
    public Application cancel(Application application, Person canceller, Optional<String> comment) {

        ApplicationAction commentStatus;
        Person person = application.getPerson();

        boolean cancellingAllowedApplication = application.hasStatus(ApplicationStatus.ALLOWED);
        boolean isUserCancellingOwnApplication = canceller.equals(person);

        application.setCanceller(canceller);
        application.setCancelDate(DateMidnight.now());

        /*
         * Updating applicationAction and applicationStatus according
         * to parameters
         */
        if (cancellingAllowedApplication) {
            /*
             * Security is handled up the call stack. If the
             * canceller is not the owner of the application
             * it is OK to cancel directly.
             */
            if(!isUserCancellingOwnApplication) {
                application.setStatus(ApplicationStatus.CANCELLED);
                commentStatus = ApplicationAction.CANCELLED;
            }

            /*
             * Users cannot cancel already allowed applications
             * directly. Their commentStatus will be CANCEL_REQUESTED
             * and the application.status will remain ALLOWED until
             * the office or a boss approves the request.
             */
            else {
                commentStatus = ApplicationAction.CANCEL_REQUESTED;
            }
        }
        else {
            application.setStatus(ApplicationStatus.REVOKED);
            commentStatus = ApplicationAction.REVOKED;
        }

        applicationService.save(application);

        LOG.info("Cancelled application for leave: " + application);

        ApplicationComment createdComment = commentService.create(application, commentStatus, comment, canceller);

        //handling mails only after created comment and application have been saved
        if (cancellingAllowedApplication) {
            if (!isUserCancellingOwnApplication) {
                mailService.sendCancelledNotification(application, false, createdComment);
            }
            else {
                mailService.sendCancellationRequest(application, createdComment);
            }
        }
        else {
            if(!isUserCancellingOwnApplication) {
                mailService.sendCancelledNotification(application, true, createdComment);
            }
        }

        accountInteractionService.updateRemainingVacationDays(application.getStartDate().getYear(), person);

        Optional<AbsenceMapping> absenceMapping = absenceMappingService.getAbsenceByIdAndType(application.getId(),
                AbsenceType.VACATION);

        if (absenceMapping.isPresent()) {
            calendarSyncService.deleteAbsence(absenceMapping.get().getEventId());
            absenceMappingService.delete(absenceMapping.get());
        }

        return application;
    }


    @Override
    public Application createFromConvertedSickNote(Application application, Person creator) {

        // create an application for leave that is allowed directly
        application.setApplier(creator);
        application.setStatus(ApplicationStatus.ALLOWED);

        signService.signApplicationByBoss(application, creator);
        applicationService.save(application);

        commentService.create(application, ApplicationAction.CONVERTED, Optional.<String>empty(), creator);
        mailService.sendSickNoteConvertedToVacationNotification(application);

        return application;
    }


    @Override
    public Application remind(Application application) throws RemindAlreadySentException,
        ImpatientAboutApplicationForLeaveProcessException {

        DateMidnight remindDate = application.getRemindDate();

        if (remindDate == null) {
            DateMidnight minDateForNotification = application.getApplicationDate()
                .plusDays(MIN_DAYS_LEFT_BEFORE_REMINDING_IS_POSSIBLE);

            if (minDateForNotification.isAfterNow()) {
                throw new ImpatientAboutApplicationForLeaveProcessException("It's too early to remind the bosses!");
            }
        }

        if (remindDate != null && remindDate.isEqual(DateMidnight.now())) {
            throw new RemindAlreadySentException("Reminding is possible maximum one time per day!");
        }

        mailService.sendRemindBossNotification(application);

        application.setRemindDate(DateMidnight.now());
        applicationService.save(application);

        return application;
    }


    @Override
    public Application refer(Application application, Person recipient, Person sender) {

        mailService.sendReferApplicationNotification(application, recipient, sender);

        return application;
    }
}
