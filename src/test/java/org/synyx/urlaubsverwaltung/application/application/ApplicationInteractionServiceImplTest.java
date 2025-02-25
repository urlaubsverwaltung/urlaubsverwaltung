package org.synyx.urlaubsverwaltung.application.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.support.StaticMessageSource;
import org.synyx.urlaubsverwaltung.account.AccountInteractionService;
import org.synyx.urlaubsverwaltung.application.comment.ApplicationComment;
import org.synyx.urlaubsverwaltung.application.comment.ApplicationCommentAction;
import org.synyx.urlaubsverwaltung.application.comment.ApplicationCommentService;
import org.synyx.urlaubsverwaltung.application.vacationtype.ProvidedVacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonDeletedEvent;
import org.synyx.urlaubsverwaltung.person.Role;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.time.ZoneOffset.UTC;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createApplication;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createPerson;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createVacationType;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.application.comment.ApplicationCommentAction.CANCELLED;
import static org.synyx.urlaubsverwaltung.application.comment.ApplicationCommentAction.CANCEL_REQUESTED;
import static org.synyx.urlaubsverwaltung.application.comment.ApplicationCommentAction.CANCEL_REQUESTED_DECLINED;
import static org.synyx.urlaubsverwaltung.application.comment.ApplicationCommentAction.CONVERTED;
import static org.synyx.urlaubsverwaltung.application.comment.ApplicationCommentAction.EDITED;
import static org.synyx.urlaubsverwaltung.application.comment.ApplicationCommentAction.REFERRED;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@ExtendWith(MockitoExtension.class)
class ApplicationInteractionServiceImplTest {

    private ApplicationInteractionServiceImpl sut;

    @Mock
    private ApplicationService applicationService;
    @Mock
    private ApplicationCommentService commentService;
    @Mock
    private AccountInteractionService accountInteractionService;
    @Mock
    private ApplicationMailService applicationMailService;
    @Mock
    private DepartmentService departmentService;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    private final Clock clock = Clock.systemUTC();

    @BeforeEach
    void setUp() {
        sut = new ApplicationInteractionServiceImpl(applicationService, commentService, accountInteractionService,
            applicationMailService, departmentService, clock, applicationEventPublisher);
    }

    // APPLY FOR LEAVE -------------------------------------------------------------------------------------------------
    @Test
    void ensureApplyForLeaveChangesStateAndOtherAttributesAndSavesTheApplicationForLeave() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Person applier = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Optional<String> comment = of("Foo");

        Application applicationForLeave = getDummyApplication(person);
        when(applicationService.save(applicationForLeave)).thenReturn(applicationForLeave);

        sut.apply(applicationForLeave, applier, comment);

        assertThat(applicationForLeave.getStatus()).isEqualTo(WAITING);
        assertThat(applicationForLeave.getPerson()).isEqualTo(person);
        assertThat(applicationForLeave.getApplier()).isEqualTo(applier);
        assertThat(applicationForLeave.getApplicationDate()).isEqualTo(LocalDate.now(UTC));

        verify(applicationService).save(applicationForLeave);
        verify(commentService).create(applicationForLeave, ApplicationCommentAction.APPLIED, comment, applier);

