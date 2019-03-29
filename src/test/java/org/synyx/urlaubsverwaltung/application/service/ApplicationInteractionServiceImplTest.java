package org.synyx.urlaubsverwaltung.application.service;

import org.joda.time.DateMidnight;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.synyx.urlaubsverwaltung.account.service.AccountInteractionService;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationAction;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationComment;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.application.domain.VacationCategory;
import org.synyx.urlaubsverwaltung.application.service.exception.ImpatientAboutApplicationForLeaveProcessException;
import org.synyx.urlaubsverwaltung.application.service.exception.RemindAlreadySentException;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.mail.MailService;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.sync.CalendarSyncService;
import org.synyx.urlaubsverwaltung.sync.absence.Absence;
import org.synyx.urlaubsverwaltung.sync.absence.AbsenceMapping;
import org.synyx.urlaubsverwaltung.sync.absence.AbsenceMappingService;
import org.synyx.urlaubsverwaltung.sync.absence.AbsenceType;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static java.util.Optional.of;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;


public class ApplicationInteractionServiceImplTest {

    private ApplicationInteractionService service;

    private ApplicationService applicationService;
    private ApplicationCommentService commentService;
    private AccountInteractionService accountInteractionService;
    private MailService mailService;
    private CalendarSyncService calendarSyncService;
    private AbsenceMappingService absenceMappingService;
    private SettingsService settingsService;
    private DepartmentService departmentService;

    @Before
    public void setUp() {

        applicationService = mock(ApplicationService.class);
        commentService = mock(ApplicationCommentService.class);
        accountInteractionService = mock(AccountInteractionService.class);
        mailService = mock(MailService.class);
        calendarSyncService = mock(CalendarSyncService.class);
        absenceMappingService = mock(AbsenceMappingService.class);
        settingsService = mock(SettingsService.class);
        departmentService = mock(DepartmentService.class);

        when(calendarSyncService.addAbsence(any(Absence.class))).thenReturn(of("42"));
        when(absenceMappingService.getAbsenceByIdAndType(anyInt(), eq(AbsenceType.VACATION)))
            .thenReturn(of(new AbsenceMapping(1, AbsenceType.VACATION, "42")));
        when(settingsService.getSettings()).thenReturn(new Settings());

        service = new ApplicationInteractionServiceImpl(applicationService, commentService, accountInteractionService, mailService, calendarSyncService, absenceMappingService, settingsService,
                departmentService);
    }


    // APPLY FOR LEAVE -------------------------------------------------------------------------------------------------

    @Test
    public void ensureApplyForLeaveChangesStateAndOtherAttributesAndSavesTheApplicationForLeave() {

        Person person = TestDataCreator.createPerson("muster");
        Person applier = TestDataCreator.createPerson("applier");
        Optional<String> comment = of("Foo");

        Application applicationForLeave = getDummyApplication(person);

        service.apply(applicationForLeave, applier, comment);

        Assert.assertEquals("Wrong state", ApplicationStatus.WAITING, applicationForLeave.getStatus());
        Assert.assertEquals("Wrong person", person, applicationForLeave.getPerson());
        Assert.assertEquals("Wrong applier", applier, applicationForLeave.getApplier());
        Assert.assertEquals("Wrong application date", DateMidnight.now(), applicationForLeave.getApplicationDate());

        verify(applicationService).save(applicationForLeave);

        verify(commentService)
            .create(eq(applicationForLeave), eq(ApplicationAction.APPLIED), eq(comment), eq(applier));
    }


    private Application getDummyApplication(Person person) {

        Application applicationForLeave = new Application();
        applicationForLeave.setPerson(person);
        applicationForLeave.setStartDate(new DateMidnight(2013, 2, 1));
        applicationForLeave.setEndDate(new DateMidnight(2013, 2, 5));
        applicationForLeave.setDayLength(DayLength.FULL);
        applicationForLeave.setHolidayReplacement(TestDataCreator.createPerson());

        return applicationForLeave;
    }


    @Test
    public void ensureApplyingForLeaveAddsCalendarEvent() {

        Person person = TestDataCreator.createPerson("muster");
        Person applier = TestDataCreator.createPerson("applier");
        Optional<String> comment = of("Foo");

        Application applicationForLeave = getDummyApplication(person);

        service.apply(applicationForLeave, applier, comment);

        verify(calendarSyncService).addAbsence(any(Absence.class));
        verify(absenceMappingService).create(isNull(), eq(AbsenceType.VACATION), anyString());
    }


    @Test
    public void ensureSendsConfirmationEmailToPersonAndNotificationEmailToBossesWhenApplyingForOneself() {

        Person person = TestDataCreator.createPerson();

        Application applicationForLeave = getDummyApplication(person);

        ApplicationComment applicationComment = new ApplicationComment(person);
        when(commentService.create(eq(applicationForLeave), eq(ApplicationAction.APPLIED), any(), eq(person))).thenReturn(applicationComment);

        service.apply(applicationForLeave, person, of("Foo"));

        verify(mailService).sendConfirmation(eq(applicationForLeave), eq(applicationComment));
        verify(mailService, never())
            .sendAppliedForLeaveByOfficeNotification(eq(applicationForLeave), any(ApplicationComment.class));

        verify(mailService)
            .sendNewApplicationNotification(eq(applicationForLeave), eq(applicationComment));
    }


