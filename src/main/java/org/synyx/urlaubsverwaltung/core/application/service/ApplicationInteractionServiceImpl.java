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
import org.synyx.urlaubsverwaltung.core.department.Department;
import org.synyx.urlaubsverwaltung.core.department.DepartmentService;
import org.synyx.urlaubsverwaltung.core.mail.MailService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.Role;
import org.synyx.urlaubsverwaltung.core.settings.CalendarSettings;
import org.synyx.urlaubsverwaltung.core.settings.SettingsService;
import org.synyx.urlaubsverwaltung.core.sync.CalendarSyncService;
import org.synyx.urlaubsverwaltung.core.sync.absence.Absence;
import org.synyx.urlaubsverwaltung.core.sync.absence.AbsenceMapping;
import org.synyx.urlaubsverwaltung.core.sync.absence.AbsenceMappingService;
import org.synyx.urlaubsverwaltung.core.sync.absence.AbsenceTimeConfiguration;
import org.synyx.urlaubsverwaltung.core.sync.absence.AbsenceType;
import org.synyx.urlaubsverwaltung.core.sync.absence.EventType;

import java.util.List;
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
    private final ApplicationCommentService commentService;
    private final MailService mailService;
    private final CalendarSyncService calendarSyncService;
    private final AbsenceMappingService absenceMappingService;
    private final SettingsService settingsService;
    private final DepartmentService departmentService;

    @Autowired
    public ApplicationInteractionServiceImpl(ApplicationService applicationService,
                                             ApplicationCommentService commentService,
                                             AccountInteractionService accountInteractionService,
                                             MailService mailService,
                                             CalendarSyncService calendarSyncService,
                                             AbsenceMappingService absenceMappingService,
                                             SettingsService settingsService,
                                             DepartmentService departmentService) {

        this.applicationService = applicationService;
        this.commentService = commentService;
        this.accountInteractionService = accountInteractionService;
        this.mailService = mailService;
        this.calendarSyncService = calendarSyncService;
        this.absenceMappingService = absenceMappingService;
        this.settingsService = settingsService;
        this.departmentService = departmentService;
    }

    @Override
    public Application apply(Application application, Person applier, Optional<String> comment) {

        Person person = application.getPerson();

        List<Department> departments = departmentService.getAssignedDepartmentsOfMember(person);

        // check if a two stage approval is set for the Department
        departments.stream().filter(Department::isTwoStageApproval).forEach(department ->
                application.setTwoStageApproval(true));

        application.setStatus(ApplicationStatus.WAITING);
        application.setApplier(applier);
        application.setApplicationDate(DateMidnight.now());

        applicationService.save(application);

        LOG.info("Created application for leave: " + application.toString());

        // COMMENT
        ApplicationComment createdComment = commentService.create(application, ApplicationAction.APPLIED, comment,
                applier);

        // EMAILS
        if (person.equals(applier)) {
            // person himself applies for leave
            // person gets a confirmation email with the data of the application for leave
            mailService.sendConfirmation(application, createdComment);
        } else {
            // someone else (normally the office) applies for leave on behalf of the person
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
    public Application allow(Application application, Person privilegedUser, Optional<String> comment) {

        // Boss is a very might dude
        if (privilegedUser.hasRole(Role.BOSS)) {
            return allowFinally(application, privilegedUser, comment);
        }

        // Second stage authority has almost the same power (except on own applications)
        boolean isSecondStageAuthority = privilegedUser.hasRole(Role.SECOND_STAGE_AUTHORITY)
                && departmentService.isSecondStageAuthorityOfPerson(privilegedUser, application.getPerson());

        boolean isOwnApplication = application.getPerson().equals(privilegedUser);

        if (isSecondStageAuthority && !isOwnApplication) {
            return allowFinally(application, privilegedUser, comment);
        }

        // Department head can be mighty only in some cases
        boolean isDepartmentHead = privilegedUser.hasRole(Role.DEPARTMENT_HEAD)
            && departmentService.isDepartmentHeadOfPerson(privilegedUser, application.getPerson());

        // DEPARTMENT_HEAD can _not_ allow SECOND_STAGE_AUTHORITY
        boolean isSecondStageAuthorityApplication =
                application.getPerson().hasRole(Role.SECOND_STAGE_AUTHORITY);

        if (isDepartmentHead && !isOwnApplication && !isSecondStageAuthorityApplication) {
            if (application.isTwoStageApproval()) {
                return allowTemporary(application, privilegedUser, comment);
            }

            return allowFinally(application, privilegedUser, comment);
        }

        throw new IllegalStateException("Applications for leave can be allowed only by a privileged user!");
    }


    private Application allowTemporary(Application applicationForLeave, Person privilegedUser,
        Optional<String> comment) {

        boolean alreadyAllowed = applicationForLeave.hasStatus(ApplicationStatus.TEMPORARY_ALLOWED)
            || applicationForLeave.hasStatus(ApplicationStatus.ALLOWED);

        if (alreadyAllowed) {
            // Early return - do nothing if expected status already set

            LOG.info("Application for leave is already in an allowed status, do nothing: "
                + applicationForLeave.toString());

            return applicationForLeave;
        }

        applicationForLeave.setStatus(ApplicationStatus.TEMPORARY_ALLOWED);
        applicationForLeave.setBoss(privilegedUser);
        applicationForLeave.setEditedDate(DateMidnight.now());

        applicationService.save(applicationForLeave);

        LOG.info("Temporary allowed application for leave: " + applicationForLeave.toString());

        ApplicationComment createdComment = commentService.create(applicationForLeave,
                ApplicationAction.TEMPORARY_ALLOWED, comment, privilegedUser);

        mailService.sendTemporaryAllowedNotification(applicationForLeave, createdComment);

        return applicationForLeave;
    }


    private Application allowFinally(Application applicationForLeave, Person privilegedUser, Optional<String> comment) {

        if (applicationForLeave.hasStatus(ApplicationStatus.ALLOWED)) {
            // Early return - do nothing if expected status already set

            LOG.info("Application for leave is already in an allowed status, do nothing: "
                + applicationForLeave.toString());

            return applicationForLeave;
        }

        applicationForLeave.setStatus(ApplicationStatus.ALLOWED);
        applicationForLeave.setBoss(privilegedUser);
        applicationForLeave.setEditedDate(DateMidnight.now());

        applicationService.save(applicationForLeave);

        LOG.info("Allowed application for leave: " + applicationForLeave.toString());

        ApplicationComment createdComment = commentService.create(applicationForLeave, ApplicationAction.ALLOWED,
                comment, privilegedUser);

        mailService.sendAllowedNotification(applicationForLeave, createdComment);

        if (applicationForLeave.getHolidayReplacement() != null) {
            mailService.notifyHolidayReplacement(applicationForLeave);
        }

        Optional<AbsenceMapping> absenceMapping = absenceMappingService.getAbsenceByIdAndType(
                applicationForLeave.getId(), AbsenceType.VACATION);

        if (absenceMapping.isPresent()) {
            CalendarSettings calendarSettings = settingsService.getSettings().getCalendarSettings();
            AbsenceTimeConfiguration timeConfiguration = new AbsenceTimeConfiguration(calendarSettings);
            calendarSyncService.update(new Absence(applicationForLeave.getPerson(), applicationForLeave.getPeriod(),
                    EventType.ALLOWED_APPLICATION, timeConfiguration), absenceMapping.get().getEventId());
        }

        return applicationForLeave;
    }


    @Override
    public Application reject(Application application, Person privilegedUser, Optional<String> comment) {

        application.setStatus(ApplicationStatus.REJECTED);
        application.setBoss(privilegedUser);
        application.setEditedDate(DateMidnight.now());

        applicationService.save(application);

        LOG.info("Rejected application for leave: " + application.toString());

        ApplicationComment createdComment = commentService.create(application, ApplicationAction.REJECTED, comment,
                privilegedUser);

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

        Person person = application.getPerson();

        application.setCanceller(canceller);
        application.setCancelDate(DateMidnight.now());

        if (application.hasStatus(ApplicationStatus.ALLOWED) ||
                application.hasStatus(ApplicationStatus.TEMPORARY_ALLOWED)) {
            cancelApplication(application, canceller, comment);
        } else {
            revokeApplication(application, canceller, comment);
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


    private Application revokeApplication(Application application, Person canceller, Optional<String> comment) {

        application.setStatus(ApplicationStatus.REVOKED);

        applicationService.save(application);

        LOG.info("Revoked application for leave: " + application);

        ApplicationComment createdComment = commentService.create(application, ApplicationAction.REVOKED, comment,
                canceller);

        if (canceller.hasRole(Role.OFFICE) && !canceller.equals(application.getPerson())) {
            mailService.sendCancelledByOfficeNotification(application, createdComment);
        }

        return application;
    }


    private Application cancelApplication(Application application, Person canceller, Optional<String> comment) {

        /*
         * Only Office can cancel allowed applications for leave directly,
         * users have to request cancellation
         */
        if (canceller.hasRole(Role.OFFICE)) {
            application.setStatus(ApplicationStatus.CANCELLED);

            applicationService.save(application);

            LOG.info("Cancelled application for leave: " + application);

            ApplicationComment createdComment = commentService.create(application, ApplicationAction.CANCELLED, comment,
                    canceller);

            if (!canceller.equals(application.getPerson())) {
                mailService.sendCancelledByOfficeNotification(application, createdComment);
            }
        } else {
            /*
             * Users cannot cancel already allowed applications
             * directly. Their commentStatus will be CANCEL_REQUESTED
             * and the application.status will remain ALLOWED until
             * the office or a boss approves the request.
             */

            applicationService.save(application);

            LOG.info("Request cancellation of application for leave: " + application);

            ApplicationComment createdComment = commentService.create(application, ApplicationAction.CANCEL_REQUESTED,
                    comment, canceller);

            mailService.sendCancellationRequest(application, createdComment);
        }

        return application;
    }


    @Override
    public Application createFromConvertedSickNote(Application application, Person creator) {

        // create an application for leave that is allowed directly
        application.setApplier(creator);
        application.setStatus(ApplicationStatus.ALLOWED);

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
