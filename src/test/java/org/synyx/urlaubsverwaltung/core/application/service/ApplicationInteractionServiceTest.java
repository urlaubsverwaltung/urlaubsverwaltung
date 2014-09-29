package org.synyx.urlaubsverwaltung.core.application.service;

import org.joda.time.DateMidnight;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.core.application.domain.Comment;
import org.synyx.urlaubsverwaltung.core.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.core.calendar.OwnCalendarService;
import org.synyx.urlaubsverwaltung.core.mail.MailService;
import org.synyx.urlaubsverwaltung.core.person.Person;

import java.math.BigDecimal;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class ApplicationInteractionServiceTest {

    private ApplicationInteractionService service;

    private ApplicationService applicationService;
    private OwnCalendarService calendarService;
    private SignService signService;
    private CommentService commentService;
    private MailService mailService;

    @Before
    public void setUp() {

        applicationService = Mockito.mock(ApplicationService.class);
        calendarService = Mockito.mock(OwnCalendarService.class);
        signService = Mockito.mock(SignService.class);
        commentService = Mockito.mock(CommentService.class);
        mailService = Mockito.mock(MailService.class);

        service = new ApplicationInteractionServiceImpl(applicationService, calendarService, signService,
                commentService, mailService);
    }


    // START: APPLY

    @Test
    public void ensureApplyForLeaveChangesStateAndOtherAttributesAndSavesTheApplicationForLeave() {

        Person person = new Person();
        Person applier = new Person();

        Application applicationForLeave = new Application();
        applicationForLeave.setPerson(person);

        Mockito.when(calendarService.getWorkDays(Mockito.any(DayLength.class), Mockito.any(DateMidnight.class),
                Mockito.any(DateMidnight.class), Mockito.any(Person.class))).thenReturn(BigDecimal.TEN);

        service.apply(applicationForLeave, applier);

        Assert.assertEquals("Wrong state", ApplicationStatus.WAITING, applicationForLeave.getStatus());
        Assert.assertEquals("Wrong number of vacation days", BigDecimal.TEN, applicationForLeave.getDays());
        Assert.assertEquals("Wrong person", person, applicationForLeave.getPerson());
        Assert.assertEquals("Wrong applier", applier, applicationForLeave.getApplier());
        Assert.assertEquals("Wrong application date", DateMidnight.now(), applicationForLeave.getApplicationDate());

        Mockito.verify(applicationService).save(applicationForLeave);

        Mockito.verify(signService).signApplicationByUser(Mockito.eq(applicationForLeave), Mockito.eq(applier));

        Mockito.verify(commentService).saveComment(Mockito.any(Comment.class), Mockito.eq(applier),
            Mockito.eq(applicationForLeave));
    }


    @Test
    public void ensureSendsConfirmationEmailToPersonAndNotificationEmailToBossesWhenApplyingForOneself() {

        Person person = new Person();

        Application applicationForLeave = new Application();
        applicationForLeave.setPerson(person);

        Mockito.when(calendarService.getWorkDays(Mockito.any(DayLength.class), Mockito.any(DateMidnight.class),
                Mockito.any(DateMidnight.class), Mockito.any(Person.class))).thenReturn(BigDecimal.TEN);

        service.apply(applicationForLeave, person);

        Mockito.verify(mailService).sendConfirmation(Mockito.eq(applicationForLeave));
        Mockito.verify(mailService, Mockito.never()).sendAppliedForLeaveByOfficeNotification(applicationForLeave);

        Mockito.verify(mailService).sendNewApplicationNotification(Mockito.eq(applicationForLeave));
    }


    @Test
    public void ensureSendsNotificationToPersonIfApplicationForLeaveNotAppliedByOneself() {

        Person person = new Person();
        Person applier = new Person();

        Application applicationForLeave = new Application();
        applicationForLeave.setPerson(person);

        Mockito.when(calendarService.getWorkDays(Mockito.any(DayLength.class), Mockito.any(DateMidnight.class),
                Mockito.any(DateMidnight.class), Mockito.any(Person.class))).thenReturn(BigDecimal.TEN);

        service.apply(applicationForLeave, applier);

        Mockito.verify(mailService, Mockito.never()).sendConfirmation(Mockito.eq(applicationForLeave));
        Mockito.verify(mailService).sendAppliedForLeaveByOfficeNotification(applicationForLeave);

        Mockito.verify(mailService).sendNewApplicationNotification(Mockito.eq(applicationForLeave));
    }

    // END: APPLY


    // START: ALLOW

    @Test
    public void ensureAllowingApplicationForLeaveChangesStateAndOtherAttributesAndSavesTheApplicationForLeave() {

        Person person = new Person();
        Person boss = new Person();
        Comment comment = new Comment();

        Application applicationForLeave = new Application();
        applicationForLeave.setPerson(person);
        applicationForLeave.setStatus(ApplicationStatus.WAITING);

        service.allow(applicationForLeave, boss, comment);

        Assert.assertEquals("Wrong state", ApplicationStatus.ALLOWED, applicationForLeave.getStatus());
        Assert.assertEquals("Wrong person", person, applicationForLeave.getPerson());
        Assert.assertEquals("Wrong boss", boss, applicationForLeave.getBoss());
        Assert.assertEquals("Wrong edited date", DateMidnight.now(), applicationForLeave.getEditedDate());

        Mockito.verify(applicationService).save(applicationForLeave);

        Mockito.verify(signService).signApplicationByBoss(Mockito.eq(applicationForLeave), Mockito.eq(boss));

        Mockito.verify(commentService).saveComment(Mockito.eq(comment), Mockito.eq(boss),
            Mockito.eq(applicationForLeave));
    }


    @Test
    public void ensureAllowingApplicationForLeaveSendsEmailToPerson() {

        Person person = new Person();
        Person boss = new Person();
        Comment comment = new Comment();

        Application applicationForLeave = new Application();
        applicationForLeave.setPerson(person);

        service.allow(applicationForLeave, boss, comment);

        Mockito.verify(mailService).sendAllowedNotification(Mockito.eq(applicationForLeave), Mockito.eq(comment));
    }


    @Test
    public void ensureAllowingApplicationForLeaveWithRepresentativeSendsEmailToRepresentative() {

        Person person = new Person();
        Person rep = new Person();
        Person boss = new Person();
        Comment comment = new Comment();

        Application applicationForLeave = new Application();
        applicationForLeave.setPerson(person);
        applicationForLeave.setRep(rep);

        service.allow(applicationForLeave, boss, comment);

        Mockito.verify(mailService).notifyRepresentative(Mockito.eq(applicationForLeave));
    }


    // END: ALLOW

    // START: REJECT

    @Test
    public void ensureRejectingApplicationForLeaveChangesStateAndOtherAttributesAndSavesTheApplicationForLeave() {

        Person person = new Person();
        Person boss = new Person();
        Comment comment = new Comment();

        Application applicationForLeave = new Application();
        applicationForLeave.setPerson(person);
        applicationForLeave.setStatus(ApplicationStatus.WAITING);

        service.reject(applicationForLeave, boss, comment);

        Assert.assertEquals("Wrong state", ApplicationStatus.REJECTED, applicationForLeave.getStatus());
        Assert.assertEquals("Wrong person", person, applicationForLeave.getPerson());
        Assert.assertEquals("Wrong boss", boss, applicationForLeave.getBoss());
        Assert.assertEquals("Wrong edited date", DateMidnight.now(), applicationForLeave.getEditedDate());

        Mockito.verify(applicationService).save(applicationForLeave);

        Mockito.verify(signService).signApplicationByBoss(Mockito.eq(applicationForLeave), Mockito.eq(boss));

        Mockito.verify(commentService).saveComment(Mockito.eq(comment), Mockito.eq(boss),
            Mockito.eq(applicationForLeave));
    }


    @Test
    public void ensureRejectingApplicationForLeaveSendsEmailToPerson() {

        Person person = new Person();
        Person boss = new Person();
        Comment comment = new Comment();

        Application applicationForLeave = new Application();
        applicationForLeave.setPerson(person);

        service.reject(applicationForLeave, boss, comment);

        Mockito.verify(mailService).sendRejectedNotification(Mockito.eq(applicationForLeave), Mockito.eq(comment));
    }


    // END: REJECT

    // START: CANCEL

    @Test
    public void ensureCancellingNotYetAllowedApplicationForLeaveChangesStateAndOtherAttributesButSendsNoEmail() {

        Person person = new Person();
        Comment comment = new Comment();

        Application applicationForLeave = new Application();
        applicationForLeave.setPerson(person);
        applicationForLeave.setStatus(ApplicationStatus.WAITING);

        service.cancel(applicationForLeave, person, comment);

        Assert.assertEquals("Wrong state", ApplicationStatus.CANCELLED, applicationForLeave.getStatus());
        Assert.assertEquals("Wrong person", person, applicationForLeave.getPerson());
        Assert.assertEquals("Wrong canceller", person, applicationForLeave.getCanceller());
        Assert.assertEquals("Wrong cancelled date", DateMidnight.now(), applicationForLeave.getCancelDate());
        Assert.assertFalse("Must be not set to formerly allowed", applicationForLeave.isFormerlyAllowed());

        Mockito.verify(applicationService).save(applicationForLeave);

        Mockito.verify(commentService).saveComment(Mockito.eq(comment), Mockito.eq(person),
            Mockito.eq(applicationForLeave));

        Mockito.verifyZeroInteractions(mailService);
    }


    @Test
    public void ensureCancellingAllowedApplicationForLeaveChangesStateAndOtherAttributesAndSendsAnEmail() {

        Person person = new Person();
        Person canceller = new Person();
        Comment comment = new Comment();

        Application applicationForLeave = new Application();
        applicationForLeave.setPerson(person);
        applicationForLeave.setStatus(ApplicationStatus.ALLOWED);

        service.cancel(applicationForLeave, canceller, comment);

        Assert.assertEquals("Wrong state", ApplicationStatus.CANCELLED, applicationForLeave.getStatus());
        Assert.assertEquals("Wrong person", person, applicationForLeave.getPerson());
        Assert.assertEquals("Wrong canceller", canceller, applicationForLeave.getCanceller());
        Assert.assertEquals("Wrong cancelled date", DateMidnight.now(), applicationForLeave.getCancelDate());
        Assert.assertTrue("Must be set to formerly allowed", applicationForLeave.isFormerlyAllowed());

        Mockito.verify(applicationService).save(applicationForLeave);

        Mockito.verify(commentService).saveComment(Mockito.eq(comment), Mockito.eq(canceller),
            Mockito.eq(applicationForLeave));

        Mockito.verify(mailService).sendCancelledNotification(Mockito.eq(applicationForLeave), Mockito.eq(false),
            Mockito.eq(comment));
    }


    @Test
    public void ensureCancellingNotYetAllowedApplicationForLeaveOnBehalfForSomeOneChangesStateAndOtherAttributesAndSendsAnEmail() {

        Person person = new Person();
        Person canceller = new Person();
        Comment comment = new Comment();

        Application applicationForLeave = new Application();
        applicationForLeave.setPerson(person);
        applicationForLeave.setStatus(ApplicationStatus.WAITING);

        service.cancel(applicationForLeave, canceller, comment);

        Assert.assertEquals("Wrong state", ApplicationStatus.CANCELLED, applicationForLeave.getStatus());
        Assert.assertEquals("Wrong person", person, applicationForLeave.getPerson());
        Assert.assertEquals("Wrong canceller", canceller, applicationForLeave.getCanceller());
        Assert.assertEquals("Wrong cancelled date", DateMidnight.now(), applicationForLeave.getCancelDate());
        Assert.assertFalse("Must not be set to formerly allowed", applicationForLeave.isFormerlyAllowed());

        Mockito.verify(applicationService).save(applicationForLeave);

        Mockito.verify(commentService).saveComment(Mockito.eq(comment), Mockito.eq(canceller),
            Mockito.eq(applicationForLeave));

        Mockito.verify(mailService).sendCancelledNotification(Mockito.eq(applicationForLeave), Mockito.eq(true),
            Mockito.eq(comment));
    }

    // END: CANCEL
}
