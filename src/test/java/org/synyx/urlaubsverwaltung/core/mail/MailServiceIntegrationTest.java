
package org.synyx.urlaubsverwaltung.core.mail;

import org.apache.velocity.app.VelocityEngine;

import org.joda.time.DateMidnight;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.jvnet.mock_javamail.Mailbox;

import org.mockito.Mockito;

import org.springframework.mail.javamail.JavaMailSenderImpl;

import org.synyx.urlaubsverwaltung.core.account.domain.Account;
import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.core.application.domain.Comment;
import org.synyx.urlaubsverwaltung.core.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.core.department.DepartmentService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.settings.Settings;
import org.synyx.urlaubsverwaltung.core.settings.SettingsService;
import org.synyx.urlaubsverwaltung.core.sync.CalendarType;
import org.synyx.urlaubsverwaltung.core.sync.absence.Absence;
import org.synyx.urlaubsverwaltung.core.sync.absence.AbsenceTimeConfiguration;

import java.io.IOException;

import java.math.BigDecimal;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * @author  Aljona Murygina
 */
public class MailServiceIntegrationTest {

    private MailServiceImpl mailService;
    private PersonService personService;
    private DepartmentService departmentService;

    private Person person;
    private Application application;
    private Settings settings;

    @Before
    public void setUp() {

        Properties velocityProperties = new Properties();
        velocityProperties.put("resource.loader", "class");
        velocityProperties.put("class.resource.loader.class",
            "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        velocityProperties.put("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.Log4JLogChute");
        velocityProperties.put("runtime.log.logsystem.log4j.logger", MailServiceIntegrationTest.class.getName());

        VelocityEngine velocityEngine = new VelocityEngine(velocityProperties);
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        personService = Mockito.mock(PersonService.class);
        departmentService = Mockito.mock(DepartmentService.class);

        SettingsService settingsService = Mockito.mock(SettingsService.class);

        mailService = new MailServiceImpl(mailSender, velocityEngine, personService, departmentService, settingsService,
                "localhorscht/");

        DateMidnight now = DateMidnight.now();

        person = new Person();
        application = new Application();
        application.setPerson(person);
        application.setVacationType(VacationType.HOLIDAY);
        application.setDayLength(DayLength.FULL);
        application.setApplicationDate(now);
        application.setStartDate(now);
        application.setEndDate(now);

        settings = new Settings();
        settings.getMailSettings().setActive(true);
        Mockito.when(settingsService.getSettings()).thenReturn(settings);
    }


    @After
    public void tearDown() {

        Mailbox.clearAll();
    }


    @Test
    public void ensureNotificationAboutNewApplicationIsSentToBossesAndDepartmentHeads() throws MessagingException,
        IOException {

        person.setLastName("Antragsteller");
        person.setFirstName("Horst");
        person.setEmail("misterhorst@test.com");

        String bossEmailAddress = "boss@boss.de";
        Person boss = new Person("boss", "Muster", "Max", bossEmailAddress);

        String departmentHeadEmailAddress = "head@muster.de";
        Person departmentHead = new Person("head", "Muster", "Michel", departmentHeadEmailAddress);

        Mockito.when(personService.getPersonsWithNotificationType(MailNotification.NOTIFICATION_BOSS))
            .thenReturn(Collections.singletonList(boss));

        Mockito.when(personService.getPersonsWithNotificationType(MailNotification.NOTIFICATION_DEPARTMENT_HEAD))
            .thenReturn(Collections.singletonList(departmentHead));

        Mockito.when(departmentService.isDepartmentHeadOfPerson(Mockito.eq(departmentHead), Mockito.any(Person.class)))
            .thenReturn(true);

        String commentMessage = "Das ist ein Kommentar.";
        Comment comment = new Comment(person);
        comment.setText(commentMessage);

        mailService.sendNewApplicationNotification(application, comment);

        // was email sent to boss?
        List<Message> inboxOfBoss = Mailbox.get(bossEmailAddress);
        assertTrue("Boss should get the email", inboxOfBoss.size() > 0);

        // was email sent to department head?
        List<Message> inboxOfDepartmentHead = Mailbox.get(departmentHeadEmailAddress);
        assertTrue("Department head should get the email", inboxOfDepartmentHead.size() > 0);

        // get email
        Message msg = inboxOfDepartmentHead.get(0);

        // check subject
        assertEquals("Neuer Urlaubsantrag", msg.getSubject());

        // check from and recipient
        assertEquals(new InternetAddress(bossEmailAddress), msg.getAllRecipients()[0]);

        // check content of email
        String content = (String) msg.getContent();
        assertTrue(content.contains("Horst"));
        assertTrue(content.contains("Antragsteller"));
        assertTrue(content.contains("Antragsstellung"));
        assertTrue("No comment in mail content", content.contains(commentMessage));
        assertTrue("Wrong comment author", content.contains(comment.getPerson().getNiceName()));
    }


    @Test
    public void ensureNotificationAboutNewApplicationContainsInformationAboutDepartmentVacations()
        throws MessagingException, IOException {

        person.setLastName("Antragsteller");
        person.setFirstName("Horst");
        person.setEmail("misterhorst@test.com");

        String bossEmailAddress = "boss@boss.de";
        Person boss = new Person("boss", "Muster", "Max", bossEmailAddress);

        Person departmentMember = new Person("muster", "Muster", "Marlene", "");
        Application departmentApplication = new Application();
        departmentApplication.setStartDate(new DateMidnight(2015, 11, 5));
        departmentApplication.setEndDate(new DateMidnight(2015, 11, 6));
        departmentApplication.setDayLength(DayLength.FULL);
        departmentApplication.setPerson(departmentMember);

        Person otherDepartmentMember = new Person("schmidt", "Schmidt", "Niko", "");
        Application otherDepartmentApplication = new Application();
        otherDepartmentApplication.setStartDate(new DateMidnight(2015, 11, 4));
        otherDepartmentApplication.setEndDate(new DateMidnight(2015, 11, 4));
        otherDepartmentApplication.setDayLength(DayLength.MORNING);
        otherDepartmentApplication.setPerson(otherDepartmentMember);

        Mockito.when(personService.getPersonsWithNotificationType(MailNotification.NOTIFICATION_BOSS))
            .thenReturn(Collections.singletonList(boss));

        Mockito.when(departmentService.getApplicationsForLeaveOfMembersInDepartmentsOfPerson(Mockito.eq(person),
                    Mockito.any(DateMidnight.class), Mockito.any(DateMidnight.class)))
            .thenReturn(Arrays.asList(departmentApplication, otherDepartmentApplication));

        mailService.sendNewApplicationNotification(application, null);

        List<Message> inboxOfBoss = Mailbox.get(bossEmailAddress);
        Message message = inboxOfBoss.get(0);

        String content = (String) message.getContent();

        assertTrue(content.contains("Marlene Muster: 05.11.2015 bis 06.11.2015"));
        assertTrue(content.contains("Niko Schmidt: 04.11.2015 bis 04.11.2015"));
    }


    @Test
    public void ensureNotificationAboutAllowedApplicationIsSentToOfficeAndThePerson() throws MessagingException,
        IOException {

        person.setLastName("Test");
        person.setFirstName("Bernd");
        person.setEmail("berndo@test.com");

        String officeEmailAddress = "office@synyx.de";
        Person office = new Person("office", "Muster", "Max", officeEmailAddress);

        String bossEmailAddress = "boss@boss.de";
        Person boss = new Person("boss", "Muster", "Max", bossEmailAddress);

        Mockito.when(personService.getPersonsWithNotificationType(MailNotification.NOTIFICATION_OFFICE))
            .thenReturn(Arrays.asList(office));

        String commentMessage = "Das ist ein Kommentar.";
        Comment comment = new Comment(boss);
        comment.setText(commentMessage);

        mailService.sendAllowedNotification(application, comment);

        // were both emails sent?
        List<Message> inboxOffice = Mailbox.get(officeEmailAddress);
        assertTrue(inboxOffice.size() > 0);

        List<Message> inboxUser = Mailbox.get("berndo@test.com");
        assertTrue(inboxUser.size() > 0);

        // get email user
        Message msg = inboxUser.get(0);

        // check subject
        assertEquals("Dein Urlaubsantrag wurde bewilligt", msg.getSubject());

        // check from and recipient
        assertEquals(new InternetAddress("berndo@test.com"), msg.getAllRecipients()[0]);

        // check content of user email
        String contentUser = (String) msg.getContent();
        assertTrue(contentUser.contains("Bernd"));
        assertTrue(contentUser.contains("gestellter Antrag wurde von"));
        assertTrue(contentUser.contains("genehmigt"));
        assertTrue("No comment in mail content", contentUser.contains(commentMessage));
        assertTrue("Wrong comment author", contentUser.contains(comment.getPerson().getNiceName()));

        // get email office
        Message msgOffice = inboxOffice.get(0);

        // check subject
        assertEquals("Neuer bewilligter Antrag", msgOffice.getSubject());

        // check from and recipient
        assertEquals(new InternetAddress(officeEmailAddress), msgOffice.getAllRecipients()[0]);

        // check content of office email
        String contentOfficeMail = (String) msgOffice.getContent();
        assertTrue(contentOfficeMail.contains("Bernd"));
        assertTrue(contentOfficeMail.contains("Office"));
        assertTrue(contentOfficeMail.contains("es liegt ein neuer genehmigter Antrag vor"));
        assertTrue(contentOfficeMail.contains("Erholungsurlaub"));
        assertTrue("No comment in mail content", contentOfficeMail.contains(commentMessage));
        assertTrue("Wrong comment author", contentOfficeMail.contains(comment.getPerson().getNiceName()));
    }


    @Test
    public void ensureNotificationAboutRejectedApplicationIsSentToPerson() throws MessagingException, IOException {

        person.setLastName("Test");
        person.setFirstName("Franz");
        person.setEmail("franzi@test.com");

        String bossEmailAddress = "boss@boss.de";
        Person boss = new Person("boss", "Muster", "Max", bossEmailAddress);

        String commentMessage = "Das ist ein Kommentar.";
        Comment comment = new Comment(boss);
        comment.setText(commentMessage);

        mailService.sendRejectedNotification(application, comment);

        // was email sent?
        List<Message> inbox = Mailbox.get("franzi@test.com");
        assertTrue(inbox.size() > 0);

        Message msg = inbox.get(0);

        // check subject
        assertEquals("Dein Urlaubsantrag wurde abgelehnt", msg.getSubject());

        // check from and recipient
        assertEquals(new InternetAddress("franzi@test.com"), msg.getAllRecipients()[0]);

        // check content of email
        String content = (String) msg.getContent();
        assertTrue(content.contains("Franz"));
        assertTrue(content.contains("abgelehnt"));
        assertTrue("No comment in mail content", content.contains(commentMessage));
        assertTrue("Wrong comment author", content.contains(comment.getPerson().getNiceName()));
    }


    @Test
    public void ensureAfterApplyingForLeaveAConfirmationNotificationIsSentToPerson() throws MessagingException,
        IOException {

        person.setLastName("Test");
        person.setFirstName("Hildegard");
        person.setEmail("hilde@test.com");

        String commentMessage = "Das ist ein Kommentar.";
        Comment comment = new Comment(person);
        comment.setText(commentMessage);

        mailService.sendConfirmation(application, comment);

        // was email sent?
        List<Message> inbox = Mailbox.get("hilde@test.com");
        assertTrue(inbox.size() > 0);

        Message msg = inbox.get(0);

        // check subject
        assertTrue(msg.getSubject().contains("Antragsstellung"));

        // check from and recipient
        assertEquals(new InternetAddress("hilde@test.com"), msg.getAllRecipients()[0]);

        // check content of email
        String content = (String) msg.getContent();
        assertTrue(content.contains("Hildegard"));
        assertTrue(content.contains("dein Urlaubsantrag wurde erfolgreich eingereicht"));
        assertTrue("No comment in mail content", content.contains(commentMessage));
        assertTrue("Wrong comment author", content.contains(comment.getPerson().getNiceName()));
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

        String commentMessage = "Das ist ein Kommentar.";
        Comment comment = new Comment(office);
        comment.setText(commentMessage);

        mailService.sendCancelledNotification(application, true, comment);

        // was email sent?
        List<Message> inboxApplicant = Mailbox.get("muster@mann.de");
        assertTrue(inboxApplicant.size() > 0);

        Message msg = inboxApplicant.get(0);

        // check subject
        assertEquals("Dein Antrag wurde storniert", msg.getSubject());

        // check from and recipient
        assertEquals(new InternetAddress("muster@mann.de"), msg.getAllRecipients()[0]);

        // check content of email
        String content = (String) msg.getContent();
        assertTrue(content.contains("dein Urlaubsantrag wurde von Magdalena"));
        assertTrue(content.contains("wende dich bitte direkt an Magdalena"));
        assertTrue("No comment in mail content", content.contains(commentMessage));
        assertTrue("Wrong comment author", content.contains(comment.getPerson().getNiceName()));
    }


    @Test
    public void ensureOfficeMembersGetANotificationIfAPersonCancelsAnAllowedApplication() throws MessagingException,
        IOException {

        person.setLastName("Test");
        person.setFirstName("Heinrich");

        String officeEmailAddress = "office@office.de";
        Person office = new Person("office", "Office", "Marlene", officeEmailAddress);

        Mockito.when(personService.getPersonsWithNotificationType(MailNotification.NOTIFICATION_OFFICE))
            .thenReturn(Arrays.asList(office));

        String commentMessage = "Das ist ein Kommentar.";
        Comment comment = new Comment(person);
        comment.setText(commentMessage);

        mailService.sendCancelledNotification(application, false, comment);

        // ENSURE OFFICE MEMBERS HAVE GOT CORRECT EMAIL
        List<Message> inboxOffice = Mailbox.get(officeEmailAddress);
        assertTrue(inboxOffice.size() > 0);

        Message msg = inboxOffice.get(0);

        assertEquals("Ein Antrag wurde storniert", msg.getSubject());

        assertEquals(new InternetAddress(officeEmailAddress), msg.getAllRecipients()[0]);

        String content = (String) msg.getContent();
        assertTrue(content.contains("Der Urlaubsantrag von Heinrich Test"));
        assertTrue(content.contains("wurde storniert"));
        assertTrue("No comment in mail content", content.contains(commentMessage));
        assertTrue("Wrong comment author", content.contains(comment.getPerson().getNiceName()));
    }


    @Test
    public void ensureAdministratorGetsANotificationIfAKeyGeneratingErrorOccurred() throws MessagingException,
        IOException {

        mailService.sendKeyGeneratingErrorNotification("horscht", "Message of exception");

        List<Message> inbox = Mailbox.get(settings.getMailSettings().getAdministrator());
        assertTrue(inbox.size() > 0);

        Message msg = inbox.get(0);

        assertEquals("Fehler beim Generieren der Keys", msg.getSubject());

        String content = (String) msg.getContent();

        assertTrue(content.contains("ist ein Fehler aufgetreten"));
        assertTrue(content.contains("Message of exception"));
    }


    @Test
    public void ensureAdministratorGetsANotificationIfASignErrorOccurred() throws MessagingException, IOException {

        mailService.sendSignErrorNotification(5, "Message of exception");

        List<Message> inbox = Mailbox.get(settings.getMailSettings().getAdministrator());
        assertTrue(inbox.size() > 0);

        Message msg = inbox.get(0);

        assertEquals("Fehler beim Signieren eines Antrags", msg.getSubject());

        String content = (String) msg.getContent();

        assertTrue(content.contains(
                "Beim Versuch den Urlaubsantrag mit der ID '5' zu signieren, ist ein Fehler aufgetreten."));
        assertTrue(content.contains("Message of exception"));
    }


    @Test
    public void ensurePersonGetsANotificationIfAnOfficeMemberAppliedForLeaveForThisPerson() throws MessagingException,
        IOException {

        person.setLastName("Müller");
        person.setFirstName("Günther");
        person.setEmail("bla@test.com");

        String officeEmailAddress = "office@office.de";
        Person office = new Person("office", "Office", "Marlene", officeEmailAddress);

        String commentMessage = "Das ist ein Kommentar.";
        Comment comment = new Comment(office);
        comment.setText(commentMessage);

        application.setApplier(office);
        mailService.sendAppliedForLeaveByOfficeNotification(application, comment);

        // was email sent?
        List<Message> inbox = Mailbox.get("bla@test.com");
        assertTrue(inbox.size() > 0);

        Message msg = inbox.get(0);

        // check subject
        assertTrue(msg.getSubject().contains("dich wurde ein Urlaubsantrag eingereicht"));

        // check from and recipient
        assertEquals(new InternetAddress("bla@test.com"), msg.getAllRecipients()[0]);

        // check content of email
        String content = (String) msg.getContent();
        assertTrue(content.contains("Marlene Office hat einen Urlaubsantrag"));
        assertTrue("No comment in mail content", content.contains(commentMessage));
        assertTrue("Wrong comment author", content.contains(comment.getPerson().getNiceName()));
    }


    @Test
    public void ensureCorrectFrom() throws MessagingException, IOException {

        person.setLastName("Test");
        person.setFirstName("Hildegard");
        person.setEmail("hilde@test.com");

        mailService.sendConfirmation(application, null);

        List<Message> inbox = Mailbox.get("hilde@test.com");
        assertTrue(inbox.size() > 0);

        Message msg = inbox.get(0);

        Address[] from = msg.getFrom();
        Assert.assertNotNull("From must be set", from);
        Assert.assertEquals("From must be only one email address", 1, from.length);
        Assert.assertEquals("Wrong from", settings.getMailSettings().getFrom(), from[0].toString());
    }


    @Test
    public void ensureOfficeGetsNotificationAfterAccountUpdating() throws MessagingException, IOException {

        Account accountOne = new Account();
        accountOne.setRemainingVacationDays(new BigDecimal("3"));
        accountOne.setPerson(new Person("muster", "Muster", "Marlene", ""));

        Account accountTwo = new Account();
        accountTwo.setRemainingVacationDays(new BigDecimal("5.5"));
        accountTwo.setPerson(new Person("mustermann", "Mustermann", "Max", ""));

        Account accountThree = new Account();
        accountThree.setRemainingVacationDays(new BigDecimal("-1"));
        accountThree.setPerson(new Person("horst", "Horst", "Dieter", ""));

        String officeEmailAddress = "office@office.de";
        Person office = new Person("", "", "", officeEmailAddress);

        Mockito.when(personService.getPersonsWithNotificationType(MailNotification.NOTIFICATION_OFFICE))
            .thenReturn(Arrays.asList(office));

        mailService.sendSuccessfullyUpdatedAccountsNotification(Arrays.asList(accountOne, accountTwo, accountThree));

        // ENSURE OFFICE MEMBERS HAVE GOT CORRECT EMAIL
        List<Message> inboxOffice = Mailbox.get(officeEmailAddress);
        assertTrue(inboxOffice.size() > 0);

        Message mail = inboxOffice.get(0);

        // check subject
        assertEquals("Wrong subject", "Auswertung Resturlaubstage", mail.getSubject());

        // check content
        String content = (String) mail.getContent();
        assertTrue(content.contains("Stand Resturlaubstage zum 1. Januar " + DateMidnight
                .now().getYear()));
        assertTrue(content.contains("Marlene Muster: 3"));
        assertTrue(content.contains("Max Mustermann: 5"));
        assertTrue(content.contains("Dieter Horst: -1"));
    }


    @Test
    public void ensureCorrectHolidayReplacementMailIsSent() throws MessagingException, IOException {

        Person holidayReplacement = new Person("muster", "Muster", "Marlene", "mmuster@test.de");
        application.setHolidayReplacement(holidayReplacement);

        mailService.notifyHolidayReplacement(application);

        // was email sent?
        List<Message> inbox = Mailbox.get(holidayReplacement.getEmail());
        assertTrue(inbox.size() > 0);

        Message msg = inbox.get(0);

        // check subject
        assertTrue(msg.getSubject().contains("Urlaubsvertretung"));

        // check from and recipient
        assertEquals(new InternetAddress(holidayReplacement.getEmail()), msg.getAllRecipients()[0]);

        // check content of email
        String content = (String) msg.getContent();
        assertTrue(content.contains("Hallo Marlene Muster"));
        assertTrue(content.contains("Urlaubsvertretung"));
    }


    @Test
    public void ensureAdministratorGetsANotificationIfACalendarSyncErrorOccurred() throws MessagingException,
        IOException {

        Person person = new Person("muster", "Muster", "Marlene", "marlene@muster.de");

        Application application = new Application();
        application.setDayLength(DayLength.FULL);
        application.setStartDate(DateMidnight.now());
        application.setEndDate(DateMidnight.now());
        application.setPerson(person);
        application.setStatus(ApplicationStatus.ALLOWED);

        Absence absence = new Absence(application, new AbsenceTimeConfiguration(settings.getCalendarSettings()));

        mailService.sendCalendarSyncErrorNotification("Kalendername", absence, "Calendar sync failed");

        List<Message> inbox = Mailbox.get(settings.getMailSettings().getAdministrator());
        assertTrue(inbox.size() > 0);

        Message msg = inbox.get(0);

        assertEquals("Fehler beim Synchronisieren des Kalenders", msg.getSubject());

        String content = (String) msg.getContent();

        assertTrue(content.contains("Kalendername"));
        assertTrue(content.contains("Calendar sync failed"));
        assertTrue(content.contains(person.getNiceName()));
    }


    @Test
    public void ensureAdministratorGetsANotificationIfAEventDeleteErrorOccurred() throws MessagingException,
        IOException {

        mailService.sendCalendarDeleteErrorNotification("Kalendername", "eventId", "event delete failed");

        List<Message> inbox = Mailbox.get(settings.getMailSettings().getAdministrator());
        assertTrue(inbox.size() > 0);

        Message msg = inbox.get(0);

        assertEquals("Fehler beim Löschen eines Kalendereintrags", msg.getSubject());

        String content = (String) msg.getContent();

        assertTrue(content.contains("Kalendername"));
        assertTrue(content.contains("eventId"));
        assertTrue(content.contains("event delete failed"));
    }


    @Test
    public void ensureAdministratorGetsANotificationIfAEventUpdateErrorOccurred() throws MessagingException,
        IOException {

        Person person = new Person("muster", "Muster", "Marlene", "marlene@muster.de");

        Application application = new Application();
        application.setDayLength(DayLength.FULL);
        application.setStartDate(DateMidnight.now());
        application.setEndDate(DateMidnight.now());
        application.setPerson(person);
        application.setStatus(ApplicationStatus.ALLOWED);

        Absence absence = new Absence(application, new AbsenceTimeConfiguration(settings.getCalendarSettings()));

        mailService.sendCalendarUpdateErrorNotification("Kalendername", absence, "eventId", "event update failed");

        List<Message> inbox = Mailbox.get(settings.getMailSettings().getAdministrator());
        assertTrue(inbox.size() > 0);

        Message msg = inbox.get(0);

        assertEquals("Fehler beim Aktualisieren eines Kalendereintrags", msg.getSubject());

        String content = (String) msg.getContent();

        assertTrue(content.contains("Kalendername"));
        assertTrue(content.contains("eventId"));
        assertTrue(content.contains("event update failed"));
        assertTrue(content.contains(person.getNiceName()));
    }


    @Test
    public void ensureAdministratorGetsANotificationIfConnectionToExchangeCalendarFailed() throws MessagingException,
        IOException {

        mailService.sendCalendarConnectionErrorNotification(CalendarType.EWS, "Kalendername", "exception description");

        String administrator1 = settings.getMailSettings().getAdministrator();
        List<Message> inbox = Mailbox.get(administrator1);
        assertTrue(inbox.size() > 0);

        Message msg = inbox.get(0);

        assertEquals("Verbindung zum Kalender konnte nicht hergestellt werden", msg.getSubject());

        String content = (String) msg.getContent();

        assertTrue(content.contains("Verbindung zum Exchange Kalender 'Kalendername'"));
        assertTrue(content.contains("exception description"));
    }


    @Test
    public void ensureAdministratorGetsANotificationIfConnectionToGoogleCalendarFailed() throws MessagingException,
        IOException {

        mailService.sendCalendarConnectionErrorNotification(CalendarType.GOOGLE, "Kalendername",
            "exception description");

        String administrator1 = settings.getMailSettings().getAdministrator();
        List<Message> inbox = Mailbox.get(administrator1);
        assertTrue(inbox.size() > 0);

        Message msg = inbox.get(0);

        assertEquals("Verbindung zum Kalender konnte nicht hergestellt werden", msg.getSubject());

        String content = (String) msg.getContent();

        assertTrue(content.contains("Verbindung zum Google Kalender 'Kalendername'"));
        assertTrue(content.contains("exception description"));
    }


    @Test
    public void ensureAdministratorGetsANotificationIfSettingsGetUpdated() throws MessagingException, IOException {

        mailService.sendSuccessfullyUpdatedSettingsNotification(settings);

        List<Message> inbox = Mailbox.get(settings.getMailSettings().getAdministrator());
        assertTrue(inbox.size() > 0);

        Message msg = inbox.get(0);

        assertEquals("Einstellungen aktualisiert", msg.getSubject());

        String content = (String) msg.getContent();

        assertTrue(content.contains("Einstellungen"));
        assertTrue(content.contains(settings.getMailSettings().getHost()));
        assertTrue(content.contains(settings.getMailSettings().getPort().toString()));
    }


    @Test
    public void ensureThatSendUserCreationNotification() throws MessagingException, IOException {

        Person person = new Person("muster", "Muster", "Marlene", "mmuster@test.de");
        String rawPassword = "secret";

        mailService.sendUserCreationNotification(person, rawPassword);

        List<Message> inbox = Mailbox.get(person.getEmail());
        assertTrue(inbox.size() > 0);

        Message msg = inbox.get(0);

        // check subject
        assertTrue(msg.getSubject().contains("Account für Urlaubsverwaltung erstellt"));

        // check from and recipient
        assertEquals(new InternetAddress(person.getEmail()), msg.getAllRecipients()[0]);

        // check content of email
        String content = (String) msg.getContent();
        assertTrue(content.contains("Hallo Marlene Muster"));
        assertTrue(content.contains(person.getLoginName()));
        assertTrue(content.contains(rawPassword));
    }
}
