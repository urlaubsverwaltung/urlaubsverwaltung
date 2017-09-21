package org.synyx.urlaubsverwaltung.core.application.service;

import org.joda.time.DateMidnight;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.synyx.urlaubsverwaltung.core.account.service.AccountInteractionService;
import org.synyx.urlaubsverwaltung.core.application.domain.*;
import org.synyx.urlaubsverwaltung.core.application.service.exception.ImpatientAboutApplicationForLeaveProcessException;
import org.synyx.urlaubsverwaltung.core.application.service.exception.RemindAlreadySentException;
import org.synyx.urlaubsverwaltung.core.department.DepartmentService;
import org.synyx.urlaubsverwaltung.core.mail.MailService;
import org.synyx.urlaubsverwaltung.core.period.DayLength;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.Role;
import org.synyx.urlaubsverwaltung.core.settings.Settings;
import org.synyx.urlaubsverwaltung.core.settings.SettingsService;
import org.synyx.urlaubsverwaltung.core.sync.CalendarSyncService;
import org.synyx.urlaubsverwaltung.core.sync.absence.Absence;
import org.synyx.urlaubsverwaltung.core.sync.absence.AbsenceMapping;
import org.synyx.urlaubsverwaltung.core.sync.absence.AbsenceMappingService;
import org.synyx.urlaubsverwaltung.core.sync.absence.AbsenceType;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
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

        applicationService = Mockito.mock(ApplicationService.class);
        commentService = Mockito.mock(ApplicationCommentService.class);
        accountInteractionService = Mockito.mock(AccountInteractionService.class);
        mailService = Mockito.mock(MailService.class);
        calendarSyncService = Mockito.mock(CalendarSyncService.class);
        absenceMappingService = Mockito.mock(AbsenceMappingService.class);
        settingsService = Mockito.mock(SettingsService.class);
        departmentService = Mockito.mock(DepartmentService.class);

        Mockito.when(calendarSyncService.addAbsence(any(Absence.class))).thenReturn(Optional.of("42"));
        Mockito.when(absenceMappingService.getAbsenceByIdAndType(anyInt(), eq(AbsenceType.VACATION)))
            .thenReturn(Optional.of(new AbsenceMapping(1, AbsenceType.VACATION, "42")));
        Mockito.when(settingsService.getSettings()).thenReturn(new Settings());

        service = new ApplicationInteractionServiceImpl(applicationService, commentService, accountInteractionService, mailService, calendarSyncService, absenceMappingService, settingsService,
                departmentService);
    }


    // APPLY FOR LEAVE -------------------------------------------------------------------------------------------------

    @Test
    public void ensureApplyForLeaveChangesStateAndOtherAttributesAndSavesTheApplicationForLeave() {

        Person person = TestDataCreator.createPerson("muster");
        Person applier = TestDataCreator.createPerson("applier");
        Optional<String> comment = Optional.of("Foo");

        Application applicationForLeave = getDummyApplication(person);

        service.apply(applicationForLeave, applier, comment);

        Assert.assertEquals("Wrong state", ApplicationStatus.WAITING, applicationForLeave.getStatus());
        Assert.assertEquals("Wrong person", person, applicationForLeave.getPerson());
        Assert.assertEquals("Wrong applier", applier, applicationForLeave.getApplier());
        Assert.assertEquals("Wrong application date", DateMidnight.now(), applicationForLeave.getApplicationDate());

        Mockito.verify(applicationService).save(applicationForLeave);

        Mockito.verify(commentService)
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
        Optional<String> comment = Optional.of("Foo");

        Application applicationForLeave = getDummyApplication(person);

        service.apply(applicationForLeave, applier, comment);

        Mockito.verify(calendarSyncService).addAbsence(any(Absence.class));
        Mockito.verify(absenceMappingService).create(anyInt(), eq(AbsenceType.VACATION), anyString());
    }


    @Test
    public void ensureSendsConfirmationEmailToPersonAndNotificationEmailToBossesWhenApplyingForOneself() {

        Person person = TestDataCreator.createPerson();

        Application applicationForLeave = getDummyApplication(person);

        service.apply(applicationForLeave, person, Optional.of("Foo"));

        Mockito.verify(mailService).sendConfirmation(eq(applicationForLeave), any(ApplicationComment.class));
        Mockito.verify(mailService, Mockito.never())
            .sendAppliedForLeaveByOfficeNotification(eq(applicationForLeave), any(ApplicationComment.class));

        Mockito.verify(mailService)
            .sendNewApplicationNotification(eq(applicationForLeave), any(ApplicationComment.class));
    }


    @Test
    public void ensureSendsNotificationToPersonIfApplicationForLeaveNotAppliedByOneself() {

        Person person = TestDataCreator.createPerson("muster");
        Person applier = TestDataCreator.createPerson("applier");

        Application applicationForLeave = getDummyApplication(person);

        service.apply(applicationForLeave, applier, Optional.of("Foo"));

        Mockito.verify(mailService, Mockito.never())
            .sendConfirmation(eq(applicationForLeave), any(ApplicationComment.class));
        Mockito.verify(mailService)
            .sendAppliedForLeaveByOfficeNotification(eq(applicationForLeave), any(ApplicationComment.class));

        Mockito.verify(mailService)
            .sendNewApplicationNotification(eq(applicationForLeave), any(ApplicationComment.class));
    }


    @Test
    public void ensureApplyingForLeaveUpdatesTheRemainingVacationDays() {

        Person person = TestDataCreator.createPerson("muster");
        Person applier = TestDataCreator.createPerson("applier");
        Optional<String> comment = Optional.of("Foo");

        Application applicationForLeave = getDummyApplication(person);

        service.apply(applicationForLeave, applier, comment);

        Mockito.verify(accountInteractionService).updateRemainingVacationDays(2013, person);
    }

    // ALLOW APPLICATION FOR LEAVE -------------------------------------------------------------------------------------


    // ALLOWING - BOSS
    @Test
    public void ensureWaitingApplicationForLeaveCanBeAllowedByBoss() {

        Person person = TestDataCreator.createPerson("muster");
        Person boss = TestDataCreator.createPerson("boss", Role.USER, Role.BOSS);
        Optional<String> comment = Optional.of("Foo");

        Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(ApplicationStatus.WAITING);

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

        Mockito.verify(applicationService).save(applicationForLeave);

        Mockito.verify(commentService)
            .create(eq(applicationForLeave), eq(action), eq(optionalComment), eq(privilegedUser));
    }


    private void assertCalendarSyncIsExecuted() {

        Mockito.verify(calendarSyncService).update(any(Absence.class), anyString());
        Mockito.verify(absenceMappingService).getAbsenceByIdAndType(anyInt(), eq(AbsenceType.VACATION));
    }


    private void assertAllowedNotificationIsSent(Application applicationForLeave) {

        Mockito.verify(mailService).sendAllowedNotification(eq(applicationForLeave), any(ApplicationComment.class));

        Mockito.verify(mailService, Mockito.never())
            .sendTemporaryAllowedNotification(any(Application.class), any(ApplicationComment.class));
    }


    @Test
    public void ensureTemporaryAllowedApplicationForLeaveCanBeAllowedByBoss() {

        Person person = TestDataCreator.createPerson("muster");
        Person boss = TestDataCreator.createPerson("boss", Role.USER, Role.BOSS);
        Optional<String> comment = Optional.of("Foo");

        Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(ApplicationStatus.TEMPORARY_ALLOWED);
        applicationForLeave.setTwoStageApproval(false);

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
        Optional<String> comment = Optional.of("Foo");

        Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(ApplicationStatus.TEMPORARY_ALLOWED);
        applicationForLeave.setTwoStageApproval(true);

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
        Optional<String> comment = Optional.of("Foo");

        Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(ApplicationStatus.ALLOWED);

        service.allow(applicationForLeave, boss, comment);

        Assert.assertEquals("Status should not be changed", ApplicationStatus.ALLOWED, applicationForLeave.getStatus());

        Mockito.verifyZeroInteractions(applicationService);
        Mockito.verifyZeroInteractions(commentService);
        Mockito.verifyZeroInteractions(mailService);
        Mockito.verifyZeroInteractions(calendarSyncService);
        Mockito.verifyZeroInteractions(absenceMappingService);
    }


    // ALLOWING - DEPARTMENT HEAD

    @Test(expected = IllegalStateException.class)
    public void ensureThrowsWhenExecutingAllowProcessWithNotPrivilegedUser() {

        Person person = TestDataCreator.createPerson("muster");
        Person user = TestDataCreator.createPerson("user");
        user.setPermissions(Collections.singletonList(Role.USER));

        Mockito.when(departmentService.isDepartmentHeadOfPerson(eq(user), eq(person))).thenReturn(false);
        Mockito.when(departmentService.isSecondStageAuthorityOfPerson(eq(user), eq(person))).thenReturn(false);

        Optional<String> comment = Optional.of("Foo");

        Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(ApplicationStatus.WAITING);

        service.allow(applicationForLeave, user, comment);
    }


    @Test
    public void ensureWaitingApplicationForLeaveCanBeAllowedByDepartmentHead() {

        Person person = TestDataCreator.createPerson("muster");
        Person departmentHead = TestDataCreator.createPerson("head", Role.USER, Role.DEPARTMENT_HEAD);
        Mockito.when(departmentService.isDepartmentHeadOfPerson(eq(departmentHead), eq(person))).thenReturn(true);

        Optional<String> comment = Optional.of("Foo");

        Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(ApplicationStatus.WAITING);

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
        Mockito.when(departmentService.isDepartmentHeadOfPerson(eq(departmentHead), eq(person))).thenReturn(true);

        Optional<String> comment = Optional.of("Foo");

        Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(ApplicationStatus.WAITING);
        applicationForLeave.setTwoStageApproval(true);

        service.allow(applicationForLeave, departmentHead, comment);

        assertApplicationForLeaveHasChangedStatus(applicationForLeave, ApplicationStatus.TEMPORARY_ALLOWED, person,
            departmentHead);
        assertApplicationForLeaveAndCommentAreSaved(applicationForLeave, ApplicationAction.TEMPORARY_ALLOWED, comment,
            departmentHead);
        assertNoCalendarSyncOccurs();
        assertTemporaryAllowedNotificationIsSent(applicationForLeave);
    }


    private void assertNoCalendarSyncOccurs() {

        Mockito.verifyZeroInteractions(calendarSyncService);
        Mockito.verifyZeroInteractions(absenceMappingService);
    }


    private void assertTemporaryAllowedNotificationIsSent(Application applicationForLeave) {

        Mockito.verify(mailService)
            .sendTemporaryAllowedNotification(eq(applicationForLeave), any(ApplicationComment.class));

        Mockito.verify(mailService, Mockito.never())
            .sendAllowedNotification(any(Application.class), any(ApplicationComment.class));
    }


    @Test
    public void ensureIfTemporaryAllowedApplicationForLeaveIsAllowedByDepartmentHeadWithTwoStageApprovalIsActiveNothingHappens() {

        Person person = TestDataCreator.createPerson("muster");
        Person departmentHead = TestDataCreator.createPerson("head", Role.USER, Role.DEPARTMENT_HEAD);
        Mockito.when(departmentService.isDepartmentHeadOfPerson(eq(departmentHead), eq(person))).thenReturn(true);

        Optional<String> comment = Optional.of("Foo");

        Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(ApplicationStatus.TEMPORARY_ALLOWED);
        applicationForLeave.setTwoStageApproval(true);

        service.allow(applicationForLeave, departmentHead, comment);

        Assert.assertEquals("Status should not be changed", ApplicationStatus.TEMPORARY_ALLOWED,
            applicationForLeave.getStatus());

        Mockito.verifyZeroInteractions(applicationService);
        Mockito.verifyZeroInteractions(commentService);
        Mockito.verifyZeroInteractions(mailService);
        Mockito.verifyZeroInteractions(calendarSyncService);
        Mockito.verifyZeroInteractions(absenceMappingService);
    }


    @Test
    public void ensureIfTemporaryAllowedApplicationForLeaveIsAllowedByDepartmentHeadWithTwoStageApprovalNotActiveStatusIsChanged() {

        Person person = TestDataCreator.createPerson("muster");
        Person departmentHead = TestDataCreator.createPerson("head", Role.USER, Role.DEPARTMENT_HEAD);
        Mockito.when(departmentService.isDepartmentHeadOfPerson(eq(departmentHead), eq(person))).thenReturn(true);

        Optional<String> comment = Optional.of("Foo");

        Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(ApplicationStatus.TEMPORARY_ALLOWED);
        applicationForLeave.setTwoStageApproval(false);

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
        Mockito.when(departmentService.isSecondStageAuthorityOfPerson(eq(secondStage), eq(person))).thenReturn(true);

        Optional<String> comment = Optional.of("Foo");

        Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(ApplicationStatus.WAITING);

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
        Mockito.when(departmentService.isSecondStageAuthorityOfPerson(eq(secondStage), eq(person))).thenReturn(true);

        Optional<String> comment = Optional.of("Foo");

        Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(ApplicationStatus.WAITING);
        applicationForLeave.setTwoStageApproval(true);

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
        Mockito.when(departmentService.isSecondStageAuthorityOfPerson(eq(secondStage), eq(person))).thenReturn(true);

        Optional<String> comment = Optional.of("Foo");

        Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(ApplicationStatus.TEMPORARY_ALLOWED);
        applicationForLeave.setTwoStageApproval(true);

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

        Mockito.when(departmentService.isDepartmentHeadOfPerson(eq(departmentHead), eq(secondStageAuthority))).thenReturn(true);
        Mockito.when(departmentService.isSecondStageAuthorityOfPerson(eq(secondStageAuthority), eq(departmentHead))).thenReturn(true);

        Optional<String> comment = Optional.of("Foo");

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

        Mockito.when(departmentService.isDepartmentHeadOfPerson(eq(departmentHead), eq(secondStageAuthority))).thenReturn(true);
        Mockito.when(departmentService.isSecondStageAuthorityOfPerson(eq(secondStageAuthority), eq(departmentHead))).thenReturn(true);

        Optional<String> comment = Optional.of("Foo");

        Application applicationForLeave = getDummyApplication(secondStageAuthority);
        applicationForLeave.setStatus(ApplicationStatus.WAITING);
        applicationForLeave.setTwoStageApproval(true);

        service.allow(applicationForLeave, departmentHead, comment);
    }

    @Test(expected = IllegalStateException.class)
    public void ensureSecondStageAuthorityCanNotAllowHimself() {

        Person secondStageAuthority = TestDataCreator.createPerson("secondStageAuthority");
        secondStageAuthority.setPermissions(Arrays.asList(Role.USER, Role.SECOND_STAGE_AUTHORITY));

        Mockito.when(departmentService.isSecondStageAuthorityOfPerson(eq(secondStageAuthority), eq(secondStageAuthority))).thenReturn(true);

        Optional<String> comment = Optional.of("Foo");

        Application applicationForLeave = getDummyApplication(secondStageAuthority);
        applicationForLeave.setStatus(ApplicationStatus.WAITING);

        service.allow(applicationForLeave, secondStageAuthority, comment);
    }


    @Test(expected = IllegalStateException.class)
    public void ensureDepartmentHeadCanNotAllowHimself() {

        Person departmentHead = TestDataCreator.createPerson("departmentHead");
        departmentHead.setPermissions(Arrays.asList(Role.USER, Role.DEPARTMENT_HEAD));

        Mockito.when(departmentService.isDepartmentHeadOfPerson(eq(departmentHead), eq(departmentHead))).thenReturn(true);

        Optional<String> comment = Optional.of("Foo");

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

        service.allow(applicationForLeave, boss, Optional.of("Foo"));

        Mockito.verify(mailService).notifyHolidayReplacement(eq(applicationForLeave));
    }


    @Test
    public void ensureAllowingApplicationForLeaveWithoutHolidayReplacementDoesNotSendNotification() {

        Person person = TestDataCreator.createPerson("muster");
        Person boss = TestDataCreator.createPerson("boss", Role.USER, Role.BOSS);

        Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(ApplicationStatus.WAITING);
        applicationForLeave.setHolidayReplacement(null);

        service.allow(applicationForLeave, boss, Optional.of("Foo"));

        Mockito.verify(mailService, Mockito.never()).notifyHolidayReplacement(any(Application.class));
    }


    @Test
    public void ensureTemporaryAllowingApplicationForLeaveWithHolidayReplacementDoesNotSendNotification() {

        Person person = TestDataCreator.createPerson("muster");
        Person replacement = TestDataCreator.createPerson("replacement");
        Person departmentHead = TestDataCreator.createPerson("head", Role.USER, Role.DEPARTMENT_HEAD);
        Mockito.when(departmentService.isDepartmentHeadOfPerson(eq(departmentHead), eq(person))).thenReturn(true);

        Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(ApplicationStatus.WAITING);
        applicationForLeave.setHolidayReplacement(replacement);
        applicationForLeave.setTwoStageApproval(true);

        service.allow(applicationForLeave, departmentHead, Optional.of("Foo"));

        Mockito.verify(mailService, Mockito.never()).notifyHolidayReplacement(any(Application.class));
    }


    // REJECT APPLICATION FOR LEAVE ------------------------------------------------------------------------------------

    @Test
    public void ensureRejectingApplicationForLeaveChangesStateAndOtherAttributesAndSavesTheApplicationForLeave() {

        Person person = TestDataCreator.createPerson("muster");
        Person boss = TestDataCreator.createPerson("boss");

        Optional<String> comment = Optional.of("Foo");

        Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(ApplicationStatus.WAITING);

        service.reject(applicationForLeave, boss, comment);

        Assert.assertEquals("Wrong state", ApplicationStatus.REJECTED, applicationForLeave.getStatus());
        Assert.assertEquals("Wrong person", person, applicationForLeave.getPerson());
        Assert.assertEquals("Wrong boss", boss, applicationForLeave.getBoss());
        Assert.assertEquals("Wrong edited date", DateMidnight.now(), applicationForLeave.getEditedDate());

        Mockito.verify(applicationService).save(applicationForLeave);

        Mockito.verify(commentService)
            .create(eq(applicationForLeave), eq(ApplicationAction.REJECTED), eq(comment), eq(boss));
    }


    @Test
    public void ensureRejectingApplicationForLeaveDeletesCalendarEvent() {

        Person person = TestDataCreator.createPerson("muster");
        Person boss = TestDataCreator.createPerson("boss");

        Optional<String> comment = Optional.of("Foo");

        Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(ApplicationStatus.WAITING);

        service.reject(applicationForLeave, boss, comment);

        Mockito.verify(calendarSyncService).deleteAbsence(anyString());
        Mockito.verify(absenceMappingService).delete(any(AbsenceMapping.class));
    }


    @Test
    public void ensureRejectingApplicationForLeaveSendsEmailToPerson() {

        Person person = TestDataCreator.createPerson("muster");
        Person boss = TestDataCreator.createPerson("boss");

        Application applicationForLeave = getDummyApplication(person);

        service.reject(applicationForLeave, boss, Optional.of("Foo"));

        Mockito.verify(mailService).sendRejectedNotification(eq(applicationForLeave), any(ApplicationComment.class));
    }


    // CANCEL APPLICATION FOR LEAVE ------------------------------------------------------------------------------------

    @Test
    public void ensureCancellingNotYetAllowedApplicationForLeaveChangesStateAndOtherAttributesButSendsNoEmail() {

        Person person = TestDataCreator.createPerson();
        Optional<String> comment = Optional.of("Foo");

        Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(ApplicationStatus.WAITING);

        service.cancel(applicationForLeave, person, comment);

        Assert.assertEquals("Wrong state", ApplicationStatus.REVOKED, applicationForLeave.getStatus());
        Assert.assertEquals("Wrong person", person, applicationForLeave.getPerson());
        Assert.assertEquals("Wrong canceller", person, applicationForLeave.getCanceller());
        Assert.assertEquals("Wrong cancelled date", DateMidnight.now(), applicationForLeave.getCancelDate());
        Assert.assertFalse("Must not be formerly allowed", applicationForLeave.isFormerlyAllowed());

        Mockito.verify(applicationService).save(applicationForLeave);

        Mockito.verify(commentService)
            .create(eq(applicationForLeave), eq(ApplicationAction.REVOKED), eq(comment), eq(person));

        Mockito.verifyZeroInteractions(mailService);
    }


    @Test
    public void ensureCancellingApplicationForLeaveDeletesCalendarEvent() {

        Person person = TestDataCreator.createPerson("muster");
        Person canceller = TestDataCreator.createPerson("canceller");

        Optional<String> comment = Optional.of("Foo");

        Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(ApplicationStatus.WAITING);

        service.cancel(applicationForLeave, canceller, comment);

        Mockito.verify(calendarSyncService).deleteAbsence(anyString());
        Mockito.verify(absenceMappingService).delete(any(AbsenceMapping.class));
    }


    @Test
    public void ensureCancellingAllowedApplicationByOwnerCreatesACancellationRequest() {

        Person person = TestDataCreator.createPerson("muster");

        Optional<String> comment = Optional.of("Foo");

        Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(ApplicationStatus.ALLOWED);

        service.cancel(applicationForLeave, person, comment);

        Mockito.verify(applicationService).save(applicationForLeave);

        Mockito.verify(commentService)
            .create(eq(applicationForLeave), eq(ApplicationAction.CANCEL_REQUESTED), eq(comment), eq(person));

        Mockito.verify(mailService).sendCancellationRequest(eq(applicationForLeave), any(ApplicationComment.class));
    }


    @Test
    public void ensureCancellingAllowedApplicationByOwnerThatIsOfficeCancelsTheApplicationForLeaveDirectly() {

        Person person = TestDataCreator.createPerson("muster");
        person.setPermissions(Arrays.asList(Role.USER, Role.OFFICE));

        Optional<String> comment = Optional.of("Foo");

        Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(ApplicationStatus.ALLOWED);

        service.cancel(applicationForLeave, person, comment);

        Assert.assertEquals("Wrong state", ApplicationStatus.CANCELLED, applicationForLeave.getStatus());
        Assert.assertEquals("Wrong person", person, applicationForLeave.getPerson());
        Assert.assertEquals("Wrong canceller", person, applicationForLeave.getCanceller());
        Assert.assertEquals("Wrong cancelled date", DateMidnight.now(), applicationForLeave.getCancelDate());
        Assert.assertTrue("Must be formerly allowed", applicationForLeave.isFormerlyAllowed());

        Mockito.verify(applicationService).save(applicationForLeave);

        Mockito.verify(commentService)
            .create(eq(applicationForLeave), eq(ApplicationAction.CANCELLED), eq(comment), eq(person));

        Mockito.verifyZeroInteractions(mailService);
    }


    @Test
    public void ensureCancellingAllowedApplicationForLeaveOnBehalfForSomeOneChangesStateAndOtherAttributesAndSendsAnEmail() {

        Person person = TestDataCreator.createPerson("muster");
        Person canceller = TestDataCreator.createPerson("canceller");
        canceller.setPermissions(Arrays.asList(Role.USER, Role.OFFICE));

        Optional<String> comment = Optional.of("Foo");

        Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(ApplicationStatus.ALLOWED);

        service.cancel(applicationForLeave, canceller, comment);

        Assert.assertEquals("Wrong state", ApplicationStatus.CANCELLED, applicationForLeave.getStatus());
        Assert.assertEquals("Wrong person", person, applicationForLeave.getPerson());
        Assert.assertEquals("Wrong canceller", canceller, applicationForLeave.getCanceller());
        Assert.assertEquals("Wrong cancelled date", DateMidnight.now(), applicationForLeave.getCancelDate());
        Assert.assertTrue("Must be formerly allowed", applicationForLeave.isFormerlyAllowed());

        Mockito.verify(applicationService).save(applicationForLeave);

        Mockito.verify(commentService)
            .create(eq(applicationForLeave), eq(ApplicationAction.CANCELLED), eq(comment), eq(canceller));

        Mockito.verify(mailService)
            .sendCancelledByOfficeNotification(eq(applicationForLeave), any(ApplicationComment.class));
    }


    @Test
    public void ensureCancellingNotYetAllowedApplicationForLeaveOnBehalfForSomeOneChangesStateAndOtherAttributesAndSendsAnEmail() {

        Person person = TestDataCreator.createPerson("muster");
        Person canceller = TestDataCreator.createPerson("canceller");
        canceller.setPermissions(Arrays.asList(Role.USER, Role.OFFICE));

        Optional<String> comment = Optional.of("Foo");

        Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(ApplicationStatus.WAITING);

        service.cancel(applicationForLeave, canceller, comment);

        Assert.assertEquals("Wrong state", ApplicationStatus.REVOKED, applicationForLeave.getStatus());
        Assert.assertEquals("Wrong person", person, applicationForLeave.getPerson());
        Assert.assertEquals("Wrong canceller", canceller, applicationForLeave.getCanceller());
        Assert.assertEquals("Wrong cancelled date", DateMidnight.now(), applicationForLeave.getCancelDate());
        Assert.assertFalse("Must not be formerly allowed", applicationForLeave.isFormerlyAllowed());

        Mockito.verify(applicationService).save(applicationForLeave);

        Mockito.verify(commentService)
            .create(eq(applicationForLeave), eq(ApplicationAction.REVOKED), eq(comment), eq(canceller));

        Mockito.verify(mailService)
            .sendCancelledByOfficeNotification(eq(applicationForLeave), any(ApplicationComment.class));
    }


    @Test
    public void ensureCancellingApplicationForLeaveUpdatesRemainingVacationDaysWithTheYearOfTheStartDateAsStartYear() {

        Person person = TestDataCreator.createPerson("muster");
        Person canceller = TestDataCreator.createPerson("canceller");
        canceller.setPermissions(Arrays.asList(Role.USER, Role.OFFICE));

        Optional<String> comment = Optional.of("Foo");

        Application applicationForLeave = new Application();
        applicationForLeave.setPerson(person);
        applicationForLeave.setStatus(ApplicationStatus.ALLOWED);
        applicationForLeave.setStartDate(new DateMidnight(2014, 12, 24));
        applicationForLeave.setEndDate(new DateMidnight(2015, 1, 7));
        applicationForLeave.setDayLength(DayLength.FULL);

        service.cancel(applicationForLeave, canceller, comment);

        Mockito.verify(accountInteractionService).updateRemainingVacationDays(2014, person);
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

        Mockito.verify(applicationService).save(applicationForLeave);
        Mockito.verify(commentService)
            .create(eq(applicationForLeave), eq(ApplicationAction.CONVERTED), eq(Optional.<String>empty()),
                eq(creator));
        Mockito.verify(mailService).sendSickNoteConvertedToVacationNotification(eq(applicationForLeave));

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

        Application applicationForLeave = Mockito.mock(Application.class);
        Mockito.when(applicationForLeave.getApplicationDate()).thenReturn(DateMidnight.now().minusDays(3));
        Mockito.when(applicationForLeave.getRemindDate()).thenReturn(DateMidnight.now());

        service.remind(applicationForLeave);

        Mockito.verify(applicationForLeave, Mockito.never()).setRemindDate(Mockito.any(DateMidnight.class));
        Mockito.verifyZeroInteractions(applicationService);
        Mockito.verifyZeroInteractions(mailService);
    }


    @Test(expected = ImpatientAboutApplicationForLeaveProcessException.class)
    public void ensureThrowsIfTryingToRemindTooEarly() throws RemindAlreadySentException,
        ImpatientAboutApplicationForLeaveProcessException {

        Application applicationForLeave = Mockito.mock(Application.class);
        Mockito.when(applicationForLeave.getApplicationDate()).thenReturn(DateMidnight.now());
        Mockito.when(applicationForLeave.getRemindDate()).thenReturn(null);

        service.remind(applicationForLeave);

        Mockito.verify(applicationForLeave, Mockito.never()).setRemindDate(Mockito.any(DateMidnight.class));
        Mockito.verifyZeroInteractions(applicationService);
        Mockito.verifyZeroInteractions(mailService);
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

        Mockito.verify(applicationService).save(Mockito.eq(applicationForLeave));
        Mockito.verify(mailService).sendRemindBossNotification(Mockito.eq(applicationForLeave));
    }


    // REFER -----------------------------------------------------------------------------------------------------------

    @Test
    public void ensureReferMailIsSent() {

        Person recipient = TestDataCreator.createPerson("recipient");
        Person sender = TestDataCreator.createPerson("sender");

        Application applicationForLeave = Mockito.mock(Application.class);
        Mockito.when(applicationForLeave.getPerson()).thenReturn(TestDataCreator.createPerson());

        service.refer(applicationForLeave, recipient, sender);

        Mockito.verify(mailService).sendReferApplicationNotification(applicationForLeave, recipient, sender);
    }
}