    @Test
    public void ensureSendsNotificationToPersonIfApplicationForLeaveNotAppliedByOneself() {

        Person person = TestDataCreator.createPerson("muster");
        Person applier = TestDataCreator.createPerson("applier");

        Application applicationForLeave = getDummyApplication(person);

        ApplicationComment applicationComment = new ApplicationComment(person);
        when(commentService.create(eq(applicationForLeave), eq(ApplicationAction.APPLIED), any(), eq(applier))).thenReturn(applicationComment);

        service.apply(applicationForLeave, applier, of("Foo"));

        verify(mailService, never())
            .sendConfirmation(eq(applicationForLeave), any(ApplicationComment.class));
        verify(mailService)
            .sendAppliedForLeaveByOfficeNotification(eq(applicationForLeave), eq(applicationComment));

        verify(mailService)
            .sendNewApplicationNotification(eq(applicationForLeave), eq(applicationComment));
    }


    @Test
    public void ensureApplyingForLeaveUpdatesTheRemainingVacationDays() {

        Person person = TestDataCreator.createPerson("muster");
        Person applier = TestDataCreator.createPerson("applier");
        Optional<String> comment = of("Foo");

        Application applicationForLeave = getDummyApplication(person);

        service.apply(applicationForLeave, applier, comment);

        verify(accountInteractionService).updateRemainingVacationDays(2013, person);
    }

    // ALLOW APPLICATION FOR LEAVE -------------------------------------------------------------------------------------


    // ALLOWING - BOSS
    @Test
    public void ensureWaitingApplicationForLeaveCanBeAllowedByBoss() {

        Person person = TestDataCreator.createPerson("muster");
        Person boss = TestDataCreator.createPerson("boss", Role.USER, Role.BOSS);
        Optional<String> comment = of("Foo");

        Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(ApplicationStatus.WAITING);

        AbsenceMapping absenceMapping = TestDataCreator.anyAbsenceMapping();
        when(absenceMappingService.getAbsenceByIdAndType(isNull(), eq(AbsenceType.VACATION))).thenReturn(of(absenceMapping));
        when(commentService.create(applicationForLeave, ApplicationAction.ALLOWED, comment, boss)).thenReturn(new ApplicationComment(person));

        service.allow(applicationForLeave, boss, comment);

        assertApplicationForLeaveHasChangedStatus(applicationForLeave, ApplicationStatus.ALLOWED, person, boss);
        assertApplicationForLeaveAndCommentAreSaved(applicationForLeave, ApplicationAction.ALLOWED, comment, boss);
        assertCalendarSyncIsExecuted();
        assertAllowedNotificationIsSent(applicationForLeave);
    }


    private void assertApplicationForLeaveHasChangedStatus(Application applicationForLeave, ApplicationStatus status,
        Person person, Person privilegedUser) {

        Assert.assertEquals("Wrong state", status, applicationForLeave.getStatus());
        Assert.assertEquals("Wrong person", person, applicationForLeave.getPerson());
        Assert.assertEquals("Wrong privileged user", privilegedUser, applicationForLeave.getBoss());
        Assert.assertEquals("Wrong edited date", DateMidnight.now(), applicationForLeave.getEditedDate());
    }


    private void assertApplicationForLeaveAndCommentAreSaved(Application applicationForLeave, ApplicationAction action,
        Optional<String> optionalComment, Person privilegedUser) {

        verify(applicationService).save(applicationForLeave);

        verify(commentService)
            .create(eq(applicationForLeave), eq(action), eq(optionalComment), eq(privilegedUser));
    }


    private void assertCalendarSyncIsExecuted() {

        verify(calendarSyncService).update(any(Absence.class), anyString());
        verify(absenceMappingService).getAbsenceByIdAndType(isNull(), eq(AbsenceType.VACATION));
    }


    private void assertAllowedNotificationIsSent(Application applicationForLeave) {

        verify(mailService).sendAllowedNotification(eq(applicationForLeave), any(ApplicationComment.class));

        verify(mailService, never())
            .sendTemporaryAllowedNotification(any(Application.class), any(ApplicationComment.class));
    }


    @Test
    public void ensureTemporaryAllowedApplicationForLeaveCanBeAllowedByBoss() {

        Person person = TestDataCreator.createPerson("muster");
        Person boss = TestDataCreator.createPerson("boss", Role.USER, Role.BOSS);
        Optional<String> comment = of("Foo");

        Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(ApplicationStatus.TEMPORARY_ALLOWED);
        applicationForLeave.setTwoStageApproval(false);

        AbsenceMapping absenceMapping = TestDataCreator.anyAbsenceMapping();
        when(absenceMappingService.getAbsenceByIdAndType(isNull(), eq(AbsenceType.VACATION))).thenReturn(of(absenceMapping));
        when(commentService.create(applicationForLeave, ApplicationAction.ALLOWED, comment, boss)).thenReturn(new ApplicationComment(person));

        service.allow(applicationForLeave, boss, comment);

        assertApplicationForLeaveHasChangedStatus(applicationForLeave, ApplicationStatus.ALLOWED, person, boss);
        assertApplicationForLeaveAndCommentAreSaved(applicationForLeave, ApplicationAction.ALLOWED, comment, boss);
        assertCalendarSyncIsExecuted();
        assertAllowedNotificationIsSent(applicationForLeave);
    }


