package org.synyx.urlaubsverwaltung.core.application.service;

import org.joda.time.DateMidnight;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.synyx.urlaubsverwaltung.core.account.service.AccountInteractionService;
import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.core.application.domain.Comment;
import org.synyx.urlaubsverwaltung.core.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.core.mail.MailService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.sync.CalendarProviderService;
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
    private CommentService commentService;
    private AccountInteractionService accountInteractionService;
    private SignService signService;
    private MailService mailService;
    private CalendarProviderService calendarProviderService;
    private AbsenceMappingService absenceMappingService;

    @Before
    public void setUp() {

        applicationService = Mockito.mock(ApplicationService.class);
        commentService = Mockito.mock(CommentService.class);
        accountInteractionService = Mockito.mock(AccountInteractionService.class);
        signService = Mockito.mock(SignService.class);
        mailService = Mockito.mock(MailService.class);
        calendarProviderService = Mockito.mock(CalendarProviderService.class);
        absenceMappingService = Mockito.mock(AbsenceMappingService.class);
        Mockito.when(calendarProviderService.addAbsence(any(Absence.class))).thenReturn(Optional.of("42"));
        Mockito.when(absenceMappingService.getAbsenceByIdAndType(anyInt(), eq(AbsenceType.VACATION)))
            .thenReturn(Optional.of(new AbsenceMapping(1, AbsenceType.VACATION, "42")));

        service = new ApplicationInteractionServiceImpl(applicationService, commentService, accountInteractionService,
                signService, mailService, calendarProviderService, absenceMappingService);
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

        Mockito.verify(calendarProviderService).addAbsence(any(Absence.class));
        Mockito.verify(absenceMappingService).create(eq(applicationForLeave), anyString());
    }


    private Application getDummyApplication(Person person) {

        Application applicationForLeave = new Application();
        applicationForLeave.setPerson(person);
        applicationForLeave.setStartDate(new DateMidnight(2013, 2, 1));
        applicationForLeave.setEndDate(new DateMidnight(2013, 2, 5));
        applicationForLeave.setHowLong(DayLength.FULL);

        return applicationForLeave;
    }


    @Test
    public void ensureSendsConfirmationEmailToPersonAndNotificationEmailToBossesWhenApplyingForOneself() {

        Person person = new Person();

        Application applicationForLeave = getDummyApplication(person);

        service.apply(applicationForLeave, person, Optional.of("Foo"));

        Mockito.verify(mailService).sendConfirmation(eq(applicationForLeave), any(Comment.class));
        Mockito.verify(mailService, Mockito.never())
            .sendAppliedForLeaveByOfficeNotification(eq(applicationForLeave), any(Comment.class));

        Mockito.verify(mailService).sendNewApplicationNotification(eq(applicationForLeave), any(Comment.class));
    }


    @Test
    public void ensureSendsNotificationToPersonIfApplicationForLeaveNotAppliedByOneself() {

        Person person = new Person();
        Person applier = new Person();

        Application applicationForLeave = getDummyApplication(person);

        service.apply(applicationForLeave, applier, Optional.of("Foo"));

        Mockito.verify(mailService, Mockito.never()).sendConfirmation(eq(applicationForLeave), any(Comment.class));
        Mockito.verify(mailService)
            .sendAppliedForLeaveByOfficeNotification(eq(applicationForLeave), any(Comment.class));

        Mockito.verify(mailService).sendNewApplicationNotification(eq(applicationForLeave), any(Comment.class));
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

        Mockito.verify(calendarProviderService).update(any(Absence.class), anyString());
        Mockito.verify(absenceMappingService).getAbsenceByIdAndType(anyInt(), eq(AbsenceType.VACATION));
    }


    @Test
    public void ensureAllowingApplicationForLeaveSendsEmailToPerson() {

        Person person = new Person();
        Person boss = new Person();

        Application applicationForLeave = getDummyApplication(person);

        service.allow(applicationForLeave, boss, Optional.of("Foo"));

        Mockito.verify(mailService).sendAllowedNotification(eq(applicationForLeave), any(Comment.class));
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

        Mockito.verify(calendarProviderService).update(any(Absence.class), anyString());
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

        Mockito.verify(calendarProviderService).deleteAbsence(anyString());
        Mockito.verify(absenceMappingService).delete(any(AbsenceMapping.class));
    }


    @Test
    public void ensureRejectingApplicationForLeaveSendsEmailToPerson() {

        Person person = new Person();
        Person boss = new Person();

        Application applicationForLeave = getDummyApplication(person);

        service.reject(applicationForLeave, boss, Optional.of("Foo"));

        Mockito.verify(mailService).sendRejectedNotification(eq(applicationForLeave), any(Comment.class));
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

        Mockito.verify(calendarProviderService).deleteAbsence(anyString());
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

        Mockito.verify(mailService).sendCancelledNotification(eq(applicationForLeave), eq(false), any(Comment.class));
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

        Mockito.verify(mailService).sendCancelledNotification(eq(applicationForLeave), eq(true), any(Comment.class));
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
        applicationForLeave.setHowLong(DayLength.FULL);

        service.cancel(applicationForLeave, canceller, comment);

        Mockito.verify(accountInteractionService).updateRemainingVacationDays(2014, person);
    }

    // END: CANCEL
}
