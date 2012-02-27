/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.service;

import org.apache.velocity.app.VelocityEngine;

import org.junit.After;
import org.junit.AfterClass;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.jvnet.mock_javamail.Mailbox;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import org.synyx.urlaubsverwaltung.domain.Application;
import org.synyx.urlaubsverwaltung.domain.DayLength;
import org.synyx.urlaubsverwaltung.domain.Person;
import org.synyx.urlaubsverwaltung.domain.VacationType;
import org.synyx.urlaubsverwaltung.util.PropertiesUtil;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;


/**
 * This is a test for MailService implementation for the properties file: messages_de.properties.
 *
 * @author  Aljona Murygina
 */
public class MailServiceImplTest {

    private static final String MAIL_PROPERTIES_FILE = "mail.properties"; // custom configuration like email
                                                                          // addresses, etc.

    private MailServiceImpl instance;
    private JavaMailSender mailSender = new JavaMailSenderImpl();
    private VelocityEngine velocityEngine = new VelocityEngine();
    private Properties mailProperties;

    private Person person;
    private Application application;

    public MailServiceImplTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }


    @AfterClass
    public static void tearDownClass() throws Exception {
    }


    @Before
    public void setUp() throws Exception {

        instance = new MailServiceImpl(mailSender, velocityEngine);
        mailProperties = PropertiesUtil.load(MAIL_PROPERTIES_FILE);
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


    /** Test of sendExpireNotification method, of class MailServiceImpl. */
    @Test
    public void testSendExpireNotification() throws MessagingException, IOException {

        person.setLastName("Test");
        person.setFirstName("Günther");
        person.setEmail("guentherklein@test.com");

        List<Person> persons = new ArrayList<Person>();
        persons.add(person);

        instance.sendExpireNotification(persons);

        // was email sent?
        List<Message> inbox = Mailbox.get("guentherklein@test.com");
        assertTrue(inbox.size() > 0);

        // get email
        Message msg = inbox.get(0);

        // check subject
        assertEquals("Erinnerung Resturlaub", msg.getSubject());
        assertNotSame("subject", msg.getSubject());

        // check from and recipient
        assertEquals(new InternetAddress("guentherklein@test.com"), msg.getAllRecipients()[0]);

        // check content of email
        String content = (String) msg.getContent();
        assertTrue(content.contains("Günther"));
        assertTrue(content.contains("Test"));
        assertTrue(content.contains("Kalenderjahr"));
        assertFalse(content.contains("Mist"));
    }


    /** Test of sendNewApplicationNotification method, of class MailServiceImpl. */
    @Test
    public void testSendNewApplicationNotification() throws MessagingException, IOException {

        person.setLastName("Antragsteller");
        person.setFirstName("Horst");
        person.setEmail("misterhorst@test.com");

        instance.sendNewApplicationNotification(application);

        // was email sent?
        List<Message> inbox = Mailbox.get(mailProperties.getProperty("email.boss"));
        assertTrue(inbox.size() > 0);

        // get email
        Message msg = inbox.get(0);

        // check subject
        assertEquals("Neuer Urlaubsantrag", msg.getSubject());
        assertNotSame("subject", msg.getSubject());

        // check from and recipient
        assertEquals(new InternetAddress(mailProperties.getProperty("email.boss")), msg.getAllRecipients()[0]);

        // check content of email
        String content = (String) msg.getContent();
        assertTrue(content.contains("Horst"));
        assertTrue(content.contains("Antragsteller"));
        assertTrue(content.contains("Antragsstellung"));
        assertFalse(content.contains("Mist"));
    }


    /** Test of sendAllowedNotification method, of class MailServiceImpl. */
    @Test
    public void testSendAllowedNotification() throws MessagingException, IOException {

        person.setLastName("Test");
        person.setFirstName("Bernd");
        person.setEmail("berndo@test.com");

        instance.sendAllowedNotification(application);

        // were both emails sent?
        List<Message> inboxOffice = Mailbox.get(mailProperties.getProperty("email.office"));
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
        assertEquals(new InternetAddress(mailProperties.getProperty("email.office")), msgOffice.getAllRecipients()[0]);

        // check content of email
        String contentOfficeMail = (String) msgOffice.getContent();
        assertTrue(contentOfficeMail.contains("Bernd"));
        assertTrue(contentOfficeMail.contains("Office"));
        assertTrue(contentOfficeMail.contains("es liegt ein neuer genehmigter Antrag vor"));
        assertFalse(contentOfficeMail.contains("Mist"));
    }


    /** Test of sendRejectedNotification method, of class MailServiceImpl. */
    @Test
    public void testSendRejectedNotification() throws MessagingException, IOException {

        person.setLastName("Test");
        person.setFirstName("Franz");
        person.setEmail("franzi@test.com");

        instance.sendRejectedNotification(application);

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


    /** Test of sendConfirmation method, of class MailServiceImpl. */
    @Test
    public void testSendConfirmation() throws MessagingException, IOException {

        person.setLastName("Test");
        person.setFirstName("Hildegard");
        person.setEmail("hilde@test.com");

        instance.sendConfirmation(application);

        // was email sent?
        List<Message> inbox = Mailbox.get("hilde@test.com");
        assertTrue(inbox.size() > 0);

        Message msg = inbox.get(0);

        // check subject
        assertEquals("Bestätigung Antragsstellung", msg.getSubject());
        assertNotSame("subject", msg.getSubject());

        // check from and recipient
        assertEquals(new InternetAddress("hilde@test.com"), msg.getAllRecipients()[0]);

        // check content of email
        String content = (String) msg.getContent();
        assertTrue(content.contains("Hildegard"));
        assertTrue(content.contains("dein Urlaubsantrag wurde erfolgreich eingereicht"));
        assertFalse(content.contains("Mist"));
    }


    /** Test of sendWeeklyVacationForecast method, of class MailServiceImpl. */
    @Test
    public void testSendWeeklyVacationForecast() throws MessagingException, IOException {

        person.setLastName("Test");
        person.setFirstName("Heinz");

        Person newPerson = new Person();
        newPerson.setLastName("Dingsda");
        newPerson.setFirstName("Aha");

        List<Person> persons = new ArrayList<Person>();
        persons.add(person);
        persons.add(newPerson);

        instance.sendWeeklyVacationForecast(persons);

        // was email sent?
        List<Message> inbox = Mailbox.get(mailProperties.getProperty("email.all"));
        assertTrue(inbox.size() > 0);

        Message msg = inbox.get(0);

        // check subject
        assertEquals("Diese Woche im Urlaub", msg.getSubject());
        assertNotSame("subject", msg.getSubject());

        // check from and recipient
        assertEquals(new InternetAddress(mailProperties.getProperty("email.all")), msg.getAllRecipients()[0]);

        // check content of email
        String content = (String) msg.getContent();
        assertTrue(content.contains("Sternchen"));
        assertTrue(content.contains("Dingsda"));
        assertTrue(content.contains("Aha"));
        assertTrue(content.contains("Test"));
        assertTrue(content.contains("Heinz"));
        assertFalse(content.contains("Hunz"));
        assertTrue(content.contains("folgende Mitarbeiter sind diese Woche im Urlaub"));
        assertFalse(content.contains("Mist"));
    }


    /** Test of sendCancelledNotification method, of class MailServiceImpl. */
    @Test
    public void testSendCancelledNotification() throws MessagingException, IOException {

        person.setLastName("Test");
        person.setFirstName("Heinrich");

        // test for cancelledByOffice == false
        // i.e. office gets a mail

        instance.sendCancelledNotification(application, false);

        // was email sent?
        List<Message> inboxOffice = Mailbox.get(mailProperties.getProperty("email.office"));
        assertTrue(inboxOffice.size() > 0);

        Message msg = inboxOffice.get(0);

        // check subject
        assertEquals("Ein Antrag wurde storniert", msg.getSubject());
        assertNotSame("subject", msg.getSubject());

        // check from and recipient
        assertEquals(new InternetAddress(mailProperties.getProperty("email.office")), msg.getAllRecipients()[0]);

        // check content of email
        String content = (String) msg.getContent();
        assertTrue(content.contains("Der Mitarbeiter Heinrich Test hat seinen Urlaubsantrag storniert"));
        assertFalse(content.contains("Mist"));

        // test for cancelledByOffice == true
        // i.e. applicant gets a mail

        person.setLastName("Mann");
        person.setFirstName("Muster");
        person.setEmail("muster@mann.de");

        Person office = new Person();
        office.setLastName("Musteroffice");
        office.setFirstName("Magdalena");

        application.setOffice(office);

        instance.sendCancelledNotification(application, true);

        // was email sent?
        List<Message> inboxApplicant = Mailbox.get("muster@mann.de");
        assertTrue(inboxApplicant.size() > 0);

        msg = inboxApplicant.get(0);

        // check subject
        assertEquals("Dein Antrag wurde storniert", msg.getSubject());
        assertNotSame("subject", msg.getSubject());

        // check from and recipient
        assertEquals(new InternetAddress("muster@mann.de"), msg.getAllRecipients()[0]);

        // check content of email
        content = (String) msg.getContent();
        assertTrue(content.contains("dein Urlaubsantrag wurde von Magdalena"));
        assertTrue(content.contains("wende dich bitte direkt an Magdalena"));
        assertFalse(content.contains("Mist"));
    }


    /** Test of sendKeyGeneratingErrorNotification method, of class MailServiceImpl. */
    @Test
    public void testSendKeyGeneratingErrorNotification() throws AddressException, MessagingException, IOException {

        instance.sendKeyGeneratingErrorNotification("müller");

        List<Message> inbox = Mailbox.get(mailProperties.getProperty("email.manager"));
        assertTrue(inbox.size() > 0);

        Message msg = inbox.get(0);

        assertEquals("Fehler beim Generieren der Keys", msg.getSubject());

        String content = (String) msg.getContent();

        assertTrue(content.contentEquals(
                "An error occured during key generation for person with login müller failed."));
    }


    /** Test of sendSignErrorNotification method, of class MailServiceImpl. */
    @Test
    public void testSendSignErrorNotification() throws AddressException, MessagingException, IOException {

        instance.sendSignErrorNotification(5, "test exception message");

        List<Message> inbox = Mailbox.get(mailProperties.getProperty("email.manager"));
        assertTrue(inbox.size() > 0);

        Message msg = inbox.get(0);

        assertEquals("Fehler beim Signieren eines Antrags", msg.getSubject());

        String content = (String) msg.getContent();

        assertTrue(content.contains("An error occured while signing the application with id 5"));
    }


    /** Test of sendAppliedForLeaveByOfficeNotification method, of class MailServiceImpl. */
    @Test
    public void testSendAppliedForLeaveByOfficeNotification() throws AddressException, MessagingException, IOException {

        person.setLastName("Müller");
        person.setFirstName("Günther");
        person.setEmail("bla@test.com");

        instance.sendAppliedForLeaveByOfficeNotification(application);

        // was email sent?
        List<Message> inbox = Mailbox.get("bla@test.com");
        assertTrue(inbox.size() > 0);

        Message msg = inbox.get(0);

        // check subject
        assertEquals("Für dich wurde ein Urlaubsantrag eingereicht", msg.getSubject());
        assertNotSame("subject", msg.getSubject());

        // check from and recipient
        assertEquals(new InternetAddress("bla@test.com"), msg.getAllRecipients()[0]);

        // check content of email
        String content = (String) msg.getContent();
        assertTrue(content.contains("Hallo Günther"));
        assertTrue(content.contains("das Office hat einen Urlaubsantrag"));
        assertFalse(content.contains("Mist"));
    }
}