    @Test
    public void ensureTemporaryAllowedApplicationForLeaveCanBeAllowedByBossEvenWithTwoStageApprovalActive() {

        Person person = TestDataCreator.createPerson("muster");
        Person boss = TestDataCreator.createPerson("boss", Role.USER, Role.BOSS);
        Optional<String> comment = of("Foo");

        Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(ApplicationStatus.TEMPORARY_ALLOWED);
        applicationForLeave.setTwoStageApproval(true);

        AbsenceMapping absenceMapping = TestDataCreator.anyAbsenceMapping();
        when(absenceMappingService.getAbsenceByIdAndType(isNull(), eq(AbsenceType.VACATION))).thenReturn(of(absenceMapping));
        when(commentService.create(applicationForLeave, ApplicationAction.ALLOWED, comment, boss)).thenReturn(new ApplicationComment(person));

        service.allow(applicationForLeave, boss, comment);

        assertApplicationForLeaveHasChangedStatus(applicationForLeave, ApplicationStatus.ALLOWED, person, boss);
        assertApplicationForLeaveAndCommentAreSaved(applicationForLeave, ApplicationAction.ALLOWED, comment, boss);
        assertCalendarSyncIsExecuted();
        assertAllowedNotificationIsSent(applicationForLeave);
    }


    @Test
    public void ensureIfAllowedApplicationForLeaveIsAllowedAgainNothingHappens() {

        Person person = TestDataCreator.createPerson("muster");
        Person boss = TestDataCreator.createPerson("boss", Role.USER, Role.BOSS);
        Optional<String> comment = of("Foo");

        Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(ApplicationStatus.ALLOWED);

        service.allow(applicationForLeave, boss, comment);

        Assert.assertEquals("Status should not be changed", ApplicationStatus.ALLOWED, applicationForLeave.getStatus());

        verifyZeroInteractions(applicationService);
        verifyZeroInteractions(commentService);
        verifyZeroInteractions(mailService);
        verifyZeroInteractions(calendarSyncService);
        verifyZeroInteractions(absenceMappingService);
    }


    // ALLOWING - DEPARTMENT HEAD

    @Test(expected = IllegalStateException.class)
    public void ensureThrowsWhenExecutingAllowProcessWithNotPrivilegedUser() {

        Person person = TestDataCreator.createPerson("muster");
        Person user = TestDataCreator.createPerson("user");
        user.setPermissions(Collections.singletonList(Role.USER));

        when(departmentService.isDepartmentHeadOfPerson(eq(user), eq(person))).thenReturn(false);
        when(departmentService.isSecondStageAuthorityOfPerson(eq(user), eq(person))).thenReturn(false);

        Optional<String> comment = of("Foo");

        Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(ApplicationStatus.WAITING);

        service.allow(applicationForLeave, user, comment);
    }


    @Test
    public void ensureWaitingApplicationForLeaveCanBeAllowedByDepartmentHead() {

        Person person = TestDataCreator.createPerson("muster");
        Person departmentHead = TestDataCreator.createPerson("head", Role.USER, Role.DEPARTMENT_HEAD);
        when(departmentService.isDepartmentHeadOfPerson(eq(departmentHead), eq(person))).thenReturn(true);

        Optional<String> comment = of("Foo");

        Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(ApplicationStatus.WAITING);

        AbsenceMapping absenceMapping = TestDataCreator.anyAbsenceMapping();
        when(absenceMappingService.getAbsenceByIdAndType(isNull(), eq(AbsenceType.VACATION))).thenReturn(of(absenceMapping));
        when(commentService.create(applicationForLeave, ApplicationAction.ALLOWED, comment, departmentHead)).thenReturn(new ApplicationComment(person));

        service.allow(applicationForLeave, departmentHead, comment);

        assertApplicationForLeaveHasChangedStatus(applicationForLeave, ApplicationStatus.ALLOWED, person,
            departmentHead);
        assertApplicationForLeaveAndCommentAreSaved(applicationForLeave, ApplicationAction.ALLOWED, comment,
            departmentHead);
        assertCalendarSyncIsExecuted();
        assertAllowedNotificationIsSent(applicationForLeave);
    }


    @Test
    public void ensureWaitingApplicationForLeaveCanOnlyBeAllowedTemporaryByDepartmentHeadIfTwoStageApprovalIsActive() {

        Person person = TestDataCreator.createPerson("muster");
        Person departmentHead = TestDataCreator.createPerson("head", Role.USER, Role.DEPARTMENT_HEAD);
        when(departmentService.isDepartmentHeadOfPerson(eq(departmentHead), eq(person))).thenReturn(true);

        Optional<String> comment = of("Foo");

        Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(ApplicationStatus.WAITING);
        applicationForLeave.setTwoStageApproval(true);

        AbsenceMapping absenceMapping = TestDataCreator.anyAbsenceMapping();
        when(absenceMappingService.getAbsenceByIdAndType(isNull(), eq(AbsenceType.VACATION))).thenReturn(of(absenceMapping));
        when(commentService.create(applicationForLeave, ApplicationAction.TEMPORARY_ALLOWED, comment, departmentHead)).thenReturn(new ApplicationComment(person));

        service.allow(applicationForLeave, departmentHead, comment);

        assertApplicationForLeaveHasChangedStatus(applicationForLeave, ApplicationStatus.TEMPORARY_ALLOWED, person,
            departmentHead);
        assertApplicationForLeaveAndCommentAreSaved(applicationForLeave, ApplicationAction.TEMPORARY_ALLOWED, comment,
            departmentHead);
        assertNoCalendarSyncOccurs();
        assertTemporaryAllowedNotificationIsSent(applicationForLeave);
    }


