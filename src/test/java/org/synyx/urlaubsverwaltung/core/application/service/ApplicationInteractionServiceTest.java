package org.synyx.urlaubsverwaltung.core.application.service;

import com.google.common.base.Optional;

import org.joda.time.DateMidnight;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.synyx.urlaubsverwaltung.core.account.service.AccountInteractionService;
import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.core.application.domain.Comment;
import org.synyx.urlaubsverwaltung.core.mail.MailService;
import org.synyx.urlaubsverwaltung.core.person.Person;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class ApplicationInteractionServiceTest {

    private ApplicationInteractionService service;

    private ApplicationService applicationService;
    private CommentService commentService;
    private AccountInteractionService accountInteractionService;
    private SignService signService;
    private MailService mailService;

    @Before
    public void setUp() {

        applicationService = Mockito.mock(ApplicationService.class);
        commentService = Mockito.mock(CommentService.class);
        accountInteractionService = Mockito.mock(AccountInteractionService.class);
        signService = Mockito.mock(SignService.class);
        mailService = Mockito.mock(MailService.class);

        service = new ApplicationInteractionServiceImpl(applicationService, commentService, accountInteractionService,
                signService, mailService);
    }


    // START: APPLY

    @Test
    public void ensureApplyForLeaveChangesStateAndOtherAttributesAndSavesTheApplicationForLeave() {

        Person person = new Person();
        Person applier = new Person();
        Optional<String> comment = Optional.of("Foo");

        Application applicationForLeave = new Application();
        applicationForLeave.setPerson(person);
        applicationForLeave.setStartDate(new DateMidnight(2013, 2, 1));
        applicationForLeave.setEndDate(new DateMidnight(2013, 2, 5));

        service.apply(applicationForLeave, applier, comment);

        Assert.assertEquals("Wrong state", ApplicationStatus.WAITING, applicationForLeave.getStatus());
        Assert.assertEquals("Wrong person", person, applicationForLeave.getPerson());
        Assert.assertEquals("Wrong applier", applier, applicationForLeave.getApplier());
        Assert.assertEquals("Wrong application date", DateMidnight.now(), applicationForLeave.getApplicationDate());

        Mockito.verify(applicationService).save(applicationForLeave);

        Mockito.verify(signService).signApplicationByUser(Mockito.eq(applicationForLeave), Mockito.eq(applier));

        Mockito.verify(commentService).create(Mockito.eq(applicationForLeave), Mockito.eq(ApplicationStatus.WAITING),
            Mockito.eq(comment), Mockito.eq(applier));
    }


    @Test
    public void ensureSendsConfirmationEmailToPersonAndNotificationEmailToBossesWhenApplyingForOneself() {

        Person person = new Person();

        Application applicationForLeave = new Application();
        applicationForLeave.setPerson(person);
        applicationForLeave.setStartDate(new DateMidnight(2013, 2, 1));
        applicationForLeave.setEndDate(new DateMidnight(2013, 2, 5));

        service.apply(applicationForLeave, person, Optional.of("Foo"));

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
        applicationForLeave.setStartDate(new DateMidnight(2013, 2, 1));
        applicationForLeave.setEndDate(new DateMidnight(2013, 2, 5));

        service.apply(applicationForLeave, applier, Optional.of("Foo"));

        Mockito.verify(mailService, Mockito.never()).sendConfirmation(Mockito.eq(applicationForLeave));
        Mockito.verify(mailService).sendAppliedForLeaveByOfficeNotification(applicationForLeave);

        Mockito.verify(mailService).sendNewApplicationNotification(Mockito.eq(applicationForLeave));
    }


    @Test
    public void ensureApplyingForLeaveUpdatesTheRemainingVacationDays() {

        Person person = new Person();
        Person applier = new Person();
        Optional<String> comment = Optional.of("Foo");

        Application applicationForLeave = new Application();
        applicationForLeave.setPerson(person);
        applicationForLeave.setStartDate(new DateMidnight(2013, 2, 1));
        applicationForLeave.setEndDate(new DateMidnight(2013, 2, 5));

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

        Mockito.verify(commentService).create(Mockito.eq(applicationForLeave), Mockito.eq(ApplicationStatus.ALLOWED),
            Mockito.eq(comment), Mockito.eq(boss));
    }


    @Test
    public void ensureAllowingApplicationForLeaveSendsEmailToPerson() {

        Person person = new Person();
        Person boss = new Person();

        Application applicationForLeave = new Application();
        applicationForLeave.setPerson(person);

        service.allow(applicationForLeave, boss, Optional.of("Foo"));

        Mockito.verify(mailService).sendAllowedNotification(Mockito.eq(applicationForLeave),
            Mockito.any(Comment.class));
    }


    @Test
    public void ensureAllowingApplicationForLeaveWithRepresentativeSendsEmailToRepresentative() {

        Person person = new Person();
        Person rep = new Person();
        Person boss = new Person();

        Application applicationForLeave = new Application();
        applicationForLeave.setPerson(person);
        applicationForLeave.setHolidayReplacement(rep);

        service.allow(applicationForLeave, boss, Optional.of("Foo"));

        Mockito.verify(mailService).notifyHolidayReplacement(Mockito.eq(applicationForLeave));
    }


    // END: ALLOW

    // START: REJECT

    @Test
    public void ensureRejectingApplicationForLeaveChangesStateAndOtherAttributesAndSavesTheApplicationForLeave() {

        Person person = new Person();
        Person boss = new Person();
        Optional<String> comment = Optional.of("Foo");

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

        Mockito.verify(commentService).create(Mockito.eq(applicationForLeave), Mockito.eq(ApplicationStatus.REJECTED),
            Mockito.eq(comment), Mockito.eq(boss));
    }


    @Test
    public void ensureRejectingApplicationForLeaveSendsEmailToPerson() {

        Person person = new Person();
        Person boss = new Person();

        Application applicationForLeave = new Application();
        applicationForLeave.setPerson(person);

        service.reject(applicationForLeave, boss, Optional.of("Foo"));

        Mockito.verify(mailService).sendRejectedNotification(Mockito.eq(applicationForLeave),
            Mockito.any(Comment.class));
    }


    // END: REJECT

    // START: CANCEL

    @Test
    public void ensureCancellingNotYetAllowedApplicationForLeaveChangesStateAndOtherAttributesButSendsNoEmail() {

        Person person = new Person();
        Optional<String> comment = Optional.of("Foo");

        Application applicationForLeave = new Application();
        applicationForLeave.setPerson(person);
        applicationForLeave.setStatus(ApplicationStatus.WAITING);
        applicationForLeave.setStartDate(new DateMidnight(2015, 3, 24));
        applicationForLeave.setEndDate(new DateMidnight(2015, 4, 2));

        service.cancel(applicationForLeave, person, comment);

        Assert.assertEquals("Wrong state", ApplicationStatus.REVOKED, applicationForLeave.getStatus());
        Assert.assertEquals("Wrong person", person, applicationForLeave.getPerson());
        Assert.assertEquals("Wrong canceller", person, applicationForLeave.getCanceller());
        Assert.assertEquals("Wrong cancelled date", DateMidnight.now(), applicationForLeave.getCancelDate());
        Assert.assertFalse("Must not be formerly allowed", applicationForLeave.isFormerlyAllowed());

        Mockito.verify(applicationService).save(applicationForLeave);

        Mockito.verify(commentService).create(Mockito.eq(applicationForLeave), Mockito.eq(ApplicationStatus.REVOKED),
            Mockito.eq(comment), Mockito.eq(person));

        Mockito.verifyZeroInteractions(mailService);
    }


    @Test
    public void ensureCancellingAllowedApplicationForLeaveChangesStateAndOtherAttributesAndSendsAnEmail() {

        Person person = new Person();
        Person canceller = new Person();
        Optional<String> comment = Optional.of("Foo");

        Application applicationForLeave = new Application();
        applicationForLeave.setPerson(person);
        applicationForLeave.setStatus(ApplicationStatus.ALLOWED);
        applicationForLeave.setStartDate(new DateMidnight(2015, 3, 24));
        applicationForLeave.setEndDate(new DateMidnight(2015, 4, 2));

        service.cancel(applicationForLeave, canceller, comment);

        Assert.assertEquals("Wrong state", ApplicationStatus.CANCELLED, applicationForLeave.getStatus());
        Assert.assertEquals("Wrong person", person, applicationForLeave.getPerson());
        Assert.assertEquals("Wrong canceller", canceller, applicationForLeave.getCanceller());
        Assert.assertEquals("Wrong cancelled date", DateMidnight.now(), applicationForLeave.getCancelDate());
        Assert.assertTrue("Must be formerly allowed", applicationForLeave.isFormerlyAllowed());

        Mockito.verify(applicationService).save(applicationForLeave);

        Mockito.verify(commentService).create(Mockito.eq(applicationForLeave), Mockito.eq(ApplicationStatus.CANCELLED),
            Mockito.eq(comment), Mockito.eq(canceller));

        Mockito.verify(mailService).sendCancelledNotification(Mockito.eq(applicationForLeave), Mockito.eq(false),
            Mockito.any(Comment.class));
    }


    @Test
    public void ensureCancellingNotYetAllowedApplicationForLeaveOnBehalfForSomeOneChangesStateAndOtherAttributesAndSendsAnEmail() {

        Person person = new Person();
        Person canceller = new Person();
        Optional<String> comment = Optional.of("Foo");

        Application applicationForLeave = new Application();
        applicationForLeave.setPerson(person);
        applicationForLeave.setStatus(ApplicationStatus.WAITING);
        applicationForLeave.setStartDate(new DateMidnight(2015, 3, 24));
        applicationForLeave.setEndDate(new DateMidnight(2015, 4, 2));

        service.cancel(applicationForLeave, canceller, comment);

        Assert.assertEquals("Wrong state", ApplicationStatus.REVOKED, applicationForLeave.getStatus());
        Assert.assertEquals("Wrong person", person, applicationForLeave.getPerson());
        Assert.assertEquals("Wrong canceller", canceller, applicationForLeave.getCanceller());
        Assert.assertEquals("Wrong cancelled date", DateMidnight.now(), applicationForLeave.getCancelDate());
        Assert.assertFalse("Must not be formerly allowed", applicationForLeave.isFormerlyAllowed());

        Mockito.verify(applicationService).save(applicationForLeave);

        Mockito.verify(commentService).create(Mockito.eq(applicationForLeave), Mockito.eq(ApplicationStatus.REVOKED),
            Mockito.eq(comment), Mockito.eq(canceller));

        Mockito.verify(mailService).sendCancelledNotification(Mockito.eq(applicationForLeave), Mockito.eq(true),
            Mockito.any(Comment.class));
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

        service.cancel(applicationForLeave, canceller, comment);

        Mockito.verify(accountInteractionService).updateRemainingVacationDays(2014, person);
    }

    // END: CANCEL
}
