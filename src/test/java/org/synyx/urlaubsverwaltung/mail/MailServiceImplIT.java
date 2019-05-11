package org.synyx.urlaubsverwaltung.mail;

import freemarker.template.Configuration;
import no.api.freemarker.java8.Java8ObjectWrapper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.mock_javamail.Mailbox;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactory;
import org.synyx.urlaubsverwaltung.account.domain.Account;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationComment;
import org.synyx.urlaubsverwaltung.application.domain.VacationCategory;
import org.synyx.urlaubsverwaltung.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import static java.lang.invoke.MethodHandles.lookup;
import static java.time.ZoneOffset.UTC;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_BOSS_ALL;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_OFFICE;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;


@RunWith(MockitoJUnitRunner.class)
public class MailServiceImplIT {

    private static final Logger LOGGER = getLogger(lookup().lookupClass());

    private static final StaticMessageSource MESSAGE_SOURCE;


    static {
        MESSAGE_SOURCE = new StaticMessageSource();

        try {
            Properties messageProperties = new Properties();
            messageProperties.load(new ClassPathResource("messages_de.properties").getInputStream());

            Map<String, String> messages = messageProperties.entrySet().stream()
                .collect(toMap(e -> e.getKey().toString(), e -> e.getValue().toString()));

            MESSAGE_SOURCE.addMessages(messages, Locale.GERMAN);
        } catch (IOException e) {
            LOGGER.debug("Could not load messages properties file from classpath", e);
        }
    }

    private MailServiceImpl sut;

    @Mock
    private PersonService personService;
    @Mock
    private DepartmentService departmentService;
    @Mock
    private SettingsService settingsService;

    private Person person;
    private Person boss;
    private Person departmentHead;
    private Person secondStage;
    private Person office;
    private Application application;
    private Settings settings;

    @Before
    public void setUp() throws Exception {

        FreeMarkerConfigurationFactory freeMarkerConfigurationFactory = new FreeMarkerConfigurationFactory();
        freeMarkerConfigurationFactory.setDefaultEncoding("UTF-8");
        freeMarkerConfigurationFactory.setTemplateLoaderPath("classpath:/org/synyx/urlaubsverwaltung/core/mail/");
        Configuration configuration = freeMarkerConfigurationFactory.createConfiguration();
        configuration.setObjectWrapper(new Java8ObjectWrapper(Configuration.VERSION_2_3_28));

        MailBuilder mailBuilder = new MailBuilder(configuration);
        MailSender mailSender = new MailSender(new JavaMailSenderImpl());
        RecipientService recipientService = new RecipientService(personService, departmentService);
        sut = new MailServiceImpl(MESSAGE_SOURCE, mailBuilder, mailSender, recipientService, departmentService,
            settingsService);

        person = TestDataCreator.createPerson("user", "Lieschen", "Müller", "lieschen@firma.test");
        application = createApplication(person);

        settings = new Settings();
        settings.getMailSettings().setActive(true);
        settings.getMailSettings().setBaseLinkURL("http://urlaubsverwaltung/");
        when(settingsService.getSettings()).thenReturn(settings);

        // BOSS
        boss = TestDataCreator.createPerson("boss", "Hugo", "Boss", "boss@firma.test");
        boss.setPermissions(singletonList(BOSS));
        when(personService.getPersonsWithNotificationType(NOTIFICATION_BOSS_ALL)).thenReturn(singletonList(boss));

        // DEPARTMENT HEAD
        departmentHead = TestDataCreator.createPerson("head", "Michel", "Mustermann", "head@firma.test");
        departmentHead.setPermissions(singletonList(DEPARTMENT_HEAD));
        when(personService.getPersonsWithNotificationType(NOTIFICATION_DEPARTMENT_HEAD)).thenReturn(singletonList(departmentHead));
        when(departmentService.isDepartmentHeadOfPerson(eq(departmentHead), any(Person.class))).thenReturn(true);

        // SECOND STAGE AUTHORITY
        secondStage = TestDataCreator.createPerson("manager", "Kai", "Schmitt", "manager@firma.test");
        secondStage.setPermissions(singletonList(SECOND_STAGE_AUTHORITY));
        when(personService.getPersonsWithNotificationType(NOTIFICATION_SECOND_STAGE_AUTHORITY)).thenReturn(singletonList(secondStage));
        when(departmentService.isSecondStageAuthorityOfPerson(eq(secondStage), any(Person.class))).thenReturn(true);

        // OFFICE
        office = TestDataCreator.createPerson("office", "Marlene", "Muster", "office@firma.test");
        office.setPermissions(singletonList(OFFICE));
        when(personService.getPersonsWithNotificationType(NOTIFICATION_OFFICE)).thenReturn(singletonList(office));
    }