    private void assertNoCalendarSyncOccurs() {

        verifyZeroInteractions(calendarSyncService);
        verifyZeroInteractions(absenceMappingService);
    }


    private void assertTemporaryAllowedNotificationIsSent(Application applicationForLeave) {

        verify(mailService)
            .sendTemporaryAllowedNotification(eq(applicationForLeave), any(ApplicationComment.class));

        verify(mailService, never())
            .sendAllowedNotification(any(Application.class), any(ApplicationComment.class));
    }


    @Test
    public void ensureIfTemporaryAllowedApplicationForLeaveIsAllowedByDepartmentHeadWithTwoStageApprovalIsActiveNothingHappens() {

        Person person = TestDataCreator.createPerson("muster");
        Person departmentHead = TestDataCreator.createPerson("head", Role.USER, Role.DEPARTMENT_HEAD);
        when(departmentService.isDepartmentHeadOfPerson(eq(departmentHead), eq(person))).thenReturn(true);

        Optional<String> comment = of("Foo");

        Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(ApplicationStatus.TEMPORARY_ALLOWED);
        applicationForLeave.setTwoStageApproval(true);

        service.allow(applicationForLeave, departmentHead, comment);

        Assert.assertEquals("Status should not be changed", ApplicationStatus.TEMPORARY_ALLOWED,
            applicationForLeave.getStatus());

        verifyZeroInteractions(applicationService);
        verifyZeroInteractions(commentService);
        verifyZeroInteractions(mailService);
        verifyZeroInteractions(calendarSyncService);
        verifyZeroInteractions(absenceMappingService);
    }


    @Test
    public void ensureIfTemporaryAllowedApplicationForLeaveIsAllowedByDepartmentHeadWithTwoStageApprovalNotActiveStatusIsChanged() {

        Person person = TestDataCreator.createPerson("muster");
        Person departmentHead = TestDataCreator.createPerson("head", Role.USER, Role.DEPARTMENT_HEAD);
        when(departmentService.isDepartmentHeadOfPerson(eq(departmentHead), eq(person))).thenReturn(true);

        Optional<String> comment = of("Foo");

        Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(ApplicationStatus.TEMPORARY_ALLOWED);
        applicationForLeave.setTwoStageApproval(false);


        when(commentService.create(any(), any(), any(), any())).thenReturn(new ApplicationComment(person));
        AbsenceMapping absenceMapping = TestDataCreator.anyAbsenceMapping();
        when(absenceMappingService.getAbsenceByIdAndType(isNull(), any())).thenReturn(of(absenceMapping));

        service.allow(applicationForLeave, departmentHead, comment);

        assertApplicationForLeaveHasChangedStatus(applicationForLeave, ApplicationStatus.ALLOWED, person,
            departmentHead);
        assertApplicationForLeaveAndCommentAreSaved(applicationForLeave, ApplicationAction.ALLOWED, comment,
            departmentHead);
        assertCalendarSyncIsExecuted();
        assertAllowedNotificationIsSent(applicationForLeave);
    }


    // ALLOWING - SECOND STAGE AUTHORITY

    @Test
    public void ensureWaitingApplicationForLeaveCanBeAllowedBySecondStageAuthority() {

        Person person = TestDataCreator.createPerson("muster");
        Person secondStage = TestDataCreator.createPerson("manager", Role.USER, Role.SECOND_STAGE_AUTHORITY);
        when(departmentService.isSecondStageAuthorityOfPerson(eq(secondStage), eq(person))).thenReturn(true);

        Optional<String> comment = of("Foo");

        Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(ApplicationStatus.WAITING);

        AbsenceMapping absenceMapping = TestDataCreator.anyAbsenceMapping();
        when(absenceMappingService.getAbsenceByIdAndType(isNull(), eq(AbsenceType.VACATION))).thenReturn(of(absenceMapping));
        when(commentService.create(applicationForLeave, ApplicationAction.ALLOWED, comment, secondStage)).thenReturn(new ApplicationComment(person));


        service.allow(applicationForLeave, secondStage, comment);

        assertApplicationForLeaveHasChangedStatus(applicationForLeave, ApplicationStatus.ALLOWED, person, secondStage);
        assertApplicationForLeaveAndCommentAreSaved(applicationForLeave, ApplicationAction.ALLOWED, comment,
            secondStage);
        assertCalendarSyncIsExecuted();
        assertAllowedNotificationIsSent(applicationForLeave);
    }


    @Test
    public void ensureWaitingApplicationForLeaveCanBeAllowedBySecondStageAuthorityIfTwoStageApprovalIsActive() {

        Person person = TestDataCreator.createPerson("muster");
        Person secondStage = TestDataCreator.createPerson("manager", Role.USER, Role.SECOND_STAGE_AUTHORITY);
        when(departmentService.isSecondStageAuthorityOfPerson(eq(secondStage), eq(person))).thenReturn(true);

        Optional<String> comment = of("Foo");

        Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(ApplicationStatus.WAITING);
        applicationForLeave.setTwoStageApproval(true);

        AbsenceMapping absenceMapping = TestDataCreator.anyAbsenceMapping();
        when(absenceMappingService.getAbsenceByIdAndType(isNull(), eq(AbsenceType.VACATION))).thenReturn(of(absenceMapping));
        when(commentService.create(applicationForLeave, ApplicationAction.ALLOWED, comment, secondStage)).thenReturn(new ApplicationComment(person));



        service.allow(applicationForLeave, secondStage, comment);

        assertApplicationForLeaveHasChangedStatus(applicationForLeave, ApplicationStatus.ALLOWED, person, secondStage);
        assertApplicationForLeaveAndCommentAreSaved(applicationForLeave, ApplicationAction.ALLOWED, comment,
            secondStage);
        assertCalendarSyncIsExecuted();
        assertAllowedNotificationIsSent(applicationForLeave);
    }