        ArgumentCaptor<ApplicationAppliedEvent> argumentCaptor = ArgumentCaptor.forClass(ApplicationAppliedEvent.class);
        verify(applicationEventPublisher).publishEvent(argumentCaptor.capture());
        final ApplicationAppliedEvent event = argumentCaptor.getValue();
        assertThat(event.application()).isEqualTo(applicationForLeave);
        assertThat(event.createdAt()).isBeforeOrEqualTo(Instant.now());
        assertThat(event.id()).isNotNull();
    }

    @Test
    void ensureSendsConfirmationEmailToPersonAndNotificationEmailToBossesWhenApplyingForOneself() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final Application applicationForLeave = getDummyApplication(person);
        when(applicationService.save(applicationForLeave)).thenReturn(applicationForLeave);

        final ApplicationComment applicationComment = new ApplicationComment(
            1L, Instant.now(clock), applicationForLeave, ApplicationCommentAction.ALLOWED, person, "");

        when(commentService.create(eq(applicationForLeave), eq(ApplicationCommentAction.APPLIED), any(), eq(person))).thenReturn(applicationComment);

        sut.apply(applicationForLeave, person, of("Foo"));

        verify(applicationMailService).sendAppliedNotification(applicationForLeave, applicationComment);
        verify(applicationMailService, never()).sendAppliedByManagementNotification(eq(applicationForLeave), any(ApplicationComment.class));
        verify(applicationMailService).sendAppliedNotificationToManagement(applicationForLeave, applicationComment);
    }

    @Test
    void ensureSendsNotificationToPersonIfApplicationForLeaveNotAppliedByOneself() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Person applier = new Person("muster", "Muster", "Marlene", "muster@example.org");
        applier.setPermissions(List.of(OFFICE));

        Application applicationForLeave = getDummyApplication(person);
        when(applicationService.save(applicationForLeave)).thenReturn(applicationForLeave);

        final ApplicationComment applicationComment = new ApplicationComment(
            1L, Instant.now(clock), applicationForLeave, ApplicationCommentAction.ALLOWED, person, "");

        when(commentService.create(eq(applicationForLeave), eq(ApplicationCommentAction.APPLIED), any(), eq(applier))).thenReturn(applicationComment);

        sut.apply(applicationForLeave, applier, of("Foo"));

        verify(applicationMailService, never()).sendAppliedNotification(eq(applicationForLeave), any(ApplicationComment.class));
        verify(applicationMailService).sendAppliedByManagementNotification(applicationForLeave, applicationComment);
        verify(applicationMailService).sendAppliedNotificationToManagement(applicationForLeave, applicationComment);
    }

    @Test
    void ensureApplyingForLeaveUpdatesTheRemainingVacationDays() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Person applier = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Optional<String> comment = of("Foo");

        final Application applicationForLeave = getDummyApplication(person);
        when(applicationService.save(applicationForLeave)).thenReturn(applicationForLeave);

        sut.apply(applicationForLeave, applier, comment);

        verify(accountInteractionService).updateRemainingVacationDays(2013, person);
    }

    // Direct ALLOW APPLICATION FOR LEAVE -------------------------------------------------------------------------------------
    @Test
    void ensureApplicationForLeaveCanBeAllowedDirectly() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Optional<String> comment = of("Foo");

        final VacationType<?> holidayType = ProvidedVacationType.builder(new StaticMessageSource()).build();

        final Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setVacationType(holidayType);
        applicationForLeave.setStatus(ALLOWED);
        when(applicationService.save(applicationForLeave)).thenReturn(applicationForLeave);

        final ApplicationComment applicationComment = new ApplicationComment(
            1L, Instant.now(clock), applicationForLeave, ApplicationCommentAction.ALLOWED_DIRECTLY, person, "");

        when(commentService.create(applicationForLeave, ApplicationCommentAction.ALLOWED_DIRECTLY, comment, person))
            .thenReturn(applicationComment);

        sut.directAllow(applicationForLeave, person, comment);

        assertApplicationForLeaveAndCommentAreSaved(applicationForLeave, ApplicationCommentAction.ALLOWED_DIRECTLY, comment, person);

        verify(applicationMailService).sendConfirmationAllowedDirectly(eq(applicationForLeave), any(ApplicationComment.class));
        verify(applicationMailService, never()).sendConfirmationAllowedDirectlyByManagement(any(Application.class), any(ApplicationComment.class));
        verify(applicationMailService).sendDirectlyAllowedNotificationToManagement(any(Application.class), any(ApplicationComment.class));
        verify(applicationMailService).notifyHolidayReplacementAboutDirectlyAllowedApplication(any(HolidayReplacementEntity.class), any(Application.class));

        ArgumentCaptor<ApplicationAllowedEvent> argumentCaptor = ArgumentCaptor.forClass(ApplicationAllowedEvent.class);
        verify(applicationEventPublisher).publishEvent(argumentCaptor.capture());
        final ApplicationAllowedEvent event = argumentCaptor.getValue();
        assertThat(event.application()).isEqualTo(applicationForLeave);
        assertThat(event.createdAt()).isBeforeOrEqualTo(Instant.now());
        assertThat(event.id()).isNotNull();
    }

    @Test
    void ensureApplicationForLeaveCanBeAllowedDirectlyWithCalendarSync() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Optional<String> comment = of("Foo");

        final VacationType<?> holidayType = ProvidedVacationType.builder(new StaticMessageSource()).build();

        final Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setVacationType(holidayType);
        applicationForLeave.setStatus(ALLOWED);
        when(applicationService.save(applicationForLeave)).thenReturn(applicationForLeave);

        final ApplicationComment applicationComment = new ApplicationComment(
            1L, Instant.now(clock), applicationForLeave, ApplicationCommentAction.ALLOWED_DIRECTLY, person, "Foo");

        when(commentService.create(applicationForLeave, ApplicationCommentAction.ALLOWED_DIRECTLY, comment, person))
            .thenReturn(applicationComment);

        sut.directAllow(applicationForLeave, person, comment);

        assertApplicationForLeaveAndCommentAreSaved(applicationForLeave, ApplicationCommentAction.ALLOWED_DIRECTLY, comment, person);

        verify(applicationMailService).sendConfirmationAllowedDirectly(eq(applicationForLeave), any(ApplicationComment.class));
        verify(applicationMailService, never()).sendConfirmationAllowedDirectlyByManagement(any(Application.class), any(ApplicationComment.class));
        verify(applicationMailService).sendDirectlyAllowedNotificationToManagement(any(Application.class), any(ApplicationComment.class));
        verify(applicationMailService).notifyHolidayReplacementAboutDirectlyAllowedApplication(any(HolidayReplacementEntity.class), any(Application.class));
    }

    @Test
    void ensureApplicationForLeaveCanBeAllowedDirectlyByOffice() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Person office = new Person("office", "Muster", "Marlene", "muster@example.org");
        office.setPermissions(List.of(OFFICE));
        final Optional<String> comment = of("Foo");

        final VacationType<?> holidayType = ProvidedVacationType.builder(new StaticMessageSource()).build();

        final Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setVacationType(holidayType);
        applicationForLeave.setStatus(ALLOWED);
        when(applicationService.save(applicationForLeave)).thenReturn(applicationForLeave);

        final ApplicationComment applicationComment = new ApplicationComment(
            1L, Instant.now(clock), applicationForLeave, ApplicationCommentAction.ALLOWED_DIRECTLY, person, "");

        when(commentService.create(applicationForLeave, ApplicationCommentAction.ALLOWED_DIRECTLY, comment, office))
            .thenReturn(applicationComment);

        sut.directAllow(applicationForLeave, office, comment);

        assertApplicationForLeaveAndCommentAreSaved(applicationForLeave, ApplicationCommentAction.ALLOWED_DIRECTLY, comment, office);

        verify(applicationMailService, never()).sendConfirmationAllowedDirectly(eq(applicationForLeave), any(ApplicationComment.class));
        verify(applicationMailService).sendConfirmationAllowedDirectlyByManagement(any(Application.class), any(ApplicationComment.class));
        verify(applicationMailService).sendDirectlyAllowedNotificationToManagement(any(Application.class), any(ApplicationComment.class));
        verify(applicationMailService).notifyHolidayReplacementAboutDirectlyAllowedApplication(any(HolidayReplacementEntity.class), any(Application.class));
    }

    // ALLOW APPLICATION FOR LEAVE -------------------------------------------------------------------------------------
    // ALLOWING - BOSS
    @Test
    void ensureWaitingApplicationForLeaveCanBeAllowedByBoss() throws NotPrivilegedToApproveException {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Person boss = createPerson("boss", USER, Role.BOSS);
        final Optional<String> comment = of("Foo");

        final Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(WAITING);
        when(applicationService.save(applicationForLeave)).thenReturn(applicationForLeave);

        final ApplicationComment applicationComment = new ApplicationComment(
            1L, Instant.now(clock), applicationForLeave, ApplicationCommentAction.ALLOWED, person, "");

        when(commentService.create(applicationForLeave, ApplicationCommentAction.ALLOWED, comment, boss))
            .thenReturn(applicationComment);

        sut.allow(applicationForLeave, boss, comment);

        assertApplicationForLeaveHasChangedStatus(applicationForLeave, ALLOWED, person, boss);
        assertApplicationForLeaveAndCommentAreSaved(applicationForLeave, ApplicationCommentAction.ALLOWED, comment, boss);
        assertAllowedNotificationIsSent(applicationForLeave);

        ArgumentCaptor<ApplicationAllowedEvent> argumentCaptor = ArgumentCaptor.forClass(ApplicationAllowedEvent.class);
        verify(applicationEventPublisher).publishEvent(argumentCaptor.capture());
        final ApplicationAllowedEvent event = argumentCaptor.getValue();
        assertThat(event.application()).isEqualTo(applicationForLeave);
        assertThat(event.createdAt()).isBeforeOrEqualTo(Instant.now());
        assertThat(event.id()).isNotNull();
    }

    @Test
    void ensureTemporaryAllowedApplicationForLeaveCanBeAllowedByBoss() throws NotPrivilegedToApproveException {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Person boss = createPerson("boss", USER, Role.BOSS);
        final Optional<String> comment = of("Foo");

        final Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(TEMPORARY_ALLOWED);
        applicationForLeave.setTwoStageApproval(false);
        when(applicationService.save(applicationForLeave)).thenReturn(applicationForLeave);

        final ApplicationComment applicationComment = new ApplicationComment(
            1L, Instant.now(clock), applicationForLeave, ApplicationCommentAction.ALLOWED, boss, "Foo");

        when(commentService.create(applicationForLeave, ApplicationCommentAction.ALLOWED, comment, boss))
            .thenReturn(applicationComment);

        sut.allow(applicationForLeave, boss, comment);

        assertApplicationForLeaveHasChangedStatus(applicationForLeave, ALLOWED, person, boss);
        assertApplicationForLeaveAndCommentAreSaved(applicationForLeave, ApplicationCommentAction.ALLOWED, comment, boss);
        assertAllowedNotificationIsSent(applicationForLeave);
    }

    @Test
    void ensureTemporaryAllowedApplicationForLeaveCanBeAllowedByBossEvenWithTwoStageApprovalActive() throws NotPrivilegedToApproveException {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Person boss = createPerson("boss", USER, Role.BOSS);
        final Optional<String> comment = of("Foo");

        final Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(TEMPORARY_ALLOWED);
        applicationForLeave.setTwoStageApproval(true);
        when(applicationService.save(applicationForLeave)).thenReturn(applicationForLeave);

        final ApplicationComment applicationComment = new ApplicationComment(
            1L, Instant.now(clock), applicationForLeave, ApplicationCommentAction.ALLOWED, person, "");

        when(commentService.create(applicationForLeave, ApplicationCommentAction.ALLOWED, comment, boss))
            .thenReturn(applicationComment);

        sut.allow(applicationForLeave, boss, comment);

        assertApplicationForLeaveHasChangedStatus(applicationForLeave, ALLOWED, person, boss);
        assertApplicationForLeaveAndCommentAreSaved(applicationForLeave, ApplicationCommentAction.ALLOWED, comment, boss);
        assertAllowedNotificationIsSent(applicationForLeave);
    }

    @Test
    void ensureIfAllowedApplicationForLeaveIsAllowedAgainNothingHappens() throws NotPrivilegedToApproveException {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Person boss = createPerson("boss", USER, Role.BOSS);
        final Optional<String> comment = of("Foo");

        final Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(ALLOWED);

        sut.allow(applicationForLeave, boss, comment);

        assertThat(applicationForLeave.getStatus()).isEqualTo(ALLOWED);

        verifyNoInteractions(applicationService);
        verifyNoInteractions(commentService);
        verifyNoInteractions(applicationMailService);
    }

    // ALLOWING - DEPARTMENT HEAD
    @Test
    void ensureThrowsWhenExecutingAllowProcessWithNotPrivilegedUser() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Person user = new Person("muster", "Muster", "Marlene", "muster@example.org");
        user.setPermissions(Collections.singletonList(USER));

        final Optional<String> comment = of("Foo");

        final Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(WAITING);

        assertThrows(NotPrivilegedToApproveException.class, () -> sut.allow(applicationForLeave, user, comment));
    }

    @Test
    void ensureWaitingApplicationForLeaveCanBeAllowedByDepartmentHead() throws NotPrivilegedToApproveException {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Person departmentHead = createPerson("head", USER, DEPARTMENT_HEAD);
        when(departmentService.isDepartmentHeadAllowedToManagePerson(departmentHead, person)).thenReturn(true);

        final Optional<String> comment = of("Foo");

        final Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(WAITING);
        when(applicationService.save(applicationForLeave)).thenReturn(applicationForLeave);

        final ApplicationComment applicationComment = new ApplicationComment(
            1L, Instant.now(clock), applicationForLeave, ApplicationCommentAction.ALLOWED, person, "");

        when(commentService.create(applicationForLeave, ApplicationCommentAction.ALLOWED, comment, departmentHead))
            .thenReturn(applicationComment);

        sut.allow(applicationForLeave, departmentHead, comment);

        assertApplicationForLeaveHasChangedStatus(applicationForLeave, ALLOWED, person, departmentHead);
        assertApplicationForLeaveAndCommentAreSaved(applicationForLeave, ApplicationCommentAction.ALLOWED, comment, departmentHead);
        assertAllowedNotificationIsSent(applicationForLeave);
    }

    @Test
    void ensureWaitingApplicationForLeaveCanOnlyBeAllowedTemporaryByDepartmentHeadIfTwoStageApprovalIsActive() throws NotPrivilegedToApproveException {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Person departmentHead = createPerson("head", USER, DEPARTMENT_HEAD);
        when(departmentService.isDepartmentHeadAllowedToManagePerson(departmentHead, person)).thenReturn(true);

        final Optional<String> comment = of("Foo");

        final Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(WAITING);
        applicationForLeave.setTwoStageApproval(true);
        when(applicationService.save(applicationForLeave)).thenReturn(applicationForLeave);

        final ApplicationComment applicationComment = new ApplicationComment(
            1L, Instant.now(clock), applicationForLeave, ApplicationCommentAction.TEMPORARY_ALLOWED, person, "");

        when(commentService.create(applicationForLeave, ApplicationCommentAction.TEMPORARY_ALLOWED, comment, departmentHead))
            .thenReturn(applicationComment);

        sut.allow(applicationForLeave, departmentHead, comment);

        assertApplicationForLeaveHasChangedStatus(applicationForLeave, TEMPORARY_ALLOWED, person, departmentHead);
        assertApplicationForLeaveAndCommentAreSaved(applicationForLeave, ApplicationCommentAction.TEMPORARY_ALLOWED, comment, departmentHead);
        assertTemporaryAllowedNotificationIsSent(applicationForLeave);

        ArgumentCaptor<ApplicationAllowedTemporarilyEvent> argumentCaptor = ArgumentCaptor.forClass(ApplicationAllowedTemporarilyEvent.class);
        verify(applicationEventPublisher).publishEvent(argumentCaptor.capture());
        final ApplicationAllowedTemporarilyEvent event = argumentCaptor.getValue();
        assertThat(event.application()).isEqualTo(applicationForLeave);
        assertThat(event.createdAt()).isBeforeOrEqualTo(Instant.now());
        assertThat(event.id()).isNotNull();
    }

    @Test
    void ensureIfTemporaryAllowedApplicationForLeaveIsAllowedByDepartmentHeadWithTwoStageApprovalIsActiveNothingHappens() throws NotPrivilegedToApproveException {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Person departmentHead = createPerson("head", USER, DEPARTMENT_HEAD);
        when(departmentService.isDepartmentHeadAllowedToManagePerson(departmentHead, person)).thenReturn(true);

        final Optional<String> comment = of("Foo");

        final Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(TEMPORARY_ALLOWED);
        applicationForLeave.setTwoStageApproval(true);

        sut.allow(applicationForLeave, departmentHead, comment);

        assertThat(applicationForLeave.getStatus()).isEqualTo(TEMPORARY_ALLOWED);

        verifyNoInteractions(applicationService);
        verifyNoInteractions(commentService);
        verifyNoInteractions(applicationMailService);
    }

    @Test
    void ensureIfTemporaryAllowedApplicationForLeaveIsAllowedByDepartmentHeadWithTwoStageApprovalNotActiveStatusIsChanged() throws NotPrivilegedToApproveException {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Person departmentHead = createPerson("head", USER, DEPARTMENT_HEAD);
        when(departmentService.isDepartmentHeadAllowedToManagePerson(departmentHead, person)).thenReturn(true);

        final Optional<String> comment = of("Foo");

        final Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(TEMPORARY_ALLOWED);
        applicationForLeave.setTwoStageApproval(false);
        when(applicationService.save(applicationForLeave)).thenReturn(applicationForLeave);

        final ApplicationComment applicationComment = new ApplicationComment(
            1L, Instant.now(clock), applicationForLeave, ApplicationCommentAction.ALLOWED, person, "");

        when(commentService.create(any(), any(), any(), any())).thenReturn(applicationComment);

        sut.allow(applicationForLeave, departmentHead, comment);

        assertApplicationForLeaveHasChangedStatus(applicationForLeave, ALLOWED, person, departmentHead);
        assertApplicationForLeaveAndCommentAreSaved(applicationForLeave, ApplicationCommentAction.ALLOWED, comment, departmentHead);
        assertAllowedNotificationIsSent(applicationForLeave);
    }

    // ALLOWING - SECOND STAGE AUTHORITY
    @Test
    void ensureWaitingApplicationForLeaveCanBeAllowedBySecondStageAuthority() throws NotPrivilegedToApproveException {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Person secondStage = createPerson("manager", USER, SECOND_STAGE_AUTHORITY);
        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(secondStage, person)).thenReturn(true);

        final Optional<String> comment = of("Foo");

        final Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(WAITING);
        when(applicationService.save(applicationForLeave)).thenReturn(applicationForLeave);

        final ApplicationComment applicationComment = new ApplicationComment(
            1L, Instant.now(clock), applicationForLeave, ApplicationCommentAction.ALLOWED, person, "");

        when(commentService.create(applicationForLeave, ApplicationCommentAction.ALLOWED, comment, secondStage))
            .thenReturn(applicationComment);

        sut.allow(applicationForLeave, secondStage, comment);

        assertApplicationForLeaveHasChangedStatus(applicationForLeave, ALLOWED, person, secondStage);
        assertApplicationForLeaveAndCommentAreSaved(applicationForLeave, ApplicationCommentAction.ALLOWED, comment, secondStage);
        assertAllowedNotificationIsSent(applicationForLeave);

        ArgumentCaptor<ApplicationAllowedEvent> argumentCaptor = ArgumentCaptor.forClass(ApplicationAllowedEvent.class);
        verify(applicationEventPublisher).publishEvent(argumentCaptor.capture());
        final ApplicationAllowedEvent event = argumentCaptor.getValue();
        assertThat(event.application()).isEqualTo(applicationForLeave);
        assertThat(event.createdAt()).isBeforeOrEqualTo(Instant.now());
        assertThat(event.id()).isNotNull();
    }

    @Test
    void ensureWaitingApplicationForLeaveCanBeAllowedBySecondStageAuthorityIfTwoStageApprovalIsActive() throws NotPrivilegedToApproveException {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Person secondStage = createPerson("manager", USER, SECOND_STAGE_AUTHORITY);
        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(secondStage, person)).thenReturn(true);

        final Optional<String> comment = of("Foo");

        final Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(WAITING);
        applicationForLeave.setTwoStageApproval(true);
        when(applicationService.save(applicationForLeave)).thenReturn(applicationForLeave);

        final ApplicationComment applicationComment = new ApplicationComment(
            1L, Instant.now(clock), applicationForLeave, ApplicationCommentAction.ALLOWED, person, "");

        when(commentService.create(applicationForLeave, ApplicationCommentAction.ALLOWED, comment, secondStage))
            .thenReturn(applicationComment);

        sut.allow(applicationForLeave, secondStage, comment);

        assertApplicationForLeaveHasChangedStatus(applicationForLeave, ALLOWED, person, secondStage);
        assertApplicationForLeaveAndCommentAreSaved(applicationForLeave, ApplicationCommentAction.ALLOWED, comment, secondStage);
        assertAllowedNotificationIsSent(applicationForLeave);
    }

    @Test
    void ensureTemporaryAllowedApplicationForLeaveCanBeAllowedBySecondStageAuthorityIfTwoStageApprovalIsActive() throws NotPrivilegedToApproveException {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Person secondStage = createPerson("manager", USER, SECOND_STAGE_AUTHORITY);
        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(secondStage, person)).thenReturn(true);

        final Optional<String> comment = of("Foo");

        final Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(TEMPORARY_ALLOWED);
        applicationForLeave.setTwoStageApproval(true);
        when(applicationService.save(applicationForLeave)).thenReturn(applicationForLeave);

        final ApplicationComment applicationComment = new ApplicationComment(
            1L, Instant.now(clock), applicationForLeave, ApplicationCommentAction.ALLOWED, secondStage, "Foo");

        when(commentService.create(applicationForLeave, ApplicationCommentAction.ALLOWED, comment, secondStage))
            .thenReturn(applicationComment);

        sut.allow(applicationForLeave, secondStage, comment);

        assertApplicationForLeaveHasChangedStatus(applicationForLeave, ALLOWED, person, secondStage);
        assertApplicationForLeaveAndCommentAreSaved(applicationForLeave, ApplicationCommentAction.ALLOWED, comment, secondStage);
        assertAllowedNotificationIsSent(applicationForLeave);
    }

    @Test
    void ensureDepartmentHeadCanBeAllowedBySecondStageAuthority() throws NotPrivilegedToApproveException {

        final Person departmentHead = new Person("muster", "Muster", "Marlene", "muster@example.org");
        departmentHead.setPermissions(asList(USER, DEPARTMENT_HEAD));

        final Person secondStageAuthority = new Person("muster", "Muster", "Marlene", "muster@example.org");
        secondStageAuthority.setPermissions(asList(USER, SECOND_STAGE_AUTHORITY));

        final boolean isSecondStage = departmentService.isSecondStageAuthorityAllowedToManagePerson(eq(secondStageAuthority), eq(departmentHead));
        when(isSecondStage).thenReturn(true);

        final Application applicationForLeave = getDummyApplication(departmentHead);
        applicationForLeave.setStatus(WAITING);
        when(applicationService.save(applicationForLeave)).thenReturn(applicationForLeave);

        final ApplicationComment applicationComment = new ApplicationComment(
            1L, Instant.now(clock), applicationForLeave, ApplicationCommentAction.ALLOWED, departmentHead, "");

        final Optional<String> comment = of("Foo");
        when(commentService.create(applicationForLeave, ApplicationCommentAction.ALLOWED, comment, secondStageAuthority))
            .thenReturn(applicationComment);

        sut.allow(applicationForLeave, secondStageAuthority, comment);
        assertApplicationForLeaveHasChangedStatus(applicationForLeave, ALLOWED, departmentHead, secondStageAuthority);
        assertApplicationForLeaveAndCommentAreSaved(applicationForLeave, ApplicationCommentAction.ALLOWED, comment, secondStageAuthority);
        assertAllowedNotificationIsSent(applicationForLeave);

        ArgumentCaptor<ApplicationAllowedEvent> argumentCaptor = ArgumentCaptor.forClass(ApplicationAllowedEvent.class);
        verify(applicationEventPublisher).publishEvent(argumentCaptor.capture());
        final ApplicationAllowedEvent event = argumentCaptor.getValue();
        assertThat(event.application()).isEqualTo(applicationForLeave);
        assertThat(event.createdAt()).isBeforeOrEqualTo(Instant.now());
        assertThat(event.id()).isNotNull();
    }

    @Test
    void ensureSecondStageAuthorityIfDifferentDepartmentButMemberOfOwnDepartmentCanBeAllowedByDepartmentHead() throws NotPrivilegedToApproveException {

        final Person departmentHead = new Person("muster", "Muster", "Marlene", "muster@example.org");
        departmentHead.setPermissions(asList(USER, DEPARTMENT_HEAD));

        final Person secondStageAuthority = new Person("muster", "Muster", "Marlene", "muster@example.org");
        secondStageAuthority.setPermissions(asList(USER, SECOND_STAGE_AUTHORITY));

        when(departmentService.isDepartmentHeadAllowedToManagePerson(departmentHead, secondStageAuthority)).thenReturn(true);
        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(departmentHead, secondStageAuthority)).thenReturn(false);
        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(secondStageAuthority, departmentHead)).thenReturn(false);

        final Optional<String> comment = of("Foo");

        final Application applicationForLeave = getDummyApplication(secondStageAuthority);
        applicationForLeave.setStatus(WAITING);
        applicationForLeave.setTwoStageApproval(true);

        when(applicationService.save(applicationForLeave)).then(returnsFirstArg());

        final Application allowedApplicationForLeave = sut.allow(applicationForLeave, departmentHead, comment);
        assertApplicationForLeaveHasChangedStatus(allowedApplicationForLeave, TEMPORARY_ALLOWED, secondStageAuthority, departmentHead);
    }

    @Test
    void ensureSecondStageAuthorityOfSameDepartmentCanNotBeAllowedByDepartmentHead() {

        final Person departmentHead = new Person("muster", "Muster", "Marlene", "muster@example.org");
        departmentHead.setPermissions(asList(USER, DEPARTMENT_HEAD));

        final Person secondStageAuthority = new Person("muster", "Muster", "Marlene", "muster@example.org");
        secondStageAuthority.setPermissions(asList(USER, SECOND_STAGE_AUTHORITY));

        when(departmentService.isDepartmentHeadAllowedToManagePerson(departmentHead, secondStageAuthority)).thenReturn(true);
        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(departmentHead, secondStageAuthority)).thenReturn(false);
        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(secondStageAuthority, departmentHead)).thenReturn(true);

        final Optional<String> comment = of("Foo");

        final Application applicationForLeave = getDummyApplication(secondStageAuthority);
        applicationForLeave.setStatus(WAITING);
        applicationForLeave.setTwoStageApproval(true);

        assertThrows(NotPrivilegedToApproveException.class, () -> sut.allow(applicationForLeave, departmentHead, comment));
    }

    @Test
    void ensureSecondStageAuthorityCanNotAllowHimself() {

        final Person secondStageAuthority = new Person("muster", "Muster", "Marlene", "muster@example.org");
        secondStageAuthority.setPermissions(asList(USER, SECOND_STAGE_AUTHORITY));

        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(secondStageAuthority, secondStageAuthority)).thenReturn(true);

        final Optional<String> comment = of("Foo");

        final Application applicationForLeave = getDummyApplication(secondStageAuthority);
        applicationForLeave.setStatus(WAITING);

        assertThrows(NotPrivilegedToApproveException.class, () -> sut.allow(applicationForLeave, secondStageAuthority, comment));
    }

    @Test
    void ensureDepartmentHeadCanNotAllowHimself() {

        final Person departmentHead = new Person("muster", "Muster", "Marlene", "muster@example.org");
        departmentHead.setPermissions(asList(USER, DEPARTMENT_HEAD));

        when(departmentService.isDepartmentHeadAllowedToManagePerson(departmentHead, departmentHead)).thenReturn(true);

        final Optional<String> comment = of("Foo");

        final Application applicationForLeave = getDummyApplication(departmentHead);
        applicationForLeave.setStatus(WAITING);

        assertThrows(NotPrivilegedToApproveException.class, () -> sut.allow(applicationForLeave, departmentHead, comment));
    }

    // ALLOWING - REPLACEMENT NOTIFICATION
    @Test
    void ensureAllowingApplicationForLeaveWithHolidayReplacementSendsNotificationToReplacement() throws NotPrivilegedToApproveException {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Person replacementPerson = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Person boss = createPerson("boss", USER, Role.BOSS);

        final HolidayReplacementEntity replacementEntity = new HolidayReplacementEntity();
        replacementEntity.setPerson(replacementPerson);

        final Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(WAITING);
        applicationForLeave.setHolidayReplacements(List.of(replacementEntity));
        when(applicationService.save(applicationForLeave)).thenReturn(applicationForLeave);

        sut.allow(applicationForLeave, boss, of("Foo"));

        verify(applicationMailService).notifyHolidayReplacementAllow(replacementEntity, applicationForLeave);
    }

    @Test
    void ensureAllowingApplicationForLeaveWithoutHolidayReplacementDoesNotSendNotification() throws NotPrivilegedToApproveException {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Person boss = createPerson("boss", USER, Role.BOSS);

        final Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(WAITING);
        applicationForLeave.setHolidayReplacements(emptyList());
        when(applicationService.save(applicationForLeave)).thenReturn(applicationForLeave);

        sut.allow(applicationForLeave, boss, of("Foo"));

        verify(applicationMailService, never()).notifyHolidayReplacementAllow(any(), any());
    }

    @Test
    void ensureTemporaryAllowingApplicationForLeaveWithHolidayReplacementDoesNotSendNotification() throws NotPrivilegedToApproveException {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Person replacementPerson = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Person departmentHead = createPerson("head", USER, DEPARTMENT_HEAD);
        when(departmentService.isDepartmentHeadAllowedToManagePerson(departmentHead, person)).thenReturn(true);

        final HolidayReplacementEntity replacementEntity = new HolidayReplacementEntity();
        replacementEntity.setPerson(replacementPerson);

        final Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(WAITING);
        applicationForLeave.setHolidayReplacements(List.of(replacementEntity));
        applicationForLeave.setTwoStageApproval(true);

        sut.allow(applicationForLeave, departmentHead, of("Foo"));

        verify(applicationMailService, never()).notifyHolidayReplacementAllow(any(), any());
    }

    // REJECT APPLICATION FOR LEAVE ------------------------------------------------------------------------------------
    @Test
    void ensureRejectingApplicationForLeaveChangesStateAndOtherAttributesAndSavesTheApplicationForLeave() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Person boss = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final Optional<String> comment = of("Foo");

        final Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(WAITING);
        when(applicationService.save(applicationForLeave)).thenReturn(applicationForLeave);

        sut.reject(applicationForLeave, boss, comment);

        assertThat(applicationForLeave.getStatus()).isEqualTo(ApplicationStatus.REJECTED);
        assertThat(applicationForLeave.getPerson()).isEqualTo(person);
        assertThat(applicationForLeave.getBoss()).isEqualTo(boss);
        assertThat(applicationForLeave.getEditedDate()).isEqualTo(LocalDate.now(UTC));

        verify(applicationService).save(applicationForLeave);
        verify(commentService).create(applicationForLeave, ApplicationCommentAction.REJECTED, comment, boss);

        ArgumentCaptor<ApplicationRejectedEvent> argumentCaptor = ArgumentCaptor.forClass(ApplicationRejectedEvent.class);
        verify(applicationEventPublisher).publishEvent(argumentCaptor.capture());
        final ApplicationRejectedEvent event = argumentCaptor.getValue();
        assertThat(event.application()).isEqualTo(applicationForLeave);
        assertThat(event.createdAt()).isBeforeOrEqualTo(Instant.now());
        assertThat(event.id()).isNotNull();
    }

    @Test
    void ensureRejectingApplicationForLeaveSendsEmailToPerson() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Person boss = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final Application applicationForLeave = getDummyApplication(person);
        when(applicationService.save(applicationForLeave)).thenReturn(applicationForLeave);

        final Optional<String> optionalComment = of("Foo");
        final ApplicationComment applicationComment = new ApplicationComment(
            1L, Instant.now(clock), applicationForLeave, ApplicationCommentAction.REJECTED, person, "");

        when(commentService.create(applicationForLeave, ApplicationCommentAction.REJECTED, optionalComment, boss)).thenReturn(applicationComment);

        sut.reject(applicationForLeave, boss, optionalComment);

        verify(applicationMailService).sendRejectedNotification(applicationForLeave, applicationComment);
    }

    // CANCEL APPLICATION FOR LEAVE ------------------------------------------------------------------------------------
    @Test
    void ensureCancelledNotYetAllowedApplicationForLeaveChangesStateAndOtherAttributesAndSendsEmail() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Optional<String> comment = of("Foo");

        final Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(WAITING);
        when(applicationService.save(applicationForLeave)).thenReturn(applicationForLeave);

        final ApplicationComment applicationComment = new ApplicationComment(
            1L, Instant.now(clock), applicationForLeave, ApplicationCommentAction.ALLOWED, person, "");

        when(commentService.create(applicationForLeave, ApplicationCommentAction.REVOKED, comment, person)).thenReturn(applicationComment);

        sut.cancel(applicationForLeave, person, comment);
        assertThat(applicationForLeave.getStatus()).isEqualTo(ApplicationStatus.REVOKED);
        assertThat(applicationForLeave.getPerson()).isEqualTo(person);
        assertThat(applicationForLeave.getCanceller()).isEqualTo(person);
        assertThat(applicationForLeave.getCancelDate()).isEqualTo(LocalDate.now(UTC));
        assertThat(applicationForLeave.isFormerlyAllowed()).isFalse();

        verify(applicationMailService).sendRevokedNotifications(applicationForLeave, applicationComment);

        ArgumentCaptor<ApplicationRevokedEvent> argumentCaptor = ArgumentCaptor.forClass(ApplicationRevokedEvent.class);
        verify(applicationEventPublisher).publishEvent(argumentCaptor.capture());
        final ApplicationRevokedEvent event = argumentCaptor.getValue();
        assertThat(event.application()).isEqualTo(applicationForLeave);
        assertThat(event.createdAt()).isBeforeOrEqualTo(Instant.now());
        assertThat(event.id()).isNotNull();
    }

    @Test
    void ensureCancellingAllowedApplicationByOwnerCreatesACancellationRequest() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Optional<String> comment = of("Foo");

        final Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(ALLOWED);
        when(applicationService.save(applicationForLeave)).thenReturn(applicationForLeave);

        final ApplicationComment applicationComment = new ApplicationComment(
            1L, Instant.now(clock), applicationForLeave, ApplicationCommentAction.ALLOWED, person, "");

        when(commentService.create(any(Application.class), any(ApplicationCommentAction.class), any(), any(Person.class)))
            .thenReturn(applicationComment);

        sut.cancel(applicationForLeave, person, comment);
        assertThat(applicationForLeave.getStatus()).isEqualTo(ApplicationStatus.ALLOWED_CANCELLATION_REQUESTED);

        verify(applicationService).save(applicationForLeave);
        verify(commentService).create(applicationForLeave, CANCEL_REQUESTED, comment, person);
        verify(applicationMailService).sendCancellationRequest(eq(applicationForLeave), any(ApplicationComment.class));

        ArgumentCaptor<ApplicationCancellationRequestedEvent> argumentCaptor = ArgumentCaptor.forClass(ApplicationCancellationRequestedEvent.class);
        verify(applicationEventPublisher).publishEvent(argumentCaptor.capture());
        final ApplicationCancellationRequestedEvent event = argumentCaptor.getValue();
        assertThat(event.application()).isEqualTo(applicationForLeave);
        assertThat(event.createdAt()).isBeforeOrEqualTo(Instant.now());
        assertThat(event.id()).isNotNull();
    }

    @Test
    void ensureCancellingAllowedApplicationByOwnerThatIsOfficeCancelsTheApplicationForLeaveDirectly() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setPermissions(asList(USER, OFFICE));

        final Optional<String> comment = of("Foo");

        final Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(ALLOWED);
        when(applicationService.save(applicationForLeave)).thenReturn(applicationForLeave);

        final ApplicationComment applicationComment = new ApplicationComment(
            1L, Instant.now(clock), applicationForLeave, ApplicationCommentAction.ALLOWED, person, "");

        when(commentService.create(applicationForLeave, CANCELLED, comment, person)).thenReturn(applicationComment);

        sut.cancel(applicationForLeave, person, comment);
        assertThat(applicationForLeave.getStatus()).isEqualTo(ApplicationStatus.CANCELLED);
        assertThat(applicationForLeave.getPerson()).isEqualTo(person);
        assertThat(applicationForLeave.getCanceller()).isEqualTo(person);
        assertThat(applicationForLeave.getCancelDate()).isEqualTo(LocalDate.now(UTC));
        assertThat(applicationForLeave.isFormerlyAllowed()).isTrue();

        verify(applicationMailService).sendCancelledConfirmationByManagement(applicationForLeave, applicationComment);

        ArgumentCaptor<ApplicationCancelledEvent> argumentCaptor = ArgumentCaptor.forClass(ApplicationCancelledEvent.class);
        verify(applicationEventPublisher).publishEvent(argumentCaptor.capture());
        final ApplicationCancelledEvent event = argumentCaptor.getValue();
        assertThat(event.application()).isEqualTo(applicationForLeave);
        assertThat(event.createdAt()).isBeforeOrEqualTo(Instant.now());
        assertThat(event.id()).isNotNull();
    }

    @Test
    void ensureCancellingAllowedApplicationForLeaveOnBehalfForSomeOneChangesStateAndOtherAttributesAndSendsAnEmail() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Person canceller = new Person("muster", "Muster", "Marlene", "muster@example.org");
        canceller.setPermissions(asList(USER, OFFICE));

        final Optional<String> comment = of("Foo");

        final Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(ALLOWED);
        when(applicationService.save(applicationForLeave)).thenReturn(applicationForLeave);

        final ApplicationComment applicationComment = new ApplicationComment(
            1L, Instant.now(clock), applicationForLeave, ApplicationCommentAction.ALLOWED, person, "");

        when(commentService.create(any(Application.class), any(ApplicationCommentAction.class), any(), any(Person.class)))
            .thenReturn(applicationComment);

        sut.cancel(applicationForLeave, canceller, comment);

        assertThat(applicationForLeave.getStatus()).isEqualTo(ApplicationStatus.CANCELLED);
        assertThat(applicationForLeave.getPerson()).isEqualTo(person);
        assertThat(applicationForLeave.getCanceller()).isEqualTo(canceller);
        assertThat(applicationForLeave.getCancelDate()).isEqualTo(LocalDate.now(UTC));
        assertThat(applicationForLeave.isFormerlyAllowed()).isTrue();

        verify(applicationService).save(applicationForLeave);
        verify(commentService).create(applicationForLeave, CANCELLED, comment, canceller);
        verify(applicationMailService).sendCancelledConfirmationByManagement(eq(applicationForLeave), any(ApplicationComment.class));
    }

    @Test
    void cancellingNotYetAllowedApplicationAsOffice() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Person canceller = new Person("muster", "Muster", "Marlene", "muster@example.org");
        canceller.setPermissions(asList(USER, OFFICE));

        final Optional<String> comment = of("Foo");

        final Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(WAITING);
        when(applicationService.save(applicationForLeave)).thenReturn(applicationForLeave);

        final ApplicationComment applicationComment = new ApplicationComment(
            1L, Instant.now(clock), applicationForLeave, ApplicationCommentAction.ALLOWED, person, "");

        when(commentService.create(any(Application.class), any(ApplicationCommentAction.class), any(), any(Person.class)))
            .thenReturn(applicationComment);

        sut.cancel(applicationForLeave, canceller, comment);
        assertThat(applicationForLeave.getStatus()).isEqualTo(ApplicationStatus.REVOKED);
        assertThat(applicationForLeave.getPerson()).isEqualTo(person);
        assertThat(applicationForLeave.getCanceller()).isEqualTo(canceller);
        assertThat(applicationForLeave.getCancelDate()).isEqualTo(LocalDate.now(UTC));
        assertThat(applicationForLeave.isFormerlyAllowed()).isFalse();

        verify(applicationService).save(applicationForLeave);
        verify(commentService).create(applicationForLeave, ApplicationCommentAction.REVOKED, comment, canceller);
        verify(applicationMailService).sendRevokedNotifications(eq(applicationForLeave), any(ApplicationComment.class));
    }

    @Test
    void cancellingNotYetAllowedApplicationAsUser() {

        final Person person = createPerson("muster");
        final Person canceller = createPerson("canceller");

        final Optional<String> comment = of("Foo");

        final Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(WAITING);
        when(applicationService.save(applicationForLeave)).thenReturn(applicationForLeave);

        final ApplicationComment applicationComment = new ApplicationComment(
            1L, Instant.now(clock), applicationForLeave, ApplicationCommentAction.ALLOWED, person, "");

        when(commentService.create(any(Application.class), any(ApplicationCommentAction.class), any(), any(Person.class)))
            .thenReturn(applicationComment);

        sut.cancel(applicationForLeave, canceller, comment);
        assertThat(applicationForLeave.getStatus()).isEqualTo(ApplicationStatus.REVOKED);
        assertThat(applicationForLeave.getPerson()).isEqualTo(person);
        assertThat(applicationForLeave.getCanceller()).isEqualTo(canceller);
        assertThat(applicationForLeave.getCancelDate()).isEqualTo(LocalDate.now(UTC));
        assertThat(applicationForLeave.isFormerlyAllowed()).isFalse();

        verify(applicationService).save(applicationForLeave);
        verify(commentService).create(applicationForLeave, ApplicationCommentAction.REVOKED, comment, canceller);
        verify(applicationMailService).sendRevokedNotifications(applicationForLeave, applicationComment);
    }

    @Test
    void ensureCancellingApplicationForLeaveUpdatesRemainingVacationDaysWithTheYearOfTheStartDateAsStartYear() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Person canceller = new Person("muster", "Muster", "Marlene", "muster@example.org");
        canceller.setPermissions(asList(USER, OFFICE));

        final Optional<String> comment = of("Foo");

        final Application applicationForLeave = new Application();
        applicationForLeave.setPerson(person);
        applicationForLeave.setStatus(ALLOWED);
        applicationForLeave.setStartDate(LocalDate.of(2014, 12, 24));
        applicationForLeave.setEndDate(LocalDate.of(2015, 1, 7));
        applicationForLeave.setDayLength(DayLength.FULL);
        when(applicationService.save(applicationForLeave)).thenReturn(applicationForLeave);

        sut.cancel(applicationForLeave, canceller, comment);

        verify(accountInteractionService).updateRemainingVacationDays(2014, person);
    }

    // DIRECT CANCEL APPLICATION FOR LEAVE -----------------------------------------------------------------------------

    @Test
    void ensureDirectCancelByApplicantChangesStateAndOtherAttributesAndSendsEmail() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);
        final Optional<String> comment = of("Foo");

        final Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(WAITING);
        when(applicationService.save(applicationForLeave)).thenReturn(applicationForLeave);

        final ApplicationComment applicationComment = new ApplicationComment(
            1L, Instant.now(clock), applicationForLeave, ApplicationCommentAction.CANCELLED_DIRECTLY, person, "");

        when(commentService.create(applicationForLeave, ApplicationCommentAction.CANCELLED_DIRECTLY, comment, person)).thenReturn(applicationComment);

        final HolidayReplacementEntity holidayReplacement = new HolidayReplacementEntity();
        applicationForLeave.setHolidayReplacements(List.of(holidayReplacement));

        final Application savedApplication = sut.directCancel(applicationForLeave, person, comment);
        assertThat(savedApplication.getStatus()).isEqualTo(ApplicationStatus.CANCELLED);
        assertThat(savedApplication.getPerson()).isEqualTo(person);
        assertThat(savedApplication.getCanceller()).isEqualTo(person);
        assertThat(savedApplication.getCancelDate()).isEqualTo(LocalDate.now(UTC));

        verify(applicationMailService).sendCancelledDirectlyConfirmationByApplicant(savedApplication, applicationComment);
        verify(applicationMailService).sendCancelledDirectlyToManagement(savedApplication, applicationComment);

        verify(applicationMailService).notifyHolidayReplacementAboutCancellation(holidayReplacement, savedApplication);

        verify(accountInteractionService).updateRemainingVacationDays(savedApplication.getStartDate().getYear(), person);

        ArgumentCaptor<ApplicationCancelledEvent> argumentCaptor = ArgumentCaptor.forClass(ApplicationCancelledEvent.class);
        verify(applicationEventPublisher).publishEvent(argumentCaptor.capture());
        final ApplicationCancelledEvent event = argumentCaptor.getValue();
        assertThat(event.application()).isEqualTo(applicationForLeave);
        assertThat(event.createdAt()).isBeforeOrEqualTo(Instant.now());
        assertThat(event.id()).isNotNull();
    }

    @Test
    void ensureDirectCancelByOfficeChangesStateAndOtherAttributesAndSendsEmail() {

        final Person office = new Person("office", "office", "orlanda", "office@example.org");
        office.setId(1L);
        office.setPermissions(List.of(USER, OFFICE));

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        person.setId(2L);
        final Optional<String> comment = of("Foo");

        final Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(WAITING);
        when(applicationService.save(applicationForLeave)).thenReturn(applicationForLeave);

        final ApplicationComment applicationComment = new ApplicationComment(
            1L, Instant.now(clock), applicationForLeave, ApplicationCommentAction.CANCELLED_DIRECTLY, person, "");

        when(commentService.create(applicationForLeave, ApplicationCommentAction.CANCELLED_DIRECTLY, comment, office)).thenReturn(applicationComment);

        final HolidayReplacementEntity holidayReplacement = new HolidayReplacementEntity();
        applicationForLeave.setHolidayReplacements(List.of(holidayReplacement));

        final Application savedApplication = sut.directCancel(applicationForLeave, office, comment);
        assertThat(savedApplication.getStatus()).isEqualTo(ApplicationStatus.CANCELLED);
        assertThat(savedApplication.getPerson()).isEqualTo(person);
        assertThat(savedApplication.getCanceller()).isEqualTo(office);
        assertThat(savedApplication.getCancelDate()).isEqualTo(LocalDate.now(UTC));

        verify(applicationMailService).sendCancelledDirectlyConfirmationByManagement(savedApplication, applicationComment);
        verify(applicationMailService).sendCancelledDirectlyToManagement(savedApplication, applicationComment);

        verify(applicationMailService).notifyHolidayReplacementAboutCancellation(holidayReplacement, savedApplication);

        verify(accountInteractionService).updateRemainingVacationDays(savedApplication.getStartDate().getYear(), person);
    }

    // decline cancellation request -------------------------------------------------------------------------------------
    @Test
    void declineCancellationRequest() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Person canceller = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final Application applicationForLeave = new Application();
        applicationForLeave.setPerson(person);
        applicationForLeave.setStatus(ALLOWED_CANCELLATION_REQUESTED);
        applicationForLeave.setStartDate(LocalDate.of(2014, 12, 24));
        applicationForLeave.setEndDate(LocalDate.of(2015, 1, 7));
        applicationForLeave.setDayLength(DayLength.FULL);
        when(applicationService.save(applicationForLeave)).thenReturn(applicationForLeave);

        final Optional<String> comment = of("Anfrage kann nicht storniert werden!");
        final ApplicationComment applicationComment = new ApplicationComment(
            1L, Instant.now(clock), applicationForLeave, CANCEL_REQUESTED_DECLINED, person, "");

        when(commentService.create(applicationForLeave, CANCEL_REQUESTED_DECLINED, comment, canceller)).thenReturn(applicationComment);

        final Application application = sut.declineCancellationRequest(applicationForLeave, canceller, comment);
        assertThat(application.getStatus()).isEqualTo(ALLOWED);

        verify(applicationMailService).sendDeclinedCancellationRequestApplicationNotification(application, applicationComment);

        ArgumentCaptor<ApplicationDeclinedCancellationRequestEvent> argumentCaptor = ArgumentCaptor.forClass(ApplicationDeclinedCancellationRequestEvent.class);
        verify(applicationEventPublisher).publishEvent(argumentCaptor.capture());
        final ApplicationDeclinedCancellationRequestEvent event = argumentCaptor.getValue();
        assertThat(event.application()).isEqualTo(applicationForLeave);
        assertThat(event.createdAt()).isBeforeOrEqualTo(Instant.now());
        assertThat(event.id()).isNotNull();
    }

    @Test
    void declineCancellationRequestWrongStatus() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Person canceller = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final Optional<String> comment = of("Anfrage kann nicht storniert werden!");

        final Application applicationForLeave = new Application();
        applicationForLeave.setPerson(person);
        applicationForLeave.setStatus(ALLOWED);
        applicationForLeave.setStartDate(LocalDate.of(2014, 12, 24));
        applicationForLeave.setEndDate(LocalDate.of(2015, 1, 7));
        applicationForLeave.setDayLength(DayLength.FULL);

        assertThatThrownBy(() -> sut.declineCancellationRequest(applicationForLeave, canceller, comment))
            .isInstanceOf(DeclineCancellationRequestedApplicationForLeaveNotAllowedException.class);

        verifyNoMoreInteractions(commentService, applicationMailService, applicationService);
    }

    // CREATE APPLICATION FOR LEAVE FROM CONVERTED SICK NOTE -----------------------------------------------------------
    @Test
    void ensureCreatedApplicationForLeaveFromConvertedSickNoteIsAllowedDirectly() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Person creator = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final Application applicationForLeave = new Application();
        applicationForLeave.setPerson(person);
        applicationForLeave.setStatus(null);
        applicationForLeave.setStartDate(LocalDate.of(2014, 12, 24));
        applicationForLeave.setEndDate(LocalDate.of(2015, 1, 7));
        applicationForLeave.setDayLength(DayLength.FULL);
        when(applicationService.save(applicationForLeave)).thenReturn(applicationForLeave);

        sut.createFromConvertedSickNote(applicationForLeave, creator);

        verify(applicationService).save(applicationForLeave);
        verify(commentService).create(applicationForLeave, CONVERTED, Optional.empty(), creator);
        verify(applicationMailService).sendSickNoteConvertedToVacationNotification(applicationForLeave);

        assertThat(applicationForLeave.getStatus()).isNotNull();
        assertThat(applicationForLeave.getApplier()).isNotNull();
        assertThat(applicationForLeave.getPerson()).isNotNull();

        assertThat(applicationForLeave.getStatus()).isEqualTo(ALLOWED);
        assertThat(applicationForLeave.getApplier()).isEqualTo(creator);
        assertThat(applicationForLeave.getPerson()).isEqualTo(person);

        ArgumentCaptor<ApplicationCreatedFromSickNoteEvent> argumentCaptor = ArgumentCaptor.forClass(ApplicationCreatedFromSickNoteEvent.class);
        verify(applicationEventPublisher).publishEvent(argumentCaptor.capture());
        final ApplicationCreatedFromSickNoteEvent event = argumentCaptor.getValue();
        assertThat(event.application()).isEqualTo(applicationForLeave);
        assertThat(event.createdAt()).isBeforeOrEqualTo(Instant.now());
        assertThat(event.id()).isNotNull();
    }

    // REMIND ----------------------------------------------------------------------------------------------------------
    @Test
    void ensureThrowsIfAlreadySentRemindToday() {

        final Application applicationForLeave = mock(Application.class);
        when(applicationForLeave.getRemindDate()).thenReturn(LocalDate.now(UTC));

        assertThatThrownBy(() -> sut.remind(applicationForLeave)).isInstanceOf(RemindAlreadySentException.class);

        verify(applicationForLeave, never()).setRemindDate(any(LocalDate.class));
        verifyNoInteractions(applicationService);
        verifyNoInteractions(applicationMailService);
    }

    @Test
    void ensureThrowsIfTryingToRemindTooEarly() {

        final Application applicationForLeave = mock(Application.class);
        when(applicationForLeave.getApplicationDate()).thenReturn(LocalDate.now(UTC));
        when(applicationForLeave.getRemindDate()).thenReturn(null);

        assertThatThrownBy(() -> sut.remind(applicationForLeave))
            .isInstanceOf(ImpatientAboutApplicationForLeaveProcessException.class);

        verify(applicationForLeave, never()).setRemindDate(any(LocalDate.class));
        verifyNoInteractions(applicationService);
        verifyNoInteractions(applicationMailService);
    }

    @Test
    void ensureUpdatesRemindDateAndSendsMail() throws RemindAlreadySentException,
        ImpatientAboutApplicationForLeaveProcessException {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Application applicationForLeave = createApplication(person, createVacationType(1L, HOLIDAY, new StaticMessageSource()));
        applicationForLeave.setApplicationDate(LocalDate.now(UTC).minusDays(3));
        applicationForLeave.setRemindDate(LocalDate.now(UTC).minusDays(1));

        sut.remind(applicationForLeave);

        assertThat(applicationForLeave.getRemindDate()).isNotNull();
        assertThat(applicationForLeave.getRemindDate()).isEqualTo(LocalDate.now(UTC));

        verify(applicationService).save(applicationForLeave);
        verify(applicationMailService).sendRemindNotificationToManagement(applicationForLeave);
    }

    // REFER -----------------------------------------------------------------------------------------------------------
    @Test
    void ensureReferMailIsSent() {

        final Person recipient = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Person sender = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final Application applicationForLeave = mock(Application.class);
        sut.refer(applicationForLeave, recipient, sender);

        verify(applicationMailService).sendReferredToManagementNotification(applicationForLeave, recipient, sender);
    }

    @Test
    void ensureReferCommentWasAdded() {

        final Person recipient = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Person sender = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final Application applicationForLeave = mock(Application.class);
        sut.refer(applicationForLeave, recipient, sender);

        verify(commentService).create(applicationForLeave, REFERRED, Optional.of(recipient.getNiceName()), sender);
    }

    // GET -----------------------------------------------------------------------------------------------------------
    @Test
    void getApplicationForLeave() {

        final Long applicationId = 1L;

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Application application = createApplication(person, createVacationType(1L, HOLIDAY, new StaticMessageSource()));
        application.setId(applicationId);

        when(applicationService.getApplicationById(applicationId)).thenReturn(Optional.of(application));

        final Optional<Application> getApplication = sut.get(applicationId);
        assertThat(getApplication).hasValue(application);
    }

    // EDIT -----------------------------------------------------------------------------------------------------------
    @Test
    void ensureToEditApplicationForLeaveForMyself() {

        final Long applicationId = 1L;

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Application application = createApplication(person, createVacationType(1L, HOLIDAY, new StaticMessageSource()));
        application.setStatus(WAITING);
        application.setId(applicationId);
        when(applicationService.save(application)).thenReturn(application);

        final Optional<String> comment = of("Comment");

        final Application editedApplication = sut.edit(application, application, person, comment);
        assertThat(editedApplication.getStatus()).isEqualTo(WAITING);

        verify(commentService).create(editedApplication, EDITED, comment, person);
        verify(applicationMailService).sendEditedNotification(editedApplication, person);
        verifyNoMoreInteractions(applicationMailService);

        ArgumentCaptor<ApplicationUpdatedEvent> argumentCaptor = ArgumentCaptor.forClass(ApplicationUpdatedEvent.class);
        verify(applicationEventPublisher).publishEvent(argumentCaptor.capture());
        final ApplicationUpdatedEvent event = argumentCaptor.getValue();
        assertThat(event.application()).isEqualTo(editedApplication);
        assertThat(event.createdAt()).isBeforeOrEqualTo(Instant.now());
        assertThat(event.id()).isNotNull();
    }

    @Test
    void ensureToEditApplicationForLeaveForAnotherUserWithOfficePermission() {

        final Long applicationId = 1L;

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Application application = createApplication(person, createVacationType(1L, HOLIDAY, new StaticMessageSource()));
        application.setStatus(ALLOWED);
        application.setId(applicationId);
        when(applicationService.save(application)).thenReturn(application);

        final Optional<String> comment = of("Comment");

        final Person office = new Person("muster", "Muster", "Marlene", "muster@example.org");
        office.setPermissions(List.of(USER, OFFICE));
        final Application editedApplication = sut.edit(application, application, office, comment);
        assertThat(editedApplication.getStatus()).isEqualTo(ALLOWED);

        verify(commentService).create(editedApplication, EDITED, comment, office);
        verify(applicationMailService).sendEditedNotification(editedApplication, office);
        verifyNoMoreInteractions(applicationMailService);

        ArgumentCaptor<ApplicationUpdatedEvent> argumentCaptor = ArgumentCaptor.forClass(ApplicationUpdatedEvent.class);
        verify(applicationEventPublisher).publishEvent(argumentCaptor.capture());
        final ApplicationUpdatedEvent event = argumentCaptor.getValue();
        assertThat(event.application()).isEqualTo(editedApplication);
        assertThat(event.createdAt()).isBeforeOrEqualTo(Instant.now());
        assertThat(event.id()).isNotNull();
    }

    @Test
    void editApplicationForLeaveHolidayReplacementAdded() {

        final Long applicationId = 1L;

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final Person newHolidayReplacement = new Person("muster", "Muster", "Marlene", "muster@example.org");
        newHolidayReplacement.setId(1L);

        final HolidayReplacementEntity newReplacementEntity = new HolidayReplacementEntity();
        newReplacementEntity.setPerson(newHolidayReplacement);

        final Application newApplication = createApplication(person, createVacationType(1L, HOLIDAY, new StaticMessageSource()));
        newApplication.setStatus(WAITING);
        newApplication.setId(applicationId);
        newApplication.setHolidayReplacements(List.of(newReplacementEntity));
        when(applicationService.save(newApplication)).thenReturn(newApplication);

        final Application oldApplication = createApplication(person, createVacationType(1L, HOLIDAY, new StaticMessageSource()));

        final Optional<String> comment = of("Comment");

        final Application editedApplication = sut.edit(oldApplication, newApplication, person, comment);
        assertThat(editedApplication.getStatus()).isEqualTo(WAITING);

        verify(commentService).create(editedApplication, EDITED, comment, person);
        verify(applicationMailService).notifyHolidayReplacementForApply(newReplacementEntity, newApplication);
        verify(applicationMailService).sendEditedNotification(editedApplication, person);
        verifyNoMoreInteractions(applicationMailService);
    }

    @Test
    void editApplicationForLeaveHolidayReplacementRemoved() {

        final Long applicationId = 1L;

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final Application newApplication = createApplication(person, createVacationType(1L, HOLIDAY, new StaticMessageSource()));
        newApplication.setStatus(WAITING);
        newApplication.setId(applicationId);
        when(applicationService.save(newApplication)).thenReturn(newApplication);

        final Person oldHolidayReplacement = new Person("muster", "Muster", "Marlene", "muster@example.org");
        oldHolidayReplacement.setId(2L);

        final HolidayReplacementEntity replacementEntity = new HolidayReplacementEntity();
        replacementEntity.setPerson(oldHolidayReplacement);

        final Application oldApplication = createApplication(person, createVacationType(1L, HOLIDAY, new StaticMessageSource()));
        oldApplication.setHolidayReplacements(List.of(replacementEntity));

        final Optional<String> comment = of("Comment");

        final Application editedApplication = sut.edit(oldApplication, newApplication, person, comment);
        assertThat(editedApplication.getStatus()).isEqualTo(WAITING);

        verify(commentService).create(editedApplication, EDITED, comment, person);
        verify(applicationMailService).notifyHolidayReplacementAboutCancellation(replacementEntity, newApplication);
        verify(applicationMailService).sendEditedNotification(editedApplication, person);
        verifyNoMoreInteractions(applicationMailService);
    }

    @Test
    void editApplicationForLeaveHolidayRelevantEntriesChangedFromTo() {

        final Long applicationId = 1L;

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final Person holidayReplacementPerson = new Person("muster", "Muster", "Marlene", "muster@example.org");
        holidayReplacementPerson.setId(2L);

        final HolidayReplacementEntity replacementEntity = new HolidayReplacementEntity();
        replacementEntity.setPerson(holidayReplacementPerson);

        final Application newApplication = createApplication(person, createVacationType(1L, HOLIDAY, new StaticMessageSource()));
        newApplication.setStatus(WAITING);
        newApplication.setId(applicationId);
        newApplication.setStartDate(LocalDate.of(2020, 10, 3));
        newApplication.setEndDate(LocalDate.of(2020, 10, 3));
        newApplication.setHolidayReplacements(List.of(replacementEntity));
        when(applicationService.save(newApplication)).thenReturn(newApplication);

        final Application oldApplication = createApplication(person, createVacationType(1L, HOLIDAY, new StaticMessageSource()));
        oldApplication.setHolidayReplacements(List.of(replacementEntity));
        oldApplication.setStartDate(LocalDate.of(2020, 10, 4));
        oldApplication.setEndDate(LocalDate.of(2020, 10, 4));

        final Optional<String> comment = of("Comment");

        final Application editedApplication = sut.edit(oldApplication, newApplication, person, comment);
        assertThat(editedApplication.getStatus()).isEqualTo(WAITING);

        verify(commentService).create(editedApplication, EDITED, comment, person);
        verify(applicationMailService).notifyHolidayReplacementAboutEdit(replacementEntity, newApplication);
        verify(applicationMailService).sendEditedNotification(editedApplication, person);
        verifyNoMoreInteractions(applicationMailService);
    }

    @Test
    void editApplicationForLeaveHolidayRelevantEntriesChangedDayLength() {

        final Long applicationId = 1L;

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        final Person holidayReplacementPerson = new Person("muster", "Muster", "Marlene", "muster@example.org");
        holidayReplacementPerson.setId(2L);

        final HolidayReplacementEntity replacementEntity = new HolidayReplacementEntity();
        replacementEntity.setPerson(holidayReplacementPerson);

        final Application newApplication = createApplication(person, createVacationType(1L, HOLIDAY, new StaticMessageSource()));
        newApplication.setStatus(WAITING);
        newApplication.setId(applicationId);
        newApplication.setDayLength(DayLength.FULL);
        newApplication.setHolidayReplacements(List.of(replacementEntity));
        when(applicationService.save(newApplication)).thenReturn(newApplication);

        final Application oldApplication = createApplication(person, createVacationType(1L, HOLIDAY, new StaticMessageSource()));
        oldApplication.setHolidayReplacements(List.of(replacementEntity));
        oldApplication.setDayLength(DayLength.NOON);

        final Optional<String> comment = of("Comment");

        final Application editedApplication = sut.edit(oldApplication, newApplication, person, comment);
        assertThat(editedApplication.getStatus()).isEqualTo(WAITING);

        verify(commentService).create(editedApplication, EDITED, comment, person);
        verify(applicationMailService).notifyHolidayReplacementAboutEdit(replacementEntity, newApplication);
        verify(applicationMailService).sendEditedNotification(editedApplication, person);
        verifyNoMoreInteractions(applicationMailService);
    }

    @Test
    void editApplicationForLeaveHasWrongStatusAndIsOwnApplication() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Application application = createApplication(person, createVacationType(1L, HOLIDAY, new StaticMessageSource()));
        application.setStatus(ALLOWED);

        final Optional<String> comment = of("Comment");

        assertThatThrownBy(() -> sut.edit(application, application, person, comment))
            .isInstanceOf(EditApplicationForLeaveNotAllowedException.class);

        verifyNoInteractions(applicationMailService);
        verifyNoInteractions(commentService);
    }

    @Test
    void editApplicationWithDifferentPerson() {

        final Person oldPerson = new Person();
        final Application oldApplication = createApplication(oldPerson, createVacationType(1L, HOLIDAY, new StaticMessageSource()));
        oldApplication.setStatus(WAITING);
        final Person newPerson = new Person();
        final Application newApplication = createApplication(newPerson, createVacationType(1L, HOLIDAY, new StaticMessageSource()));
        newApplication.setStatus(WAITING);

        final Optional<String> comment = of("Comment");

        final Person otherPerson = new Person();
        assertThatThrownBy(() -> sut.edit(oldApplication, newApplication, otherPerson, comment))
            .isInstanceOf(EditApplicationForLeaveNotAllowedException.class);

        verifyNoInteractions(applicationMailService);
        verifyNoInteractions(commentService);
    }

    @Test
    void ensureDeletionOfApplicationAndCommentsOnPersonDeletedEvent() {
        final Person person = new Person();
        final long personId = 42;
        person.setId(personId);

        sut.deleteAllByPerson(new PersonDeletedEvent(person));

        InOrder inOrder = inOrder(commentService, applicationService);
        inOrder.verify(commentService).deleteByApplicationPerson(person);
        inOrder.verify(commentService).deleteCommentAuthor(person);
        inOrder.verify(applicationService).deleteApplicationsByPerson(person);
        inOrder.verify(applicationService).deleteInteractionWithApplications(person);
    }

    @Test
    void ensureApplicationDeletedEventsArePublishedWhenPersonIsDeleted() {

        final Person person = new Person();
        final long personId = 42;
        person.setId(personId);

        final Application application = new Application();
        application.setId(42L);
        when(applicationService.deleteApplicationsByPerson(person)).thenReturn(List.of(application));

        sut.deleteAllByPerson(new PersonDeletedEvent(person));

        ArgumentCaptor<ApplicationDeletedEvent> argumentCaptor = ArgumentCaptor.forClass(ApplicationDeletedEvent.class);
        verify(applicationEventPublisher).publishEvent(argumentCaptor.capture());
        final ApplicationDeletedEvent event = argumentCaptor.getValue();
        assertThat(event.application()).isEqualTo(application);
        assertThat(event.createdAt()).isBeforeOrEqualTo(Instant.now());
        assertThat(event.id()).isNotNull();
    }

    private void assertApplicationForLeaveHasChangedStatus(Application applicationForLeave, ApplicationStatus status,
                                                           Person person, Person privilegedUser) {
        assertThat(applicationForLeave.getStatus()).isEqualTo(status);
        assertThat(applicationForLeave.getPerson()).isEqualTo(person);
        assertThat(applicationForLeave.getBoss()).isEqualTo(privilegedUser);
        assertThat(applicationForLeave.getEditedDate()).isEqualTo(LocalDate.now(UTC));
    }


    private void assertApplicationForLeaveAndCommentAreSaved(Application applicationForLeave, ApplicationCommentAction action,
                                                             Optional<String> optionalComment, Person privilegedUser) {
        verify(applicationService).save(applicationForLeave);
        verify(commentService).create(applicationForLeave, action, optionalComment, privilegedUser);
    }

    private void assertAllowedNotificationIsSent(Application applicationForLeave) {
        verify(applicationMailService).sendAllowedNotification(eq(applicationForLeave), any(ApplicationComment.class));
        verify(applicationMailService, never()).sendTemporaryAllowedNotification(any(Application.class), any(ApplicationComment.class));
    }

    private void assertTemporaryAllowedNotificationIsSent(Application applicationForLeave) {
        verify(applicationMailService).sendTemporaryAllowedNotification(eq(applicationForLeave), any(ApplicationComment.class));
        verify(applicationMailService, never()).sendAllowedNotification(any(Application.class), any(ApplicationComment.class));
    }

    private Application getDummyApplication(Person person) {

        final Person replacementPerson = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final HolidayReplacementEntity replacementEntity = new HolidayReplacementEntity();
        replacementEntity.setPerson(replacementPerson);

        final Application applicationForLeave = new Application();
        applicationForLeave.setPerson(person);
        applicationForLeave.setStartDate(LocalDate.of(2013, 2, 1));
        applicationForLeave.setEndDate(LocalDate.of(2013, 2, 5));
        applicationForLeave.setDayLength(DayLength.FULL);
        applicationForLeave.setHolidayReplacements(List.of(replacementEntity));

        return applicationForLeave;
    }
}
