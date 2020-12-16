package org.synyx.urlaubsverwaltung.application.service;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.absence.Absence;
import org.synyx.urlaubsverwaltung.absence.AbsenceMapping;
import org.synyx.urlaubsverwaltung.absence.AbsenceMappingService;
import org.synyx.urlaubsverwaltung.absence.AbsenceTimeConfiguration;
import org.synyx.urlaubsverwaltung.absence.TimeSettings;
import org.synyx.urlaubsverwaltung.account.AccountInteractionService;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationCommentAction;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationComment;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.calendarintegration.CalendarSyncService;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.absence.AbsenceType.VACATION;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationCommentAction.APPLIED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationCommentAction.CANCELLED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationCommentAction.CANCEL_REQUESTED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationCommentAction.CANCEL_REQUESTED_DECLINED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationCommentAction.EDITED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationCommentAction.REVOKED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.ALLOWED_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.REJECTED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;


@Service
@Transactional
public class ApplicationInteractionServiceImpl implements ApplicationInteractionService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private static final int MIN_DAYS_LEFT_BEFORE_REMINDING_IS_POSSIBLE = 2;

    private final ApplicationService applicationService;
    private final AccountInteractionService accountInteractionService;
    private final ApplicationCommentService commentService;
    private final ApplicationMailService applicationMailService;
    private final CalendarSyncService calendarSyncService;
    private final AbsenceMappingService absenceMappingService;
    private final TimeSettings timeSettings;
    private final DepartmentService departmentService;
    private final Clock clock;

    @Autowired
    public ApplicationInteractionServiceImpl(ApplicationService applicationService,
                                             ApplicationCommentService commentService,
                                             AccountInteractionService accountInteractionService,
                                             ApplicationMailService applicationMailService,
                                             CalendarSyncService calendarSyncService,
                                             AbsenceMappingService absenceMappingService,
                                             SettingsService settingsService,
                                             DepartmentService departmentService, Clock clock) {

        this.applicationService = applicationService;
        this.commentService = commentService;
        this.accountInteractionService = accountInteractionService;
        this.applicationMailService = applicationMailService;
        this.calendarSyncService = calendarSyncService;
        this.absenceMappingService = absenceMappingService;
        this.timeSettings = settingsService.getSettings().getTimeSettings();
        this.departmentService = departmentService;
        this.clock = clock;
    }

    @Override
    public Application apply(Application application, Person applier, Optional<String> comment) {

        final Person person = application.getPerson();
        final List<Department> departments = departmentService.getAssignedDepartmentsOfMember(person);
        // check if a two stage approval is set for the Department
        departments.stream()
            .filter(Department::isTwoStageApproval)
            .forEach(department -> application.setTwoStageApproval(true));

        application.setStatus(WAITING);
        application.setApplier(applier);
        application.setApplicationDate(LocalDate.now(clock));

        final Application savedApplication = applicationService.save(application);

        LOG.info("Created application for leave: {}", savedApplication);

        // COMMENT
        final         ApplicationComment createdComment = commentService.create(savedApplication, APPLIED, comment, applier);

        // EMAILS
        if (person.equals(applier)) {
            // person himself applies for leave
            // person gets a confirmation email with the data of the application for leave
            applicationMailService.sendConfirmation(savedApplication, createdComment);
        } else if (applier.hasRole(OFFICE)) {
            // if a person with the office role applies for leave on behalf of the person
            // person gets an email that someone else has applied for leave on behalf
            applicationMailService.sendAppliedForLeaveByOfficeNotification(savedApplication, createdComment);
        }

        // relevant management person gets email that a new application for leave has been created
        applicationMailService.sendNewApplicationNotification(savedApplication, createdComment);

        // update remaining vacation days (if there is already a holidays account for next year)
        accountInteractionService.updateRemainingVacationDays(savedApplication.getStartDate().getYear(), person);

        final Optional<String> eventId = calendarSyncService.addAbsence(new Absence(savedApplication.getPerson(),
            savedApplication.getPeriod(), new AbsenceTimeConfiguration(timeSettings)));
        eventId.ifPresent(s -> absenceMappingService.create(savedApplication.getId(), VACATION, s));

        return savedApplication;
    }

    @Override
    public Application allow(Application application, Person privilegedUser, Optional<String> comment) {

        // Boss is a very might dude
        if (privilegedUser.hasRole(BOSS)) {
            return allowFinally(application, privilegedUser, comment);
        }

        // Second stage authority has almost the same power (except on own applications)
        final boolean isSecondStageAuthority = privilegedUser.hasRole(SECOND_STAGE_AUTHORITY)
            && departmentService.isSecondStageAuthorityOfPerson(privilegedUser, application.getPerson());

        final boolean isOwnApplication = application.getPerson().equals(privilegedUser);

        if (isSecondStageAuthority && !isOwnApplication) {
            return allowFinally(application, privilegedUser, comment);
        }

        // Department head can be mighty only in some cases
        final boolean isDepartmentHead = privilegedUser.hasRole(DEPARTMENT_HEAD)
            && departmentService.isDepartmentHeadOfPerson(privilegedUser, application.getPerson());

        // DEPARTMENT_HEAD can _not_ allow SECOND_STAGE_AUTHORITY
        final boolean isSecondStageAuthorityApplication = application.getPerson().hasRole(SECOND_STAGE_AUTHORITY);

        if (isDepartmentHead && !isOwnApplication && !isSecondStageAuthorityApplication) {
            if (application.isTwoStageApproval()) {
                return allowTemporary(application, privilegedUser, comment);
            }

            return allowFinally(application, privilegedUser, comment);
        }

        throw new IllegalStateException("Applications for leave can be allowed only by a privileged user!");
    }


    private Application allowTemporary(Application applicationForLeave, Person privilegedUser, Optional<String> comment) {

        final boolean alreadyAllowed = applicationForLeave.hasStatus(TEMPORARY_ALLOWED) || applicationForLeave.hasStatus(ALLOWED);
        if (alreadyAllowed) {
            // Early return - do nothing if expected status already set
            LOG.info("Application for leave is already in an allowed status, do nothing: {}", applicationForLeave);

            return applicationForLeave;
        }

        applicationForLeave.setStatus(TEMPORARY_ALLOWED);
        applicationForLeave.setBoss(privilegedUser);
        applicationForLeave.setEditedDate(LocalDate.now(clock));
        final Application savedApplication = applicationService.save(applicationForLeave);

        LOG.info("Temporary allowed application for leave: {}", savedApplication);

        final ApplicationComment createdComment = commentService.create(savedApplication,
            ApplicationCommentAction.TEMPORARY_ALLOWED, comment, privilegedUser);

        applicationMailService.sendTemporaryAllowedNotification(savedApplication, createdComment);

        return savedApplication;
    }


    private Application allowFinally(Application applicationForLeave, Person privilegedUser, Optional<String> comment) {

        if (applicationForLeave.hasStatus(ALLOWED)) {
            // Early return - do nothing if expected status already set
            LOG.info("Application for leave is already in an allowed status, do nothing: {}", applicationForLeave);
            return applicationForLeave;
        }

        applicationForLeave.setStatus(ALLOWED);
        applicationForLeave.setBoss(privilegedUser);
        applicationForLeave.setEditedDate(LocalDate.now(clock));
        final Application savedApplication = applicationService.save(applicationForLeave);

        LOG.info("Allowed application for leave: {}", savedApplication);

        final ApplicationComment createdComment = commentService.create(savedApplication, ApplicationCommentAction.ALLOWED,
            comment, privilegedUser);

        applicationMailService.sendAllowedNotification(savedApplication, createdComment);

        if (savedApplication.getHolidayReplacement() != null) {
            applicationMailService.notifyHolidayReplacementAllow(savedApplication);
        }

        return savedApplication;
    }


    @Override
    public Application reject(Application application, Person privilegedUser, Optional<String> comment) {

        application.setStatus(REJECTED);
        application.setBoss(privilegedUser);
        application.setEditedDate(LocalDate.now(clock));
        final Application savedApplication = applicationService.save(application);

        LOG.info("Rejected application for leave: {}", savedApplication);

        final ApplicationComment createdComment = commentService.create(savedApplication, ApplicationCommentAction.REJECTED, comment,
            privilegedUser);

        applicationMailService.sendRejectedNotification(savedApplication, createdComment);

        if (savedApplication.getHolidayReplacement() != null) {
            applicationMailService.notifyHolidayReplacementAboutCancellation(savedApplication);
        }

        final Optional<AbsenceMapping> absenceMapping = absenceMappingService.getAbsenceByIdAndType(savedApplication.getId(), VACATION);
        if (absenceMapping.isPresent()) {
            calendarSyncService.deleteAbsence(absenceMapping.get().getEventId());
            absenceMappingService.delete(absenceMapping.get());
        }

        return application;
    }


    @Override
    public Application cancel(Application application, Person canceller, Optional<String> comment) {

        final Person person = application.getPerson();

        application.setCanceller(canceller);
        application.setCancelDate(LocalDate.now(clock));

        if (application.hasStatus(ALLOWED) || application.hasStatus(ALLOWED_CANCELLATION_REQUESTED) || application.hasStatus(TEMPORARY_ALLOWED)) {
            cancelApplication(application, canceller, comment);
        } else if (application.hasStatus(WAITING)) {
            revokeApplication(application, canceller, comment);
        }

        accountInteractionService.updateRemainingVacationDays(application.getStartDate().getYear(), person);

        final Optional<AbsenceMapping> absenceMapping = absenceMappingService.getAbsenceByIdAndType(application.getId(), VACATION);
        if (absenceMapping.isPresent()) {
            calendarSyncService.deleteAbsence(absenceMapping.get().getEventId());
            absenceMappingService.delete(absenceMapping.get());
        }

        return application;
    }


    private void revokeApplication(Application application, Person canceller, Optional<String> comment) {

        application.setStatus(ApplicationStatus.REVOKED);
        final Application savedApplication = applicationService.save(application);
        LOG.info("Revoked application for leave: {}", savedApplication);

        final ApplicationComment savedComment = commentService.create(savedApplication, REVOKED, comment, canceller);
        applicationMailService.sendRevokedNotifications(application, savedComment);
    }


    private void cancelApplication(Application application, Person canceller, Optional<String> comment) {

        if (canceller.hasRole(OFFICE)) {
            /*
             * Only Office can cancel allowed applications for leave directly,
             * users have to request cancellation
             */

            application.setStatus(ApplicationStatus.CANCELLED);
            final Application savedApplication = applicationService.save(application);

            LOG.info("Cancelled application for leave: {}", savedApplication);

            final ApplicationComment savedComment = commentService.create(savedApplication, CANCELLED, comment, canceller);
            applicationMailService.sendCancelledByOfficeNotification(savedApplication, savedComment);
        } else {
            /*
             * Users cannot cancel already allowed applications directly.
             * Their comment status will be CANCEL_REQUESTED
             * and the application status will be ALLOWED_CANCELLATION_REQUESTED until
             * the office or a boss approves the request.
             */
            application.setStatus(ALLOWED_CANCELLATION_REQUESTED);
            final Application savedApplication = applicationService.save(application);

            LOG.info("Request cancellation of application for leave: {}", savedApplication);

            final ApplicationComment createdComment = commentService.create(savedApplication, CANCEL_REQUESTED, comment, canceller);
            applicationMailService.sendCancellationRequest(savedApplication, createdComment);
        }
    }

    @Override
    public Application declineCancellationRequest(Application applicationForLeave, Person person, Optional<String> comment){

        if (applicationForLeave.getStatus().compareTo(ALLOWED_CANCELLATION_REQUESTED) != 0) {
            throw new DeclineCancellationRequestedApplicationForLeaveNotAllowedException(format("Cannot cancel the cancellation " +
                "request of the application for leave with id %d because the status is %s and not " +
                "allowed_cancellation_requested.", applicationForLeave.getId(), applicationForLeave.getStatus()));
        }

        applicationForLeave.setStatus(ALLOWED);
        applicationForLeave.setEditedDate(LocalDate.now(clock));
        final Application savedApplication = applicationService.save(applicationForLeave);

        final ApplicationComment applicationComment = commentService.create(savedApplication, CANCEL_REQUESTED_DECLINED, comment, person);

        applicationMailService.sendDeclinedCancellationRequestApplicationNotification(savedApplication, applicationComment);

        return savedApplication;
    }

    @Override
    public Application createFromConvertedSickNote(Application application, Person creator) {

        // create an application for leave that is allowed directly
        application.setApplier(creator);
        application.setStatus(ALLOWED);

        final Application savedApplication = applicationService.save(application);

        commentService.create(savedApplication, ApplicationCommentAction.CONVERTED, Optional.empty(), creator);
        applicationMailService.sendSickNoteConvertedToVacationNotification(savedApplication);

        return savedApplication;
    }

    @Override
    public Application remind(Application application) throws RemindAlreadySentException, ImpatientAboutApplicationForLeaveProcessException {

        final LocalDate remindDate = application.getRemindDate();
        if (remindDate == null) {
            final LocalDate minDateForNotification = application.getApplicationDate().plusDays(MIN_DAYS_LEFT_BEFORE_REMINDING_IS_POSSIBLE);

            if (minDateForNotification.isAfter(LocalDate.now(clock))) {
                throw new ImpatientAboutApplicationForLeaveProcessException("It's too early to remind the bosses!");
            }
        }

        if (remindDate != null && remindDate.isEqual(LocalDate.now(clock))) {
            throw new RemindAlreadySentException("Reminding is possible maximum one time per day!");
        }

        applicationMailService.sendRemindBossNotification(application);

        application.setRemindDate(LocalDate.now(clock));
        return applicationService.save(application);
    }

    @Override
    public Application refer(Application application, Person recipient, Person sender) {

        commentService.create(application, ApplicationCommentAction.REFERRED, Optional.of(recipient.getNiceName()), sender);
        applicationMailService.sendReferApplicationNotification(application, recipient, sender);

        return application;
    }

    public Optional<Application> get(Integer applicationId) {
        return applicationService.getApplicationById(applicationId);
    }

    @Override
    public Application edit(Application applicationForLeave, Person person, Optional<String> comment) {

        if (applicationForLeave.getStatus().compareTo(WAITING) != 0) {
            throw new EditApplicationForLeaveNotAllowedException(format("Cannot edit application for leave " +
                "with id %d because the status is %s and not waiting.", applicationForLeave.getId(), applicationForLeave.getStatus()));
        }

        applicationForLeave.setStatus(WAITING);
        applicationForLeave.setEditedDate(LocalDate.now(clock));
        final Application savedApplication = applicationService.save(applicationForLeave);

        commentService.create(savedApplication, EDITED, comment, person);

        applicationMailService.sendEditedApplicationNotification(savedApplication, person);

        return savedApplication;
    }
}