    @Test
    public void ensureTemporaryAllowedApplicationForLeaveCanBeAllowedBySecondStageAuthorityIfTwoStageApprovalIsActive() {

        Person person = TestDataCreator.createPerson("muster");
        Person secondStage = TestDataCreator.createPerson("manager", Role.USER, Role.SECOND_STAGE_AUTHORITY);
        when(departmentService.isSecondStageAuthorityOfPerson(eq(secondStage), eq(person))).thenReturn(true);

        Optional<String> comment = of("Foo");

        Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(ApplicationStatus.TEMPORARY_ALLOWED);
        applicationForLeave.setTwoStageApproval(true);

        AbsenceMapping absenceMapping = TestDataCreator.anyAbsenceMapping();
        when(absenceMappingService.getAbsenceByIdAndType(isNull(), eq(AbsenceType.VACATION))).thenReturn(of(absenceMapping));
        when(commentService.create(applicationForLeave, ApplicationAction.ALLOWED, comment, secondStage)).thenReturn(new ApplicationComment(person));

        service.allow(applicationForLeave, secondStage, comment);

        assertApplicationForLeaveHasChangedStatus(applicationForLeave, ApplicationStatus.ALLOWED, person, secondStage);
        assertApplicationForLeaveAndCommentAreSaved(applicationForLeave, ApplicationAction.ALLOWED, comment,
            secondStage);
        assertCalendarSyncIsExecuted();
        assertAllowedNotificationIsSent(applicationForLeave);
    }

    @Test
    public void ensureDepartmentHeadCanBeAllowedBySecondStageAuthority() {

        Person departmentHead = TestDataCreator.createPerson("departmentHead");
        departmentHead.setPermissions(Arrays.asList(Role.USER, Role.DEPARTMENT_HEAD));

        Person secondStageAuthority = TestDataCreator.createPerson("secondStageAuthority");
        secondStageAuthority.setPermissions(Arrays.asList(Role.USER, Role.SECOND_STAGE_AUTHORITY));

        when(departmentService.isDepartmentHeadOfPerson(eq(departmentHead), eq(secondStageAuthority))).thenReturn(true);
        when(departmentService.isSecondStageAuthorityOfPerson(eq(secondStageAuthority), eq(departmentHead))).thenReturn(true);

        Optional<String> comment = of("Foo");

        Application applicationForLeave = getDummyApplication(departmentHead);
        applicationForLeave.setStatus(ApplicationStatus.WAITING);

        service.allow(applicationForLeave, secondStageAuthority, comment);
    }


    @Test(expected = IllegalStateException.class)
    public void ensureSecondStageAuthorityCanNotBeAllowedByDepartmentHead() {

        Person departmentHead = TestDataCreator.createPerson("departmentHead");
        departmentHead.setPermissions(Arrays.asList(Role.USER, Role.DEPARTMENT_HEAD));

        Person secondStageAuthority = TestDataCreator.createPerson("secondStageAuthority");
        secondStageAuthority.setPermissions(Arrays.asList(Role.USER, Role.SECOND_STAGE_AUTHORITY));

        when(departmentService.isDepartmentHeadOfPerson(eq(departmentHead), eq(secondStageAuthority))).thenReturn(true);
        when(departmentService.isSecondStageAuthorityOfPerson(eq(secondStageAuthority), eq(departmentHead))).thenReturn(true);

        Optional<String> comment = of("Foo");

        Application applicationForLeave = getDummyApplication(secondStageAuthority);
        applicationForLeave.setStatus(ApplicationStatus.WAITING);
        applicationForLeave.setTwoStageApproval(true);

        service.allow(applicationForLeave, departmentHead, comment);
    }

    @Test(expected = IllegalStateException.class)
    public void ensureSecondStageAuthorityCanNotAllowHimself() {

        Person secondStageAuthority = TestDataCreator.createPerson("secondStageAuthority");
        secondStageAuthority.setPermissions(Arrays.asList(Role.USER, Role.SECOND_STAGE_AUTHORITY));

        when(departmentService.isSecondStageAuthorityOfPerson(eq(secondStageAuthority), eq(secondStageAuthority))).thenReturn(true);

        Optional<String> comment = of("Foo");

        Application applicationForLeave = getDummyApplication(secondStageAuthority);
        applicationForLeave.setStatus(ApplicationStatus.WAITING);

        service.allow(applicationForLeave, secondStageAuthority, comment);
    }


    @Test(expected = IllegalStateException.class)
    public void ensureDepartmentHeadCanNotAllowHimself() {

        Person departmentHead = TestDataCreator.createPerson("departmentHead");
        departmentHead.setPermissions(Arrays.asList(Role.USER, Role.DEPARTMENT_HEAD));

        when(departmentService.isDepartmentHeadOfPerson(eq(departmentHead), eq(departmentHead))).thenReturn(true);

        Optional<String> comment = of("Foo");

        Application applicationForLeave = getDummyApplication(departmentHead);
        applicationForLeave.setStatus(ApplicationStatus.WAITING);

        service.allow(applicationForLeave, departmentHead, comment);
    }