    @After
    public void tearDown() {

        Mailbox.clearAll();
    }


    @Test
    public void ensureNotificationAboutNewApplicationIsSentToBossesAndDepartmentHeads() throws MessagingException, IOException {

        ApplicationComment comment = createDummyComment(person, "Hätte gerne Urlaub");

        sut.sendNewApplicationNotification(application, comment);

        // was email sent to boss?
        List<Message> inboxOfBoss = Mailbox.get(boss.getEmail());
        assertTrue("Boss should get the email", inboxOfBoss.size() == 1);

        // was email sent to department head?
        List<Message> inboxOfDepartmentHead = Mailbox.get(departmentHead.getEmail());
        assertTrue("Department head should get the email", inboxOfDepartmentHead.size() == 1);

        // get email
        Message msgBoss = inboxOfBoss.get(0);
        Message msgDepartmentHead = inboxOfDepartmentHead.get(0);

        verifyNotificationAboutNewApplication(boss, msgBoss, application.getPerson().getNiceName(), comment);
        verifyNotificationAboutNewApplication(departmentHead, msgDepartmentHead, application.getPerson().getNiceName(),
            comment);
    }


    @Test
    public void ensureNotificationAboutNewApplicationOfSecondStageAuthorityIsSentToBosses() throws MessagingException, IOException {

        ApplicationComment comment = createDummyComment(secondStage, "Hätte gerne Urlaub");
        application.setPerson(secondStage);

        sut.sendNewApplicationNotification(application, comment);

        // was email sent to boss?
        List<Message> inboxOfBoss = Mailbox.get(boss.getEmail());
        assertTrue("Boss should get the email", inboxOfBoss.size() == 1);

        // no email sent to department head
        List<Message> inboxOfDepartmentHead = Mailbox.get(departmentHead.getEmail());
        assertTrue("Department head should get no email", inboxOfDepartmentHead.size() == 0);

        // get email
        Message msgBoss = inboxOfBoss.get(0);

        verifyNotificationAboutNewApplication(boss, msgBoss, application.getPerson().getNiceName(), comment);
    }


    @Test
    public void ensureNotificationAboutNewApplicationOfDepartmentHeadIsSentToSecondaryStageAuthority()
        throws MessagingException, IOException {

        ApplicationComment comment = createDummyComment(departmentHead, "Hätte gerne Urlaub");
        application.setPerson(departmentHead);

        sut.sendNewApplicationNotification(application, comment);

        // was email sent to boss?
        List<Message> inboxOfBoss = Mailbox.get(boss.getEmail());
        assertTrue("Boss should get the email", inboxOfBoss.size() == 1);

        // was email sent to secondary stage?
        List<Message> inboxOfSecondaryStage = Mailbox.get(secondStage.getEmail());
        assertTrue("Secondary stage should get the email", inboxOfSecondaryStage.size() == 1);

        // get email
        Message msgBoss = inboxOfBoss.get(0);
        Message msgSecondaryStage = inboxOfSecondaryStage.get(0);

        verifyNotificationAboutNewApplication(boss, msgBoss, application.getPerson().getNiceName(), comment);
        verifyNotificationAboutNewApplication(secondStage, msgSecondaryStage, application.getPerson().getNiceName(),
            comment);
    }

