
package org.synyx.urlaubsverwaltung.core.mail;

import org.apache.velocity.app.VelocityEngine;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.jvnet.mock_javamail.Mailbox;

import org.mockito.Mockito;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import org.springframework.util.ReflectionUtils;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;

import java.io.IOException;

import java.lang.reflect.Field;

import java.util.Arrays;
import java.util.List;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;

import static org.junit.Assert.*;


/**
 * @author  Aljona Murygina
 */
public class MailServiceIntegrationTest {

    private MailServiceImpl mailService;
    private JavaMailSender mailSender;
    private VelocityEngine velocityEngine;
    private PersonService personService;

    private Person person;
    private Application application;

    @Before
    public void setUp() throws Exception {

        velocityEngine = new VelocityEngine();
        mailSender = new JavaMailSenderImpl();
        personService = Mockito.mock(PersonService.class);

        mailService = new MailServiceImpl(mailSender, velocityEngine, personService);

        person = new Person();
        application = new Application();
        application.setPerson(person);
        application.setVacationType(VacationType.HOLIDAY);
        application.setHowLong(DayLength.FULL);
    }


    @After
    public void tearDown() {

        Mailbox.clearAll();
    }


    @Test
    public void ensureNotificationAboutNewApplicationIsSentToBosses() throws MessagingException, IOException {

        person.setLastName("Antragsteller");
        person.setFirstName("Horst");
        person.setEmail("misterhorst@test.com");

        String bossEmailAddress = "boss@boss.de";
        Person boss = new Person("boss", "Muster", "Max", bossEmailAddress);

        Mockito.when(personService.getPersonsWithNotificationType(MailNotification.NOTIFICATION_BOSS)).thenReturn(Arrays
            .asList(boss));

        mailService.sendNewApplicationNotification(application);

        // was email sent?
        List<Message> inbox = Mailbox.get(bossEmailAddress);
        assertTrue(inbox.size() > 0);

        // get email
        Message msg = inbox.get(0);

        // check subject
        assertEquals("Neuer Urlaubsantrag", msg.getSubject());
        assertNotSame("subject", msg.getSubject());

        // check from and recipient
        assertEquals(new InternetAddress(bossEmailAddress), msg.getAllRecipients()[0]);

        // check content of email
        String content = (String) msg.getContent();
        assertTrue(content.contains("Horst"));
        assertTrue(content.contains("Antragsteller"));
        assertTrue(content.contains("Antragsstellung"));
        assertFalse(content.contains("Mist"));
    }


    @Test
    public void ensureNotificationAboutAllowedApplicationIsSentToOfficeAndThePerson() throws MessagingException,
        IOException {

        person.setLastName("Test");
        person.setFirstName("Bernd");
        person.setEmail("berndo@test.com");

        String officeEmailAddress = "office@synyx.de";
        Person office = new Person("office", "Muster", "Max", officeEmailAddress);

        Mockito.when(personService.getPersonsWithNotificationType(MailNotification.NOTIFICATION_OFFICE)).thenReturn(
            Arrays.asList(office));

        mailService.sendAllowedNotification(application, null);

        // were both emails sent?
        List<Message> inboxOffice = Mailbox.get(officeEmailAddress);
        assertTrue(inboxOffice.size() > 0);

        List<Message> inboxUser = Mailbox.get("berndo@test.com");
        assertTrue(inboxUser.size() > 0);

        // get email user
        Message msg = inboxUser.get(0);

        // check subject
        assertEquals("Dein Urlaubsantrag wurde bewilligt", msg.getSubject());
        assertNotSame("subject", msg.getSubject());

        // check from and recipient
        assertEquals(new InternetAddress("berndo@test.com"), msg.getAllRecipients()[0]);

        // check content of email
        String content = (String) msg.getContent();
        assertTrue(content.contains("Bernd"));
        assertTrue(content.contains("gestellter Antrag wurde von"));
        assertTrue(content.contains("genehmigt"));
        assertFalse(content.contains("Mist"));

        // get email office
        Message msgOffice = inboxOffice.get(0);

        // check subject
        assertEquals("Neuer bewilligter Antrag", msgOffice.getSubject());
        assertNotSame("subject", msgOffice.getSubject());

        // check from and recipient
        assertEquals(new InternetAddress(officeEmailAddress), msgOffice.getAllRecipients()[0]);

        // check content of email
        String contentOfficeMail = (String) msgOffice.getContent();
        assertTrue(contentOfficeMail.contains("Bernd"));
        assertTrue(contentOfficeMail.contains("Office"));
        assertTrue(contentOfficeMail.contains("es liegt ein neuer genehmigter Antrag vor"));
        assertFalse(contentOfficeMail.contains("Mist"));
    }


    @Test
    public void ensureNotificationAboutRejectedApplicationIsSentToPerson() throws MessagingException, IOException {

        person.setLastName("Test");
        person.setFirstName("Franz");
        person.setEmail("franzi@test.com");

        mailService.sendRejectedNotification(application, null);

        // was email sent?
        List<Message> inbox = Mailbox.get("franzi@test.com");
        assertTrue(inbox.size() > 0);

        Message msg = inbox.get(0);

        // check subject
        assertEquals("Dein Urlaubsantrag wurde abgelehnt", msg.getSubject());
        assertNotSame("subject", msg.getSubject());

        // check from and recipient
        assertEquals(new InternetAddress("franzi@test.com"), msg.getAllRecipients()[0]);

        // check content of email
        String content = (String) msg.getContent();
        assertTrue(content.contains("Franz"));
        assertTrue(content.contains("abgelehnt"));
        assertFalse(content.contains("Mist"));
    }