    // ALLOWING - HOLIDAY REPLACEMENT NOTIFICATION

    @Test
    public void ensureAllowingApplicationForLeaveWithHolidayReplacementSendsNotificationToReplacement() {

        Person person = TestDataCreator.createPerson("muster");
        Person replacement = TestDataCreator.createPerson("replacement");
        Person boss = TestDataCreator.createPerson("boss", Role.USER, Role.BOSS);

        Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(ApplicationStatus.WAITING);
        applicationForLeave.setHolidayReplacement(replacement);

        service.allow(applicationForLeave, boss, of("Foo"));

        verify(mailService).notifyHolidayReplacement(eq(applicationForLeave));
    }


    @Test
    public void ensureAllowingApplicationForLeaveWithoutHolidayReplacementDoesNotSendNotification() {

        Person person = TestDataCreator.createPerson("muster");
        Person boss = TestDataCreator.createPerson("boss", Role.USER, Role.BOSS);

        Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(ApplicationStatus.WAITING);
        applicationForLeave.setHolidayReplacement(null);

        service.allow(applicationForLeave, boss, of("Foo"));

        verify(mailService, never()).notifyHolidayReplacement(any(Application.class));
    }


    @Test
    public void ensureTemporaryAllowingApplicationForLeaveWithHolidayReplacementDoesNotSendNotification() {

        Person person = TestDataCreator.createPerson("muster");
        Person replacement = TestDataCreator.createPerson("replacement");
        Person departmentHead = TestDataCreator.createPerson("head", Role.USER, Role.DEPARTMENT_HEAD);
        when(departmentService.isDepartmentHeadOfPerson(eq(departmentHead), eq(person))).thenReturn(true);

        Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(ApplicationStatus.WAITING);
        applicationForLeave.setHolidayReplacement(replacement);
        applicationForLeave.setTwoStageApproval(true);

        service.allow(applicationForLeave, departmentHead, of("Foo"));

        verify(mailService, never()).notifyHolidayReplacement(any(Application.class));
    }


    // REJECT APPLICATION FOR LEAVE ------------------------------------------------------------------------------------

    @Test
    public void ensureRejectingApplicationForLeaveChangesStateAndOtherAttributesAndSavesTheApplicationForLeave() {

        Person person = TestDataCreator.createPerson("muster");
        Person boss = TestDataCreator.createPerson("boss");

        Optional<String> comment = of("Foo");

        Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(ApplicationStatus.WAITING);

        service.reject(applicationForLeave, boss, comment);

        Assert.assertEquals("Wrong state", ApplicationStatus.REJECTED, applicationForLeave.getStatus());
        Assert.assertEquals("Wrong person", person, applicationForLeave.getPerson());
        Assert.assertEquals("Wrong boss", boss, applicationForLeave.getBoss());
        Assert.assertEquals("Wrong edited date", DateMidnight.now(), applicationForLeave.getEditedDate());

        verify(applicationService).save(applicationForLeave);

        verify(commentService)
            .create(eq(applicationForLeave), eq(ApplicationAction.REJECTED), eq(comment), eq(boss));
    }


    @Test
    public void ensureRejectingApplicationForLeaveDeletesCalendarEvent() {

        Person person = TestDataCreator.createPerson("muster");
        Person boss = TestDataCreator.createPerson("boss");

        Optional<String> comment = of("Foo");

        Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(ApplicationStatus.WAITING);

        AbsenceMapping absenceMapping = TestDataCreator.anyAbsenceMapping();
        when(absenceMappingService.getAbsenceByIdAndType(isNull(), eq(AbsenceType.VACATION))).thenReturn(of(absenceMapping));

        service.reject(applicationForLeave, boss, comment);

        verify(calendarSyncService).deleteAbsence(absenceMapping.getEventId());
        verify(absenceMappingService).delete(absenceMapping);
    }


    @Test
    public void ensureRejectingApplicationForLeaveSendsEmailToPerson() {

        Person person = TestDataCreator.createPerson("muster");
        Person boss = TestDataCreator.createPerson("boss");

        Application applicationForLeave = getDummyApplication(person);

        Optional<String> optionalComment = of("Foo");
        ApplicationComment applicationComment = new ApplicationComment(person);

        when(commentService.create(applicationForLeave, ApplicationAction.REJECTED, optionalComment, boss)).thenReturn(applicationComment);

        service.reject(applicationForLeave, boss, optionalComment);

        verify(mailService).sendRejectedNotification(eq(applicationForLeave), eq(applicationComment));
    }


    // CANCEL APPLICATION FOR LEAVE ------------------------------------------------------------------------------------

    @Test
    public void ensureCancellingNotYetAllowedApplicationForLeaveChangesStateAndOtherAttributesButSendsNoEmail() {

        Person person = TestDataCreator.createPerson();
        Optional<String> comment = of("Foo");

        Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(ApplicationStatus.WAITING);

        service.cancel(applicationForLeave, person, comment);

        Assert.assertEquals("Wrong state", ApplicationStatus.REVOKED, applicationForLeave.getStatus());
        Assert.assertEquals("Wrong person", person, applicationForLeave.getPerson());
        Assert.assertEquals("Wrong canceller", person, applicationForLeave.getCanceller());
        Assert.assertEquals("Wrong cancelled date", DateMidnight.now(), applicationForLeave.getCancelDate());
        Assert.assertFalse("Must not be formerly allowed", applicationForLeave.isFormerlyAllowed());

        verify(applicationService).save(applicationForLeave);

        verify(commentService)
            .create(eq(applicationForLeave), eq(ApplicationAction.REVOKED), eq(comment), eq(person));

        verifyZeroInteractions(mailService);
    }