    @Test
    public void ensureNotificationAboutNewApplicationContainsInformationAboutDepartmentVacations()
        throws MessagingException, IOException {

        VacationType vacationType = TestDataCreator.createVacationType(VacationCategory.HOLIDAY, "application.data.vacationType.holiday");

        Person departmentMember = TestDataCreator.createPerson("muster", "Marlene", "Muster", "mmuster@foo.de");
        Application departmentApplication = TestDataCreator.createApplication(departmentMember, vacationType,
            LocalDate.of(2015, 11, 5), LocalDate.of(2015, 11, 6), DayLength.FULL);

        Person otherDepartmentMember = TestDataCreator.createPerson("schmidt", "Niko", "Schmidt", "nschmidt@foo.de");
        Application otherDepartmentApplication = TestDataCreator.createApplication(otherDepartmentMember, vacationType,
            LocalDate.of(2015, 11, 4), LocalDate.of(2015, 11, 4), DayLength.MORNING);

        when(personService.getPersonsWithNotificationType(NOTIFICATION_BOSS_ALL))
            .thenReturn(singletonList(boss));

        when(departmentService.getApplicationsForLeaveOfMembersInDepartmentsOfPerson(eq(person),
            any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(Arrays.asList(departmentApplication, otherDepartmentApplication));

        sut.sendNewApplicationNotification(application, null);

        List<Message> inboxOfBoss = Mailbox.get(boss.getEmail());
        Message message = inboxOfBoss.get(0);

        String content = (String) message.getContent();

        assertTrue(content.contains("es liegt ein neuer zu genehmigender Antrag vor: http://urlaubsverwaltung/web/application/1234"));
        assertTrue(content.contains("Marlene Muster: 05.11.2015 bis 06.11.2015"));
        assertTrue(content.contains("Niko Schmidt: 04.11.2015 bis 04.11.2015"));
    }

    @Test
    public void ensureNotificationAboutTemporaryAllowedApplicationIsSentToSecondStageAuthoritiesAndToPerson()
        throws MessagingException, IOException {

        ApplicationComment comment = createDummyComment(secondStage, "OK, spricht von meiner Seite aus nix dagegen");

        sut.sendTemporaryAllowedNotification(application, comment);

        // were both emails sent?
        List<Message> inboxSecondStage = Mailbox.get(secondStage.getEmail());
        assertTrue(inboxSecondStage.size() == 1);

        List<Message> inboxUser = Mailbox.get(person.getEmail());
        assertTrue(inboxUser.size() == 1);

        // get email user
        Message msg = inboxUser.get(0);

        // check subject
        assertEquals("Dein Urlaubsantrag wurde vorläufig bewilligt", msg.getSubject());

        // check from and recipient
        assertEquals(new InternetAddress(person.getEmail()), msg.getAllRecipients()[0]);

        // check content of user email
        String contentUser = (String) msg.getContent();
        assertTrue(contentUser.contains("Hallo Lieschen Müller"));
        assertTrue(contentUser.contains(
            "Bitte beachte, dass dieser erst noch von einem entsprechend Verantwortlichen freigegeben werden muss"));
        assertTrue("No comment in mail content", contentUser.contains(comment.getText()));
        assertTrue("Wrong comment author", contentUser.contains(comment.getPerson().getNiceName()));
        assertTrue(contentUser.contains("Link zum Antrag: http://urlaubsverwaltung/web/application/1234"));

        // get email office
        Message msgSecondStage = inboxSecondStage.get(0);

        // check subject
        assertEquals("Ein Urlaubsantrag wurde vorläufig bewilligt", msgSecondStage.getSubject());

        // check from and recipient
        assertEquals(new InternetAddress(secondStage.getEmail()), msgSecondStage.getAllRecipients()[0]);

        // check content of office email
        String contentSecondStageMail = (String) msgSecondStage.getContent();
        assertTrue(contentSecondStageMail.contains("es liegt ein neuer zu genehmigender Antrag vor: http://urlaubsverwaltung/web/application/1234"));
        assertTrue(contentSecondStageMail.contains("Der Antrag wurde bereits vorläufig genehmigt und muss nun noch endgültig freigegeben werden"));
        assertTrue(contentSecondStageMail.contains("Lieschen Müller"));
        assertTrue(contentSecondStageMail.contains("Erholungsurlaub"));
        assertTrue("No comment in mail content", contentSecondStageMail.contains(comment.getText()));
        assertTrue("Wrong comment author", contentSecondStageMail.contains(comment.getPerson().getNiceName()));
    }

    @Test
    public void ensureAfterApplyingForLeaveAConfirmationNotificationIsSentToPerson() throws MessagingException,
        IOException {

        ApplicationComment comment = createDummyComment(person, "Hätte gerne Urlaub");

        sut.sendConfirmation(application, comment);

        // was email sent?
        List<Message> inbox = Mailbox.get(person.getEmail());
        assertTrue(inbox.size() > 0);

        Message msg = inbox.get(0);

        // check subject
        assertTrue(msg.getSubject().contains("Antragsstellung"));

        // check from and recipient
        assertEquals(new InternetAddress(person.getEmail()), msg.getAllRecipients()[0]);

        // check content of email
        String content = (String) msg.getContent();
        assertTrue(content.contains("Hallo Lieschen Müller"));
        assertTrue(content.contains("dein Urlaubsantrag wurde erfolgreich eingereicht"));
        assertTrue("No comment in mail content", content.contains(comment.getText()));
        assertTrue("Wrong comment author", content.contains(comment.getPerson().getNiceName()));
        assertTrue(content.contains("Link zum Antrag: http://urlaubsverwaltung/web/application/1234"));
    }


    @Test
    public void ensurePersonGetsANotificationIfOfficeCancelledOneOfHisApplications() throws MessagingException,
        IOException {

        application.setCanceller(office);

        ApplicationComment comment = createDummyComment(office, "Geht leider nicht");

        sut.sendCancelledByOfficeNotification(application, comment);

        // was email sent?
        List<Message> inboxApplicant = Mailbox.get(person.getEmail());
        assertTrue(inboxApplicant.size() > 0);

        Message msg = inboxApplicant.get(0);

        // check subject
        assertEquals("Dein Antrag wurde storniert", msg.getSubject());

        // check from and recipient
        assertEquals(new InternetAddress(person.getEmail()), msg.getAllRecipients()[0]);

        // check content of email
        String content = (String) msg.getContent();
        assertTrue(content.contains("Hallo Lieschen Müller"));
        assertTrue(content.contains("Marlene Muster hat einen deiner Urlaubsanträge storniert."));
        assertTrue("No comment in mail content", content.contains(comment.getText()));
        assertTrue("Wrong comment author", content.contains(comment.getPerson().getNiceName()));
        assertTrue(content.contains("Es handelt sich um folgenden Urlaubsantrag: http://urlaubsverwaltung/web/application/1234"));
    }

    @Test
    public void ensurePersonGetsANotificationIfAnOfficeMemberAppliedForLeaveForThisPerson() throws MessagingException,
        IOException {

        ApplicationComment comment = createDummyComment(office, "Habe das mal für dich beantragt");

        application.setApplier(office);
        sut.sendAppliedForLeaveByOfficeNotification(application, comment);

        // was email sent?
        List<Message> inbox = Mailbox.get(person.getEmail());
        assertTrue(inbox.size() > 0);

        Message msg = inbox.get(0);

        // check subject
        assertTrue(msg.getSubject().contains("Für dich wurde ein Urlaubsantrag eingereicht"));

        // check from and recipient
        assertEquals(new InternetAddress(person.getEmail()), msg.getAllRecipients()[0]);

        // check content of email
        String content = (String) msg.getContent();
        assertTrue(content.contains("Hallo Lieschen Müller"));
        assertTrue(content.contains("Marlene Muster hat einen Urlaubsantrag für dich gestellt"));
        assertTrue("No comment in mail content", content.contains(comment.getText()));
        assertTrue("Wrong comment author", content.contains(comment.getPerson().getNiceName()));
        assertTrue(content.contains("Link zum Antrag: http://urlaubsverwaltung/web/application/1234"));
    }


    @Test
    public void ensureCorrectFrom() throws MessagingException {

        sut.sendConfirmation(application, null);

        List<Message> inbox = Mailbox.get(person.getEmail());
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
        accountOne.setPerson(TestDataCreator.createPerson("muster", "Marlene", "Muster", "marlene@firma.test"));

        Account accountTwo = new Account();
        accountTwo.setRemainingVacationDays(new BigDecimal("5.5"));
        accountTwo.setPerson(TestDataCreator.createPerson("mustermann", "Max", "Mustermann", "max@mustermann.de"));

        Account accountThree = new Account();
        accountThree.setRemainingVacationDays(new BigDecimal("-1"));
        accountThree.setPerson(TestDataCreator.createPerson("dings", "Horst", "Dings", "horst@dings.de"));

        sut.sendSuccessfullyUpdatedAccountsNotification(Arrays.asList(accountOne, accountTwo, accountThree));

        // ENSURE OFFICE MEMBERS HAVE GOT CORRECT EMAIL
        List<Message> inboxOffice = Mailbox.get(office.getEmail());
        assertTrue(inboxOffice.size() > 0);

        Message mail = inboxOffice.get(0);

        // check subject
        assertEquals("Wrong subject", "Auswertung Resturlaubstage", mail.getSubject());

        // check content
        String content = (String) mail.getContent();
        assertTrue(content.contains("Stand Resturlaubstage zum 1. Januar " + ZonedDateTime.now(UTC).getYear()));
        assertTrue(content.contains("Marlene Muster: 3"));
        assertTrue(content.contains("Max Mustermann: 5"));
        assertTrue(content.contains("Horst Dings: -1"));
    }


    @Test
    public void ensureAdministratorGetsANotificationIfSettingsGetUpdated() throws MessagingException, IOException {

        sut.sendSuccessfullyUpdatedSettingsNotification(settings);

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
    public void ensureBossesAndDepartmentHeadsGetRemindMail() throws MessagingException, IOException {

        sut.sendRemindBossNotification(application);

        // was email sent to boss?
        List<Message> inboxOfBoss = Mailbox.get(boss.getEmail());
        assertTrue("Boss should get exactly one email", inboxOfBoss.size() == 1);

        // was email sent to department head?
        List<Message> inboxOfDepartmentHead = Mailbox.get(departmentHead.getEmail());
        assertTrue("Department head should get exactly one email", inboxOfDepartmentHead.size() == 1);

        // has mail correct attributes?
        Message msg = inboxOfBoss.get(0);

        // check subject
        assertTrue(msg.getSubject().contains("Erinnerung wartender Urlaubsantrag"));

        // check from and recipient
        assertEquals(new InternetAddress(boss.getEmail()), msg.getAllRecipients()[0]);

        // check content of email
        String content = (String) msg.getContent();
        assertTrue(content.contains("Hallo Hugo Boss"));
        assertTrue(content.contains("Link zum Antrag: http://urlaubsverwaltung/web/application/1234"));
    }


    @Test
    public void ensureSendRemindForWaitingApplicationsReminderNotification() throws Exception {

        // PERSONs
        Person personDepartmentA = TestDataCreator.createPerson("personDepartmentA");
        Person personDepartmentB = TestDataCreator.createPerson("personDepartmentB");
        Person personDepartmentC = TestDataCreator.createPerson("personDepartmentC");

        // APPLICATIONs
        Application applicationA = createApplication(personDepartmentA);
        Application applicationB = createApplication(personDepartmentB);
        Application applicationC = createApplication(personDepartmentC);

        // DEPARTMENT HEADs
        Person departmentHeadA = TestDataCreator.createPerson("headAC", "Heinz", "Wurst", "headAC@firma.test");
        Person departmentHeadB = TestDataCreator.createPerson("headB", "Michel", "Mustermann", "headB@firma.test");

        when(personService.getPersonsWithNotificationType(NOTIFICATION_DEPARTMENT_HEAD))
            .thenReturn(Arrays.asList(departmentHeadA, departmentHeadB));

        when(departmentService.isDepartmentHeadOfPerson(eq(departmentHeadA), eq(personDepartmentA)))
            .thenReturn(true);
        when(departmentService.isDepartmentHeadOfPerson(eq(departmentHeadB), eq(personDepartmentB)))
            .thenReturn(true);
        when(departmentService.isDepartmentHeadOfPerson(eq(departmentHeadA), eq(personDepartmentC)))
            .thenReturn(true);

        sut.sendRemindForWaitingApplicationsReminderNotification(Arrays.asList(applicationA, applicationB,
            applicationC));

        verifyInbox(boss, Arrays.asList(applicationA, applicationB, applicationC));
        verifyInbox(departmentHeadA, Arrays.asList(applicationA, applicationC));
        verifyInbox(departmentHeadB, singletonList(applicationB));
    }


    private void verifyInbox(Person inboxOwner, List<Application> applications) throws MessagingException, IOException {

        List<Message> inbox = Mailbox.get(inboxOwner.getEmail());
        assertTrue(inboxOwner.getLoginName() + " should get one email", inbox.size() == 1);

        Message msg = inbox.get(0);

        assertTrue("Wrong subject in Mail for " + inboxOwner.getLoginName(),
            msg.getSubject().contains("Erinnerung für wartende Urlaubsanträge"));

        String content = (String) msg.getContent();

        assertTrue(content.contains("Hallo " + inboxOwner.getNiceName()));

        for (Application application : applications) {
            assertTrue(content.contains(application.getApplier().getNiceName()));
            assertTrue(content.contains("http://urlaubsverwaltung/web/application/1234"));
        }
    }


    @Test
    public void ensurePersonAndOfficeGetMailIfSickNoteReachesEndOfSickPay() throws MessagingException, IOException {

        SickNote sickNote = TestDataCreator.createSickNote(person);

        sut.sendEndOfSickPayNotification(sickNote);

        // was email sent to office?
        List<Message> inboxOffice = Mailbox.get(office.getEmail());
        assertTrue("Person should get email", inboxOffice.size() > 0);

        // was email sent to person?
        List<Message> inboxPerson = Mailbox.get(person.getEmail());
        assertTrue("Person should get email", inboxPerson.size() > 0);

        // has mail correct attributes?
        assertCorrectEndOfSickPayMail(inboxOffice.get(0), office);
        assertCorrectEndOfSickPayMail(inboxPerson.get(0), person);
    }


    private void assertCorrectEndOfSickPayMail(Message msg, Person recipient) throws MessagingException, IOException {

        // check subject
        assertTrue("Wrong subject", msg.getSubject().contains("Ende der Lohnfortzahlung"));

        // check from and recipient
        assertEquals(new InternetAddress(recipient.getEmail()), msg.getAllRecipients()[0]);

        // check content of email
        String content = (String) msg.getContent();
        assertTrue(content.contains("Hallo Lieschen Müller,\r\nHallo Office,"));
        assertTrue(content.contains(
            "Der Anspruch auf Lohnfortzahlung durch den Arbeitgeber im Krankheitsfall besteht für maximal 42 Tag(e)"));
        assertTrue(content.contains("erreicht in Kürze die 42 Tag(e) Grenze"));
    }

    private Application createApplication(Person person) {

        LocalDate now = LocalDate.now(UTC);
        Application application = new Application();
        application.setId(1234);
        application.setPerson(person);
        application.setVacationType(TestDataCreator.createVacationType(VacationCategory.HOLIDAY, "application.data.vacationType.holiday"));
        application.setDayLength(DayLength.FULL);
        application.setApplicationDate(now);
        application.setStartDate(now);
        application.setEndDate(now);
        application.setApplier(person);

        return application;
    }

    private void verifyNotificationAboutNewApplication(Person recipient, Message msg, String niceName,
                                                       ApplicationComment comment) throws MessagingException, IOException {

        // check subject
        assertEquals("Neuer Urlaubsantrag für " + niceName, msg.getSubject());

        // check from and recipient
        assertEquals(new InternetAddress(recipient.getEmail()), msg.getAllRecipients()[0]);

        // check content of email
        String contentDepartmentHead = (String) msg.getContent();
        assertTrue(contentDepartmentHead.contains("Hallo " + recipient.getNiceName()));
        assertTrue(contentDepartmentHead.contains(niceName));
        assertTrue(contentDepartmentHead.contains("Erholungsurlaub"));
        assertTrue(contentDepartmentHead.contains("es liegt ein neuer zu genehmigender Antrag vor"));
        assertTrue(contentDepartmentHead.contains("http://urlaubsverwaltung/web/application/1234"));
        assertTrue("No comment in mail content", contentDepartmentHead.contains(comment.getText()));
        assertTrue("Wrong comment author", contentDepartmentHead.contains(comment.getPerson().getNiceName()));
    }

    private ApplicationComment createDummyComment(Person author, String text) {

        ApplicationComment comment = new ApplicationComment(author);
        comment.setText(text);

        return comment;
    }
}
