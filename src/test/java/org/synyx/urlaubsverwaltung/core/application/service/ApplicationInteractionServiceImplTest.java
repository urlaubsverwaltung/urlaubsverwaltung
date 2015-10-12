package org.synyx.urlaubsverwaltung.core.application.service;

import org.joda.time.DateMidnight;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.synyx.urlaubsverwaltung.core.account.service.AccountInteractionService;
import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationComment;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.core.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.core.application.service.exception.ImpatientAboutApplicationForLeaveProcessException;
import org.synyx.urlaubsverwaltung.core.application.service.exception.RemindAlreadySentException;
import org.synyx.urlaubsverwaltung.core.mail.MailService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.settings.Settings;
import org.synyx.urlaubsverwaltung.core.settings.SettingsService;
import org.synyx.urlaubsverwaltung.core.sync.CalendarSyncService;
import org.synyx.urlaubsverwaltung.core.sync.absence.Absence;
import org.synyx.urlaubsverwaltung.core.sync.absence.AbsenceMapping;
import org.synyx.urlaubsverwaltung.core.sync.absence.AbsenceMappingService;
import org.synyx.urlaubsverwaltung.core.sync.absence.AbsenceType;

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
    private SignService signService;
    private MailService mailService;
    private CalendarSyncService calendarSyncService;
    private AbsenceMappingService absenceMappingService;
    private SettingsService settingsService;

    @Before
    public void setUp() {

        applicationService = Mockito.mock(ApplicationService.class);
        commentService = Mockito.mock(ApplicationCommentService.class);
        accountInteractionService = Mockito.mock(AccountInteractionService.class);
        signService = Mockito.mock(SignService.class);
        mailService = Mockito.mock(MailService.class);
        calendarSyncService = Mockito.mock(CalendarSyncService.class);
        absenceMappingService = Mockito.mock(AbsenceMappingService.class);
        settingsService = Mockito.mock(SettingsService.class);

        Mockito.when(calendarSyncService.addAbsence(any(Absence.class))).thenReturn(Optional.of("42"));
        Mockito.when(absenceMappingService.getAbsenceByIdAndType(anyInt(), eq(AbsenceType.VACATION)))
            .thenReturn(Optional.of(new AbsenceMapping(1, AbsenceType.VACATION, "42")));
        Mockito.when(settingsService.getSettings()).thenReturn(new Settings());

        service = new ApplicationInteractionServiceImpl(applicationService, commentService, accountInteractionService,
                signService, mailService, calendarSyncService, absenceMappingService, settingsService);
    }


    // START: APPLY

    @Test
    public void ensureApplyForLeaveChangesStateAndOtherAttributesAndSavesTheApplicationForLeave() {

        Person person = new Person();
        Person applier = new Person();
        Optional<String> comment = Optional.of("Foo");

        Application applicationForLeave = getDummyApplication(person);

        service.apply(applicationForLeave, applier, comment);

        Assert.assertEquals("Wrong state", ApplicationStatus.WAITING, applicationForLeave.getStatus());
        Assert.assertEquals("Wrong person", person, applicationForLeave.getPerson());
        Assert.assertEquals("Wrong applier", applier, applicationForLeave.getApplier());
        Assert.assertEquals("Wrong application date", DateMidnight.now(), applicationForLeave.getApplicationDate());

        Mockito.verify(applicationService).save(applicationForLeave);

        Mockito.verify(signService).signApplicationByUser(eq(applicationForLeave), eq(applier));

        Mockito.verify(commentService)
            .create(eq(applicationForLeave), eq(ApplicationStatus.WAITING), eq(comment), eq(applier));
    }


    private Application getDummyApplication(Person person) {

        Application applicationForLeave = new Application();
        applicationForLeave.setPerson(person);
        applicationForLeave.setStartDate(new DateMidnight(2013, 2, 1));
        applicationForLeave.setEndDate(new DateMidnight(2013, 2, 5));
        applicationForLeave.setDayLength(DayLength.FULL);

        return applicationForLeave;
    }


    @Test
    public void ensureApplyingForLeaveAddsCalendarEvent() {

        Person person = new Person();
        Person applier = new Person();
        Optional<String> comment = Optional.of("Foo");

        Application applicationForLeave = getDummyApplication(person);

        service.apply(applicationForLeave, applier, comment);

        Mockito.verify(calendarSyncService).addAbsence(any(Absence.class));
        Mockito.verify(absenceMappingService).create(eq(applicationForLeave), anyString());
    }


    @Test
    public void ensureSendsConfirmationEmailToPersonAndNotificationEmailToBossesWhenApplyingForOneself() {

        Person person = new Person();

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

        Person person = new Person();
        Person applier = new Person();

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

        Person person = new Person();
        Person applier = new Person();
        Optional<String> comment = Optional.of("Foo");

        Application applicationForLeave = getDummyApplication(person);

        service.apply(applicationForLeave, applier, comment);

        Mockito.verify(accountInteractionService).updateRemainingVacationDays(2013, person);
    }

    // END: APPLY


    // START: ALLOW

    @Test
    public void ensureAllowingApplicationForLeaveChangesStateAndOtherAttributesAndSavesTheApplicationForLeave() {

        Person person = new Person();
        Person boss = new Person();
        Optional<String> comment = Optional.of("Foo");

        Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(ApplicationStatus.WAITING);

        service.allow(applicationForLeave, boss, comment);

        Assert.assertEquals("Wrong state", ApplicationStatus.ALLOWED, applicationForLeave.getStatus());
        Assert.assertEquals("Wrong person", person, applicationForLeave.getPerson());
        Assert.assertEquals("Wrong boss", boss, applicationForLeave.getBoss());
        Assert.assertEquals("Wrong edited date", DateMidnight.now(), applicationForLeave.getEditedDate());

        Mockito.verify(applicationService).save(applicationForLeave);

        Mockito.verify(signService).signApplicationByBoss(eq(applicationForLeave), eq(boss));

        Mockito.verify(commentService)
            .create(eq(applicationForLeave), eq(ApplicationStatus.ALLOWED), eq(comment), eq(boss));
    }


    @Test
    public void ensureAllowingApplicationForLeaveUpdatesCalendarEvent() {

        Person person = new Person();
        Person boss = new Person();
        Optional<String> comment = Optional.of("Foo");

        Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(ApplicationStatus.WAITING);

        service.allow(applicationForLeave, boss, comment);

        Mockito.verify(calendarSyncService).update(any(Absence.class), anyString());
        Mockito.verify(absenceMappingService).getAbsenceByIdAndType(anyInt(), eq(AbsenceType.VACATION));
    }


    @Test
    public void ensureAllowingApplicationForLeaveSendsEmailToPerson() {

        Person person = new Person();
        Person boss = new Person();

        Application applicationForLeave = getDummyApplication(person);

        service.allow(applicationForLeave, boss, Optional.of("Foo"));

        Mockito.verify(mailService).sendAllowedNotification(eq(applicationForLeave), any(ApplicationComment.class));
    }


    @Test
    public void ensureAllowingApplicationForLeaveWithRepresentativeSendsEmailToRepresentative() {

        Person person = new Person();
        Person rep = new Person();
        Person boss = new Person();

        Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setHolidayReplacement(rep);

        service.allow(applicationForLeave, boss, Optional.of("Foo"));

        Mockito.verify(mailService).notifyHolidayReplacement(eq(applicationForLeave));
    }


    @Test
    public void ensureAllowingApplicationForLeaveExecutesCalendarSync() {

        Person person = new Person();
        Person boss = new Person();

        Application applicationForLeave = getDummyApplication(person);

        service.allow(applicationForLeave, boss, Optional.of("Foo"));

        Mockito.verify(calendarSyncService).update(any(Absence.class), anyString());
        Mockito.verify(absenceMappingService).getAbsenceByIdAndType(anyInt(), eq(AbsenceType.VACATION));
    }


    // END: ALLOW

    // START: REJECT

    @Test
    public void ensureRejectingApplicationForLeaveChangesStateAndOtherAttributesAndSavesTheApplicationForLeave() {

        Person person = new Person();
        Person boss = new Person();
        Optional<String> comment = Optional.of("Foo");

        Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(ApplicationStatus.WAITING);

        service.reject(applicationForLeave, boss, comment);

        Assert.assertEquals("Wrong state", ApplicationStatus.REJECTED, applicationForLeave.getStatus());
        Assert.assertEquals("Wrong person", person, applicationForLeave.getPerson());
        Assert.assertEquals("Wrong boss", boss, applicationForLeave.getBoss());
        Assert.assertEquals("Wrong edited date", DateMidnight.now(), applicationForLeave.getEditedDate());

        Mockito.verify(applicationService).save(applicationForLeave);

        Mockito.verify(signService).signApplicationByBoss(eq(applicationForLeave), eq(boss));

        Mockito.verify(commentService)
            .create(eq(applicationForLeave), eq(ApplicationStatus.REJECTED), eq(comment), eq(boss));
    }


    @Test
    public void ensureRejectingApplicationForLeaveDeletesCalendarEvent() {

        Person person = new Person();
        Person boss = new Person();
        Optional<String> comment = Optional.of("Foo");

        Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(ApplicationStatus.WAITING);

        service.reject(applicationForLeave, boss, comment);

        Mockito.verify(calendarSyncService).deleteAbsence(anyString());
        Mockito.verify(absenceMappingService).delete(any(AbsenceMapping.class));
    }


    @Test
    public void ensureRejectingApplicationForLeaveSendsEmailToPerson() {

        Person person = new Person();
        Person boss = new Person();

        Application applicationForLeave = getDummyApplication(person);

        service.reject(applicationForLeave, boss, Optional.of("Foo"));

        Mockito.verify(mailService).sendRejectedNotification(eq(applicationForLeave), any(ApplicationComment.class));
    }


    // END: REJECT

    // START: CANCEL

    @Test
    public void ensureCancellingNotYetAllowedApplicationForLeaveChangesStateAndOtherAttributesButSendsNoEmail() {

        Person person = new Person();
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
            .create(eq(applicationForLeave), eq(ApplicationStatus.REVOKED), eq(comment), eq(person));

        Mockito.verifyZeroInteractions(mailService);
    }


    @Test
    public void ensureCancellingApplicationForLeaveDeletesCalendarEvent() {

        Person person = new Person();
        Optional<String> comment = Optional.of("Foo");

        Application applicationForLeave = getDummyApplication(person);
        applicationForLeave.setStatus(ApplicationStatus.WAITING);

        service.cancel(applicationForLeave, person, comment);

        Mockito.verify(calendarSyncService).deleteAbsence(anyString());
        Mockito.verify(absenceMappingService).delete(any(AbsenceMapping.class));
    }


    @Test
    public void ensureCancellingAllowedApplicationForLeaveChangesStateAndOtherAttributesAndSendsAnEmail() {

        Person person = new Person();
        Person canceller = new Person();
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
            .create(eq(applicationForLeave), eq(ApplicationStatus.CANCELLED), eq(comment), eq(canceller));

        Mockito.verify(mailService)
            .sendCancelledNotification(eq(applicationForLeave), eq(false), any(ApplicationComment.class));
    }


    @Test
    public void ensureCancellingNotYetAllowedApplicationForLeaveOnBehalfForSomeOneChangesStateAndOtherAttributesAndSendsAnEmail() {

        Person person = new Person();
        Person canceller = new Person();
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
            .create(eq(applicationForLeave), eq(ApplicationStatus.REVOKED), eq(comment), eq(canceller));

        Mockito.verify(mailService)
            .sendCancelledNotification(eq(applicationForLeave), eq(true), any(ApplicationComment.class));
    }


    @Test
    public void ensureCancellingApplicationForLeaveUpdatesRemainingVacationDaysWithTheYearOfTheStartDateAsStartYear() {

        Person person = new Person();
        Person canceller = new Person();
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


    // END: CANCEL

    // START: CREATE FROM CONVERTED SICK NOTE

    @Test
    public void ensureCreatedApplicationForLeaveFromConvertedSickNoteIsAllowedDirectly() {

        Person person = new Person();
        Person creator = new Person();

        Application applicationForLeave = new Application();
        applicationForLeave.setPerson(person);
        applicationForLeave.setStatus(null);
        applicationForLeave.setStartDate(new DateMidnight(2014, 12, 24));
        applicationForLeave.setEndDate(new DateMidnight(2015, 1, 7));
        applicationForLeave.setDayLength(DayLength.FULL);

        service.createFromConvertedSickNote(applicationForLeave, creator);

        Mockito.verify(applicationService).save(applicationForLeave);
        Mockito.verify(commentService)
            .create(eq(applicationForLeave), eq(ApplicationStatus.ALLOWED), eq(Optional.<String>empty()), eq(creator));
        Mockito.verify(signService).signApplicationByBoss(eq(applicationForLeave), eq(creator));
        Mockito.verify(mailService).sendSickNoteConvertedToVacationNotification(eq(applicationForLeave));

        Assert.assertNotNull("Status should be set", applicationForLeave.getStatus());
        Assert.assertNotNull("Applier should be set", applicationForLeave.getApplier());
        Assert.assertNotNull("Person should be set", applicationForLeave.getPerson());

        Assert.assertEquals("Wrong status", ApplicationStatus.ALLOWED, applicationForLeave.getStatus());
        Assert.assertEquals("Wrong applier", creator, applicationForLeave.getApplier());
        Assert.assertEquals("Wrong person", person, applicationForLeave.getPerson());
    }


    // END: CREATE FROM CONVERTED SICK NOTE

    // START: REMIND

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

        Application applicationForLeave = new Application();
        applicationForLeave.setPerson(new Person());
        applicationForLeave.setApplicationDate(DateMidnight.now().minusDays(3));
        applicationForLeave.setRemindDate(DateMidnight.now().minusDays(1));

        service.remind(applicationForLeave);

        Assert.assertNotNull("Remind date should be set", applicationForLeave.getRemindDate());
        Assert.assertEquals("Wrong remind date", DateMidnight.now(), applicationForLeave.getRemindDate());

        Mockito.verify(applicationService).save(Mockito.eq(applicationForLeave));
        Mockito.verify(mailService).sendRemindBossNotification(Mockito.eq(applicationForLeave));
    }


    // END: REMIND

    // START: REFER

    @Test
    public void ensureReferMailIsSent() {

        Person recipient = new Person();
        Person sender = new Person();

        Application applicationForLeave = Mockito.mock(Application.class);
        Mockito.when(applicationForLeave.getPerson()).thenReturn(new Person());

        service.refer(applicationForLeave, recipient, sender);

        Mockito.verify(mailService).sendReferApplicationNotification(applicationForLeave, recipient, sender);
    }

    // END: REFER
}
