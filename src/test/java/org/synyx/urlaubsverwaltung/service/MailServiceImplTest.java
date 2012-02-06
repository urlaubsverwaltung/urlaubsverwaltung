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

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;


/**
 * This is a test for MailService implementation for the properties file: messages_de.properties.
 *
 * @author  Aljona Murygina
 */
public class MailServiceImplTest {

    private static final String FILE_PATH = "messages_de.properties";

    private MailServiceImpl instance;
    private JavaMailSender mailSender = new JavaMailSenderImpl();
    private VelocityEngine velocityEngine = new VelocityEngine();

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
    public void setUp() {

        instance = new MailServiceImpl(mailSender, velocityEngine);
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
        List<Message> inbox = Mailbox.get("email.boss");
        assertTrue(inbox.size() > 0);

        // get email
        Message msg = inbox.get(0);

        // check subject
        assertEquals("Es liegen neue Urlaubsanträge vor", msg.getSubject());
        assertNotSame("subject", msg.getSubject());

        // check from and recipient
        assertEquals(new InternetAddress("email.boss"), msg.getAllRecipients()[0]);

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

        List<Application> applications = new ArrayList<Application>();
        applications.add(application);

        instance.sendAllowedNotification(application);

        // were both emails sent?
        List<Message> inboxOffice = Mailbox.get("email.office");
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
        assertEquals(new InternetAddress("email.office"), msgOffice.getAllRecipients()[0]);

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

        List<Application> applications = new ArrayList<Application>();
        applications.add(application);

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

        List<Application> applications = new ArrayList<Application>();
        applications.add(application);

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
        List<Message> inbox = Mailbox.get("email.all");
        assertTrue(inbox.size() > 0);

        Message msg = inbox.get(0);

        // check subject
        assertEquals("Diese Woche im Urlaub", msg.getSubject());
        assertNotSame("subject", msg.getSubject());

        // check from and recipient
        assertEquals(new InternetAddress("email.all"), msg.getAllRecipients()[0]);

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

        List<Application> applications = new ArrayList<Application>();
        applications.add(application);

        // test for isBoss == true
        instance.sendCancelledNotification(application, true);

        // was email sent?
        List<Message> inboxChef = Mailbox.get("email.boss");
        assertTrue(inboxChef.size() > 0);

        Message msg = inboxChef.get(0);

        // check subject
        assertEquals("Antrag wurde storniert", msg.getSubject());
        assertNotSame("subject", msg.getSubject());

        // check from and recipient
        assertEquals(new InternetAddress("email.boss"), msg.getAllRecipients()[0]);

        // check content of email
        String content = (String) msg.getContent();
        assertTrue(content.contains("Heinrich"));
        assertTrue(content.contains("storniert"));
        assertFalse(content.contains("Mist"));
    }

//    @Test
//    public void testGetProperty() {
//
//        File propertiesFile = new File(FILE_PATH);
//
//        String key = "vac.holiday";
//
//        String returnValue = instance.getProperty(propertiesFile, key);
//
//        assertNotNull(returnValue);
//        assertEquals("Erholungsurlaub", returnValue);
//
//        key = "vac.overtime";
//
//        returnValue = instance.getProperty(propertiesFile, key);
//
//        assertNotNull(returnValue);
//        assertEquals("Überstunden abbummeln", returnValue);
//
//        key = "unsinn";
//
//        returnValue = instance.getProperty(propertiesFile, key);
//
//        assertNull(returnValue);
//        assertNotSame("Erholungsurlaub", returnValue);
//    }
}