    @Test
    public void ensureAfterApplyingForLeaveAConfirmationNotificationIsSentToPerson() throws MessagingException,
        IOException {

        person.setLastName("Test");
        person.setFirstName("Hildegard");
        person.setEmail("hilde@test.com");

        mailService.sendConfirmation(application);

        // was email sent?
        List<Message> inbox = Mailbox.get("hilde@test.com");
        assertTrue(inbox.size() > 0);

        Message msg = inbox.get(0);

        // check subject
        assertTrue(msg.getSubject().contains("Antragsstellung"));
        assertNotSame("subject", msg.getSubject());

        // check from and recipient
        assertEquals(new InternetAddress("hilde@test.com"), msg.getAllRecipients()[0]);

        // check content of email
        String content = (String) msg.getContent();
        assertTrue(content.contains("Hildegard"));
        assertTrue(content.contains("dein Urlaubsantrag wurde erfolgreich eingereicht"));
        assertFalse(content.contains("Mist"));
    }


    @Test
    public void ensurePersonGetsANotificationIfOfficeCancelledOneOfHisApplications() throws MessagingException,
        IOException {

        person.setLastName("Mann");
        person.setFirstName("Muster");
        person.setEmail("muster@mann.de");

        Person office = new Person();
        office.setLastName("Musteroffice");
        office.setFirstName("Magdalena");

        application.setCanceller(office);

        boolean cancelledByOffice = true;
        mailService.sendCancelledNotification(application, cancelledByOffice, null);

        // was email sent?
        List<Message> inboxApplicant = Mailbox.get("muster@mann.de");
        assertTrue(inboxApplicant.size() > 0);

        Message msg = inboxApplicant.get(0);

        // check subject
        assertEquals("Dein Antrag wurde storniert", msg.getSubject());
        assertNotSame("subject", msg.getSubject());

        // check from and recipient
        assertEquals(new InternetAddress("muster@mann.de"), msg.getAllRecipients()[0]);

        // check content of email
        String content = (String) msg.getContent();
        assertTrue(content.contains("dein Urlaubsantrag wurde von Magdalena"));
        assertTrue(content.contains("wende dich bitte direkt an Magdalena"));
        assertFalse(content.contains("Mist"));
    }


    @Test
    public void ensureOfficeMembersGetANotificationIfAPersonCancelsAnAllowedApplication() throws MessagingException,
        IOException {

        person.setLastName("Test");
        person.setFirstName("Heinrich");

        String officeEmailAddress = "office@office.de";
        Person office = new Person("office", "Office", "Marlene", officeEmailAddress);

        Mockito.when(personService.getPersonsWithNotificationType(MailNotification.NOTIFICATION_OFFICE)).thenReturn(
            Arrays.asList(office));

        boolean cancelledByOffice = false;
        mailService.sendCancelledNotification(application, cancelledByOffice, null);

        // ENSURE OFFICE MEMBERS HAVE GOT CORRECT EMAIL
        List<Message> inboxOffice = Mailbox.get(officeEmailAddress);
        assertTrue(inboxOffice.size() > 0);

        Message msg = inboxOffice.get(0);

        assertEquals("Ein Antrag wurde storniert", msg.getSubject());
        assertNotSame("subject", msg.getSubject());

        assertEquals(new InternetAddress(officeEmailAddress), msg.getAllRecipients()[0]);

        String content = (String) msg.getContent();
        assertTrue(content.contains("Der Urlaubsantrag von Heinrich Test"));
        assertTrue(content.contains("wurde storniert"));
        assertFalse(content.contains("Mist"));
    }


    @Test
    public void ensureTechnicalManagerGetsANotificationIfAKeyGeneratingErrorOccurred() throws MessagingException,
        IOException {

        mailService.emailManager = "manager@uv.de";
        mailService.sendKeyGeneratingErrorNotification("horscht");

        List<Message> inbox = Mailbox.get("manager@uv.de");
        assertTrue(inbox.size() > 0);

        Message msg = inbox.get(0);

        assertEquals("Fehler beim Generieren der Keys", msg.getSubject());

        String content = (String) msg.getContent();

        assertTrue(content.contentEquals(
                "An error occured during key generation for person with login horscht failed."));
    }


    @Test
    public void ensureTechnicalManagerGetsANotificationIfASignErrorOccurred() throws MessagingException, IOException {

        mailService.emailManager = "manager@uv.de";
        mailService.sendSignErrorNotification(5, "test exception message");

        List<Message> inbox = Mailbox.get("manager@uv.de");
        assertTrue(inbox.size() > 0);

        Message msg = inbox.get(0);

        assertEquals("Fehler beim Signieren eines Antrags", msg.getSubject());

        String content = (String) msg.getContent();

        assertTrue(content.contains("An error occured while signing the application with id 5"));
    }


    @Test
    public void ensurePersonGetsANotificationIfAnOfficeMemberAppliedForLeaveForThisPerson() throws MessagingException,
        IOException {

        person.setLastName("Müller");
        person.setFirstName("Günther");
        person.setEmail("bla@test.com");

        Person applier = new Person();
        applier.setFirstName("Hans");
        applier.setLastName("Wurst");

        application.setApplier(applier);
        mailService.sendAppliedForLeaveByOfficeNotification(application);

        // was email sent?
        List<Message> inbox = Mailbox.get("bla@test.com");
        assertTrue(inbox.size() > 0);

        Message msg = inbox.get(0);

        // check subject
        assertTrue(msg.getSubject().contains("dich wurde ein Urlaubsantrag eingereicht"));
        assertNotSame("subject", msg.getSubject());

        // check from and recipient
        assertEquals(new InternetAddress("bla@test.com"), msg.getAllRecipients()[0]);

        // check content of email
        String content = (String) msg.getContent();
        assertTrue(content.contains("Hans Wurst hat einen Urlaubsantrag"));
        assertFalse(content.contains("Mist"));
    }
}
