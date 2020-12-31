package org.synyx.urlaubsverwaltung.application.service;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.apache.commons.mail.util.MimeMessageParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.TestContainersBase;
import org.synyx.urlaubsverwaltung.TestDataCreator;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationComment;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.mail.MailProperties;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import javax.activation.DataSource;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.time.ZoneOffset.UTC;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.application.domain.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_BOSS_ALL;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;

@SpringBootTest(properties = {"spring.mail.port=3025", "spring.mail.host=localhost"})
@Transactional
class ApplicationMailServiceIT extends TestContainersBase {

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    @RegisterExtension
    public final GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP_IMAP);

    @Autowired
    private ApplicationMailService sut;
    @Autowired
    private PersonService personService;
    @Autowired
    private MailProperties mailProperties;
    @Autowired
    private Clock clock;

    @MockBean
    private ApplicationRecipientService applicationRecipientService;
    @MockBean
    private DepartmentService departmentService;

    @Test
    void ensureNotificationAboutAllowedApplicationIsSentToOfficeAndThePerson() throws MessagingException, IOException {

        final Person person = new Person("user", "Mueller", "Lieschen", "lieschen@example.org");

        final Person office = new Person("office", "Muster", "Marlene", "office@example.org");
        office.setPermissions(singletonList(OFFICE));
        office.setNotifications(singletonList(NOTIFICATION_OFFICE));
        personService.save(office);

        final Person boss = new Person("boss", "Boss", "Hugo", "boss@example.org");
        boss.setPermissions(singletonList(BOSS));

        final Application application = createApplication(person);
        application.setBoss(boss);

        final ApplicationComment comment = new ApplicationComment(boss, clock);
        comment.setText("OK, Urlaub kann genommen werden");

        sut.sendAllowedNotification(application, comment);

        // were both emails sent?
        MimeMessage[] inboxOffice = greenMail.getReceivedMessagesForDomain(office.getEmail());
        assertThat(inboxOffice.length).isOne();

        MimeMessage[] inboxUser = greenMail.getReceivedMessagesForDomain(person.getEmail());
        assertThat(inboxUser.length).isOne();

        // check email user attributes
        Message msg = inboxUser[0];
        assertThat(msg.getSubject()).isEqualTo("Dein Urlaubsantrag wurde bewilligt");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of user email
        String contentUser = GreenMailUtil.getBody(msg);
        assertThat(contentUser).contains("Lieschen Mueller");
        assertThat(contentUser).contains("gestellter Antrag wurde von Hugo Boss genehmigt");
        assertThat(contentUser).contains(comment.getText());
        assertThat(contentUser).contains(comment.getPerson().getNiceName());
        assertThat(contentUser).contains("/web/application/1234");
        assertThat(contentUser).contains("filename=calendar.ics");

        // check email office attributes
        Message msgOffice = inboxOffice[0];
        assertThat(msgOffice.getSubject()).isEqualTo("Neuer bewilligter Antrag");
        assertThat(new InternetAddress(office.getEmail())).isEqualTo(msgOffice.getAllRecipients()[0]);

        // check content of office email
        String contentOfficeMail = (String) msgOffice.getContent();
        assertThat(contentOfficeMail).contains("Hallo Marlene Muster");
        assertThat(contentOfficeMail).contains("es liegt ein neuer genehmigter Antrag vor");
        assertThat(contentOfficeMail).contains("Lieschen Mueller");
        assertThat(contentOfficeMail).contains("Erholungsurlaub");
        assertThat(contentOfficeMail).contains(comment.getText());
        assertThat(contentOfficeMail).contains(comment.getPerson().getNiceName());
        assertThat(contentOfficeMail).contains("es liegt ein neuer genehmigter Antrag vor:");
        assertThat(contentOfficeMail).contains("/web/application/1234");
        assertThat(contentOfficeMail).doesNotContain("filename=calendar.ics");
    }

    @Test
    void ensureNotificationAboutRejectedApplicationIsSentToApplierAndRelevantPersons() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");

        final Person boss = new Person("boss", "Boss", "Hugo", "boss@example.org");
        boss.setPermissions(singletonList(BOSS));

        final ApplicationComment comment = new ApplicationComment(boss, clock);
        comment.setText("Geht leider nicht zu dem Zeitraum");

        final Application application = createApplication(person);
        application.setBoss(boss);

        final Person departmentHead = new Person("departmentHead", "Head", "Department", "dh@example.org");
        when(applicationRecipientService.getRecipientsOfInterest(application)).thenReturn(List.of(boss, departmentHead));

        sut.sendRejectedNotification(application, comment);

        // was email sent?
        MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(person.getEmail());
        assertThat(inbox.length).isOne();

        // check content of user email
        Message msg = inbox[0];
        assertThat(msg.getSubject()).isEqualTo("Dein Urlaubsantrag wurde abgelehnt");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        String content = (String) msg.getContent();
        assertThat(content).contains("Hallo Lieschen Müller");
        assertThat(content).contains("wurde leider von Hugo Boss abgelehnt");
        assertThat(content).contains("/web/application/1234");
        assertThat(content).contains(comment.getText());
        assertThat(content).contains(comment.getPerson().getNiceName());

        // was email sent to boss
        MimeMessage[] inboxBoss = greenMail.getReceivedMessagesForDomain(boss.getEmail());
        assertThat(inboxBoss.length).isOne();

        Message msgBoss = inboxBoss[0];
        assertThat(msgBoss.getSubject()).isEqualTo("Ein Urlaubsantrag wurde abgelehnt");

        String contentBoss = (String) msgBoss.getContent();
        assertThat(contentBoss).contains("Hallo Hugo Boss");
        assertThat(contentBoss).contains("der von Lieschen Müller am");
        assertThat(contentBoss).contains("gestellte Antrag wurde von Hugo Boss abgelehnt");
        assertThat(contentBoss).contains(comment.getText());
        assertThat(contentBoss).contains(comment.getPerson().getNiceName());

        // was email sent to departmentHead
        MimeMessage[] inboxDepartmentHead = greenMail.getReceivedMessagesForDomain(departmentHead.getEmail());
        assertThat(inboxDepartmentHead.length).isOne();

        Message msgDepartmentHead = inboxDepartmentHead[0];
        assertThat(msgDepartmentHead.getSubject()).isEqualTo("Ein Urlaubsantrag wurde abgelehnt");

        String contentDepartmentHead = (String) msgDepartmentHead.getContent();
        assertThat(contentDepartmentHead).contains("Hallo Department Head");
        assertThat(contentDepartmentHead).contains("der von Lieschen Müller am");
        assertThat(contentDepartmentHead).contains("gestellte Antrag wurde von Hugo Boss abgelehnt");
        assertThat(contentDepartmentHead).contains(comment.getText());
        assertThat(contentDepartmentHead).contains(comment.getPerson().getNiceName());
    }

    @Test
    void ensureCorrectReferMail() throws MessagingException, IOException {

        final Person recipient = new Person("recipient", "Muster", "Max", "mustermann@example.org");
        final Person sender = new Person("sender", "Grimes", "Rick", "rick@grimes.com");

        final Application application = createApplication(recipient);

        sut.sendReferApplicationNotification(application, recipient, sender);

        // was email sent?
        MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(recipient.getEmail());
        assertThat(inbox.length).isOne();

        // check content of user email
        Message msg = inbox[0];
        assertThat(msg.getSubject()).contains("Hilfe bei der Entscheidung über einen Urlaubsantrag");
        assertThat(new InternetAddress(recipient.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        String content = (String) msg.getContent();
        assertThat(content).contains("Hallo Max Muster");
        assertThat(content).contains("Rick Grimes bittet dich um Hilfe bei der Entscheidung über einen Urlaubsantrag");
        assertThat(content).contains("/web/application/1234");
    }

    @Test
    void sendDeclinedCancellationRequestApplicationNotification() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");

        final Person office = new Person("office", "Muster", "Marlene", "office@example.org");
        office.setPermissions(singletonList(OFFICE));
        office.setNotifications(singletonList(NOTIFICATION_OFFICE));
        personService.save(office);

        final ApplicationComment comment = new ApplicationComment(person, clock);
        comment.setPerson(office);
        comment.setText("Stornierung abgelehnt!");

        final Application application = createApplication(person);
        application.setStatus(ApplicationStatus.ALLOWED);
        application.setStartDate(LocalDate.of(2020, 5, 29));
        application.setEndDate(LocalDate.of(2020, 5, 29));

        final Person relevantPerson = new Person("relevant", "Person", "Relevant", "relevantperson@example.org");
        final List<Person> relevantPersons = new ArrayList<>();
        relevantPersons.add(relevantPerson);

        when(applicationRecipientService.getRecipientsOfInterest(application)).thenReturn(relevantPersons);
        when(applicationRecipientService.getRecipientsWithOfficeNotifications()).thenReturn(List.of(office));

        sut.sendDeclinedCancellationRequestApplicationNotification(application, comment);

        // send mail to applicant?
        MimeMessage[] inboxPerson = greenMail.getReceivedMessagesForDomain(person.getEmail());
        assertThat(inboxPerson.length).isOne();

        Message msgPerson = inboxPerson[0];
        assertThat(msgPerson.getSubject()).contains("Stornierungsantrag abgelehnt");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msgPerson.getAllRecipients()[0]);

        String contentPerson = (String) msgPerson.getContent();
        assertThat(contentPerson).contains("Hallo Lieschen Müller");
        assertThat(contentPerson).contains("dein Stornierungsantrag des genehmigten Urlaub");
        assertThat(contentPerson).contains("29.05.2020 bis 29.05.2020 wurde abgelehnt.");
        assertThat(contentPerson).contains("Kommentar von Marlene Muster zur Ablehnung deines Stornierungsantrags: Stornierung abgelehnt!");
        assertThat(contentPerson).contains("/web/application/1234");

        // send mail to office
        MimeMessage[] inboxOffice = greenMail.getReceivedMessagesForDomain(office.getEmail());
        assertThat(inboxOffice.length).isOne();

        Message msg = inboxOffice[0];
        assertThat(msg.getSubject()).contains("Stornierungsantrag abgelehnt");
        assertThat(new InternetAddress(office.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        String contentOffice = (String) msg.getContent();
        assertThat(contentOffice).contains("Hallo Marlene Muster");
        assertThat(contentOffice).contains("der Stornierungsantrag von Lieschen Müller des genehmigten Urlaub vom");
        assertThat(contentOffice).contains("29.05.2020 bis 29.05.2020 wurde abgelehnt.");
        assertThat(contentPerson).contains("Kommentar von Marlene Muster zur Ablehnung deines Stornierungsantrags: Stornierung abgelehnt!");
        assertThat(contentOffice).contains("/web/application/1234");

        // was email sent to relevant person
        MimeMessage[] inboxRelevantPerson = greenMail.getReceivedMessagesForDomain(relevantPerson.getEmail());
        assertThat(inboxRelevantPerson.length).isOne();

        Message msgRelevantPerson = inboxRelevantPerson[0];
        assertThat(msgRelevantPerson.getSubject()).isEqualTo("Stornierungsantrag abgelehnt");
        assertThat(new InternetAddress(relevantPerson.getEmail())).isEqualTo(msgRelevantPerson.getAllRecipients()[0]);

        String contentRelevantPerson = (String) msgRelevantPerson.getContent();
        assertThat(contentRelevantPerson).contains("Hallo Relevant Person");
        assertThat(contentRelevantPerson).contains("der Stornierungsantrag von Lieschen Müller des genehmigten Urlaub vom");
        assertThat(contentRelevantPerson).contains("29.05.2020 bis 29.05.2020 wurde abgelehnt.");
        assertThat(contentRelevantPerson).contains("Kommentar von Marlene Muster zur Ablehnung des Stornierungsantrags: Stornierung abgelehnt!");
        assertThat(contentRelevantPerson).contains("/web/application/1234");
    }

    @Test
    void ensureApplicantAndOfficeGetsMailAboutCancellationRequest() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");

        final Person office = new Person("office", "Muster", "Marlene", "office@example.org");
        office.setPermissions(singletonList(OFFICE));
        office.setNotifications(singletonList(NOTIFICATION_OFFICE));
        personService.save(office);

        final ApplicationComment comment = new ApplicationComment(person, clock);
        comment.setText("Bitte stornieren!");

        final Application application = createApplication(person);
        application.setStartDate(LocalDate.of(2020, 5, 29));
        application.setEndDate(LocalDate.of(2020, 5, 29));

        when(applicationRecipientService.getRecipientsWithOfficeNotifications()).thenReturn(List.of(office));

        sut.sendCancellationRequest(application, comment);

        // send mail to applicant?
        MimeMessage[] inboxPerson = greenMail.getReceivedMessagesForDomain(person.getEmail());
        assertThat(inboxPerson.length).isOne();

        Message msgPerson = inboxPerson[0];
        assertThat(msgPerson.getSubject()).contains("Stornierung wurde beantragt");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msgPerson.getAllRecipients()[0]);

        String contentPerson = (String) msgPerson.getContent();
        assertThat(contentPerson).contains("Hallo Lieschen Müller");
        assertThat(contentPerson).contains("dein Antrag zum Stornieren deines bereits genehmigten Antrags ");
        assertThat(contentPerson).contains("29.05.2020 bis 29.05.2020 wurde eingereicht.");
        assertThat(contentPerson).contains("/web/application/1234");
        assertThat(contentPerson).contains("Überblick deiner offenen Stornierungsanträge findest du unter ");
        assertThat(contentPerson).contains("/web/application#cancellation-requests");

        // send mail to all relevant persons?
        MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(office.getEmail());
        assertThat(inbox.length).isOne();

        Message msg = inbox[0];
        assertThat(msg.getSubject()).contains("Ein Benutzer beantragt die Stornierung eines genehmigten Antrags");
        assertThat(new InternetAddress(office.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        String contentOffice = (String) msg.getContent();
        assertThat(contentOffice).contains("Hallo Marlene Muster");
        assertThat(contentOffice).contains("hat beantragt den bereits genehmigten Urlaub");
        assertThat(contentOffice).contains("/web/application/1234");
        assertThat(contentOffice).contains("Überblick aller offenen Stornierungsanträge findest du unter");
        assertThat(contentOffice).contains("/web/application#cancellation-requests");
    }

    @Test
    void ensurePersonGetsMailIfApplicationForLeaveHasBeenConvertedToSickNote() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");

        final Person office = new Person("office", "Muster", "Marlene", "office@example.org");
        office.setPermissions(singletonList(OFFICE));

        final Application application = createApplication(person);
        application.setApplier(office);

        sut.sendSickNoteConvertedToVacationNotification(application);

        // was email sent?
        MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(person.getEmail());
        assertThat(inbox.length).isOne();

        // has mail correct attributes?
        Message msg = inbox[0];

        // check subject
        assertThat(msg.getSubject()).contains("Deine Krankmeldung wurde zu Urlaub umgewandelt");

        // check from and recipient
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        String content = (String) msg.getContent();
        assertThat(content).contains("Hallo Lieschen Müller");
        assertThat(content).contains("Marlene Muster hat deine Krankmeldung zu Urlaub umgewandelt");
        assertThat(content).contains("/web/application/1234");
    }

    @Test
    void ensureCorrectHolidayReplacementApplyMailIsSent() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        final Application application = createApplication(person);
        application.setStartDate(LocalDate.of(2020, 12, 18));
        application.setEndDate(LocalDate.of(2020, 12, 18));

        final Person holidayReplacement = new Person("replacement", "Teria", "Mar", "replacement@example.org");
        application.setHolidayReplacement(holidayReplacement);

        sut.notifyHolidayReplacementForApply(application);

        // was email sent?
        MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(holidayReplacement.getEmail());
        assertThat(inbox.length).isOne();

        Message msg = inbox[0];
        assertThat(msg.getSubject()).contains("Mögliche Urlaubsvertretung");
        assertThat(new InternetAddress(holidayReplacement.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        String content = (String) msg.getContent();
        assertThat(content).contains("Hallo Mar Teria");
        assertThat(content).contains("Lieschen Müller hat dich bei einer Abwesenheit als Vertretung vorgesehen.");
        assertThat(content).contains("Es handelt sich um den Zeitraum von 18.12.2020 bis 18.12.2020, ganztägig.");
    }

    @Test
    void ensureCorrectHolidayReplacementAllowMailIsSent() throws Exception {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        final Application application = createApplication(person);
        application.setStartDate(LocalDate.of(2020, 5, 29));
        application.setEndDate(LocalDate.of(2020, 5, 29));

        final Person holidayReplacement = new Person("replacement", "Teria", "Mar", "replacement@example.org");
        application.setHolidayReplacement(holidayReplacement);

        sut.notifyHolidayReplacementAllow(application);

        // was email sent?
        MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(holidayReplacement.getEmail());
        assertThat(inbox.length).isOne();

        MimeMessage msg = inbox[0];
        assertThat(msg.getSubject()).contains("Urlaubsvertretung");
        assertThat(new InternetAddress(holidayReplacement.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        String content = readPlainContent(msg);
        assertThat(content).contains("Hallo Mar Teria");
        assertThat(content).contains("die Abwesenheit von Lieschen Müller wurde genehmigt.");
        assertThat(content).contains("Du wurdest damit für den Zeitraum vom 29.05.2020 bis 29.05.2020, ganztägig als Vertretung eingetragen.");

        final List<DataSource> attachments = getAttachments(msg);
        assertThat(attachments.get(0).getName()).contains("calendar.ics");
    }

    @Test
    void ensureCorrectHolidayReplacementCancellationMailIsSent() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        final Application application = createApplication(person);
        application.setStartDate(LocalDate.of(2020, 12, 18));
        application.setEndDate(LocalDate.of(2020, 12, 18));

        final Person holidayReplacement = new Person("replacement", "Teria", "Mar", "replacement@example.org");
        application.setHolidayReplacement(holidayReplacement);

        sut.notifyHolidayReplacementAboutCancellation(application);

        // was email sent?
        MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(holidayReplacement.getEmail());
        assertThat(inbox.length).isOne();

        Message msg = inbox[0];
        assertThat(msg.getSubject()).contains("Mögliche Urlaubsvertretung abgelehnt");
        assertThat(new InternetAddress(holidayReplacement.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        String content = (String) msg.getContent();
        assertThat(content).contains("Hallo Mar Teria");
        assertThat(content).contains("du bist für die Abwesenheit von Lieschen Müller");
        assertThat(content).contains("im Zeitraum von 18.12.2020 bis 18.12.2020, ganztägig,");
        assertThat(content).contains("nicht mehr als Vertretung vorgesehen.");
    }

    @Test
    void ensureCorrectHolidayReplacementEditMailIsSent() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        final Application application = createApplication(person);
        application.setStartDate(LocalDate.of(2020, 12, 18));
        application.setEndDate(LocalDate.of(2020, 12, 18));

        final Person holidayReplacement = new Person("replacement", "Teria", "Mar", "replacement@example.org");
        application.setHolidayReplacement(holidayReplacement);

        sut.notifyHolidayReplacementAboutEdit(application);

        // was email sent?
        MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(holidayReplacement.getEmail());
        assertThat(inbox.length).isOne();

        Message msg = inbox[0];
        assertThat(msg.getSubject()).contains("Mögliche Urlaubsvertretung editiert");
        assertThat(new InternetAddress(holidayReplacement.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        String content = (String) msg.getContent();
        assertThat(content).contains("Hallo Mar Teria");
        assertThat(content).contains("der Zeitraum für die Abwesenheit von Lieschen Müller bei dem du als Vertretung vorgesehen bist, hat sich geändert.");
        assertThat(content).contains("Der neue Zeitraum ist von 18.12.2020 bis 18.12.2020, ganztägig.");
    }

    @Test
    void ensureCorrectFrom() throws MessagingException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");

        final Application application = createApplication(person);

        sut.sendConfirmation(application, null);

        MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(person.getEmail());
        assertThat(inbox.length).isOne();

        Message msg = inbox[0];
        Address[] from = msg.getFrom();
        assertThat(from).isNotNull();
        assertThat(from.length).isOne();
        assertThat(from[0]).hasToString(mailProperties.getSender());
    }

    @Test
    void ensureAfterApplyingForLeaveAConfirmationNotificationIsSentToPerson() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");

        final Application application = createApplication(person);

        final ApplicationComment comment = new ApplicationComment(person, clock);
        comment.setText("Hätte gerne Urlaub");

        sut.sendConfirmation(application, comment);

        // was email sent?
        MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(person.getEmail());
        assertThat(inbox.length).isOne();

        Message msg = inbox[0];
        assertThat(msg.getSubject()).contains("Antragsstellung");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        String content = (String) msg.getContent();
        assertThat(content).contains("Hallo Lieschen Müller");
        assertThat(content).contains("dein Urlaubsantrag wurde erfolgreich eingereicht");
        assertThat(content).contains(comment.getText());
        assertThat(content).contains(comment.getPerson().getNiceName());
        assertThat(content).contains("/web/application/1234");
    }


    @Test
    void ensurePersonGetsANotificationIfAnOfficeMemberAppliedForLeaveForThisPerson() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");

        final Application application = createApplication(person);

        final ApplicationComment comment = new ApplicationComment(person, clock);
        comment.setText("Habe das mal für dich beantragt");

        final Person office = new Person("office", "Muster", "Marlene", "office@example.org");
        office.setPermissions(singletonList(OFFICE));

        application.setApplier(office);
        sut.sendAppliedForLeaveByOfficeNotification(application, comment);

        // was email sent?
        MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(person.getEmail());
        assertThat(inbox.length).isOne();

        Message msg = inbox[0];
        assertThat(msg.getSubject()).contains("Für dich wurde ein Urlaubsantrag eingereicht");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        String content = (String) msg.getContent();
        assertThat(content).contains("Hallo Lieschen Müller");
        assertThat(content).contains("Marlene Muster hat einen Urlaubsantrag für dich gestellt");
        assertThat(content).contains(comment.getText());
        assertThat(content).contains(comment.getPerson().getNiceName());
        assertThat(content).contains("/web/application/1234");
    }

    @Test
    void ensurePersonAndRelevantPersonsGetsANotificationIfPersonCancelledOneOfHisApplications() throws MessagingException,
        IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");

        final Application application = createApplication(person);
        application.setCanceller(person);

        final ApplicationComment comment = new ApplicationComment(person, clock);
        comment.setText("Wrong date - revoked");

        final Person relevantPerson = new Person("relevant", "Person", "Relevant", "relevantperson@example.org");
        when(applicationRecipientService.getRecipientsOfInterest(application)).thenReturn(List.of(relevantPerson));

        sut.sendRevokedNotifications(application, comment);

        // was email sent to applicant
        MimeMessage[] inboxApplicant = greenMail.getReceivedMessagesForDomain(person.getEmail());
        assertThat(inboxApplicant.length).isOne();

        Message msg = inboxApplicant[0];
        assertThat(msg.getSubject()).isEqualTo("Dein Urlaubsantrag wurde erfolgreich storniert");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        String content = (String) msg.getContent();
        assertThat(content).contains("Hallo Lieschen Müller");
        assertThat(content).contains("nicht genehmigter Antrag wurde von dir erfolgreich");
        assertThat(content).contains(comment.getText());
        assertThat(content).contains(comment.getPerson().getNiceName());
        assertThat(content).contains("/web/application/1234");

        // was email sent to relevant person
        MimeMessage[] inboxRelevantPerson = greenMail.getReceivedMessagesForDomain(relevantPerson.getEmail());
        assertThat(inboxRelevantPerson.length).isOne();

        Message msgRelevantPerson = inboxRelevantPerson[0];
        assertThat(msgRelevantPerson.getSubject()).isEqualTo("Ein nicht genehmigter Urlaubsantrag wurde erfolgreich storniert");
        assertThat(new InternetAddress(relevantPerson.getEmail())).isEqualTo(msgRelevantPerson.getAllRecipients()[0]);

        String contentRelevantPerson = (String) msgRelevantPerson.getContent();
        assertThat(contentRelevantPerson).contains("Hallo Relevant Person");
        assertThat(contentRelevantPerson).contains("nicht genehmigter Antrag wurde von Lieschen Müller wurde durch Lieschen Müller storniert.");
        assertThat(contentRelevantPerson).contains(comment.getText());
        assertThat(contentRelevantPerson).contains(comment.getPerson().getNiceName());
        assertThat(contentRelevantPerson).contains("/web/application/1234");
    }

    @Test
    void ensurePersonAndRelevantPersonsGetsANotificationIfNotApplicantCancelledThisApplication() throws MessagingException,
        IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        final Application application = createApplication(person);

        final Person office = new Person("office", "Person", "Office", "office@example.org");
        application.setCanceller(office);

        final ApplicationComment comment = new ApplicationComment(office, clock);
        comment.setText("Wrong information - revoked");

        final Person relevantPerson = new Person("relevant", "Person", "Relevant", "relevantperson@example.org");
        when(applicationRecipientService.getRecipientsOfInterest(application)).thenReturn(List.of(relevantPerson));

        sut.sendRevokedNotifications(application, comment);

        // was email sent to applicant
        MimeMessage[] inboxApplicant = greenMail.getReceivedMessagesForDomain(person.getEmail());
        assertThat(inboxApplicant.length).isOne();

        Message msg = inboxApplicant[0];
        assertThat(msg.getSubject()).isEqualTo("Dein Urlaubsantrag wurde storniert");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        String content = (String) msg.getContent();
        assertThat(content).contains("Hallo Lieschen Müller");
        assertThat(content).contains("gestellter, nicht genehmigter Antrag wurde von Office Person storniert.");
        assertThat(content).contains(comment.getText());
        assertThat(content).contains(comment.getPerson().getNiceName());
        assertThat(content).contains("/web/application/1234");

        // was email sent to relevant person
        MimeMessage[] inboxRelevantPerson = greenMail.getReceivedMessagesForDomain(relevantPerson.getEmail());
        assertThat(inboxRelevantPerson.length).isOne();

        Message msgRelevantPerson = inboxRelevantPerson[0];
        assertThat(msgRelevantPerson.getSubject()).isEqualTo("Ein nicht genehmigter Urlaubsantrag wurde erfolgreich storniert");
        assertThat(new InternetAddress(relevantPerson.getEmail())).isEqualTo(msgRelevantPerson.getAllRecipients()[0]);

        String contentRelevantPerson = (String) msgRelevantPerson.getContent();
        assertThat(contentRelevantPerson).contains("Hallo Relevant Person");
        assertThat(contentRelevantPerson).contains("nicht genehmigter Antrag wurde von Lieschen Müller wurde durch Office Person storniert.");
        assertThat(contentRelevantPerson).contains(comment.getText());
        assertThat(contentRelevantPerson).contains(comment.getPerson().getNiceName());
        assertThat(contentRelevantPerson).contains("/web/application/1234");
    }

    @Test
    void ensurePersonGetsANotificationIfOfficeCancelledOneOfHisApplications() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");

        final Person office = new Person("office", "Muster", "Marlene", "office@example.org");
        office.setPermissions(singletonList(OFFICE));

        final Application application = createApplication(person);
        application.setApplicationDate(LocalDate.of(2020, 5, 29));
        application.setCanceller(office);

        final ApplicationComment comment = new ApplicationComment(person, clock);
        comment.setText("Geht leider nicht");

        final Person relevantPerson = new Person("relevant", "Person", "Relevant", "relevantperson@example.org");
        final List<Person> relevantPersons = new ArrayList<>();
        relevantPersons.add(relevantPerson);

        when(applicationRecipientService.getRecipientsOfInterest(application)).thenReturn(relevantPersons);
        when(applicationRecipientService.getRecipientsWithOfficeNotifications()).thenReturn(List.of(office));

        sut.sendCancelledByOfficeNotification(application, comment);

        // was email sent to applicant?
        MimeMessage[] inboxApplicant = greenMail.getReceivedMessagesForDomain(person.getEmail());
        assertThat(inboxApplicant.length).isOne();

        Message msg = inboxApplicant[0];
        assertThat(msg.getSubject()).isEqualTo("Dein Antrag wurde storniert");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        String content = (String) msg.getContent();
        assertThat(content).contains("Hallo Lieschen Müller");
        assertThat(content).contains("Marlene Muster hat einen deiner Urlaubsanträge storniert.");
        assertThat(content).contains(comment.getText());
        assertThat(content).contains(comment.getPerson().getNiceName());
        assertThat(content).contains("/web/application/1234");

        // was email sent to relevant person?
        MimeMessage[] inboxRelevantPerson = greenMail.getReceivedMessagesForDomain(relevantPerson.getEmail());
        assertThat(inboxRelevantPerson.length).isOne();

        Message msgRelevantPerson = inboxRelevantPerson[0];
        assertThat(msgRelevantPerson.getSubject()).isEqualTo("Ein Antrag wurde vom Office storniert");
        assertThat(new InternetAddress(relevantPerson.getEmail())).isEqualTo(msgRelevantPerson.getAllRecipients()[0]);

        String contentRelevantPerson = (String) msgRelevantPerson.getContent();
        assertThat(contentRelevantPerson).contains("Hallo Relevant Person");
        assertThat(contentRelevantPerson).contains("Marlene Muster hat den Urlaubsantrag von Lieschen Müller vom 29.05.2020 storniert.");
        assertThat(contentRelevantPerson).contains(comment.getText());
        assertThat(contentRelevantPerson).contains(comment.getPerson().getNiceName());
        assertThat(contentRelevantPerson).contains("/web/application/1234");
    }

    @Test
    void ensureNotificationAboutNewApplicationIsSentToBossesAndDepartmentHeads() throws MessagingException, IOException {

        final Person boss = new Person("boss", "Boss", "Hugo", "boss@example.org");
        boss.setPermissions(singletonList(BOSS));
        boss.setNotifications(singletonList(NOTIFICATION_BOSS_ALL));

        final Person departmentHead = new Person("departmentHead", "Kopf", "Senior", "head@example.org");
        departmentHead.setPermissions(singletonList(DEPARTMENT_HEAD));

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");

        final ApplicationComment comment = new ApplicationComment(person, clock);
        comment.setText("Hätte gerne Urlaub");

        final Application application = createApplication(person);

        when(departmentService.getApplicationsForLeaveOfMembersInDepartmentsOfPerson(person, application.getStartDate(), application.getEndDate())).thenReturn(singletonList(application));
        when(applicationRecipientService.getRecipientsOfInterest(application)).thenReturn(asList(boss, departmentHead));

        sut.sendNewApplicationNotification(application, comment);

        // was email sent to boss?
        MimeMessage[] inboxOfBoss = greenMail.getReceivedMessagesForDomain(boss.getEmail());
        assertThat(inboxOfBoss.length).isOne();

        // was email sent to department head?
        MimeMessage[] inboxOfDepartmentHead = greenMail.getReceivedMessagesForDomain(departmentHead.getEmail());
        assertThat(inboxOfDepartmentHead.length).isOne();

        // get email
        Message msgBoss = inboxOfBoss[0];
        Message msgDepartmentHead = inboxOfDepartmentHead[0];

        verifyNotificationAboutNewApplication(boss, msgBoss, application.getPerson().getNiceName(), comment);
        verifyNotificationAboutNewApplication(departmentHead, msgDepartmentHead, application.getPerson().getNiceName(),
            comment);
    }

    @Test
    void ensureNotificationAboutNewApplicationOfSecondStageAuthorityIsSentToBosses() throws MessagingException, IOException {

        final Person boss = new Person("boss", "Boss", "Hugo", "boss@example.org");
        boss.setPermissions(singletonList(BOSS));

        final Person secondStage = new Person("manager", "Schmitt", "Kai", "manager@example.org");
        secondStage.setPermissions(singletonList(SECOND_STAGE_AUTHORITY));

        final Person departmentHead = new Person("departmentHead", "Kopf", "Senior", "head@example.org");
        departmentHead.setPermissions(singletonList(DEPARTMENT_HEAD));

        final ApplicationComment comment = new ApplicationComment(secondStage, clock);
        comment.setText("Hätte gerne Urlaub");

        final Application application = createApplication(secondStage);

        when(departmentService.getApplicationsForLeaveOfMembersInDepartmentsOfPerson(secondStage, application.getStartDate(), application.getEndDate())).thenReturn(singletonList(application));
        when(applicationRecipientService.getRecipientsOfInterest(application)).thenReturn(asList(boss, departmentHead));

        sut.sendNewApplicationNotification(application, comment);

        // was email sent to boss?
        MimeMessage[] inboxOfBoss = greenMail.getReceivedMessagesForDomain(boss.getEmail());
        assertThat(inboxOfBoss.length).isOne();

        // no email sent to department head
        MimeMessage[] inboxOfDepartmentHead = greenMail.getReceivedMessagesForDomain(departmentHead.getEmail());
        assertThat(inboxOfDepartmentHead.length).isOne();

        // get email
        Message msgBoss = inboxOfBoss[0];
        verifyNotificationAboutNewApplication(boss, msgBoss, application.getPerson().getNiceName(), comment);
    }

    @Test
    void ensureNotificationAboutNewApplicationOfDepartmentHeadIsSentToSecondaryStageAuthority() throws MessagingException, IOException {

        final Person boss = new Person("boss", "Boss", "Hugo", "boss@example.org");
        boss.setPermissions(singletonList(BOSS));

        final Person secondStage = new Person("manager", "Schmitt", "Kai", "manager@example.org");
        secondStage.setPermissions(singletonList(SECOND_STAGE_AUTHORITY));

        final Person departmentHead = new Person("departmentHead", "Kopf", "Senior", "head@example.org");
        departmentHead.setPermissions(singletonList(DEPARTMENT_HEAD));

        final ApplicationComment comment = new ApplicationComment(departmentHead, clock);
        comment.setText("Hätte gerne Urlaub");

        final Application application = createApplication(departmentHead);

        when(departmentService.getApplicationsForLeaveOfMembersInDepartmentsOfPerson(departmentHead, application.getStartDate(), application.getEndDate())).thenReturn(singletonList(application));
        when(applicationRecipientService.getRecipientsOfInterest(application)).thenReturn(asList(boss, secondStage));

        sut.sendNewApplicationNotification(application, comment);

        // was email sent to boss?
        MimeMessage[] inboxOfBoss = greenMail.getReceivedMessagesForDomain(boss.getEmail());
        assertThat(inboxOfBoss.length).isOne();

        // was email sent to secondary stage?
        MimeMessage[] inboxOfSecondaryStage = greenMail.getReceivedMessagesForDomain(secondStage.getEmail());
        assertThat(inboxOfSecondaryStage.length).isOne();

        // get email
        Message msgBoss = inboxOfBoss[0];
        Message msgSecondaryStage = inboxOfSecondaryStage[0];

        verifyNotificationAboutNewApplication(boss, msgBoss, application.getPerson().getNiceName(), comment);
        verifyNotificationAboutNewApplication(secondStage, msgSecondaryStage, application.getPerson().getNiceName(),
            comment);
    }

    @Test
    void ensureNotificationAboutTemporaryAllowedApplicationIsSentToSecondStageAuthoritiesAndToPerson()
        throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");

        final Person secondStage = new Person("manager", "Schmitt", "Kai", "manager@example.org");
        secondStage.setPermissions(singletonList(SECOND_STAGE_AUTHORITY));

        final ApplicationComment comment = new ApplicationComment(secondStage, clock);
        comment.setText("OK, spricht von meiner Seite aus nix dagegen");

        final Application application = createApplication(person);

        when(departmentService.getApplicationsForLeaveOfMembersInDepartmentsOfPerson(person, application.getStartDate(), application.getEndDate())).thenReturn(singletonList(application));
        when(applicationRecipientService.getRecipientsForTemporaryAllow(application)).thenReturn(singletonList(secondStage));

        sut.sendTemporaryAllowedNotification(application, comment);

        // were both emails sent?
        MimeMessage[] inboxSecondStage = greenMail.getReceivedMessagesForDomain(secondStage.getEmail());
        assertThat(inboxSecondStage.length).isOne();

        MimeMessage[] inboxUser = greenMail.getReceivedMessagesForDomain(person.getEmail());
        assertThat(inboxUser.length).isOne();

        // get email user
        Message msg = inboxUser[0];
        assertThat(msg.getSubject()).isEqualTo("Dein Urlaubsantrag wurde vorläufig bewilligt");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of user email
        String contentUser = (String) msg.getContent();
        assertThat(contentUser).contains("Hallo Lieschen Müller");
        assertThat(contentUser).contains("Bitte beachte, dass dieser erst noch von einem entsprechend Verantwortlichen freigegeben werden muss");
        assertThat(contentUser).contains(comment.getText());
        assertThat(contentUser).contains(comment.getPerson().getNiceName());
        assertThat(contentUser).contains("Link zum Antrag:");
        assertThat(contentUser).contains("/web/application/1234");

        // get email office
        Message msgSecondStage = inboxSecondStage[0];
        assertThat(msgSecondStage.getSubject()).isEqualTo("Ein Urlaubsantrag wurde vorläufig bewilligt");
        assertThat(new InternetAddress(secondStage.getEmail())).isEqualTo(msgSecondStage.getAllRecipients()[0]);

        // check content of office email
        String contentSecondStageMail = (String) msgSecondStage.getContent();
        assertThat(contentSecondStageMail).contains("es liegt ein neuer zu genehmigender Antrag vor:");
        assertThat(contentSecondStageMail).contains("/web/application/1234");
        assertThat(contentSecondStageMail).contains("Der Antrag wurde bereits vorläufig genehmigt und muss nun noch endgültig freigegeben werden");
        assertThat(contentSecondStageMail).contains("Lieschen Müller");
        assertThat(contentSecondStageMail).contains("Erholungsurlaub");
        assertThat(contentSecondStageMail).contains(comment.getText());
        assertThat(contentSecondStageMail).contains(comment.getPerson().getNiceName());
    }

    @Test
    void ensureBossesAndDepartmentHeadsGetRemindMail() throws MessagingException, IOException {

        final Person boss = new Person("boss", "Boss", "Hugo", "boss@example.org");
        boss.setPermissions(singletonList(BOSS));

        final Person departmentHead = new Person("departmentHead", "Kopf", "Senior", "head@example.org");
        departmentHead.setPermissions(singletonList(DEPARTMENT_HEAD));

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");

        final ApplicationComment comment = new ApplicationComment(person, clock);
        comment.setText("OK, spricht von meiner Seite aus nix dagegen");

        final Application application = createApplication(person);

        when(applicationRecipientService.getRecipientsOfInterest(application)).thenReturn(asList(boss, departmentHead));

        sut.sendRemindBossNotification(application);

        // was email sent to boss?
        MimeMessage[] inboxOfBoss = greenMail.getReceivedMessagesForDomain(boss.getEmail());
        assertThat(inboxOfBoss.length).isOne();

        // was email sent to department head?
        MimeMessage[] inboxOfDepartmentHead = greenMail.getReceivedMessagesForDomain(departmentHead.getEmail());
        assertThat(inboxOfDepartmentHead.length).isOne();

        // has mail correct attributes?
        Message msg = inboxOfBoss[0];
        assertThat(msg.getSubject()).contains("Erinnerung wartender Urlaubsantrag");
        assertThat(new InternetAddress(boss.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        String content = (String) msg.getContent();
        assertThat(content).contains("Hallo Hugo Boss");
        assertThat(content).contains("/web/application/1234");
    }

    @Test
    void ensureSendRemindForWaitingApplicationsReminderNotification() throws Exception {

        // PERSONs
        final Person personDepartmentA = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Person personDepartmentB = new Person("muster", "Muster", "Marlene", "muster@example.org");
        final Person personDepartmentC = new Person("muster", "Muster", "Marlene", "muster@example.org");

        // APPLICATIONs
        final Application applicationA = createApplication(personDepartmentA);
        applicationA.setId(1);
        final Application applicationB = createApplication(personDepartmentB);
        applicationB.setId(2);
        final Application applicationC = createApplication(personDepartmentC);
        applicationC.setId(3);

        // DEPARTMENT HEADs
        final Person boss = new Person("boss", "Boss", "Hugo", "boss@example.org");
        final Person departmentHeadA = new Person("headAC", "Wurst", "Heinz", "headAC@example.org");
        final Person departmentHeadB = new Person("headB", "Mustermann", "Michel", "headB@example.org");

        when(applicationRecipientService.getRecipientsOfInterest(applicationA)).thenReturn(asList(boss, departmentHeadA));
        when(applicationRecipientService.getRecipientsOfInterest(applicationB)).thenReturn(asList(boss, departmentHeadB));
        when(applicationRecipientService.getRecipientsOfInterest(applicationC)).thenReturn(asList(boss, departmentHeadA));

        sut.sendRemindForWaitingApplicationsReminderNotification(asList(applicationA, applicationB, applicationC));

        verifyInbox(boss, asList(applicationA, applicationB, applicationC));
        verifyInbox(departmentHeadA, asList(applicationA, applicationC));
        verifyInbox(departmentHeadB, singletonList(applicationB));
    }

    @Test
    void sendEditedApplicationNotification() throws Exception {

        final Person recipient = new Person("recipient", "Muster", "Max", "mustermann@example.org");
        final Application application = createApplication(recipient);
        application.setPerson(recipient);

        sut.sendEditedApplicationNotification(application, recipient);

        // was email sent?
        MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(recipient.getEmail());
        assertThat(inbox.length).isOne();

        // check content of user email
        Message msg = inbox[0];
        assertThat(msg.getSubject()).contains("Urlaubsantrag von Max Muster wurde erfolgreich editiert");
        assertThat(new InternetAddress(recipient.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        String content = (String) msg.getContent();
        assertThat(content).contains("Hallo Max Muster");
        assertThat(content).contains("der Urlaubsantrag von Max Muster wurde angepasst.");
        assertThat(content).contains("/web/application/1234");
    }

    private void verifyInbox(Person inboxOwner, List<Application> applications) throws MessagingException, IOException {

        MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(inboxOwner.getEmail());
        assertThat(inbox.length).isOne();

        Message msg = inbox[0];
        assertThat(msg.getSubject()).contains("Erinnerung für wartende Urlaubsanträge");

        String content = (String) msg.getContent();
        assertThat(content).contains("Hallo " + inboxOwner.getNiceName());

        for (Application application : applications) {
            assertThat(content).contains(application.getApplier().getNiceName());
            assertThat(content).contains("/web/application/" + application.getId());
        }
    }

    private void verifyNotificationAboutNewApplication(Person recipient, Message msg, String niceName,
                                                       ApplicationComment comment) throws MessagingException, IOException {

        // check subject
        assertThat(msg.getSubject()).isEqualTo("Neuer Urlaubsantrag für " + niceName);

        // check from and recipient
        assertThat(new InternetAddress(recipient.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        String contentDepartmentHead = (String) msg.getContent();
        assertThat(contentDepartmentHead).contains("Hallo " + recipient.getNiceName());
        assertThat(contentDepartmentHead).contains(niceName);
        assertThat(contentDepartmentHead).contains("Erholungsurlaub");
        assertThat(contentDepartmentHead).contains("es liegt ein neuer zu genehmigender Antrag vor");
        assertThat(contentDepartmentHead).contains("/web/application/1234");
        assertThat(contentDepartmentHead).contains(comment.getText());
        assertThat(contentDepartmentHead).contains(comment.getPerson().getNiceName());
    }

    private Application createApplication(Person person) {

        final LocalDate now = LocalDate.now(UTC);

        Application application = new Application();
        application.setId(1234);
        application.setPerson(person);
        application.setVacationType(TestDataCreator.createVacationType(HOLIDAY, "application.data.vacationType.holiday"));
        application.setDayLength(FULL);
        application.setApplicationDate(now);
        application.setStartDate(now);
        application.setEndDate(now);
        application.setApplier(person);

        return application;
    }

    private String readPlainContent(MimeMessage message) throws Exception {
        return new MimeMessageParser(message).parse().getPlainContent();
    }

    private List<DataSource> getAttachments(MimeMessage message) throws Exception {
        return new MimeMessageParser(message).parse().getAttachmentList();
    }
}