    @Test
    public void ensureCancellingApplicationForLeaveDeletesCalendarEvent() {

        Person person = TestDataCreator.createPerson("muster");
        Person canceller = TestDataCreator.createPerson("canceller");

        Optional<String> comment = of("Foo");

        Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(ApplicationStatus.WAITING);

        AbsenceMapping absenceMapping = TestDataCreator.anyAbsenceMapping();
        when(absenceMappingService.getAbsenceByIdAndType(null, AbsenceType.VACATION)).thenReturn(of(absenceMapping));

        service.cancel(applicationForLeave, canceller, comment);

        verify(calendarSyncService).deleteAbsence(anyString());
        verify(absenceMappingService).delete(any(AbsenceMapping.class));
    }


    @Test
    public void ensureCancellingAllowedApplicationByOwnerCreatesACancellationRequest() {

        Person person = TestDataCreator.createPerson("muster");

        Optional<String> comment = of("Foo");

        Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(ApplicationStatus.ALLOWED);

        when(commentService.create(any(Application.class), any(ApplicationAction.class), any(), any(Person.class)))
                .thenReturn(new ApplicationComment(person));

        service.cancel(applicationForLeave, person, comment);

        verify(applicationService).save(applicationForLeave);

        verify(commentService)
            .create(eq(applicationForLeave), eq(ApplicationAction.CANCEL_REQUESTED), eq(comment), eq(person));

        verify(mailService).sendCancellationRequest(eq(applicationForLeave), any(ApplicationComment.class));
    }


    @Test
    public void ensureCancellingAllowedApplicationByOwnerThatIsOfficeCancelsTheApplicationForLeaveDirectly() {

        Person person = TestDataCreator.createPerson("muster");
        person.setPermissions(Arrays.asList(Role.USER, Role.OFFICE));

        Optional<String> comment = of("Foo");

        Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(ApplicationStatus.ALLOWED);

        service.cancel(applicationForLeave, person, comment);

        Assert.assertEquals("Wrong state", ApplicationStatus.CANCELLED, applicationForLeave.getStatus());
        Assert.assertEquals("Wrong person", person, applicationForLeave.getPerson());
        Assert.assertEquals("Wrong canceller", person, applicationForLeave.getCanceller());
        Assert.assertEquals("Wrong cancelled date", DateMidnight.now(), applicationForLeave.getCancelDate());
        Assert.assertTrue("Must be formerly allowed", applicationForLeave.isFormerlyAllowed());

        verify(applicationService).save(applicationForLeave);

        verify(commentService)
            .create(eq(applicationForLeave), eq(ApplicationAction.CANCELLED), eq(comment), eq(person));

        verifyZeroInteractions(mailService);
    }


    @Test
    public void ensureCancellingAllowedApplicationForLeaveOnBehalfForSomeOneChangesStateAndOtherAttributesAndSendsAnEmail() {

        Person person = TestDataCreator.createPerson("muster");
        Person canceller = TestDataCreator.createPerson("canceller");
        canceller.setPermissions(Arrays.asList(Role.USER, Role.OFFICE));

        Optional<String> comment = of("Foo");

        Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(ApplicationStatus.ALLOWED);

        when(commentService.create(any(Application.class), any(ApplicationAction.class), any(), any(Person.class)))
                .thenReturn(new ApplicationComment(person));

        service.cancel(applicationForLeave, canceller, comment);

        Assert.assertEquals("Wrong state", ApplicationStatus.CANCELLED, applicationForLeave.getStatus());
        Assert.assertEquals("Wrong person", person, applicationForLeave.getPerson());
        Assert.assertEquals("Wrong canceller", canceller, applicationForLeave.getCanceller());
        Assert.assertEquals("Wrong cancelled date", DateMidnight.now(), applicationForLeave.getCancelDate());
        Assert.assertTrue("Must be formerly allowed", applicationForLeave.isFormerlyAllowed());

        verify(applicationService).save(applicationForLeave);

        verify(commentService)
            .create(eq(applicationForLeave), eq(ApplicationAction.CANCELLED), eq(comment), eq(canceller));

        verify(mailService)
            .sendCancelledByOfficeNotification(eq(applicationForLeave), any(ApplicationComment.class));
    }


