package org.synyx.urlaubsverwaltung.application.service;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.absence.Absence;
import org.synyx.urlaubsverwaltung.absence.AbsenceMapping;
import org.synyx.urlaubsverwaltung.absence.AbsenceMappingService;
import org.synyx.urlaubsverwaltung.absence.AbsenceTimeConfiguration;
import org.synyx.urlaubsverwaltung.absence.AbsenceType;
import org.synyx.urlaubsverwaltung.account.service.AccountInteractionService;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationAction;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationComment;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.calendarintegration.CalendarSyncService;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.CalendarSettings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.lang.invoke.MethodHandles.lookup;
import static java.time.ZoneOffset.UTC;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationAction.CANCEL_REQUESTED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationAction.REVOKED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.TEMPORARY_ALLOWED;
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
    private final SettingsService settingsService;
    private final DepartmentService departmentService;

    @Autowired
    public ApplicationInteractionServiceImpl(ApplicationService applicationService,
                                             ApplicationCommentService commentService,
                                             AccountInteractionService accountInteractionService,
                                             ApplicationMailService applicationMailService, CalendarSyncService calendarSyncService,
                                             AbsenceMappingService absenceMappingService,
                                             SettingsService settingsService,
                                             DepartmentService departmentService) {

        this.applicationService = applicationService;
        this.commentService = commentService;
        this.accountInteractionService = accountInteractionService;
        this.applicationMailService = applicationMailService;
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
        departments.stream().filter(Department::isTwoStageApproval).forEach(department -> application.setTwoStageApproval(true));

        application.setStatus(ApplicationStatus.WAITING);
        application.setApplier(applier);
        application.setApplicationDate(LocalDate.now(UTC));

        final Application savedApplication = applicationService.save(application);

        LOG.info("Created application for leave: {}", savedApplication);

        // COMMENT
        ApplicationComment createdComment = commentService.create(savedApplication, ApplicationAction.APPLIED, comment,
            applier);

        // EMAILS
        if (person.equals(applier)) {
            // person himself applies for leave
            // person gets a confirmation email with the data of the application for leave
            applicationMailService.sendConfirmation(savedApplication, createdComment);
        } else {
            // someone else (normally the office) applies for leave on behalf of the person
            // person gets an email that someone else has applied for leave on behalf
            applicationMailService.sendAppliedForLeaveByOfficeNotification(savedApplication, createdComment);
        }

        // bosses gets email that a new application for leave has been created
        applicationMailService.sendNewApplicationNotification(savedApplication, createdComment);

        // update remaining vacation days (if there is already a holidays account for next year)
        accountInteractionService.updateRemainingVacationDays(savedApplication.getStartDate().getYear(), person);

        CalendarSettings calendarSettings = settingsService.getSettings().getCalendarSettings();
        AbsenceTimeConfiguration timeConfiguration = new AbsenceTimeConfiguration(calendarSettings);

        Optional<String> eventId = calendarSyncService.addAbsence(new Absence(savedApplication.getPerson(),
            savedApplication.getPeriod(), timeConfiguration));

        eventId.ifPresent(s -> absenceMappingService.create(savedApplication.getId(), AbsenceType.VACATION, s));

        return savedApplication;
    }

    @Override
    public Application allow(Application application, Person privilegedUser, Optional<String> comment) {

        // Boss is a very might dude
        if (privilegedUser.hasRole(BOSS)) {
            return allowFinally(application, privilegedUser, comment);
        }

        // Second stage authority has almost the same power (except on own applications)
        boolean isSecondStageAuthority = privilegedUser.hasRole(SECOND_STAGE_AUTHORITY)
            && departmentService.isSecondStageAuthorityOfPerson(privilegedUser, application.getPerson());

        boolean isOwnApplication = application.getPerson().equals(privilegedUser);

        if (isSecondStageAuthority && !isOwnApplication) {
            return allowFinally(application, privilegedUser, comment);
        }

        // Department head can be mighty only in some cases
        boolean isDepartmentHead = privilegedUser.hasRole(DEPARTMENT_HEAD)
            && departmentService.isDepartmentHeadOfPerson(privilegedUser, application.getPerson());

        // DEPARTMENT_HEAD can _not_ allow SECOND_STAGE_AUTHORITY
        boolean isSecondStageAuthorityApplication = application.getPerson().hasRole(SECOND_STAGE_AUTHORITY);

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

        boolean alreadyAllowed = applicationForLeave.hasStatus(TEMPORARY_ALLOWED)
            || applicationForLeave.hasStatus(ALLOWED);

        if (alreadyAllowed) {
            // Early return - do nothing if expected status already set
            LOG.info("Application for leave is already in an allowed status, do nothing: {}", applicationForLeave);

            return applicationForLeave;
        }

        applicationForLeave.setStatus(TEMPORARY_ALLOWED);
        applicationForLeave.setBoss(privilegedUser);
        applicationForLeave.setEditedDate(LocalDate.now(UTC));
        final Application savedApplication = applicationService.save(applicationForLeave);

        LOG.info("Temporary allowed application for leave: {}", savedApplication);

        final ApplicationComment createdComment = commentService.create(savedApplication,
            ApplicationAction.TEMPORARY_ALLOWED, comment, privilegedUser);

        applicationMailService.sendTemporaryAllowedNotification(savedApplication, createdComment);

        return savedApplication;
    }


    private Application allowFinally(Application applicationForLeave, Person privilegedUser, Optional<String> comment) {

        if (applicationForLeave.hasStatus(ALLOWED)) {
            // Early return - do nothing if expected status already set

            LOG.info("Application for leave is already in an allowed status, do nothing: {}",
                applicationForLeave);

            return applicationForLeave;
        }

        applicationForLeave.setStatus(ALLOWED);
        applicationForLeave.setBoss(privilegedUser);
        applicationForLeave.setEditedDate(LocalDate.now(UTC));
        final Application savedApplication = applicationService.save(applicationForLeave);

        LOG.info("Allowed application for leave: {}", savedApplication);

        final ApplicationComment createdComment = commentService.create(savedApplication, ApplicationAction.ALLOWED,
            comment, privilegedUser);

        applicationMailService.sendAllowedNotification(savedApplication, createdComment);

        if (savedApplication.getHolidayReplacement() != null) {
            applicationMailService.notifyHolidayReplacement(savedApplication);
        }

        return savedApplication;
    }


    @Override
    public Application reject(Application application, Person privilegedUser, Optional<String> comment) {

        application.setStatus(ApplicationStatus.REJECTED);
        application.setBoss(privilegedUser);
        application.setEditedDate(LocalDate.now(UTC));
        final Application savedApplication = applicationService.save(application);

        LOG.info("Rejected application for leave: {}", savedApplication);

        final ApplicationComment createdComment = commentService.create(savedApplication, ApplicationAction.REJECTED, comment,
            privilegedUser);

        applicationMailService.sendRejectedNotification(savedApplication, createdComment);

        Optional<AbsenceMapping> absenceMapping = absenceMappingService.getAbsenceByIdAndType(savedApplication.getId(),
            AbsenceType.VACATION);

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
        application.setCancelDate(LocalDate.now(UTC));

        if (application.hasStatus(ALLOWED) || application.hasStatus(TEMPORARY_ALLOWED)) {
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


    private void revokeApplication(Application application, Person canceller, Optional<String> comment) {

        application.setStatus(ApplicationStatus.REVOKED);
        final Application savedApplication = applicationService.save(application);

        LOG.info("Revoked application for leave: {}", savedApplication);

        final ApplicationComment savedComment = commentService.create(savedApplication, REVOKED, comment, canceller);

        if (canceller.hasRole(OFFICE)) {
            applicationMailService.sendCancelledByOfficeNotification(application, savedComment);
        }
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

            final ApplicationComment savedComment = commentService.create(savedApplication, ApplicationAction.CANCELLED, comment,
                canceller);

            if (!canceller.equals(savedApplication.getPerson())) {
                applicationMailService.sendCancelledByOfficeNotification(savedApplication, savedComment);
            }
        } else {
            /*
             * Users cannot cancel already allowed applications directly.
             * Their comment status will be CANCEL_REQUESTED
             * and the application status will remain ALLOWED until
             * the office or a boss approves the request.
             */

            final Application savedApplication = applicationService.save(application);

            LOG.info("Request cancellation of application for leave: {}", savedApplication);

            final ApplicationComment createdComment = commentService.create(savedApplication, CANCEL_REQUESTED, comment, canceller);
            applicationMailService.sendCancellationRequest(savedApplication, createdComment);
        }
    }


    @Override
    public Application createFromConvertedSickNote(Application application, Person creator) {

        // create an application for leave that is allowed directly
        application.setApplier(creator);
        application.setStatus(ALLOWED);

        final Application savedApplication = applicationService.save(application);

        commentService.create(savedApplication, ApplicationAction.CONVERTED, Optional.empty(), creator);
        applicationMailService.sendSickNoteConvertedToVacationNotification(savedApplication);

        return savedApplication;
    }


    @Override
    public Application remind(Application application) throws RemindAlreadySentException,
        ImpatientAboutApplicationForLeaveProcessException {

        LocalDate remindDate = application.getRemindDate();

        if (remindDate == null) {
            LocalDate minDateForNotification = application.getApplicationDate()
                .plusDays(MIN_DAYS_LEFT_BEFORE_REMINDING_IS_POSSIBLE);

            if (minDateForNotification.isAfter(LocalDate.now(UTC))) {
                throw new ImpatientAboutApplicationForLeaveProcessException("It's too early to remind the bosses!");
            }
        }

        if (remindDate != null && remindDate.isEqual(LocalDate.now(UTC))) {
            throw new RemindAlreadySentException("Reminding is possible maximum one time per day!");
        }

        applicationMailService.sendRemindBossNotification(application);

        application.setRemindDate(LocalDate.now(UTC));
        return applicationService.save(application);
    }


    @Override
    public Application refer(Application application, Person recipient, Person sender) {

        commentService.create(application, ApplicationAction.REFERRED, Optional.of(recipient.getNiceName()), sender);
        applicationMailService.sendReferApplicationNotification(application, recipient, sender);

        return application;
    }
}
