package org.synyx.urlaubsverwaltung.application.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.synyx.urlaubsverwaltung.absence.Absence;
import org.synyx.urlaubsverwaltung.absence.AbsenceMapping;
import org.synyx.urlaubsverwaltung.absence.AbsenceMappingService;
import org.synyx.urlaubsverwaltung.absence.AbsenceType;
import org.synyx.urlaubsverwaltung.account.service.AccountInteractionService;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationAction;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationComment;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.application.domain.VacationCategory;
import org.synyx.urlaubsverwaltung.calendarintegration.CalendarSyncService;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static java.time.ZoneOffset.UTC;
import static java.util.Arrays.asList;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationAction.CANCELLED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationAction.CANCEL_REQUESTED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationAction.REFERRED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationAction.REVOKED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.Role.USER;
import static org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator.createPerson;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationInteractionServiceImplTest {

    private ApplicationInteractionService sut;

    @Mock
    private ApplicationService applicationService;
    @Mock
    private ApplicationCommentService commentService;
    @Mock
    private AccountInteractionService accountInteractionService;
    @Mock
    private ApplicationMailService applicationMailService;
    @Mock
    private CalendarSyncService calendarSyncService;
    @Mock
    private AbsenceMappingService absenceMappingService;
    @Mock
    private SettingsService settingsService;
    @Mock
    private DepartmentService departmentService;

    @Before
    public void setUp() {

        when(calendarSyncService.addAbsence(any(Absence.class))).thenReturn(of("42"));
        when(settingsService.getSettings()).thenReturn(new Settings());

        sut = new ApplicationInteractionServiceImpl(applicationService, commentService, accountInteractionService,
            applicationMailService, calendarSyncService, absenceMappingService, settingsService, departmentService);
    }


    // APPLY FOR LEAVE -------------------------------------------------------------------------------------------------

    @Test
    public void ensureApplyForLeaveChangesStateAndOtherAttributesAndSavesTheApplicationForLeave() {

        Person person = createPerson("muster");
        Person applier = createPerson("applier");
        Optional<String> comment = of("Foo");

        Application applicationForLeave = getDummyApplication(person);
        when(applicationService.save(applicationForLeave)).thenReturn(applicationForLeave);

        sut.apply(applicationForLeave, applier, comment);

        Assert.assertEquals("Wrong state", WAITING, applicationForLeave.getStatus());
        Assert.assertEquals("Wrong person", person, applicationForLeave.getPerson());
        Assert.assertEquals("Wrong applier", applier, applicationForLeave.getApplier());
        Assert.assertEquals("Wrong application date", LocalDate.now(UTC), applicationForLeave.getApplicationDate());

        verify(applicationService).save(applicationForLeave);

        verify(commentService)
            .create(eq(applicationForLeave), eq(ApplicationAction.APPLIED), eq(comment), eq(applier));
    }


    private Application getDummyApplication(Person person) {

        Application applicationForLeave = new Application();
        applicationForLeave.setPerson(person);
        applicationForLeave.setStartDate(LocalDate.of(2013, 2, 1));
        applicationForLeave.setEndDate(LocalDate.of(2013, 2, 5));
        applicationForLeave.setDayLength(DayLength.FULL);
        applicationForLeave.setHolidayReplacement(createPerson());

        return applicationForLeave;
    }


    @Test
    public void ensureApplyingForLeaveAddsCalendarEvent() {

        Person person = createPerson("muster");
        Person applier = createPerson("applier");
        Optional<String> comment = of("Foo");

        Application applicationForLeave = getDummyApplication(person);
        when(applicationService.save(applicationForLeave)).thenReturn(applicationForLeave);

        sut.apply(applicationForLeave, applier, comment);

        verify(calendarSyncService).addAbsence(any(Absence.class));
        verify(absenceMappingService).create(isNull(), eq(AbsenceType.VACATION), anyString());
    }


    @Test
    public void ensureSendsConfirmationEmailToPersonAndNotificationEmailToBossesWhenApplyingForOneself() {

        Person person = createPerson();

        final Application applicationForLeave = getDummyApplication(person);
        when(applicationService.save(applicationForLeave)).thenReturn(applicationForLeave);

        ApplicationComment applicationComment = new ApplicationComment(person);
        when(commentService.create(eq(applicationForLeave), eq(ApplicationAction.APPLIED), any(), eq(person))).thenReturn(applicationComment);

        sut.apply(applicationForLeave, person, of("Foo"));

        verify(applicationMailService).sendConfirmation(eq(applicationForLeave), eq(applicationComment));
        verify(applicationMailService, never()).sendAppliedForLeaveByOfficeNotification(eq(applicationForLeave), any(ApplicationComment.class));
        verify(applicationMailService).sendNewApplicationNotification(eq(applicationForLeave), eq(applicationComment));
    }


    @Test
    public void ensureSendsNotificationToPersonIfApplicationForLeaveNotAppliedByOneself() {

        Person person = createPerson("muster");
        Person applier = createPerson("applier");

        Application applicationForLeave = getDummyApplication(person);
        when(applicationService.save(applicationForLeave)).thenReturn(applicationForLeave);

        ApplicationComment applicationComment = new ApplicationComment(person);
        when(commentService.create(eq(applicationForLeave), eq(ApplicationAction.APPLIED), any(), eq(applier))).thenReturn(applicationComment);

        sut.apply(applicationForLeave, applier, of("Foo"));

        verify(applicationMailService, never()).sendConfirmation(eq(applicationForLeave), any(ApplicationComment.class));
        verify(applicationMailService).sendAppliedForLeaveByOfficeNotification(eq(applicationForLeave), eq(applicationComment));
        verify(applicationMailService).sendNewApplicationNotification(eq(applicationForLeave), eq(applicationComment));
    }


    @Test
    public void ensureApplyingForLeaveUpdatesTheRemainingVacationDays() {

        final Person person = createPerson("muster");
        final Person applier = createPerson("applier");
        final Optional<String> comment = of("Foo");

        final Application applicationForLeave = getDummyApplication(person);
        when(applicationService.save(applicationForLeave)).thenReturn(applicationForLeave);

        sut.apply(applicationForLeave, applier, comment);

        verify(accountInteractionService).updateRemainingVacationDays(2013, person);
    }

    // ALLOW APPLICATION FOR LEAVE -------------------------------------------------------------------------------------


    // ALLOWING - BOSS
    @Test
    public void ensureWaitingApplicationForLeaveCanBeAllowedByBoss() {

        Person person = createPerson("muster");
        Person boss = createPerson("boss", USER, Role.BOSS);
        Optional<String> comment = of("Foo");

        final Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(WAITING);
        when(applicationService.save(applicationForLeave)).thenReturn(applicationForLeave);

        when(commentService.create(applicationForLeave, ApplicationAction.ALLOWED, comment, boss)).thenReturn(new ApplicationComment(person));

        sut.allow(applicationForLeave, boss, comment);

        assertApplicationForLeaveHasChangedStatus(applicationForLeave, ApplicationStatus.ALLOWED, person, boss);
        assertApplicationForLeaveAndCommentAreSaved(applicationForLeave, ApplicationAction.ALLOWED, comment, boss);
        assertNoCalendarSyncIsExecuted();
        assertAllowedNotificationIsSent(applicationForLeave);
    }


    private void assertApplicationForLeaveHasChangedStatus(Application applicationForLeave, ApplicationStatus status,
                                                           Person person, Person privilegedUser) {

        Assert.assertEquals("Wrong state", status, applicationForLeave.getStatus());
        Assert.assertEquals("Wrong person", person, applicationForLeave.getPerson());
        Assert.assertEquals("Wrong privileged user", privilegedUser, applicationForLeave.getBoss());
        Assert.assertEquals("Wrong edited date", LocalDate.now(UTC), applicationForLeave.getEditedDate());
    }


    private void assertApplicationForLeaveAndCommentAreSaved(Application applicationForLeave, ApplicationAction action,
                                                             Optional<String> optionalComment, Person privilegedUser) {

        verify(applicationService).save(applicationForLeave);
        verify(commentService).create(eq(applicationForLeave), eq(action), eq(optionalComment), eq(privilegedUser));
    }


    private void assertNoCalendarSyncIsExecuted() {

        verifyZeroInteractions(calendarSyncService);
        verifyZeroInteractions(absenceMappingService);
    }


    private void assertAllowedNotificationIsSent(Application applicationForLeave) {

        verify(applicationMailService).sendAllowedNotification(eq(applicationForLeave), any(ApplicationComment.class));
        verify(applicationMailService, never()).sendTemporaryAllowedNotification(any(Application.class), any(ApplicationComment.class));
    }


    @Test
    public void ensureTemporaryAllowedApplicationForLeaveCanBeAllowedByBoss() {

        Person person = createPerson("muster");
        Person boss = createPerson("boss", USER, Role.BOSS);
        Optional<String> comment = of("Foo");

        Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(ApplicationStatus.TEMPORARY_ALLOWED);
        applicationForLeave.setTwoStageApproval(false);
        when(applicationService.save(applicationForLeave)).thenReturn(applicationForLeave);

        final ApplicationComment applicationComment = commentService.create(applicationForLeave, ApplicationAction.ALLOWED, comment, boss);
        when(applicationComment).thenReturn(new ApplicationComment(person));

        sut.allow(applicationForLeave, boss, comment);

        assertApplicationForLeaveHasChangedStatus(applicationForLeave, ApplicationStatus.ALLOWED, person, boss);
        assertApplicationForLeaveAndCommentAreSaved(applicationForLeave, ApplicationAction.ALLOWED, comment, boss);
        assertNoCalendarSyncIsExecuted();
        assertAllowedNotificationIsSent(applicationForLeave);
    }


    @Test
    public void ensureTemporaryAllowedApplicationForLeaveCanBeAllowedByBossEvenWithTwoStageApprovalActive() {

        Person person = createPerson("muster");
        Person boss = createPerson("boss", USER, Role.BOSS);
        Optional<String> comment = of("Foo");

        Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(ApplicationStatus.TEMPORARY_ALLOWED);
        applicationForLeave.setTwoStageApproval(true);
        when(applicationService.save(applicationForLeave)).thenReturn(applicationForLeave);

        AbsenceMapping absenceMapping = TestDataCreator.anyAbsenceMapping();
        when(commentService.create(applicationForLeave, ApplicationAction.ALLOWED, comment, boss)).thenReturn(new ApplicationComment(person));

        sut.allow(applicationForLeave, boss, comment);

        assertApplicationForLeaveHasChangedStatus(applicationForLeave, ApplicationStatus.ALLOWED, person, boss);
        assertApplicationForLeaveAndCommentAreSaved(applicationForLeave, ApplicationAction.ALLOWED, comment, boss);
        assertNoCalendarSyncIsExecuted();
        assertAllowedNotificationIsSent(applicationForLeave);
    }


    @Test
    public void ensureIfAllowedApplicationForLeaveIsAllowedAgainNothingHappens() {

        Person person = createPerson("muster");
        Person boss = createPerson("boss", USER, Role.BOSS);
        Optional<String> comment = of("Foo");

        Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(ApplicationStatus.ALLOWED);

        sut.allow(applicationForLeave, boss, comment);

        Assert.assertEquals("Status should not be changed", ApplicationStatus.ALLOWED, applicationForLeave.getStatus());

        verifyZeroInteractions(applicationService);
        verifyZeroInteractions(commentService);
        verifyZeroInteractions(applicationMailService);
        verifyZeroInteractions(calendarSyncService);
        verifyZeroInteractions(absenceMappingService);
    }


    // ALLOWING - DEPARTMENT HEAD

    @Test(expected = IllegalStateException.class)
    public void ensureThrowsWhenExecutingAllowProcessWithNotPrivilegedUser() {

        Person person = createPerson("muster");
        Person user = createPerson("user");
        user.setPermissions(Collections.singletonList(USER));

        Optional<String> comment = of("Foo");

        Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(WAITING);

        sut.allow(applicationForLeave, user, comment);
    }


    @Test
    public void ensureWaitingApplicationForLeaveCanBeAllowedByDepartmentHead() {

        Person person = createPerson("muster");
        Person departmentHead = createPerson("head", USER, DEPARTMENT_HEAD);
        when(departmentService.isDepartmentHeadOfPerson(eq(departmentHead), eq(person))).thenReturn(true);

        Optional<String> comment = of("Foo");

        final Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(WAITING);
        when(applicationService.save(applicationForLeave)).thenReturn(applicationForLeave);

        when(commentService.create(applicationForLeave, ApplicationAction.ALLOWED, comment, departmentHead)).thenReturn(new ApplicationComment(person));

        sut.allow(applicationForLeave, departmentHead, comment);

        assertApplicationForLeaveHasChangedStatus(applicationForLeave, ApplicationStatus.ALLOWED, person,
            departmentHead);
        assertApplicationForLeaveAndCommentAreSaved(applicationForLeave, ApplicationAction.ALLOWED, comment,
            departmentHead);
        assertNoCalendarSyncIsExecuted();
        assertAllowedNotificationIsSent(applicationForLeave);
    }


    @Test
    public void ensureWaitingApplicationForLeaveCanOnlyBeAllowedTemporaryByDepartmentHeadIfTwoStageApprovalIsActive() {

        Person person = createPerson("muster");
        Person departmentHead = createPerson("head", USER, DEPARTMENT_HEAD);
        when(departmentService.isDepartmentHeadOfPerson(eq(departmentHead), eq(person))).thenReturn(true);

        Optional<String> comment = of("Foo");

        final Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(WAITING);
        applicationForLeave.setTwoStageApproval(true);
        when(applicationService.save(applicationForLeave)).thenReturn(applicationForLeave);

        when(commentService.create(applicationForLeave, ApplicationAction.TEMPORARY_ALLOWED, comment, departmentHead)).thenReturn(new ApplicationComment(person));

        sut.allow(applicationForLeave, departmentHead, comment);

        assertApplicationForLeaveHasChangedStatus(applicationForLeave, ApplicationStatus.TEMPORARY_ALLOWED, person, departmentHead);
        assertApplicationForLeaveAndCommentAreSaved(applicationForLeave, ApplicationAction.TEMPORARY_ALLOWED, comment, departmentHead);
        assertNoCalendarSyncOccurs();
        assertTemporaryAllowedNotificationIsSent(applicationForLeave);
    }


    private void assertNoCalendarSyncOccurs() {

        verifyZeroInteractions(calendarSyncService);
        verifyZeroInteractions(absenceMappingService);
    }


    private void assertTemporaryAllowedNotificationIsSent(Application applicationForLeave) {

        verify(applicationMailService).sendTemporaryAllowedNotification(eq(applicationForLeave), any(ApplicationComment.class));
        verify(applicationMailService, never()).sendAllowedNotification(any(Application.class), any(ApplicationComment.class));
    }


    @Test
    public void ensureIfTemporaryAllowedApplicationForLeaveIsAllowedByDepartmentHeadWithTwoStageApprovalIsActiveNothingHappens() {

        Person person = createPerson("muster");
        Person departmentHead = createPerson("head", USER, DEPARTMENT_HEAD);
        when(departmentService.isDepartmentHeadOfPerson(eq(departmentHead), eq(person))).thenReturn(true);

        Optional<String> comment = of("Foo");

        Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(ApplicationStatus.TEMPORARY_ALLOWED);
        applicationForLeave.setTwoStageApproval(true);

        sut.allow(applicationForLeave, departmentHead, comment);

        Assert.assertEquals("Status should not be changed", ApplicationStatus.TEMPORARY_ALLOWED,
            applicationForLeave.getStatus());

        verifyZeroInteractions(applicationService);
        verifyZeroInteractions(commentService);
        verifyZeroInteractions(applicationMailService);
        verifyZeroInteractions(calendarSyncService);
        verifyZeroInteractions(absenceMappingService);
    }


    @Test
    public void ensureIfTemporaryAllowedApplicationForLeaveIsAllowedByDepartmentHeadWithTwoStageApprovalNotActiveStatusIsChanged() {

        Person person = createPerson("muster");
        Person departmentHead = createPerson("head", USER, DEPARTMENT_HEAD);
        when(departmentService.isDepartmentHeadOfPerson(eq(departmentHead), eq(person))).thenReturn(true);

        Optional<String> comment = of("Foo");

        final Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(ApplicationStatus.TEMPORARY_ALLOWED);
        applicationForLeave.setTwoStageApproval(false);
        when(applicationService.save(applicationForLeave)).thenReturn(applicationForLeave);

        when(commentService.create(any(), any(), any(), any())).thenReturn(new ApplicationComment(person));

        sut.allow(applicationForLeave, departmentHead, comment);

        assertApplicationForLeaveHasChangedStatus(applicationForLeave, ApplicationStatus.ALLOWED, person,
            departmentHead);
        assertApplicationForLeaveAndCommentAreSaved(applicationForLeave, ApplicationAction.ALLOWED, comment,
            departmentHead);
        assertNoCalendarSyncIsExecuted();
        assertAllowedNotificationIsSent(applicationForLeave);
    }


    // ALLOWING - SECOND STAGE AUTHORITY

    @Test
    public void ensureWaitingApplicationForLeaveCanBeAllowedBySecondStageAuthority() {

        Person person = createPerson("muster");
        Person secondStage = createPerson("manager", USER, SECOND_STAGE_AUTHORITY);
        when(departmentService.isSecondStageAuthorityOfPerson(eq(secondStage), eq(person))).thenReturn(true);

        Optional<String> comment = of("Foo");

        Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(WAITING);
        when(applicationService.save(applicationForLeave)).thenReturn(applicationForLeave);

        when(commentService.create(applicationForLeave, ApplicationAction.ALLOWED, comment, secondStage)).thenReturn(new ApplicationComment(person));

        sut.allow(applicationForLeave, secondStage, comment);

        assertApplicationForLeaveHasChangedStatus(applicationForLeave, ApplicationStatus.ALLOWED, person, secondStage);
        assertApplicationForLeaveAndCommentAreSaved(applicationForLeave, ApplicationAction.ALLOWED, comment,
            secondStage);
        assertNoCalendarSyncIsExecuted();
        assertAllowedNotificationIsSent(applicationForLeave);
    }


    @Test
    public void ensureWaitingApplicationForLeaveCanBeAllowedBySecondStageAuthorityIfTwoStageApprovalIsActive() {

        Person person = createPerson("muster");
        Person secondStage = createPerson("manager", USER, SECOND_STAGE_AUTHORITY);
        when(departmentService.isSecondStageAuthorityOfPerson(eq(secondStage), eq(person))).thenReturn(true);

        Optional<String> comment = of("Foo");

        Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(WAITING);
        applicationForLeave.setTwoStageApproval(true);
        when(applicationService.save(applicationForLeave)).thenReturn(applicationForLeave);

        when(commentService.create(applicationForLeave, ApplicationAction.ALLOWED, comment, secondStage)).thenReturn(new ApplicationComment(person));

        sut.allow(applicationForLeave, secondStage, comment);

        assertApplicationForLeaveHasChangedStatus(applicationForLeave, ApplicationStatus.ALLOWED, person, secondStage);
        assertApplicationForLeaveAndCommentAreSaved(applicationForLeave, ApplicationAction.ALLOWED, comment,
            secondStage);
        assertNoCalendarSyncIsExecuted();
        assertAllowedNotificationIsSent(applicationForLeave);
    }


    @Test
    public void ensureTemporaryAllowedApplicationForLeaveCanBeAllowedBySecondStageAuthorityIfTwoStageApprovalIsActive() {

        Person person = createPerson("muster");
        Person secondStage = createPerson("manager", USER, SECOND_STAGE_AUTHORITY);
        when(departmentService.isSecondStageAuthorityOfPerson(eq(secondStage), eq(person))).thenReturn(true);

        Optional<String> comment = of("Foo");

        Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(ApplicationStatus.TEMPORARY_ALLOWED);
        applicationForLeave.setTwoStageApproval(true);
        when(applicationService.save(applicationForLeave)).thenReturn(applicationForLeave);

        final ApplicationComment applicationComment = commentService.create(applicationForLeave, ApplicationAction.ALLOWED, comment, secondStage);
        when(applicationComment).thenReturn(new ApplicationComment(person));

        sut.allow(applicationForLeave, secondStage, comment);

        assertApplicationForLeaveHasChangedStatus(applicationForLeave, ApplicationStatus.ALLOWED, person, secondStage);
        assertApplicationForLeaveAndCommentAreSaved(applicationForLeave, ApplicationAction.ALLOWED, comment,
            secondStage);
        assertAllowedNotificationIsSent(applicationForLeave);
        verifyZeroInteractions(calendarSyncService);
    }

    @Test
    public void ensureDepartmentHeadCanBeAllowedBySecondStageAuthority() {

        Person departmentHead = createPerson("departmentHead");
        departmentHead.setPermissions(asList(USER, DEPARTMENT_HEAD));

        Person secondStageAuthority = createPerson("secondStageAuthority");
        secondStageAuthority.setPermissions(asList(USER, SECOND_STAGE_AUTHORITY));

        final boolean isSecondStage = departmentService.isSecondStageAuthorityOfPerson(eq(secondStageAuthority), eq(departmentHead));
        when(isSecondStage).thenReturn(true);

        final Application applicationForLeave = getDummyApplication(departmentHead);
        applicationForLeave.setStatus(WAITING);
        when(applicationService.save(applicationForLeave)).thenReturn(applicationForLeave);

        Optional<String> comment = of("Foo");
        when(commentService.create(applicationForLeave, ApplicationAction.ALLOWED, comment, secondStageAuthority)).thenReturn(new ApplicationComment(departmentHead));

        sut.allow(applicationForLeave, secondStageAuthority, comment);
        assertApplicationForLeaveHasChangedStatus(applicationForLeave, ApplicationStatus.ALLOWED, departmentHead, secondStageAuthority);
        assertApplicationForLeaveAndCommentAreSaved(applicationForLeave, ApplicationAction.ALLOWED, comment, secondStageAuthority);
        assertAllowedNotificationIsSent(applicationForLeave);
        verifyZeroInteractions(calendarSyncService);
    }


    @Test(expected = IllegalStateException.class)
    public void ensureSecondStageAuthorityCanNotBeAllowedByDepartmentHead() {

        Person departmentHead = createPerson("departmentHead");
        departmentHead.setPermissions(asList(USER, DEPARTMENT_HEAD));

        Person secondStageAuthority = createPerson("secondStageAuthority");
        secondStageAuthority.setPermissions(asList(USER, SECOND_STAGE_AUTHORITY));

        when(departmentService.isDepartmentHeadOfPerson(eq(departmentHead), eq(secondStageAuthority))).thenReturn(true);

        Optional<String> comment = of("Foo");

        Application applicationForLeave = getDummyApplication(secondStageAuthority);
        applicationForLeave.setStatus(WAITING);
        applicationForLeave.setTwoStageApproval(true);

        sut.allow(applicationForLeave, departmentHead, comment);
    }

    @Test(expected = IllegalStateException.class)
    public void ensureSecondStageAuthorityCanNotAllowHimself() {

        Person secondStageAuthority = createPerson("secondStageAuthority");
        secondStageAuthority.setPermissions(asList(USER, SECOND_STAGE_AUTHORITY));

        when(departmentService.isSecondStageAuthorityOfPerson(eq(secondStageAuthority), eq(secondStageAuthority))).thenReturn(true);

        Optional<String> comment = of("Foo");

        Application applicationForLeave = getDummyApplication(secondStageAuthority);
        applicationForLeave.setStatus(WAITING);

        sut.allow(applicationForLeave, secondStageAuthority, comment);
    }


    @Test(expected = IllegalStateException.class)
    public void ensureDepartmentHeadCanNotAllowHimself() {

        Person departmentHead = createPerson("departmentHead");
        departmentHead.setPermissions(asList(USER, DEPARTMENT_HEAD));

        when(departmentService.isDepartmentHeadOfPerson(eq(departmentHead), eq(departmentHead))).thenReturn(true);

        Optional<String> comment = of("Foo");

        Application applicationForLeave = getDummyApplication(departmentHead);
        applicationForLeave.setStatus(WAITING);

        sut.allow(applicationForLeave, departmentHead, comment);
    }

    // ALLOWING - HOLIDAY REPLACEMENT NOTIFICATION

    @Test
    public void ensureAllowingApplicationForLeaveWithHolidayReplacementSendsNotificationToReplacement() {

        Person person = createPerson("muster");
        Person replacement = createPerson("replacement");
        Person boss = createPerson("boss", USER, Role.BOSS);

        Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(WAITING);
        applicationForLeave.setHolidayReplacement(replacement);
        when(applicationService.save(applicationForLeave)).thenReturn(applicationForLeave);

        sut.allow(applicationForLeave, boss, of("Foo"));

        verify(applicationMailService).notifyHolidayReplacement(eq(applicationForLeave));
    }


    @Test
    public void ensureAllowingApplicationForLeaveWithoutHolidayReplacementDoesNotSendNotification() {

        Person person = createPerson("muster");
        Person boss = createPerson("boss", USER, Role.BOSS);

        Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(WAITING);
        applicationForLeave.setHolidayReplacement(null);
        when(applicationService.save(applicationForLeave)).thenReturn(applicationForLeave);

        sut.allow(applicationForLeave, boss, of("Foo"));

        verify(applicationMailService, never()).notifyHolidayReplacement(any(Application.class));
    }


    @Test
    public void ensureTemporaryAllowingApplicationForLeaveWithHolidayReplacementDoesNotSendNotification() {

        Person person = createPerson("muster");
        Person replacement = createPerson("replacement");
        Person departmentHead = createPerson("head", USER, DEPARTMENT_HEAD);
        when(departmentService.isDepartmentHeadOfPerson(eq(departmentHead), eq(person))).thenReturn(true);

        Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(WAITING);
        applicationForLeave.setHolidayReplacement(replacement);
        applicationForLeave.setTwoStageApproval(true);

        sut.allow(applicationForLeave, departmentHead, of("Foo"));

        verify(applicationMailService, never()).notifyHolidayReplacement(any(Application.class));
    }


    // REJECT APPLICATION FOR LEAVE ------------------------------------------------------------------------------------

    @Test
    public void ensureRejectingApplicationForLeaveChangesStateAndOtherAttributesAndSavesTheApplicationForLeave() {

        Person person = createPerson("muster");
        Person boss = createPerson("boss");

        Optional<String> comment = of("Foo");

        final Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(WAITING);
        when(applicationService.save(applicationForLeave)).thenReturn(applicationForLeave);

        sut.reject(applicationForLeave, boss, comment);

        Assert.assertEquals("Wrong state", ApplicationStatus.REJECTED, applicationForLeave.getStatus());
        Assert.assertEquals("Wrong person", person, applicationForLeave.getPerson());
        Assert.assertEquals("Wrong boss", boss, applicationForLeave.getBoss());
        Assert.assertEquals("Wrong edited date", LocalDate.now(UTC), applicationForLeave.getEditedDate());

        verify(applicationService).save(applicationForLeave);

        verify(commentService)
            .create(eq(applicationForLeave), eq(ApplicationAction.REJECTED), eq(comment), eq(boss));
    }


    @Test
    public void ensureRejectingApplicationForLeaveDeletesCalendarEvent() {

        Person person = createPerson("muster");
        Person boss = createPerson("boss");

        Optional<String> comment = of("Foo");

        Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(WAITING);
        when(applicationService.save(applicationForLeave)).thenReturn(applicationForLeave);

        AbsenceMapping absenceMapping = TestDataCreator.anyAbsenceMapping();
        final Optional<AbsenceMapping> absenceByIdAndType = absenceMappingService.getAbsenceByIdAndType(isNull(), eq(AbsenceType.VACATION));
        when(absenceByIdAndType).thenReturn(of(absenceMapping));

        sut.reject(applicationForLeave, boss, comment);

        verify(calendarSyncService).deleteAbsence(absenceMapping.getEventId());
        verify(absenceMappingService).delete(absenceMapping);
    }


    @Test
    public void ensureRejectingApplicationForLeaveSendsEmailToPerson() {

        Person person = createPerson("muster");
        Person boss = createPerson("boss");

        final Application applicationForLeave = getDummyApplication(person);
        when(applicationService.save(applicationForLeave)).thenReturn(applicationForLeave);

        Optional<String> optionalComment = of("Foo");
        ApplicationComment applicationComment = new ApplicationComment(person);

        when(commentService.create(applicationForLeave, ApplicationAction.REJECTED, optionalComment, boss)).thenReturn(applicationComment);

        sut.reject(applicationForLeave, boss, optionalComment);

        verify(applicationMailService).sendRejectedNotification(eq(applicationForLeave), eq(applicationComment));
    }


    // CANCEL APPLICATION FOR LEAVE ------------------------------------------------------------------------------------

    @Test
    public void ensureCancelledNotYetAllowedApplicationForLeaveChangesStateAndOtherAttributesAndSendsEmail() {

        final Person person = createPerson();
        final Optional<String> comment = of("Foo");

        final Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(WAITING);
        when(applicationService.save(applicationForLeave)).thenReturn(applicationForLeave);

        final ApplicationComment applicationComment = new ApplicationComment(person);
        when(commentService.create(applicationForLeave, REVOKED, comment, person)).thenReturn(applicationComment);

        sut.cancel(applicationForLeave, person, comment);
        assertThat(applicationForLeave.getStatus()).isEqualTo(ApplicationStatus.REVOKED);
        assertThat(applicationForLeave.getPerson()).isEqualTo(person);
        assertThat(applicationForLeave.getCanceller()).isEqualTo(person);
        assertThat(applicationForLeave.getCancelDate()).isEqualTo(LocalDate.now(UTC));
        assertThat(applicationForLeave.isFormerlyAllowed()).isFalse();

        verify(applicationMailService).sendRevokedNotifications(applicationForLeave, applicationComment);
    }


    @Test
    public void ensureCancellingApplicationForLeaveDeletesCalendarEvent() {

        final Person person = createPerson("muster");
        final Person canceller = createPerson("canceller");

        final Optional<String> comment = of("Foo");

        final Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(WAITING);

        final AbsenceMapping absenceMapping = TestDataCreator.anyAbsenceMapping();
        when(absenceMappingService.getAbsenceByIdAndType(null, AbsenceType.VACATION)).thenReturn(of(absenceMapping));

        sut.cancel(applicationForLeave, canceller, comment);

        verify(calendarSyncService).deleteAbsence(anyString());
        verify(absenceMappingService).delete(any(AbsenceMapping.class));
    }


    @Test
    public void ensureCancellingAllowedApplicationByOwnerCreatesACancellationRequest() {

        final Person person = createPerson("muster");
        final Optional<String> comment = of("Foo");

        final Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(ApplicationStatus.ALLOWED);
        when(applicationService.save(applicationForLeave)).thenReturn(applicationForLeave);

        when(commentService.create(any(Application.class), any(ApplicationAction.class), any(), any(Person.class)))
            .thenReturn(new ApplicationComment(person));

        sut.cancel(applicationForLeave, person, comment);

        verify(applicationService).save(applicationForLeave);
        verify(commentService).create(eq(applicationForLeave), eq(CANCEL_REQUESTED), eq(comment), eq(person));
        verify(applicationMailService).sendCancellationRequest(eq(applicationForLeave), any(ApplicationComment.class));
    }


    @Test
    public void ensureCancellingAllowedApplicationByOwnerThatIsOfficeCancelsTheApplicationForLeaveDirectly() {

        final Person person = createPerson("muster");
        person.setPermissions(asList(USER, OFFICE));

        final Optional<String> comment = of("Foo");

        final Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(ApplicationStatus.ALLOWED);
        when(applicationService.save(applicationForLeave)).thenReturn(applicationForLeave);

        final ApplicationComment applicationComment = new ApplicationComment(person);
        when(commentService.create(applicationForLeave, CANCELLED, comment, person)).thenReturn(applicationComment);

        sut.cancel(applicationForLeave, person, comment);

        assertThat(applicationForLeave.getStatus()).isEqualTo(ApplicationStatus.CANCELLED);
        assertThat(applicationForLeave.getPerson()).isEqualTo(person);
        assertThat(applicationForLeave.getCanceller()).isEqualTo(person);
        assertThat(applicationForLeave.getCancelDate()).isEqualTo(LocalDate.now(UTC));
        assertThat(applicationForLeave.isFormerlyAllowed()).isTrue();

        verify(applicationMailService).sendCancelledByOfficeNotification(applicationForLeave, applicationComment);
    }


    @Test
    public void ensureCancellingAllowedApplicationForLeaveOnBehalfForSomeOneChangesStateAndOtherAttributesAndSendsAnEmail() {

        final Person person = createPerson("muster");
        final Person canceller = createPerson("canceller");
        canceller.setPermissions(asList(USER, OFFICE));

        final Optional<String> comment = of("Foo");

        final Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(ApplicationStatus.ALLOWED);
        when(applicationService.save(applicationForLeave)).thenReturn(applicationForLeave);

        when(commentService.create(any(Application.class), any(ApplicationAction.class), any(), any(Person.class)))
            .thenReturn(new ApplicationComment(person));

        sut.cancel(applicationForLeave, canceller, comment);

        assertThat(applicationForLeave.getStatus()).isEqualTo(ApplicationStatus.CANCELLED);
        assertThat(applicationForLeave.getPerson()).isEqualTo(person);
        assertThat(applicationForLeave.getCanceller()).isEqualTo(canceller);
        assertThat(applicationForLeave.getCancelDate()).isEqualTo(LocalDate.now(UTC));
        assertThat(applicationForLeave.isFormerlyAllowed()).isTrue();

        verify(applicationService).save(applicationForLeave);
        verify(commentService).create(eq(applicationForLeave), eq(CANCELLED), eq(comment), eq(canceller));
        verify(applicationMailService).sendCancelledByOfficeNotification(eq(applicationForLeave), any(ApplicationComment.class));
    }

    @Test
    public void ensureCancellingApplicationForLeaveUpdatesRemainingVacationDaysWithTheYearOfTheStartDateAsStartYear() {

        final Person person = createPerson("muster");
        final Person canceller = createPerson("canceller");
        canceller.setPermissions(asList(USER, OFFICE));

        final Optional<String> comment = of("Foo");

        final Application applicationForLeave = new Application();
        applicationForLeave.setPerson(person);
        applicationForLeave.setStatus(ApplicationStatus.ALLOWED);
        applicationForLeave.setStartDate(LocalDate.of(2014, 12, 24));
        applicationForLeave.setEndDate(LocalDate.of(2015, 1, 7));
        applicationForLeave.setDayLength(DayLength.FULL);
        when(applicationService.save(applicationForLeave)).thenReturn(applicationForLeave);

        sut.cancel(applicationForLeave, canceller, comment);

        verify(accountInteractionService).updateRemainingVacationDays(2014, person);
    }


    // CREATE APPLICATION FOR LEAVE FROM CONVERTED SICK NOTE -----------------------------------------------------------

    @Test
    public void ensureCreatedApplicationForLeaveFromConvertedSickNoteIsAllowedDirectly() {

        Person person = createPerson("muster");
        Person creator = createPerson("creator");

        Application applicationForLeave = new Application();
        applicationForLeave.setPerson(person);
        applicationForLeave.setStatus(null);
        applicationForLeave.setStartDate(LocalDate.of(2014, 12, 24));
        applicationForLeave.setEndDate(LocalDate.of(2015, 1, 7));
        applicationForLeave.setDayLength(DayLength.FULL);
        when(applicationService.save(applicationForLeave)).thenReturn(applicationForLeave);

        sut.createFromConvertedSickNote(applicationForLeave, creator);

        verify(applicationService).save(applicationForLeave);
        verify(commentService)
            .create(eq(applicationForLeave), eq(ApplicationAction.CONVERTED), eq(Optional.empty()),
                eq(creator));
        verify(applicationMailService).sendSickNoteConvertedToVacationNotification(eq(applicationForLeave));

        Assert.assertNotNull("Status should be set", applicationForLeave.getStatus());
        Assert.assertNotNull("Applier should be set", applicationForLeave.getApplier());
        Assert.assertNotNull("Person should be set", applicationForLeave.getPerson());

        Assert.assertEquals("Wrong status", ApplicationStatus.ALLOWED, applicationForLeave.getStatus());
        Assert.assertEquals("Wrong applier", creator, applicationForLeave.getApplier());
        Assert.assertEquals("Wrong person", person, applicationForLeave.getPerson());
    }


    // REMIND ----------------------------------------------------------------------------------------------------------

    @Test(expected = RemindAlreadySentException.class)
    public void ensureThrowsIfAlreadySentRemindToday() throws RemindAlreadySentException,
        ImpatientAboutApplicationForLeaveProcessException {

        Application applicationForLeave = mock(Application.class);
        when(applicationForLeave.getRemindDate()).thenReturn(LocalDate.now(UTC));

        sut.remind(applicationForLeave);

        verify(applicationForLeave, never()).setRemindDate(any(LocalDate.class));
        verifyZeroInteractions(applicationService);
        verifyZeroInteractions(applicationMailService);
    }


    @Test(expected = ImpatientAboutApplicationForLeaveProcessException.class)
    public void ensureThrowsIfTryingToRemindTooEarly() throws RemindAlreadySentException,
        ImpatientAboutApplicationForLeaveProcessException {

        Application applicationForLeave = mock(Application.class);
        when(applicationForLeave.getApplicationDate()).thenReturn(LocalDate.now(UTC));
        when(applicationForLeave.getRemindDate()).thenReturn(null);

        sut.remind(applicationForLeave);

        verify(applicationForLeave, never()).setRemindDate(any(LocalDate.class));
        verifyZeroInteractions(applicationService);
        verifyZeroInteractions(applicationMailService);
    }


    @Test
    public void ensureUpdatesRemindDateAndSendsMail() throws RemindAlreadySentException,
        ImpatientAboutApplicationForLeaveProcessException {

        Person person = createPerson();
        Application applicationForLeave = TestDataCreator.createApplication(person,
            TestDataCreator.createVacationType(VacationCategory.HOLIDAY));
        applicationForLeave.setApplicationDate(LocalDate.now(UTC).minusDays(3));
        applicationForLeave.setRemindDate(LocalDate.now(UTC).minusDays(1));

        sut.remind(applicationForLeave);

        Assert.assertNotNull("Remind date should be set", applicationForLeave.getRemindDate());
        Assert.assertEquals("Wrong remind date", LocalDate.now(UTC), applicationForLeave.getRemindDate());

        verify(applicationService).save(eq(applicationForLeave));
        verify(applicationMailService).sendRemindBossNotification(eq(applicationForLeave));
    }


    // REFER -----------------------------------------------------------------------------------------------------------

    @Test
    public void ensureReferMailIsSent() {

        Person recipient = createPerson("recipient");
        Person sender = createPerson("sender");

        Application applicationForLeave = mock(Application.class);
        sut.refer(applicationForLeave, recipient, sender);

        verify(applicationMailService).sendReferApplicationNotification(applicationForLeave, recipient, sender);
    }

    @Test
    public void ensureReferCommentWasAdded() {

        final Person recipient = createPerson("recipient");
        final Person sender = createPerson("sender");

        final Application applicationForLeave = mock(Application.class);
        sut.refer(applicationForLeave, recipient, sender);

        verify(commentService).create(applicationForLeave, REFERRED, Optional.of(recipient.getNiceName()), sender);
    }
}