    @Test
    public void ensureCancellingNotYetAllowedApplicationForLeaveOnBehalfForSomeOneChangesStateAndOtherAttributesAndSendsAnEmail() {

        Person person = TestDataCreator.createPerson("muster");
        Person canceller = TestDataCreator.createPerson("canceller");
        canceller.setPermissions(Arrays.asList(Role.USER, Role.OFFICE));

        Optional<String> comment = of("Foo");

        Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(ApplicationStatus.WAITING);

        when(commentService.create(any(Application.class), any(ApplicationAction.class), any(), any(Person.class)))
                .thenReturn(new ApplicationComment(person));

        service.cancel(applicationForLeave, canceller, comment);

        Assert.assertEquals("Wrong state", ApplicationStatus.REVOKED, applicationForLeave.getStatus());
        Assert.assertEquals("Wrong person", person, applicationForLeave.getPerson());
        Assert.assertEquals("Wrong canceller", canceller, applicationForLeave.getCanceller());
        Assert.assertEquals("Wrong cancelled date", DateMidnight.now(), applicationForLeave.getCancelDate());
        Assert.assertFalse("Must not be formerly allowed", applicationForLeave.isFormerlyAllowed());

        verify(applicationService).save(applicationForLeave);

        verify(commentService)
            .create(eq(applicationForLeave), eq(ApplicationAction.REVOKED), eq(comment), eq(canceller));

        verify(mailService)
            .sendCancelledByOfficeNotification(eq(applicationForLeave), any(ApplicationComment.class));
    }


    @Test
    public void ensureCancellingApplicationForLeaveUpdatesRemainingVacationDaysWithTheYearOfTheStartDateAsStartYear() {

        Person person = TestDataCreator.createPerson("muster");
        Person canceller = TestDataCreator.createPerson("canceller");
        canceller.setPermissions(Arrays.asList(Role.USER, Role.OFFICE));

        Optional<String> comment = of("Foo");

        Application applicationForLeave = new Application();
        applicationForLeave.setPerson(person);
        applicationForLeave.setStatus(ApplicationStatus.ALLOWED);
        applicationForLeave.setStartDate(new DateMidnight(2014, 12, 24));
        applicationForLeave.setEndDate(new DateMidnight(2015, 1, 7));
        applicationForLeave.setDayLength(DayLength.FULL);

        service.cancel(applicationForLeave, canceller, comment);

        verify(accountInteractionService).updateRemainingVacationDays(2014, person);
    }


    // CREATE APPLICATION FOR LEAVE FROM CONVERTED SICK NOTE -----------------------------------------------------------

    @Test
    public void ensureCreatedApplicationForLeaveFromConvertedSickNoteIsAllowedDirectly() {

        Person person = TestDataCreator.createPerson("muster");
        Person creator = TestDataCreator.createPerson("creator");

        Application applicationForLeave = new Application();
        applicationForLeave.setPerson(person);
        applicationForLeave.setStatus(null);
        applicationForLeave.setStartDate(new DateMidnight(2014, 12, 24));
        applicationForLeave.setEndDate(new DateMidnight(2015, 1, 7));
        applicationForLeave.setDayLength(DayLength.FULL);

        service.createFromConvertedSickNote(applicationForLeave, creator);

        verify(applicationService).save(applicationForLeave);
        verify(commentService)
            .create(eq(applicationForLeave), eq(ApplicationAction.CONVERTED), eq(Optional.empty()),
                eq(creator));
        verify(mailService).sendSickNoteConvertedToVacationNotification(eq(applicationForLeave));

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
        when(applicationForLeave.getApplicationDate()).thenReturn(DateMidnight.now().minusDays(3));
        when(applicationForLeave.getRemindDate()).thenReturn(DateMidnight.now());

        service.remind(applicationForLeave);

        verify(applicationForLeave, never()).setRemindDate(any(DateMidnight.class));
        verifyZeroInteractions(applicationService);
        verifyZeroInteractions(mailService);
    }


    @Test(expected = ImpatientAboutApplicationForLeaveProcessException.class)
    public void ensureThrowsIfTryingToRemindTooEarly() throws RemindAlreadySentException,
        ImpatientAboutApplicationForLeaveProcessException {

        Application applicationForLeave = mock(Application.class);
        when(applicationForLeave.getApplicationDate()).thenReturn(DateMidnight.now());
        when(applicationForLeave.getRemindDate()).thenReturn(null);

        service.remind(applicationForLeave);

        verify(applicationForLeave, never()).setRemindDate(any(DateMidnight.class));
        verifyZeroInteractions(applicationService);
        verifyZeroInteractions(mailService);
    }


    @Test
    public void ensureUpdatesRemindDateAndSendsMail() throws RemindAlreadySentException,
        ImpatientAboutApplicationForLeaveProcessException {

        Person person = TestDataCreator.createPerson();
        Application applicationForLeave = TestDataCreator.createApplication(person,
                TestDataCreator.createVacationType(VacationCategory.HOLIDAY));
        applicationForLeave.setApplicationDate(DateMidnight.now().minusDays(3));
        applicationForLeave.setRemindDate(DateMidnight.now().minusDays(1));

        service.remind(applicationForLeave);

        Assert.assertNotNull("Remind date should be set", applicationForLeave.getRemindDate());
        Assert.assertEquals("Wrong remind date", DateMidnight.now(), applicationForLeave.getRemindDate());

        verify(applicationService).save(eq(applicationForLeave));
        verify(mailService).sendRemindBossNotification(eq(applicationForLeave));
    }


    // REFER -----------------------------------------------------------------------------------------------------------

    @Test
    public void ensureReferMailIsSent() {

        Person recipient = TestDataCreator.createPerson("recipient");
        Person sender = TestDataCreator.createPerson("sender");

        Application applicationForLeave = mock(Application.class);
        when(applicationForLeave.getPerson()).thenReturn(TestDataCreator.createPerson());

        service.refer(applicationForLeave, recipient, sender);

        verify(mailService).sendReferApplicationNotification(applicationForLeave, recipient, sender);
    }
}
