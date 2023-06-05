package org.synyx.urlaubsverwaltung.application.application;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.apache.commons.mail.util.MimeMessageParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.TestContainersBase;
import org.synyx.urlaubsverwaltung.application.comment.ApplicationComment;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.mail.MailProperties;
import org.synyx.urlaubsverwaltung.mail.MailRecipientService;
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
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.List;

import static java.time.Month.APRIL;
import static java.time.Month.DECEMBER;
import static java.time.Month.FEBRUARY;
import static java.time.Month.MAY;
import static java.time.Month.NOVEMBER;
import static java.time.ZoneOffset.UTC;
import static java.util.Arrays.asList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createVacationTypeEntity;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_ALLOWED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_APPLIED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_CANCELLATION;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_COLLEAGUES_ALLOWED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_COLLEAGUES_CANCELLATION;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_CONVERTED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_EDITED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT_UPCOMING;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALLOWED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CONVERTED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_EDITED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_REJECTED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_REVOKED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_WAITING_REMINDER;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_REJECTED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_REVOKED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_UPCOMING;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@SpringBootTest(properties = {"spring.mail.port=3025", "spring.mail.host=localhost", "spring.main.allow-bean-definition-overriding=true"})
@Transactional
class ApplicationMailServiceIT extends TestContainersBase {

    private static final String EMAIL_LINE_BREAK = "\r\n";

    @RegisterExtension
    static final GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP_IMAP);

    @Autowired
    private ApplicationMailService sut;
    @Autowired
    private PersonService personService;
    @Autowired
    private MailProperties mailProperties;
    @Autowired
    private Clock clock;

    @MockBean
    private MailRecipientService mailRecipientService;
    @MockBean
    private DepartmentService departmentService;

    @TestConfiguration
    public static class ClockConfig {
        @Bean
        public Clock clock() {
            return Clock.fixed(Instant.parse("2022-01-01T00:00:00.00Z"), ZoneId.of("UTC"));
        }
    }

    @Test
    void ensureNotificationAboutAllowedApplicationIsSentToApplicantManagementAndColleague() throws Exception {

        final Person person = new Person("user", "Mueller", "Lieschen", "lieschen@example.org");
        person.setId(3);
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_ALLOWED));

        final Person office = personService.create("office", "Marlene", "Muster", "office@example.org", List.of(NOTIFICATION_EMAIL_APPLICATION_ALLOWED, NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALLOWED), List.of(OFFICE));

        final Person boss = new Person("boss", "Boss", "Hugo", "boss@example.org");
        boss.setId(2);
        boss.setPermissions(List.of(BOSS));
        boss.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALLOWED));

        final Application application = createApplication(person);
        application.setApplicationDate(LocalDate.of(2021, APRIL, 12));
        application.setStartDate(LocalDate.of(2021, APRIL, 16));
        application.setEndDate(LocalDate.of(2021, APRIL, 16));
        application.setBoss(boss);

        final ApplicationComment comment = new ApplicationComment(boss, clock);
        comment.setText("OK, Urlaub kann genommen werden");

        when(mailRecipientService.getRecipientsOfInterest(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALLOWED)).thenReturn(List.of(boss, office));

        final Person colleague = new Person("colleague", "Dampf", "Hans", "dampf@example.org");
        colleague.setId(42);
        when(mailRecipientService.getColleagues(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_COLLEAGUES_ALLOWED)).thenReturn(List.of(colleague));

        sut.sendAllowedNotification(application, comment);

        // were both emails sent?
        final MimeMessage[] inboxOffice = greenMail.getReceivedMessagesForDomain(office.getEmail());
        assertThat(inboxOffice.length).isOne();

        final MimeMessage[] inboxUser = greenMail.getReceivedMessagesForDomain(person.getEmail());
        assertThat(inboxUser.length).isOne();

        final MimeMessage[] inboxBoss = greenMail.getReceivedMessagesForDomain(boss.getEmail());
        assertThat(inboxBoss.length).isOne();

        final MimeMessage[] inboxColleague = greenMail.getReceivedMessagesForDomain(colleague.getEmail());
        assertThat(inboxColleague.length).isOne();

        // check email user attributes
        final MimeMessage msg = inboxUser[0];
        assertThat(msg.getSubject()).isEqualTo("Deine Abwesenheit wurde genehmigt");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of user email
        final MimeMessage msgUser = inboxUser[0];
        assertThat(msgUser.getSubject()).isEqualTo("Deine Abwesenheit wurde genehmigt");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msgUser.getAllRecipients()[0]);
        assertThat(readPlainContent(msgUser)).isEqualTo("Hallo Lieschen Mueller," + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "deine Abwesenheit vom 16.04.2021 bis zum 16.04.2021 wurde von Hugo Boss genehmigt." + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    https://localhost:8080/web/application/1234" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Kommentar von Hugo Boss:" + EMAIL_LINE_BREAK +
            "OK, Urlaub kann genommen werden" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Informationen zur Abwesenheit:" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    Zeitraum:            16.04.2021 bis 16.04.2021, ganztägig" + EMAIL_LINE_BREAK +
            "    Art der Abwesenheit: Erholungsurlaub" + EMAIL_LINE_BREAK +
            "    Grund:               " + EMAIL_LINE_BREAK +
            "    Vertretung:          " + EMAIL_LINE_BREAK +
            "    Anschrift/Telefon:   " + EMAIL_LINE_BREAK +
            "    Erstellungsdatum:    12.04.2021" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/3/notifications anpassen." + EMAIL_LINE_BREAK);

        final List<DataSource> attachmentsUser = getAttachments(msgUser);
        assertThat(attachmentsUser.get(0).getName()).contains("calendar.ics");

        // check email office attributes
        final MimeMessage msgOffice = inboxOffice[0];
        assertThat(msgOffice.getSubject()).isEqualTo("Neue genehmigte Abwesenheit von Lieschen Mueller");
        assertThat(new InternetAddress(office.getEmail())).isEqualTo(msgOffice.getAllRecipients()[0]);
        assertThat(readPlainContent(msgOffice)).isEqualTo("Hallo Marlene Muster," + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "folgende Abwesenheit von Lieschen Mueller wurde von Hugo Boss genehmigt." + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    https://localhost:8080/web/application/1234" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Kommentar von Hugo Boss:" + EMAIL_LINE_BREAK +
            "OK, Urlaub kann genommen werden" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Informationen zur Abwesenheit:" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    Mitarbeiter:         Lieschen Mueller" + EMAIL_LINE_BREAK +
            "    Zeitraum:            16.04.2021 bis 16.04.2021, ganztägig" + EMAIL_LINE_BREAK +
            "    Art der Abwesenheit: Erholungsurlaub" + EMAIL_LINE_BREAK +
            "    Grund:               " + EMAIL_LINE_BREAK +
            "    Vertretung:          " + EMAIL_LINE_BREAK +
            "    Anschrift/Telefon:   " + EMAIL_LINE_BREAK +
            "    Erstellungsdatum:    12.04.2021" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/" + office.getId() + "/notifications anpassen." + EMAIL_LINE_BREAK);

        final List<DataSource> attachmentsOffice = getAttachments(msgOffice);
        assertThat(attachmentsOffice.get(0).getName()).contains("calendar.ics");

        // check email management attributes
        final MimeMessage msgBoss = inboxBoss[0];
        assertThat(msgBoss.getSubject()).isEqualTo("Neue genehmigte Abwesenheit von Lieschen Mueller");
        assertThat(new InternetAddress(boss.getEmail())).isEqualTo(msgBoss.getAllRecipients()[0]);
        assertThat(readPlainContent(msgBoss)).isEqualTo("Hallo Hugo Boss," + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "folgende Abwesenheit von Lieschen Mueller wurde von Hugo Boss genehmigt." + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    https://localhost:8080/web/application/1234" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Kommentar von Hugo Boss:" + EMAIL_LINE_BREAK +
            "OK, Urlaub kann genommen werden" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Informationen zur Abwesenheit:" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    Mitarbeiter:         Lieschen Mueller" + EMAIL_LINE_BREAK +
            "    Zeitraum:            16.04.2021 bis 16.04.2021, ganztägig" + EMAIL_LINE_BREAK +
            "    Art der Abwesenheit: Erholungsurlaub" + EMAIL_LINE_BREAK +
            "    Grund:               " + EMAIL_LINE_BREAK +
            "    Vertretung:          " + EMAIL_LINE_BREAK +
            "    Anschrift/Telefon:   " + EMAIL_LINE_BREAK +
            "    Erstellungsdatum:    12.04.2021" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/2/notifications anpassen." + EMAIL_LINE_BREAK);

        final List<DataSource> attachmentsBoss = getAttachments(msgBoss);
        assertThat(attachmentsBoss.get(0).getName()).contains("calendar.ics");

        // check email colleague
        final MimeMessage msgColleague = inboxColleague[0];
        assertThat(msgColleague.getSubject()).isEqualTo("Neue Abwesenheit von Lieschen Mueller");
        assertThat(new InternetAddress(colleague.getEmail())).isEqualTo(msgColleague.getAllRecipients()[0]);
        assertThat(readPlainContent(msgColleague)).isEqualTo("Hallo Hans Dampf," + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "eine Abwesenheit von Lieschen Mueller wurde erstellt:" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    Zeitraum: 16.04.2021 bis 16.04.2021, ganztägig" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Link zur Abwesenheitsübersicht: https://localhost:8080/web/absences" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/42/notifications anpassen." + EMAIL_LINE_BREAK);

        final List<DataSource> attachmentsColleague = getAttachments(msgColleague);
        assertThat(attachmentsColleague.get(0).getName()).contains("calendar.ics");
    }

    @Test
    void ensureNotificationAboutAllowedApplicationIsSentToOfficeAndThePersonWithOneReplacement() throws Exception {

        final Person person = new Person("user", "Mueller", "Lieschen", "lieschen@example.org");
        person.setId(1);
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_ALLOWED));
        final Person holidayReplacement = new Person("pennyworth", "Pennyworth", "Alfred", "pennyworth@example.org");

        final Person office = personService.create("office", "Marlene", "Muster", "office@example.org", List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALLOWED), List.of(OFFICE));

        final Person boss = new Person("boss", "Boss", "Hugo", "boss@example.org");
        boss.setPermissions(List.of(BOSS));

        final HolidayReplacementEntity holidayReplacementEntity = new HolidayReplacementEntity();
        holidayReplacementEntity.setPerson(holidayReplacement);

        final Application application = createApplication(person);
        application.setApplicationDate(LocalDate.of(2021, APRIL, 12));
        application.setStartDate(LocalDate.of(2021, APRIL, 16));
        application.setEndDate(LocalDate.of(2021, APRIL, 16));
        application.setBoss(boss);
        application.setHolidayReplacements(List.of(holidayReplacementEntity));

        final ApplicationComment comment = new ApplicationComment(boss, clock);
        comment.setText("OK, Urlaub kann genommen werden");

        when(mailRecipientService.getRecipientsOfInterest(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALLOWED)).thenReturn(List.of(office));

        sut.sendAllowedNotification(application, comment);

        // were both emails sent?
        final MimeMessage[] inboxOffice = greenMail.getReceivedMessagesForDomain(office.getEmail());
        assertThat(inboxOffice).hasSize(1);

        final MimeMessage[] inboxUser = greenMail.getReceivedMessagesForDomain(person.getEmail());
        assertThat(inboxUser.length).isOne();

        // check email user attributes
        final MimeMessage msgUser = inboxUser[0];
        assertThat(msgUser.getSubject()).isEqualTo("Deine Abwesenheit wurde genehmigt");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msgUser.getAllRecipients()[0]);
        assertThat(readPlainContent(msgUser)).isEqualTo("Hallo Lieschen Mueller," + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "deine Abwesenheit vom 16.04.2021 bis zum 16.04.2021 wurde von Hugo Boss genehmigt." + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    https://localhost:8080/web/application/1234" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Kommentar von Hugo Boss:" + EMAIL_LINE_BREAK +
            "OK, Urlaub kann genommen werden" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Informationen zur Abwesenheit:" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    Zeitraum:            16.04.2021 bis 16.04.2021, ganztägig" + EMAIL_LINE_BREAK +
            "    Art der Abwesenheit: Erholungsurlaub" + EMAIL_LINE_BREAK +
            "    Grund:               " + EMAIL_LINE_BREAK +
            "    Vertretung:          Alfred Pennyworth" + EMAIL_LINE_BREAK +
            "    Anschrift/Telefon:   " + EMAIL_LINE_BREAK +
            "    Erstellungsdatum:    12.04.2021" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen." + EMAIL_LINE_BREAK);

        final List<DataSource> attachmentsUser = getAttachments(msgUser);
        assertThat(attachmentsUser.get(0).getName()).contains("calendar.ics");

        // check email office attributes
        final MimeMessage msgOffice = inboxOffice[0];
        assertThat(msgOffice.getSubject()).isEqualTo("Neue genehmigte Abwesenheit von Lieschen Mueller");
        assertThat(new InternetAddress(office.getEmail())).isEqualTo(msgOffice.getAllRecipients()[0]);
        assertThat(readPlainContent(msgOffice)).isEqualTo("Hallo Marlene Muster," + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "folgende Abwesenheit von Lieschen Mueller wurde von Hugo Boss genehmigt." + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    https://localhost:8080/web/application/1234" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Kommentar von Hugo Boss:" + EMAIL_LINE_BREAK +
            "OK, Urlaub kann genommen werden" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Informationen zur Abwesenheit:" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    Mitarbeiter:         Lieschen Mueller" + EMAIL_LINE_BREAK +
            "    Zeitraum:            16.04.2021 bis 16.04.2021, ganztägig" + EMAIL_LINE_BREAK +
            "    Art der Abwesenheit: Erholungsurlaub" + EMAIL_LINE_BREAK +
            "    Grund:               " + EMAIL_LINE_BREAK +
            "    Vertretung:          Alfred Pennyworth" + EMAIL_LINE_BREAK +
            "    Anschrift/Telefon:   " + EMAIL_LINE_BREAK +
            "    Erstellungsdatum:    12.04.2021" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/" + office.getId() + "/notifications anpassen." + EMAIL_LINE_BREAK);

        final List<DataSource> attachmentsOffice = getAttachments(msgOffice);
        assertThat(attachmentsOffice.get(0).getName()).contains("calendar.ics");
    }

    @Test
    void ensureNotificationAboutAllowedApplicationIsSentToOfficeAndThePersonWithMultipleReplacements() throws Exception {

        final Person person = new Person("user", "Mueller", "Lieschen", "lieschen@example.org");
        person.setId(1);
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_ALLOWED));
        final Person holidayReplacementOne = new Person("pennyworth", "Pennyworth", "Alfred", "pennyworth@example.org");
        final Person holidayReplacementTwo = new Person("rob", "", "Robin", "robin@example.org");

        final Person office = personService.create("office", "Marlene", "Muster", "office@example.org", List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALLOWED), List.of(OFFICE));

        final Person boss = new Person("boss", "Boss", "Hugo", "boss@example.org");
        boss.setPermissions(List.of(BOSS));

        final HolidayReplacementEntity holidayReplacementOneEntity = new HolidayReplacementEntity();
        holidayReplacementOneEntity.setPerson(holidayReplacementOne);

        final HolidayReplacementEntity holidayReplacementTwoEntity = new HolidayReplacementEntity();
        holidayReplacementTwoEntity.setPerson(holidayReplacementTwo);

        final Application application = createApplication(person);
        application.setApplicationDate(LocalDate.of(2021, APRIL, 12));
        application.setStartDate(LocalDate.of(2021, APRIL, 16));
        application.setEndDate(LocalDate.of(2021, APRIL, 16));
        application.setBoss(boss);
        application.setHolidayReplacements(List.of(holidayReplacementOneEntity, holidayReplacementTwoEntity));

        final ApplicationComment comment = new ApplicationComment(boss, clock);
        comment.setText("OK, Urlaub kann genommen werden");

        when(mailRecipientService.getRecipientsOfInterest(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALLOWED)).thenReturn(List.of(office));

        sut.sendAllowedNotification(application, comment);

        // were both emails sent?
        final MimeMessage[] inboxOffice = greenMail.getReceivedMessagesForDomain(office.getEmail());
        assertThat(inboxOffice).hasSize(1);

        final MimeMessage[] inboxUser = greenMail.getReceivedMessagesForDomain(person.getEmail());
        assertThat(inboxUser.length).isOne();

        // check content of user email
        final MimeMessage msgUser = inboxUser[0];
        assertThat(msgUser.getSubject()).isEqualTo("Deine Abwesenheit wurde genehmigt");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msgUser.getAllRecipients()[0]);
        assertThat(readPlainContent(msgUser)).isEqualTo("Hallo Lieschen Mueller," + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "deine Abwesenheit vom 16.04.2021 bis zum 16.04.2021 wurde von Hugo Boss genehmigt." + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    https://localhost:8080/web/application/1234" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Kommentar von Hugo Boss:" + EMAIL_LINE_BREAK +
            "OK, Urlaub kann genommen werden" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Informationen zur Abwesenheit:" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    Zeitraum:            16.04.2021 bis 16.04.2021, ganztägig" + EMAIL_LINE_BREAK +
            "    Art der Abwesenheit: Erholungsurlaub" + EMAIL_LINE_BREAK +
            "    Grund:               " + EMAIL_LINE_BREAK +
            "    Vertretung:          Alfred Pennyworth, Robin" + EMAIL_LINE_BREAK +
            "    Anschrift/Telefon:   " + EMAIL_LINE_BREAK +
            "    Erstellungsdatum:    12.04.2021" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen." + EMAIL_LINE_BREAK);

        final List<DataSource> attachmentsUser = getAttachments(msgUser);
        assertThat(attachmentsUser.get(0).getName()).contains("calendar.ics");

        // check email office attributes
        final MimeMessage msgOffice = inboxOffice[0];
        assertThat(msgOffice.getSubject()).isEqualTo("Neue genehmigte Abwesenheit von Lieschen Mueller");
        assertThat(new InternetAddress(office.getEmail())).isEqualTo(msgOffice.getAllRecipients()[0]);
        assertThat(readPlainContent(msgOffice)).isEqualTo("Hallo Marlene Muster," + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "folgende Abwesenheit von Lieschen Mueller wurde von Hugo Boss genehmigt." + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    https://localhost:8080/web/application/1234" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Kommentar von Hugo Boss:" + EMAIL_LINE_BREAK +
            "OK, Urlaub kann genommen werden" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Informationen zur Abwesenheit:" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    Mitarbeiter:         Lieschen Mueller" + EMAIL_LINE_BREAK +
            "    Zeitraum:            16.04.2021 bis 16.04.2021, ganztägig" + EMAIL_LINE_BREAK +
            "    Art der Abwesenheit: Erholungsurlaub" + EMAIL_LINE_BREAK +
            "    Grund:               " + EMAIL_LINE_BREAK +
            "    Vertretung:          Alfred Pennyworth, Robin" + EMAIL_LINE_BREAK +
            "    Anschrift/Telefon:   " + EMAIL_LINE_BREAK +
            "    Erstellungsdatum:    12.04.2021" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/" + office.getId() + "/notifications anpassen." + EMAIL_LINE_BREAK);

        final List<DataSource> attachmentsOffice = getAttachments(msgOffice);
        assertThat(attachmentsOffice.get(0).getName()).contains("calendar.ics");
    }

    @Test
    void ensureNotificationAboutRejectedApplicationIsSentToApplierAndRelevantPersons() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setId(1);
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_REJECTED));

        final Person boss = new Person("boss", "Boss", "Hugo", "boss@example.org");
        boss.setId(2);
        boss.setPermissions(List.of(BOSS));
        boss.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_REJECTED));

        final ApplicationComment comment = new ApplicationComment(boss, clock);
        comment.setText("Geht leider nicht zu dem Zeitraum");

        final Application application = createApplication(person);
        application.setStartDate(LocalDate.of(2023, 5, 22));
        application.setEndDate(LocalDate.of(2023, 5, 22));
        application.setApplicationDate(LocalDate.of(2023, 5, 22));
        application.setBoss(boss);

        final Person departmentHead = new Person("departmentHead", "Head", "Department", "dh@example.org");
        departmentHead.setId(3);
        departmentHead.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_REJECTED));
        when(mailRecipientService.getRecipientsOfInterest(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_REJECTED)).thenReturn(List.of(boss, departmentHead));

        sut.sendRejectedNotification(application, comment);

        // was email sent?
        MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(person.getEmail());
        assertThat(inbox.length).isOne();

        // check content of user email
        Message msg = inbox[0];
        assertThat(msg.getSubject()).isEqualTo("Deine zu genehmigende Abwesenheit wurde abgelehnt");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        assertThat(msg.getContent()).isEqualTo("Hallo Lieschen Müller," + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "dein am 22.05.2023 gestellte Abwesenheit wurde leider von Hugo Boss abgelehnt." + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    https://localhost:8080/web/application/1234" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Begründung:" + EMAIL_LINE_BREAK +
            "Geht leider nicht zu dem Zeitraum" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen.");

        // was email sent to boss
        MimeMessage[] inboxBoss = greenMail.getReceivedMessagesForDomain(boss.getEmail());
        assertThat(inboxBoss.length).isOne();
        Message msgBoss = inboxBoss[0];
        assertThat(msgBoss.getSubject()).isEqualTo("Eine zu genehmigende Abwesenheit wurde abgelehnt");
        assertThat(msgBoss.getContent()).isEqualTo("Hallo Hugo Boss," + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "der von Lieschen Müller am 22.05.2023 gestellte Antrag wurde von Hugo Boss abgelehnt." + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    https://localhost:8080/web/application/1234" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Begründung:" + EMAIL_LINE_BREAK +
            "Geht leider nicht zu dem Zeitraum" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/2/notifications anpassen.");

        // was email sent to departmentHead
        MimeMessage[] inboxDepartmentHead = greenMail.getReceivedMessagesForDomain(departmentHead.getEmail());
        assertThat(inboxDepartmentHead.length).isOne();
        Message msgDepartmentHead = inboxDepartmentHead[0];
        assertThat(msgDepartmentHead.getSubject()).isEqualTo("Eine zu genehmigende Abwesenheit wurde abgelehnt");
        assertThat(msgDepartmentHead.getContent()).isEqualTo("Hallo Department Head," + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "der von Lieschen Müller am 22.05.2023 gestellte Antrag wurde von Hugo Boss abgelehnt." + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    https://localhost:8080/web/application/1234" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Begründung:" + EMAIL_LINE_BREAK +
            "Geht leider nicht zu dem Zeitraum" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/3/notifications anpassen.");
    }

    @Test
    void ensureCorrectReferMail() throws MessagingException, IOException {

        final Person recipient = new Person("recipient", "Muster", "Max", "mustermann@example.org");
        recipient.setId(1);
        final Person sender = new Person("sender", "Grimes", "Rick", "rick@grimes.com");

        final Application application = createApplication(recipient);
        application.setApplicationDate(LocalDate.of(2022, 5, 19));
        application.setStartDate(LocalDate.of(2022, 5, 20));
        application.setEndDate(LocalDate.of(2022, 5, 29));

        sut.sendReferredToManagementNotification(application, recipient, sender);

        // was email sent?
        final MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(recipient.getEmail());
        assertThat(inbox.length).isOne();

        // check content of user email
        final Message msg = inbox[0];
        assertThat(msg.getSubject()).isEqualTo("Hilfe bei der Entscheidung über eine zu genehmigende Abwesenheit");
        assertThat(new InternetAddress(recipient.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        final String content = (String) msg.getContent();
        assertThat(content).isEqualTo("Hallo Max Muster," + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Rick Grimes bittet dich um Hilfe bei der Bearbeitung eines Antrags von Max Muster." + EMAIL_LINE_BREAK +
            "Bitte kümmere dich um die Bearbeitung dieses Antrags oder halte ggf. nochmals Rücksprache mit Rick Grimes." + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    https://localhost:8080/web/application/1234" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Informationen zur Abwesenheit:" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    Mitarbeiter:         Max Muster" + EMAIL_LINE_BREAK +
            "    Zeitraum:            20.05.2022 bis 29.05.2022, ganztägig" + EMAIL_LINE_BREAK +
            "    Art der Abwesenheit: Erholungsurlaub" + EMAIL_LINE_BREAK +
            "    Grund:               " + EMAIL_LINE_BREAK +
            "    Vertretung:          " + EMAIL_LINE_BREAK +
            "    Anschrift/Telefon:   " + EMAIL_LINE_BREAK +
            "    Erstellungsdatum:    19.05.2022" + EMAIL_LINE_BREAK +
            "    Weitergeleitet von:  Rick Grimes" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen.");
    }

    @Test
    void sendDeclinedCancellationRequestApplicationNotification() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setId(10);
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_CANCELLATION));

        final Person office = personService.create("office", "Marlene", "Muster", "office@example.org", List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION_REQUESTED), List.of(OFFICE));

        final ApplicationComment comment = new ApplicationComment(person, clock);
        comment.setPerson(office);
        comment.setText("Stornierung abgelehnt!");

        final Application application = createApplication(person);
        application.setStatus(ApplicationStatus.ALLOWED);
        application.setStartDate(LocalDate.of(2020, 5, 29));
        application.setEndDate(LocalDate.of(2020, 5, 29));

        final Person relevantPerson = new Person("relevant", "Person", "Relevant", "relevantperson@example.org");
        relevantPerson.setId(2);
        relevantPerson.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION));
        when(mailRecipientService.getRecipientsOfInterest(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION_REQUESTED)).thenReturn(List.of(relevantPerson, office));

        sut.sendDeclinedCancellationRequestApplicationNotification(application, comment);

        // send mail to applicant?
        MimeMessage[] inboxPerson = greenMail.getReceivedMessagesForDomain(person.getEmail());
        assertThat(inboxPerson.length).isOne();

        Message msgPerson = inboxPerson[0];
        assertThat(msgPerson.getSubject()).contains("Stornierungsantrag abgelehnt");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msgPerson.getAllRecipients()[0]);

        final String contentPerson = (String) msgPerson.getContent();
        assertThat(contentPerson).contains("Hallo Lieschen Müller");
        assertThat(contentPerson).contains("dein Stornierungsantrag der genehmigten Abwesenheit");
        assertThat(contentPerson).contains("29.05.2020 bis 29.05.2020 wurde abgelehnt.");
        assertThat(contentPerson).contains("Kommentar von Marlene Muster:");
        assertThat(contentPerson).contains("Stornierung abgelehnt!");
        assertThat(contentPerson).contains("/web/application/1234");

        // send mail to office
        final MimeMessage[] inboxOffice = greenMail.getReceivedMessagesForDomain(office.getEmail());
        assertThat(inboxOffice).hasSize(1);

        final Message msg = inboxOffice[0];
        assertThat(msg.getSubject()).contains("Stornierungsantrag abgelehnt");
        assertThat(new InternetAddress(office.getEmail())).isEqualTo(msg.getAllRecipients()[0]);
        assertThat(msg.getContent()).isEqualTo("Hallo Marlene Muster," + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "der Stornierungsantrag von Lieschen Müller der genehmigten Abwesenheit vom 29.05.2020 bis 29.05.2020 wurde abgelehnt." + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    https://localhost:8080/web/application/1234" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Kommentar von Marlene Muster:" + EMAIL_LINE_BREAK +
            "Stornierung abgelehnt!" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/" + office.getId() + "/notifications anpassen.");

        // was email sent to relevant person
        MimeMessage[] inboxRelevantPerson = greenMail.getReceivedMessagesForDomain(relevantPerson.getEmail());
        assertThat(inboxRelevantPerson.length).isOne();

        Message msgRelevantPerson = inboxRelevantPerson[0];
        assertThat(msgRelevantPerson.getSubject()).isEqualTo("Stornierungsantrag abgelehnt");
        assertThat(new InternetAddress(relevantPerson.getEmail())).isEqualTo(msgRelevantPerson.getAllRecipients()[0]);
        assertThat(msgRelevantPerson.getContent()).isEqualTo("Hallo Relevant Person," + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "der Stornierungsantrag von Lieschen Müller der genehmigten Abwesenheit vom 29.05.2020 bis 29.05.2020 wurde abgelehnt." + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    https://localhost:8080/web/application/1234" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Kommentar von Marlene Muster:" + EMAIL_LINE_BREAK +
            "Stornierung abgelehnt!" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/2/notifications anpassen.");
    }

    @Test
    void ensureApplicantAndOfficeGetsMailAboutCancellationRequest() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setId(1);
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_CANCELLATION));

        final Person office = personService.create("office", "Marlene", "Muster", "office@example.org", List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION_REQUESTED), List.of(OFFICE));

        final ApplicationComment comment = new ApplicationComment(person, clock);
        comment.setText("Bitte stornieren!");

        final Application application = createApplication(person);
        application.setStartDate(LocalDate.of(2020, 5, 29));
        application.setEndDate(LocalDate.of(2020, 5, 29));

        when(mailRecipientService.getRecipientsOfInterest(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION_REQUESTED)).thenReturn(List.of(office));

        sut.sendCancellationRequest(application, comment);

        // send mail to applicant?
        MimeMessage[] inboxPerson = greenMail.getReceivedMessagesForDomain(person.getEmail());
        assertThat(inboxPerson.length).isOne();

        final Message msgPerson = inboxPerson[0];
        assertThat(msgPerson.getSubject()).contains("Stornierung wurde beantragt");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msgPerson.getAllRecipients()[0]);
        assertThat(msgPerson.getContent()).isEqualTo("Hallo Lieschen Müller," + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "dein Antrag zum Stornieren deines bereits genehmigten Antrags vom 29.05.2020 bis 29.05.2020 wurde eingereicht." + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    https://localhost:8080/web/application/1234" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Kommentar von Lieschen Müller:" + EMAIL_LINE_BREAK +
            "Bitte stornieren!" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK + EMAIL_LINE_BREAK +
            "Überblick deiner offenen Stornierungsanträge findest du unter https://localhost:8080/web/application#cancellation-requests" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen.");

        // send mail to all relevant persons?
        final MimeMessage[] inboxOffice = greenMail.getReceivedMessagesForDomain(office.getEmail());
        assertThat(inboxOffice).hasSize(1);

        final Message msgOffice = inboxOffice[0];
        assertThat(msgOffice.getSubject()).isEqualTo("Ein Benutzer beantragt die Stornierung einer genehmigten Abwesenheit");
        assertThat(new InternetAddress(office.getEmail())).isEqualTo(msgOffice.getAllRecipients()[0]);
        assertThat(msgOffice.getContent()).isEqualTo("Hallo Marlene Muster," + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Lieschen Müller möchte die bereits genehmigte Abwesenheit vom 29.05.2020 bis 29.05.2020 stornieren." + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    https://localhost:8080/web/application/1234" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Kommentar von Lieschen Müller:" + EMAIL_LINE_BREAK +
            "Bitte stornieren!" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK + EMAIL_LINE_BREAK +
            "Überblick aller offenen Stornierungsanträge findest du unter https://localhost:8080/web/application#cancellation-requests" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/" + office.getId() + "/notifications anpassen.");
    }

    @Test
    void ensurePersonGetsMailIfApplicationForLeaveHasBeenConvertedToSickNote() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setId(1);
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_CONVERTED));

        final Person office = new Person("office", "Muster", "Marlene", "office@example.org");
        office.setPermissions(List.of(OFFICE));

        final Application application = createApplication(person);
        application.setApplier(office);
        application.setStartDate(LocalDate.of(2023, FEBRUARY, 2));
        application.setEndDate(LocalDate.of(2023, FEBRUARY, 4));

        final Person relevantPerson = new Person("relevantPerson", "Relevant", "Person", "relevantPerson@example.org");
        relevantPerson.setId(2);
        relevantPerson.setPermissions(List.of(BOSS));
        relevantPerson.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CONVERTED));
        when(mailRecipientService.getRecipientsOfInterest(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CONVERTED)).thenReturn(List.of(relevantPerson));

        sut.sendSickNoteConvertedToVacationNotification(application);

        // Was email sent to applicant
        final MimeMessage[] inboxApplicant = greenMail.getReceivedMessagesForDomain(person.getEmail());
        assertThat(inboxApplicant.length).isOne();
        final Message msgApplicant = inboxApplicant[0];
        assertThat(msgApplicant.getSubject()).contains("Deine Krankmeldung wurde in eine Abwesenheit umgewandelt");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msgApplicant.getAllRecipients()[0]);
        assertThat(msgApplicant.getContent()).isEqualTo("Hallo Lieschen Müller," + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Marlene Muster hat deine Krankmeldung vom 02.02.2023 bis 04.02.2023 zu Urlaub umgewandelt." + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    https://localhost:8080/web/application/1234" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen.");

        // Was email sent to management
        final MimeMessage[] inboxManagement = greenMail.getReceivedMessagesForDomain(relevantPerson.getEmail());
        assertThat(inboxManagement.length).isOne();
        final Message msgManagement = inboxManagement[0];
        assertThat(msgManagement.getSubject()).contains("Die Krankmeldung von Lieschen Müller wurde in eine Abwesenheit umgewandelt");
        assertThat(new InternetAddress(relevantPerson.getEmail())).isEqualTo(msgManagement.getAllRecipients()[0]);
        assertThat(msgManagement.getContent()).isEqualTo("Hallo Person Relevant," + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Marlene Muster hat die Krankmeldung von Lieschen Müller vom 02.02.2023 bis 04.02.2023 zu Urlaub umgewandelt." + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    https://localhost:8080/web/application/1234" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/2/notifications anpassen.");
    }

    @Test
    void ensureNotificationAboutConfirmationAllowedDirectlySent() throws Exception {

        final Person person = new Person("user", "Mueller", "Lieschen", "lieschen@example.org");
        person.setId(1);
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_ALLOWED));

        final Application application = createApplication(person);
        application.setApplicationDate(LocalDate.of(2021, APRIL, 12));
        application.setStartDate(LocalDate.of(2021, APRIL, 16));
        application.setEndDate(LocalDate.of(2021, APRIL, 16));

        final ApplicationComment comment = new ApplicationComment(person, clock);
        comment.setText("OK, Urlaub kann genommen werden");

        final Person colleague = new Person("colleague", "colleague", "colleague", "colleague@example.org");
        colleague.setId(2);
        colleague.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_COLLEAGUES_ALLOWED));
        when(mailRecipientService.getColleagues(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_COLLEAGUES_ALLOWED)).thenReturn(List.of(colleague));

        sut.sendConfirmationAllowedDirectly(application, comment);

        // email sent to applicant
        final MimeMessage[] inboxUser = greenMail.getReceivedMessagesForDomain(person.getEmail());
        assertThat(inboxUser.length).isOne();

        final Message contentUser = inboxUser[0];
        assertThat(contentUser.getSubject()).isEqualTo("Deine Abwesenheit wurde erstellt");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(contentUser.getAllRecipients()[0]);
        assertThat(contentUser.getContent()).isEqualTo("Hallo Lieschen Mueller," + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "dein Abwesenheitsantrag wurde erfolgreich erstellt." + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    https://localhost:8080/web/application/1234" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Kommentar von Lieschen Mueller:" + EMAIL_LINE_BREAK +
            "OK, Urlaub kann genommen werden" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Informationen zur Abwesenheit:" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    Zeitraum:            16.04.2021 bis 16.04.2021, ganztägig" + EMAIL_LINE_BREAK +
            "    Art der Abwesenheit: Erholungsurlaub" + EMAIL_LINE_BREAK +
            "    Grund:               " + EMAIL_LINE_BREAK +
            "    Vertretung:          " + EMAIL_LINE_BREAK +
            "    Anschrift/Telefon:   " + EMAIL_LINE_BREAK +
            "    Erstellungsdatum:    12.04.2021" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen.");

        // email sent to colleague
        final MimeMessage[] inboxColleague = greenMail.getReceivedMessagesForDomain(colleague.getEmail());
        assertThat(inboxColleague.length).isOne();

        final Message contentColleague = inboxColleague[0];
        assertThat(contentColleague.getSubject()).isEqualTo("Neue Abwesenheit von Lieschen Mueller");
        assertThat(new InternetAddress(colleague.getEmail())).isEqualTo(contentColleague.getAllRecipients()[0]);
        assertThat(contentColleague.getContent()).isEqualTo("Hallo colleague colleague," + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "eine Abwesenheit von Lieschen Mueller wurde erstellt:" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    Zeitraum: 16.04.2021 bis 16.04.2021, ganztägig" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Link zur Abwesenheitsübersicht: https://localhost:8080/web/absences" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/2/notifications anpassen.");
    }

    @Test
    void ensureConfirmationAllowedDirectlyByOfficeSent() throws Exception {

        final Person person = new Person("user", "Mueller", "Lieschen", "lieschen@example.org");
        person.setId(1);
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_ALLOWED));

        final Person office = personService.create("office", "Marlene", "Muster", "office@example.org", List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION), List.of(OFFICE));

        final Application application = createApplication(person);
        application.setApplier(office);
        application.setApplicationDate(LocalDate.of(2021, APRIL, 12));
        application.setStartDate(LocalDate.of(2021, APRIL, 16));
        application.setEndDate(LocalDate.of(2021, APRIL, 16));

        final ApplicationComment comment = new ApplicationComment(person, clock);
        comment.setText("OK, Urlaub kann genommen werden");

        final Person colleague = new Person("colleague", "colleague", "colleague", "colleague@example.org");
        colleague.setId(2);
        colleague.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_COLLEAGUES_ALLOWED));
        when(mailRecipientService.getColleagues(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_COLLEAGUES_ALLOWED)).thenReturn(List.of(colleague));

        sut.sendConfirmationAllowedDirectlyByManagement(application, comment);

        // email sent?
        final MimeMessage[] inboxUser = greenMail.getReceivedMessagesForDomain(person.getEmail());
        assertThat(inboxUser.length).isOne();

        // check content of user email
        final Message contentUser = inboxUser[0];
        assertThat(contentUser.getSubject()).isEqualTo("Eine Abwesenheit wurde für dich erstellt");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(contentUser.getAllRecipients()[0]);
        assertThat(contentUser.getContent()).isEqualTo("Hallo Lieschen Mueller," + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Marlene Muster hat eine Abwesenheit für dich erstellt." + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    https://localhost:8080/web/application/1234" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Kommentar von Lieschen Mueller:" + EMAIL_LINE_BREAK +
            "OK, Urlaub kann genommen werden" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Informationen zur Abwesenheit:" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    Mitarbeiter:         Lieschen Mueller" + EMAIL_LINE_BREAK +
            "    Zeitraum:            16.04.2021 bis 16.04.2021, ganztägig" + EMAIL_LINE_BREAK +
            "    Art der Abwesenheit: Erholungsurlaub" + EMAIL_LINE_BREAK +
            "    Grund:               " + EMAIL_LINE_BREAK +
            "    Vertretung:          " + EMAIL_LINE_BREAK +
            "    Anschrift/Telefon:   " + EMAIL_LINE_BREAK +
            "    Erstellungsdatum:    12.04.2021" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen.");

        // email sent to colleague
        final MimeMessage[] inboxColleague = greenMail.getReceivedMessagesForDomain(colleague.getEmail());
        assertThat(inboxColleague.length).isOne();

        final Message contentColleague = inboxColleague[0];
        assertThat(contentColleague.getSubject()).isEqualTo("Neue Abwesenheit von Lieschen Mueller");
        assertThat(new InternetAddress(colleague.getEmail())).isEqualTo(contentColleague.getAllRecipients()[0]);
        assertThat(contentColleague.getContent()).isEqualTo("Hallo colleague colleague," + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "eine Abwesenheit von Lieschen Mueller wurde erstellt:" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    Zeitraum: 16.04.2021 bis 16.04.2021, ganztägig" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Link zur Abwesenheitsübersicht: https://localhost:8080/web/absences" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/2/notifications anpassen.");
    }

    @Test
    void ensureNewDirectlyAllowedApplicationNotificationSent() throws Exception {

        final Person person = new Person("user", "Mueller", "Lieschen", "lieschen@example.org");

        final Application application = createApplication(person);
        application.setApplicationDate(LocalDate.of(2021, APRIL, 12));
        application.setStartDate(LocalDate.of(2021, APRIL, 16));
        application.setEndDate(LocalDate.of(2021, APRIL, 16));

        final Person relevantPerson = new Person("relevant", "Person", "Relevant", "relevantperson@example.org");
        relevantPerson.setId(1);
        relevantPerson.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALLOWED));
        when(mailRecipientService.getRecipientsOfInterest(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALLOWED)).thenReturn(List.of(relevantPerson));

        final ApplicationComment comment = new ApplicationComment(person, clock);
        comment.setText("OK, Urlaub kann genommen werden");

        sut.sendDirectlyAllowedNotificationToManagement(application, comment);

        // email sent?
        final MimeMessage[] inboxUser = greenMail.getReceivedMessagesForDomain(relevantPerson.getEmail());
        assertThat(inboxUser.length).isOne();

        // check content of relevant person email
        final Message contentRelevantPerson = inboxUser[0];
        assertThat(contentRelevantPerson.getSubject()).isEqualTo("Neue Abwesenheit wurde von Lieschen Mueller erstellt");
        assertThat(new InternetAddress(relevantPerson.getEmail())).isEqualTo(contentRelevantPerson.getAllRecipients()[0]);
        assertThat(contentRelevantPerson.getContent()).isEqualTo("Hallo Relevant Person," + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "es wurde eine neue Abwesenheit erstellt (diese muss nicht genehmigt werden)." + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    https://localhost:8080/web/application/1234" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Kommentar von Lieschen Mueller:" + EMAIL_LINE_BREAK +
            "OK, Urlaub kann genommen werden" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Informationen zur Abwesenheit:" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    Mitarbeiter:         Lieschen Mueller" + EMAIL_LINE_BREAK +
            "    Zeitraum:            16.04.2021 bis 16.04.2021, ganztägig" + EMAIL_LINE_BREAK +
            "    Art der Abwesenheit: Erholungsurlaub" + EMAIL_LINE_BREAK +
            "    Grund:               " + EMAIL_LINE_BREAK +
            "    Vertretung:          " + EMAIL_LINE_BREAK +
            "    Anschrift/Telefon:   " + EMAIL_LINE_BREAK +
            "    Erstellungsdatum:    12.04.2021" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen.");
    }

    @Test
    void ensureNotifyHolidayReplacementAboutDirectlyAllowedApplicationSent() throws Exception {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        final Application application = createApplication(person);
        application.setStartDate(LocalDate.of(2020, 12, 18));
        application.setEndDate(LocalDate.of(2020, 12, 18));

        final Person holidayReplacement = new Person("replacement", "Teria", "Mar", "replacement@example.org");
        holidayReplacement.setId(1);
        holidayReplacement.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT));
        final HolidayReplacementEntity replacementEntity = new HolidayReplacementEntity();
        replacementEntity.setPerson(holidayReplacement);
        replacementEntity.setNote("Eine Nachricht an die Vertretung");

        application.setHolidayReplacements(List.of(replacementEntity));

        sut.notifyHolidayReplacementAboutDirectlyAllowedApplication(replacementEntity, application);

        // was email sent?
        final MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(holidayReplacement.getEmail());
        assertThat(inbox.length).isOne();

        final MimeMessage msg = inbox[0];
        assertThat(msg.getSubject()).contains("Eine Vertretung für Lieschen Müller wurde eingetragen");
        assertThat(new InternetAddress(holidayReplacement.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        assertThat(readPlainContent(msg)).isEqualTo("Hallo Mar Teria," + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "eine Abwesenheit von Lieschen Müller wurde erstellt und" + EMAIL_LINE_BREAK +
            "du wurdest für den Zeitraum vom 18.12.2020 bis 18.12.2020, ganztägig als Vertretung eingetragen." + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Notiz von Lieschen Müller an dich:" + EMAIL_LINE_BREAK +
            "Eine Nachricht an die Vertretung" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Einen Überblick deiner aktuellen und zukünftigen Vertretungen findest du unter https://localhost:8080/web/application/replacement" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen." + EMAIL_LINE_BREAK);

        final List<DataSource> attachments = getAttachments(msg);
        assertThat(attachments.get(0).getName()).contains("calendar.ics");
    }

    @Test
    void ensureCorrectHolidayReplacementApplyMailIsSent() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        final Application application = createApplication(person);
        application.setStartDate(LocalDate.of(2020, 12, 18));
        application.setEndDate(LocalDate.of(2020, 12, 18));

        final Person holidayReplacement = new Person("replacement", "Teria", "Mar", "replacement@example.org");
        holidayReplacement.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT));
        final HolidayReplacementEntity replacementEntity = new HolidayReplacementEntity();
        replacementEntity.setPerson(holidayReplacement);
        replacementEntity.setNote("Eine Nachricht an die Vertretung");

        application.setHolidayReplacements(List.of(replacementEntity));

        sut.notifyHolidayReplacementForApply(replacementEntity, application);

        // was email sent?
        MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(holidayReplacement.getEmail());
        assertThat(inbox.length).isOne();

        Message msg = inbox[0];
        assertThat(msg.getSubject()).contains("Deine vorläufig geplante Vertretung für Lieschen Müller");
        assertThat(new InternetAddress(holidayReplacement.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        String content = (String) msg.getContent();
        assertThat(content).contains("Hallo Mar Teria");
        assertThat(content).contains("Lieschen Müller hat dich bei einer Abwesenheit als Vertretung vorgesehen.");
        assertThat(content).contains("Es handelt sich um den Zeitraum von 18.12.2020 bis 18.12.2020, ganztägig.");
        assertThat(content).contains("Einen Überblick deiner aktuellen und zukünftigen Vertretungen findest du unter");
        assertThat(content).contains("/web/application/replacement");
        assertThat(content).contains("Eine Nachricht an die Vertretung");
    }

    @Test
    void ensureCorrectHolidayReplacementAllowMailIsSent() throws Exception {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        final Application application = createApplication(person);
        application.setStartDate(LocalDate.of(2020, 5, 29));
        application.setEndDate(LocalDate.of(2020, 5, 29));

        final Person holidayReplacement = new Person("replacement", "Teria", "Mar", "replacement@example.org");
        holidayReplacement.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT));
        final HolidayReplacementEntity replacementEntity = new HolidayReplacementEntity();
        replacementEntity.setPerson(holidayReplacement);
        replacementEntity.setNote("Eine Nachricht an die Vertretung");

        application.setHolidayReplacements(List.of(replacementEntity));

        sut.notifyHolidayReplacementAllow(replacementEntity, application);

        // was email sent?
        MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(holidayReplacement.getEmail());
        assertThat(inbox.length).isOne();

        MimeMessage msg = inbox[0];
        assertThat(msg.getSubject()).contains("Deine Vertretung für Lieschen Müller wurde eingeplant");
        assertThat(new InternetAddress(holidayReplacement.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        String content = readPlainContent(msg);
        assertThat(content).contains("Hallo Mar Teria");
        assertThat(content).contains("die Abwesenheit von Lieschen Müller wurde genehmigt.");
        assertThat(content).contains("Du wurdest damit für den Zeitraum vom 29.05.2020 bis 29.05.2020, ganztägig als Vertretung eingetragen.");
        assertThat(content).contains("Einen Überblick deiner aktuellen und zukünftigen Vertretungen findest du unter");
        assertThat(content).contains("/web/application/replacement");
        assertThat(content).contains("Eine Nachricht an die Vertretung");

        final List<DataSource> attachments = getAttachments(msg);
        assertThat(attachments.get(0).getName()).contains("calendar.ics");
    }

    @Test
    void ensureCorrectHolidayReplacementCancellationMailIsSent() throws Exception {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        final Application application = createApplication(person);
        application.setStartDate(LocalDate.of(2020, 12, 18));
        application.setEndDate(LocalDate.of(2020, 12, 18));

        final Person holidayReplacement = new Person("replacement", "Teria", "Mar", "replacement@example.org");
        holidayReplacement.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT));
        final HolidayReplacementEntity replacementEntity = new HolidayReplacementEntity();
        replacementEntity.setPerson(holidayReplacement);

        application.setHolidayReplacements(List.of(replacementEntity));

        sut.notifyHolidayReplacementAboutCancellation(replacementEntity, application);

        // was email sent?
        MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(holidayReplacement.getEmail());
        assertThat(inbox.length).isOne();

        MimeMessage msg = inbox[0];
        assertThat(msg.getSubject()).contains("Deine vorläufig geplante Vertretung für Lieschen Müller wurde zurückgezogen");
        assertThat(new InternetAddress(holidayReplacement.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        String content = readPlainContent(msg);
        assertThat(content).contains("Hallo Mar Teria");
        assertThat(content).contains("du bist für die Abwesenheit von Lieschen Müller");
        assertThat(content).contains("im Zeitraum von 18.12.2020 bis 18.12.2020, ganztägig,");
        assertThat(content).contains("nicht mehr als Vertretung vorgesehen.");
        assertThat(content).contains("Einen Überblick deiner aktuellen und zukünftigen Vertretungen findest du unter");
        assertThat(content).contains("/web/application/replacement");

        final List<DataSource> attachments = getAttachments(msg);
        assertThat(attachments.get(0).getName()).contains("calendar.ics");
    }

    @Test
    void ensureCorrectHolidayReplacementEditMailIsSent() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        final Application application = createApplication(person);
        application.setStartDate(LocalDate.of(2020, 12, 18));
        application.setEndDate(LocalDate.of(2020, 12, 18));

        final Person holidayReplacement = new Person("replacement", "Teria", "Mar", "replacement@example.org");
        holidayReplacement.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT));
        final HolidayReplacementEntity replacementEntity = new HolidayReplacementEntity();
        replacementEntity.setPerson(holidayReplacement);
        replacementEntity.setNote("Eine Nachricht an die Vertretung");

        application.setHolidayReplacements(List.of(replacementEntity));

        sut.notifyHolidayReplacementAboutEdit(replacementEntity, application);

        // was email sent?
        MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(holidayReplacement.getEmail());
        assertThat(inbox.length).isOne();

        Message msg = inbox[0];
        assertThat(msg.getSubject()).contains("Deine vorläufig geplante Vertretung für Lieschen Müller wurde bearbeitet");
        assertThat(new InternetAddress(holidayReplacement.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        String content = (String) msg.getContent();
        assertThat(content).contains("Hallo Mar Teria");
        assertThat(content).contains("der Zeitraum für die Abwesenheit von Lieschen Müller bei dem du als Vertretung vorgesehen bist, hat sich geändert.");
        assertThat(content).contains("Der neue Zeitraum ist von 18.12.2020 bis 18.12.2020, ganztägig.");
        assertThat(content).contains("Einen Überblick deiner aktuellen und zukünftigen Vertretungen findest du unter");
        assertThat(content).contains("/web/application/replacement");
        assertThat(content).contains("Eine Nachricht an die Vertretung");
    }

    @Test
    void ensureToSendAppliedNotificationWhereFromIsNotNull() throws MessagingException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_APPLIED));

        final Application application = createApplication(person);
        final ApplicationComment comment = new ApplicationComment(person, clock);
        comment.setText("Hätte gerne Urlaub");

        sut.sendAppliedNotification(application, comment);

        MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(person.getEmail());
        assertThat(inbox.length).isOne();

        Message msg = inbox[0];
        Address[] from = msg.getFrom();
        assertThat(from).isNotNull();
        assertThat(from.length).isOne();
        assertThat(from[0]).hasToString(String.format("%s <%s>", mailProperties.getSenderDisplayName(), mailProperties.getSender()));
    }

    @Test
    void ensureAfterApplyingForLeaveAConfirmationNotificationIsSentToPerson() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_APPLIED));

        final Application application = createApplication(person);

        final ApplicationComment comment = new ApplicationComment(person, clock);
        comment.setText("Hätte gerne Urlaub");

        sut.sendAppliedNotification(application, comment);

        // was email sent?
        MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(person.getEmail());
        assertThat(inbox.length).isOne();

        Message msg = inbox[0];
        assertThat(msg.getSubject()).contains("Antragsstellung");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        String content = (String) msg.getContent();
        assertThat(content).contains("Hallo Lieschen Müller");
        assertThat(content).contains("deine Abwesenheit wurde erfolgreich eingereicht.");
        assertThat(content).contains(comment.getText());
        assertThat(content).contains(comment.getPerson().getNiceName());
        assertThat(content).contains("/web/application/1234");
    }

    @Test
    void ensureAfterApplyingForLeaveAConfirmationNotificationIsSentToPersonWithOneReplacement() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setId(1);
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_APPLIED));
        final Person holidayReplacement = new Person("pennyworth", "Pennyworth", "Alfred", "pennyworth@example.org");

        final HolidayReplacementEntity holidayReplacementEntity = new HolidayReplacementEntity();
        holidayReplacementEntity.setPerson(holidayReplacement);

        final Application application = createApplication(person);
        application.setApplicationDate(LocalDate.of(2021, APRIL, 12));
        application.setStartDate(LocalDate.of(2021, APRIL, 16));
        application.setEndDate(LocalDate.of(2021, APRIL, 16));
        application.setHolidayReplacements(List.of(holidayReplacementEntity));

        final ApplicationComment comment = new ApplicationComment(person, clock);
        comment.setText("Hätte gerne Urlaub");

        sut.sendAppliedNotification(application, comment);

        // was email sent?
        MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(person.getEmail());
        assertThat(inbox.length).isOne();

        Message msg = inbox[0];
        assertThat(msg.getSubject()).contains("Antragsstellung");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        assertThat(msg.getContent()).isEqualTo("Hallo Lieschen Müller," + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "deine Abwesenheit wurde erfolgreich eingereicht." + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    https://localhost:8080/web/application/1234" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Kommentar von Lieschen Müller:" + EMAIL_LINE_BREAK +
            "Hätte gerne Urlaub" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Informationen zur Abwesenheit:" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    Zeitraum:            16.04.2021 bis 16.04.2021, ganztägig" + EMAIL_LINE_BREAK +
            "    Art der Abwesenheit: Erholungsurlaub" + EMAIL_LINE_BREAK +
            "    Grund:               " + EMAIL_LINE_BREAK +
            "    Vertretung:          Alfred Pennyworth" + EMAIL_LINE_BREAK +
            "    Anschrift/Telefon:   " + EMAIL_LINE_BREAK +
            "    Erstellungsdatum:    12.04.2021" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen.");
    }

    @Test
    void ensureAfterApplyingForLeaveAConfirmationNotificationIsSentToPersonWithMultipleReplacements() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setId(1);
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_APPLIED));

        final Person holidayReplacementOne = new Person("pennyworth", "Pennyworth", "Alfred", "pennyworth@example.org");
        final Person holidayReplacementTwo = new Person("rob", "", "Robin", "robin@example.org");

        final HolidayReplacementEntity holidayReplacementOneEntity = new HolidayReplacementEntity();
        holidayReplacementOneEntity.setPerson(holidayReplacementOne);

        final HolidayReplacementEntity holidayReplacementTwoEntity = new HolidayReplacementEntity();
        holidayReplacementTwoEntity.setPerson(holidayReplacementTwo);

        final Application application = createApplication(person);
        application.setApplicationDate(LocalDate.of(2021, APRIL, 12));
        application.setStartDate(LocalDate.of(2021, APRIL, 16));
        application.setEndDate(LocalDate.of(2021, APRIL, 16));
        application.setHolidayReplacements(List.of(holidayReplacementOneEntity, holidayReplacementTwoEntity));

        final ApplicationComment comment = new ApplicationComment(person, clock);
        comment.setText("Hätte gerne Urlaub");

        sut.sendAppliedNotification(application, comment);

        // was email sent?
        MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(person.getEmail());
        assertThat(inbox.length).isOne();

        Message msg = inbox[0];
        assertThat(msg.getSubject()).contains("Antragsstellung");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        assertThat(msg.getContent()).isEqualTo("Hallo Lieschen Müller," + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "deine Abwesenheit wurde erfolgreich eingereicht." + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    https://localhost:8080/web/application/1234" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Kommentar von Lieschen Müller:" + EMAIL_LINE_BREAK +
            "Hätte gerne Urlaub" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Informationen zur Abwesenheit:" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    Zeitraum:            16.04.2021 bis 16.04.2021, ganztägig" + EMAIL_LINE_BREAK +
            "    Art der Abwesenheit: Erholungsurlaub" + EMAIL_LINE_BREAK +
            "    Grund:               " + EMAIL_LINE_BREAK +
            "    Vertretung:          Alfred Pennyworth, Robin" + EMAIL_LINE_BREAK +
            "    Anschrift/Telefon:   " + EMAIL_LINE_BREAK +
            "    Erstellungsdatum:    12.04.2021" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen.");
    }

    @Test
    void ensurePersonGetsANotificationIfAnOfficeMemberAppliedForLeaveForThisPerson() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_APPLIED));

        final Application application = createApplication(person);

        final ApplicationComment comment = new ApplicationComment(person, clock);
        comment.setText("Habe das mal für dich beantragt");

        final Person office = new Person("office", "Muster", "Marlene", "office@example.org");
        office.setPermissions(List.of(OFFICE));

        application.setApplier(office);
        sut.sendAppliedByManagementNotification(application, comment);

        // was email sent?
        MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(person.getEmail());
        assertThat(inbox.length).isOne();

        Message msg = inbox[0];
        assertThat(msg.getSubject()).isEqualTo("Für dich wurde eine zu genehmigende Abwesenheit eingereicht");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        String content = (String) msg.getContent();
        assertThat(content).contains("Hallo Lieschen Müller");
        assertThat(content).contains("Marlene Muster hat eine Abwesenheit für dich gestellt.");
        assertThat(content).contains(comment.getText());
        assertThat(content).contains(comment.getPerson().getNiceName());
        assertThat(content).contains("/web/application/1234");
    }

    @Test
    void ensurePersonGetsANotificationIfAnOfficeMemberAppliedForLeaveForThisPersonWithOneReplacement() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setId(1);
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_APPLIED));

        final Person holidayReplacement = new Person("pennyworth", "Pennyworth", "Alfred", "pennyworth@example.org");
        final HolidayReplacementEntity holidayReplacementEntity = new HolidayReplacementEntity();
        holidayReplacementEntity.setPerson(holidayReplacement);

        final Application application = createApplication(person);
        application.setApplicationDate(LocalDate.of(2021, APRIL, 12));
        application.setStartDate(LocalDate.of(2021, APRIL, 16));
        application.setEndDate(LocalDate.of(2021, APRIL, 16));
        application.setHolidayReplacements(List.of(holidayReplacementEntity));

        final ApplicationComment comment = new ApplicationComment(person, clock);
        comment.setText("Habe das mal für dich beantragt");

        final Person office = new Person("office", "Muster", "Marlene", "office@example.org");
        office.setPermissions(List.of(OFFICE));

        application.setApplier(office);
        sut.sendAppliedByManagementNotification(application, comment);

        // was email sent?
        MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(person.getEmail());
        assertThat(inbox.length).isOne();

        Message msg = inbox[0];
        assertThat(msg.getSubject()).isEqualTo("Für dich wurde eine zu genehmigende Abwesenheit eingereicht");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        assertThat(msg.getContent()).isEqualTo("Hallo Lieschen Müller," + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Marlene Muster hat eine Abwesenheit für dich gestellt." + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    https://localhost:8080/web/application/1234" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Kommentar von Lieschen Müller:" + EMAIL_LINE_BREAK +
            "Habe das mal für dich beantragt" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Informationen zur Abwesenheit:" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    Zeitraum:            16.04.2021 bis 16.04.2021, ganztägig" + EMAIL_LINE_BREAK +
            "    Art der Abwesenheit: Erholungsurlaub" + EMAIL_LINE_BREAK +
            "    Grund:               " + EMAIL_LINE_BREAK +
            "    Vertretung:          Alfred Pennyworth" + EMAIL_LINE_BREAK +
            "    Anschrift/Telefon:   " + EMAIL_LINE_BREAK +
            "    Erstellungsdatum:    12.04.2021" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen.");
    }

    @Test
    void ensurePersonGetsANotificationIfAnOfficeMemberAppliedForLeaveForThisPersonWithMultipleReplacements() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setId(1);
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_APPLIED));
        final Person holidayReplacementOne = new Person("pennyworth", "Pennyworth", "Alfred", "pennyworth@example.org");
        final Person holidayReplacementTwo = new Person("rob", "", "Robin", "robin@example.org");

        final HolidayReplacementEntity holidayReplacementOneEntity = new HolidayReplacementEntity();
        holidayReplacementOneEntity.setPerson(holidayReplacementOne);

        final HolidayReplacementEntity holidayReplacementTwoEntity = new HolidayReplacementEntity();
        holidayReplacementTwoEntity.setPerson(holidayReplacementTwo);

        final Application application = createApplication(person);
        application.setApplicationDate(LocalDate.of(2021, APRIL, 12));
        application.setStartDate(LocalDate.of(2021, APRIL, 16));
        application.setEndDate(LocalDate.of(2021, APRIL, 16));
        application.setHolidayReplacements(List.of(holidayReplacementOneEntity, holidayReplacementTwoEntity));

        final ApplicationComment comment = new ApplicationComment(person, clock);
        comment.setText("Habe das mal für dich beantragt");

        final Person office = new Person("office", "Muster", "Marlene", "office@example.org");
        office.setPermissions(List.of(OFFICE));

        application.setApplier(office);
        sut.sendAppliedByManagementNotification(application, comment);

        // was email sent?
        MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(person.getEmail());
        assertThat(inbox.length).isOne();

        Message msg = inbox[0];
        assertThat(msg.getSubject()).isEqualTo("Für dich wurde eine zu genehmigende Abwesenheit eingereicht");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        assertThat(msg.getContent()).isEqualTo("Hallo Lieschen Müller," + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Marlene Muster hat eine Abwesenheit für dich gestellt." + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    https://localhost:8080/web/application/1234" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Kommentar von Lieschen Müller:" + EMAIL_LINE_BREAK +
            "Habe das mal für dich beantragt" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Informationen zur Abwesenheit:" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    Zeitraum:            16.04.2021 bis 16.04.2021, ganztägig" + EMAIL_LINE_BREAK +
            "    Art der Abwesenheit: Erholungsurlaub" + EMAIL_LINE_BREAK +
            "    Grund:               " + EMAIL_LINE_BREAK +
            "    Vertretung:          Alfred Pennyworth, Robin" + EMAIL_LINE_BREAK +
            "    Anschrift/Telefon:   " + EMAIL_LINE_BREAK +
            "    Erstellungsdatum:    12.04.2021" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen.");
    }

    @Test
    void ensurePersonAndRelevantPersonsGetsANotificationIfPersonCancelledOneOfHisApplications() throws MessagingException,
        IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_REVOKED));

        final Application application = createApplication(person);
        application.setCanceller(person);

        final ApplicationComment comment = new ApplicationComment(person, clock);
        comment.setText("Wrong date - revoked");

        final Person relevantPerson = new Person("relevant", "Person", "Relevant", "relevantperson@example.org");
        relevantPerson.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_REVOKED));
        when(mailRecipientService.getRecipientsOfInterest(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_REVOKED)).thenReturn(List.of(relevantPerson));

        sut.sendRevokedNotifications(application, comment);

        // was email sent to applicant
        MimeMessage[] inboxApplicant = greenMail.getReceivedMessagesForDomain(person.getEmail());
        assertThat(inboxApplicant.length).isOne();

        Message msg = inboxApplicant[0];
        assertThat(msg.getSubject()).isEqualTo("Deine Abwesenheit wurde erfolgreich storniert");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        String content = (String) msg.getContent();
        assertThat(content).contains("Hallo Lieschen Müller");
        assertThat(content).contains("nicht genehmigter Antrag wurde von dir erfolgreich");
        assertThat(content).contains(comment.getText());
        assertThat(content).contains(comment.getPerson().getNiceName());
        assertThat(content).contains("/web/application/1234");

        // was email sent to relevant person
        final MimeMessage[] inboxRelevantPerson = greenMail.getReceivedMessagesForDomain(relevantPerson.getEmail());
        assertThat(inboxRelevantPerson.length).isOne();

        final Message msgRelevantPerson = inboxRelevantPerson[0];
        assertThat(msgRelevantPerson.getSubject()).isEqualTo("Eine nicht genehmigte Abwesenheit wurde erfolgreich storniert");
        assertThat(new InternetAddress(relevantPerson.getEmail())).isEqualTo(msgRelevantPerson.getAllRecipients()[0]);

        final String contentRelevantPerson = (String) msgRelevantPerson.getContent();
        assertThat(contentRelevantPerson).contains("Hallo Relevant Person");
        assertThat(contentRelevantPerson).contains("nicht genehmigte Antrag von Lieschen Müller wurde storniert.");
        assertThat(contentRelevantPerson).contains(comment.getText());
        assertThat(contentRelevantPerson).contains(comment.getPerson().getNiceName());
        assertThat(contentRelevantPerson).contains("/web/application/1234");
    }

    @Test
    void ensurePersonAndRelevantPersonsGetsANotificationIfNotApplicantCancelledThisApplication() throws MessagingException,
        IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_REVOKED));
        final Application application = createApplication(person);

        final Person office = new Person("office", "Person", "Office", "office@example.org");
        application.setCanceller(office);

        final ApplicationComment comment = new ApplicationComment(office, clock);
        comment.setText("Wrong information - revoked");

        final Person relevantPerson = new Person("relevant", "Person", "Relevant", "relevantperson@example.org");
        relevantPerson.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_REVOKED));
        when(mailRecipientService.getRecipientsOfInterest(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_REVOKED)).thenReturn(List.of(relevantPerson));

        sut.sendRevokedNotifications(application, comment);

        // was email sent to applicant
        MimeMessage[] inboxApplicant = greenMail.getReceivedMessagesForDomain(person.getEmail());
        assertThat(inboxApplicant.length).isOne();

        Message msg = inboxApplicant[0];
        assertThat(msg.getSubject()).isEqualTo("Deine Abwesenheit wurde storniert");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        String content = (String) msg.getContent();
        assertThat(content).contains("Hallo Lieschen Müller");
        assertThat(content).contains("gestellter, nicht genehmigter Antrag wurde von Office Person storniert.");
        assertThat(content).contains(comment.getText());
        assertThat(content).contains(comment.getPerson().getNiceName());
        assertThat(content).contains("/web/application/1234");

        // was email sent to relevant person
        final MimeMessage[] inboxRelevantPerson = greenMail.getReceivedMessagesForDomain(relevantPerson.getEmail());
        assertThat(inboxRelevantPerson.length).isOne();

        final Message msgRelevantPerson = inboxRelevantPerson[0];
        assertThat(msgRelevantPerson.getSubject()).isEqualTo("Eine nicht genehmigte Abwesenheit wurde erfolgreich storniert");
        assertThat(new InternetAddress(relevantPerson.getEmail())).isEqualTo(msgRelevantPerson.getAllRecipients()[0]);

        final String contentRelevantPerson = (String) msgRelevantPerson.getContent();
        assertThat(contentRelevantPerson).contains("Hallo Relevant Person");
        assertThat(contentRelevantPerson).contains("nicht genehmigte Antrag von Lieschen Müller wurde von Office Person storniert.");
        assertThat(contentRelevantPerson).contains(comment.getText());
        assertThat(contentRelevantPerson).contains(comment.getPerson().getNiceName());
        assertThat(contentRelevantPerson).contains("/web/application/1234");
    }

    @Test
    void ensuresDirectCancelInformsRecipientOfInterest() throws Exception {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");

        final Application application = createApplication(person);
        application.setApplicationDate(LocalDate.of(2021, APRIL, 16));
        application.setStartDate(LocalDate.of(2021, APRIL, 16));
        application.setEndDate(LocalDate.of(2021, APRIL, 16));
        application.setDayLength(FULL);
        application.setCanceller(person);

        final ApplicationComment comment = new ApplicationComment(person, clock);
        comment.setText("Cancelled");

        final Person recipientOfInterest = new Person("relevant", "Person", "Relevant", "relevantperson@example.org");
        recipientOfInterest.setId(1);
        recipientOfInterest.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION));
        when(mailRecipientService.getRecipientsOfInterest(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION)).thenReturn(List.of(recipientOfInterest));

        sut.sendCancelledDirectlyToManagement(application, comment);

        // was email sent to applicant?
        final MimeMessage[] inboxRecipientOfInterest = greenMail.getReceivedMessagesForDomain(recipientOfInterest.getEmail());
        assertThat(inboxRecipientOfInterest.length).isOne();

        final Message msg = inboxRecipientOfInterest[0];
        assertThat(msg.getSubject()).isEqualTo("Eine Abwesenheit von Lieschen Müller wurde storniert");
        assertThat(new InternetAddress(recipientOfInterest.getEmail())).isEqualTo(msg.getAllRecipients()[0]);
        assertThat(msg.getContent()).isEqualTo("Hallo Relevant Person," + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "die Abwesenheit von Lieschen Müller vom 16.04.2021 bis zum 16.04.2021 wurde von Lieschen Müller storniert." + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    https://localhost:8080/web/application/1234" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Kommentar von Lieschen Müller:" + EMAIL_LINE_BREAK +
            "Cancelled" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Informationen zur Abwesenheit:" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    Zeitraum:            16.04.2021 bis 16.04.2021, ganztägig" + EMAIL_LINE_BREAK +
            "    Art der Abwesenheit: Erholungsurlaub" + EMAIL_LINE_BREAK +
            "    Grund:               " + EMAIL_LINE_BREAK +
            "    Vertretung:          " + EMAIL_LINE_BREAK +
            "    Anschrift/Telefon:   " + EMAIL_LINE_BREAK +
            "    Erstellungsdatum:    16.04.2021" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen.");
    }

    @Test
    void ensureApplicantReceivesNotificationsIfApplicantCancel() throws Exception {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setId(1);
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_CANCELLATION));

        final Application application = createApplication(person);
        application.setApplicationDate(LocalDate.of(2021, APRIL, 16));
        application.setStartDate(LocalDate.of(2021, APRIL, 16));
        application.setEndDate(LocalDate.of(2021, APRIL, 16));
        application.setDayLength(FULL);

        final ApplicationComment comment = new ApplicationComment(person, clock);
        comment.setText("Wrong information - cancelled");

        final Person colleague = new Person("colleague", "colleague", "colleague", "colleague@example.org");
        colleague.setId(2);
        colleague.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_COLLEAGUES_CANCELLATION));
        when(mailRecipientService.getColleagues(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_COLLEAGUES_CANCELLATION)).thenReturn(List.of(colleague));

        sut.sendCancelledDirectlyConfirmationByApplicant(application, comment);

        // was email sent to applicant
        final MimeMessage[] inboxApplicant = greenMail.getReceivedMessagesForDomain(person.getEmail());
        assertThat(inboxApplicant.length).isOne();

        final MimeMessage msg = inboxApplicant[0];
        assertThat(msg.getSubject()).isEqualTo("Deine Abwesenheit wurde storniert");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);
        assertThat(readPlainContent(msg)).isEqualTo("Hallo Lieschen Müller," + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "deine Abwesenheit vom 16.04.2021 bis zum 16.04.2021 wurde erfolgreich storniert." + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    https://localhost:8080/web/application/1234" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Kommentar von Lieschen Müller:" + EMAIL_LINE_BREAK +
            "Wrong information - cancelled" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Informationen zur Abwesenheit:" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    Zeitraum:            16.04.2021 bis 16.04.2021, ganztägig" + EMAIL_LINE_BREAK +
            "    Art der Abwesenheit: Erholungsurlaub" + EMAIL_LINE_BREAK +
            "    Grund:               " + EMAIL_LINE_BREAK +
            "    Vertretung:          " + EMAIL_LINE_BREAK +
            "    Anschrift/Telefon:   " + EMAIL_LINE_BREAK +
            "    Erstellungsdatum:    16.04.2021" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen." + EMAIL_LINE_BREAK);

        final List<DataSource> attachmentsRelevantPerson = getAttachments(msg);
        assertThat(attachmentsRelevantPerson.get(0).getName()).contains("calendar.ics");

        // check email colleague
        final MimeMessage[] inboxColleague = greenMail.getReceivedMessagesForDomain(colleague.getEmail());
        assertThat(inboxColleague.length).isOne();

        final MimeMessage msgColleague = inboxColleague[0];
        assertThat(msgColleague.getSubject()).isEqualTo("Abwesenheit von Lieschen Müller wurde zurückgenommen");
        assertThat(new InternetAddress(colleague.getEmail())).isEqualTo(msgColleague.getAllRecipients()[0]);
        assertThat(readPlainContent(msgColleague)).isEqualTo("Hallo colleague colleague," + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "eine Abwesenheit von Lieschen Müller wurde zurückgenommen:" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    Zeitraum: 16.04.2021 bis 16.04.2021, ganztägig" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Link zur Abwesenheitsübersicht: https://localhost:8080/web/absences" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/2/notifications anpassen." + EMAIL_LINE_BREAK);

        final List<DataSource> attachmentsColleague = getAttachments(msgColleague);
        assertThat(attachmentsColleague.get(0).getName()).contains("calendar.ics");
    }

    @Test
    void ensureApplicantReceivesNotificationsIfOfficeCancel() throws MessagingException,
        IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setId(1);
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_CANCELLATION));

        final Application application = createApplication(person);
        application.setApplicationDate(LocalDate.of(2021, APRIL, 16));
        application.setStartDate(LocalDate.of(2021, APRIL, 16));
        application.setEndDate(LocalDate.of(2021, APRIL, 16));

        final Person office = new Person("office", "Person", "Office", "office@example.org");
        application.setCanceller(office);

        final ApplicationComment comment = new ApplicationComment(office, clock);
        comment.setText("Wrong information - cancelled");

        final Person colleague = new Person("colleague", "colleague", "colleague", "colleague@example.org");
        colleague.setId(2);
        colleague.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_COLLEAGUES_CANCELLATION));
        when(mailRecipientService.getColleagues(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_COLLEAGUES_CANCELLATION)).thenReturn(List.of(colleague));

        sut.sendCancelledDirectlyConfirmationByManagement(application, comment);

        // was email sent to applicant
        final MimeMessage[] inboxApplicant = greenMail.getReceivedMessagesForDomain(person.getEmail());
        assertThat(inboxApplicant.length).isOne();

        final Message msg = inboxApplicant[0];
        assertThat(msg.getSubject()).isEqualTo("Eine Abwesenheit wurde für dich storniert");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        assertThat(msg.getContent()).isEqualTo("Hallo Lieschen Müller," + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "deine Abwesenheit vom 16.04.2021 bis zum 16.04.2021 wurde von Office Person erfolgreich storniert." + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    https://localhost:8080/web/application/1234" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Kommentar von Office Person:" + EMAIL_LINE_BREAK +
            "Wrong information - cancelled" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Informationen zur Abwesenheit:" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    Zeitraum:            16.04.2021 bis 16.04.2021, ganztägig" + EMAIL_LINE_BREAK +
            "    Art der Abwesenheit: Erholungsurlaub" + EMAIL_LINE_BREAK +
            "    Grund:               " + EMAIL_LINE_BREAK +
            "    Vertretung:          " + EMAIL_LINE_BREAK +
            "    Anschrift/Telefon:   " + EMAIL_LINE_BREAK +
            "    Erstellungsdatum:    16.04.2021" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen.");

        // check email colleague
        final MimeMessage[] inboxColleague = greenMail.getReceivedMessagesForDomain(colleague.getEmail());
        assertThat(inboxColleague.length).isOne();

        final MimeMessage msgColleague = inboxColleague[0];
        assertThat(msgColleague.getSubject()).isEqualTo("Abwesenheit von Lieschen Müller wurde zurückgenommen");
        assertThat(new InternetAddress(colleague.getEmail())).isEqualTo(msgColleague.getAllRecipients()[0]);
        assertThat(msgColleague.getContent()).isEqualTo("Hallo colleague colleague," + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "eine Abwesenheit von Lieschen Müller wurde zurückgenommen:" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    Zeitraum: 16.04.2021 bis 16.04.2021, ganztägig" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Link zur Abwesenheitsübersicht: https://localhost:8080/web/absences" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/2/notifications anpassen.");
    }

    @Test
    void ensurePersonGetsANotificationIfOfficeCancelledOneOfHisApplications() throws Exception {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setId(1);
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_CANCELLATION));

        final Person office = new Person("office", "Muster", "Marlene", "office@example.org");
        office.setPermissions(List.of(OFFICE));
        office.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION));

        final Application application = createApplication(person);
        application.setApplicationDate(LocalDate.of(2020, 5, 29));
        application.setStartDate(LocalDate.of(2020, 6, 15));
        application.setEndDate(LocalDate.of(2020, 6, 15));
        application.setCanceller(office);

        final ApplicationComment comment = new ApplicationComment(person, clock);
        comment.setText("Geht leider nicht");

        final Person relevantPerson = new Person("relevant", "Person", "Relevant", "relevantperson@example.org");
        relevantPerson.setId(2);
        relevantPerson.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION));
        when(mailRecipientService.getRecipientsOfInterest(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION)).thenReturn(List.of(relevantPerson, office));

        final Person colleague = new Person("colleague", "colleague", "colleague", "colleague@example.org");
        colleague.setId(42);
        when(mailRecipientService.getColleagues(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_COLLEAGUES_CANCELLATION)).thenReturn(List.of(colleague));

        sut.sendCancelledConfirmationByManagement(application, comment);

        // was email sent to applicant?
        MimeMessage[] inboxApplicant = greenMail.getReceivedMessagesForDomain(person.getEmail());
        assertThat(inboxApplicant.length).isOne();

        MimeMessage msg = inboxApplicant[0];
        assertThat(msg.getSubject()).isEqualTo("Deine zu genehmigende Abwesenheit wurde storniert");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        assertThat(readPlainContent(msg)).isEqualTo("Hallo Lieschen Müller," + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Marlene Muster hat einen deine Abwesenheit storniert." + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    https://localhost:8080/web/application/1234" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Kommentar von Lieschen Müller:" + EMAIL_LINE_BREAK +
            "Geht leider nicht" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen." + EMAIL_LINE_BREAK);

        final List<DataSource> attachments = getAttachments(msg);
        assertThat(attachments.get(0).getName()).contains("calendar.ics");

        // was email sent to relevant person?
        MimeMessage[] inboxRelevantPerson = greenMail.getReceivedMessagesForDomain(relevantPerson.getEmail());
        assertThat(inboxRelevantPerson.length).isOne();

        MimeMessage msgRelevantPerson = inboxRelevantPerson[0];
        assertThat(msgRelevantPerson.getSubject()).isEqualTo("Eine zu genehmigende Abwesenheit wurde vom Office storniert");
        assertThat(new InternetAddress(relevantPerson.getEmail())).isEqualTo(msgRelevantPerson.getAllRecipients()[0]);

        assertThat(readPlainContent(msgRelevantPerson)).isEqualTo("Hallo Relevant Person," + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Marlene Muster hat die Abwesenheit von Lieschen Müller vom 29.05.2020 storniert." + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    https://localhost:8080/web/application/1234" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Kommentar von Lieschen Müller:" + EMAIL_LINE_BREAK +
            "Geht leider nicht" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/2/notifications anpassen." + EMAIL_LINE_BREAK);

        final List<DataSource> attachmentsRelevantPerson = getAttachments(msgRelevantPerson);
        assertThat(attachmentsRelevantPerson.get(0).getName()).contains("calendar.ics");

        // was email sent to colleague?
        MimeMessage[] inboxColleague = greenMail.getReceivedMessagesForDomain(colleague.getEmail());
        assertThat(inboxColleague.length).isOne();

        MimeMessage msgColleague = inboxColleague[0];
        assertThat(msgColleague.getSubject()).isEqualTo("Abwesenheit von Lieschen Müller wurde zurückgenommen");
        assertThat(new InternetAddress(colleague.getEmail())).isEqualTo(msgColleague.getAllRecipients()[0]);

        assertThat(readPlainContent(msgColleague)).isEqualTo("Hallo colleague colleague," + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "eine Abwesenheit von Lieschen Müller wurde zurückgenommen:" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    Zeitraum: 15.06.2020 bis 15.06.2020, ganztägig" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Link zur Abwesenheitsübersicht: https://localhost:8080/web/absences" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/42/notifications anpassen." + EMAIL_LINE_BREAK);

        final List<DataSource> attachmentsColleague = getAttachments(msgColleague);
        assertThat(attachmentsColleague.get(0).getName()).contains("calendar.ics");
    }

    @Test
    void ensureNotificationAboutNewApplicationIsSentToBossesAndDepartmentHeads() throws MessagingException, IOException {

        final Person boss = new Person("boss", "Boss", "Hugo", "boss@example.org");
        boss.setId(1);
        boss.setPermissions(List.of(BOSS));
        boss.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED));

        final Person departmentHead = new Person("departmentHead", "Kopf", "Senior", "head@example.org");
        departmentHead.setId(2);
        departmentHead.setPermissions(List.of(DEPARTMENT_HEAD));
        departmentHead.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED));

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");

        final ApplicationComment comment = new ApplicationComment(person, clock);
        comment.setText("Hätte gerne Urlaub");

        final Application application = createApplication(person);
        application.setStartDate(LocalDate.of(2023, 5, 22));
        application.setEndDate(LocalDate.of(2023, 5, 22));
        application.setApplicationDate(LocalDate.of(2023, 5, 22));

        when(departmentService.getApplicationsForLeaveOfMembersInDepartmentsOfPerson(person, application.getStartDate(), application.getEndDate())).thenReturn(List.of(application));
        when(mailRecipientService.getRecipientsOfInterest(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED)).thenReturn(asList(boss, departmentHead));

        sut.sendAppliedNotificationToManagement(application, comment);

        // was email sent to boss?
        final MimeMessage[] inboxOfBoss = greenMail.getReceivedMessagesForDomain(boss.getEmail());
        assertThat(inboxOfBoss.length).isOne();

        final Message msgBoss = inboxOfBoss[0];
        assertThat(msgBoss.getSubject()).isEqualTo("Neue zu genehmigende Abwesenheit für Lieschen Müller eingereicht");
        assertThat(new InternetAddress(boss.getEmail())).isEqualTo(msgBoss.getAllRecipients()[0]);
        assertThat(msgBoss.getContent()).isEqualTo("Hallo Hugo Boss," + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "es liegt ein neuer zu genehmigender Antrag vor." + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    https://localhost:8080/web/application/1234" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Kommentar von Lieschen Müller:" + EMAIL_LINE_BREAK +
            "Hätte gerne Urlaub" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Informationen zur Abwesenheit:" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    Mitarbeiter:         Lieschen Müller" + EMAIL_LINE_BREAK +
            "    Zeitraum:            22.05.2023 bis 22.05.2023, ganztägig" + EMAIL_LINE_BREAK +
            "    Art der Abwesenheit: Erholungsurlaub" + EMAIL_LINE_BREAK +
            "    Grund:               " + EMAIL_LINE_BREAK +
            "    Vertretung:          " + EMAIL_LINE_BREAK +
            "    Anschrift/Telefon:   " + EMAIL_LINE_BREAK +
            "    Erstellungsdatum:    22.05.2023" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Überschneidende Abwesenheiten in der Abteilung des Antragsstellers:" + EMAIL_LINE_BREAK +
            "    " + EMAIL_LINE_BREAK +
            "    Lieschen Müller: 22.05.2023 bis 22.05.2023" + EMAIL_LINE_BREAK +
            "    " + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen.");

        // was email sent to department head?
        final MimeMessage[] inboxOfDepartmentHead = greenMail.getReceivedMessagesForDomain(departmentHead.getEmail());
        assertThat(inboxOfDepartmentHead.length).isOne();
        final Message msgDepartmentHead = inboxOfDepartmentHead[0];
        assertThat(msgDepartmentHead.getContent()).isEqualTo("Hallo Senior Kopf," + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "es liegt ein neuer zu genehmigender Antrag vor." + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    https://localhost:8080/web/application/1234" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Kommentar von Lieschen Müller:" + EMAIL_LINE_BREAK +
            "Hätte gerne Urlaub" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Informationen zur Abwesenheit:" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    Mitarbeiter:         Lieschen Müller" + EMAIL_LINE_BREAK +
            "    Zeitraum:            22.05.2023 bis 22.05.2023, ganztägig" + EMAIL_LINE_BREAK +
            "    Art der Abwesenheit: Erholungsurlaub" + EMAIL_LINE_BREAK +
            "    Grund:               " + EMAIL_LINE_BREAK +
            "    Vertretung:          " + EMAIL_LINE_BREAK +
            "    Anschrift/Telefon:   " + EMAIL_LINE_BREAK +
            "    Erstellungsdatum:    22.05.2023" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Überschneidende Abwesenheiten in der Abteilung des Antragsstellers:" + EMAIL_LINE_BREAK +
            "    " + EMAIL_LINE_BREAK +
            "    Lieschen Müller: 22.05.2023 bis 22.05.2023" + EMAIL_LINE_BREAK +
            "    " + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/2/notifications anpassen.");
    }

    @Test
    void ensureNotificationAboutNewApplicationOfSecondStageAuthorityIsSentToBosses() throws MessagingException, IOException {

        final Person boss = new Person("boss", "Boss", "Hugo", "boss@example.org");
        boss.setId(1);
        boss.setPermissions(List.of(BOSS));
        boss.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED));

        final Person secondStage = new Person("manager", "Schmitt", "Kai", "manager@example.org");
        secondStage.setPermissions(List.of(SECOND_STAGE_AUTHORITY));

        final Person departmentHead = new Person("departmentHead", "Kopf", "Senior", "head@example.org");
        departmentHead.setId(2);
        departmentHead.setPermissions(List.of(DEPARTMENT_HEAD));
        departmentHead.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED));

        final ApplicationComment comment = new ApplicationComment(secondStage, clock);
        comment.setText("Hätte gerne Urlaub");

        final Application application = createApplication(secondStage);
        application.setStartDate(LocalDate.of(2023, 5, 22));
        application.setEndDate(LocalDate.of(2023, 5, 22));
        application.setApplicationDate(LocalDate.of(2023, 5, 22));

        when(departmentService.getApplicationsForLeaveOfMembersInDepartmentsOfPerson(secondStage, application.getStartDate(), application.getEndDate())).thenReturn(List.of(application));
        when(mailRecipientService.getRecipientsOfInterest(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED)).thenReturn(asList(boss, departmentHead));

        sut.sendAppliedNotificationToManagement(application, comment);

        // was email sent to boss?
        final MimeMessage[] inboxOfBoss = greenMail.getReceivedMessagesForDomain(boss.getEmail());
        assertThat(inboxOfBoss.length).isOne();
        final Message msgBoss = inboxOfBoss[0];

        assertThat(msgBoss.getSubject()).isEqualTo("Neue zu genehmigende Abwesenheit für Kai Schmitt eingereicht");
        assertThat(new InternetAddress(boss.getEmail())).isEqualTo(msgBoss.getAllRecipients()[0]);
        assertThat(msgBoss.getContent()).isEqualTo("Hallo Hugo Boss," + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "es liegt ein neuer zu genehmigender Antrag vor." + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    https://localhost:8080/web/application/1234" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Kommentar von Kai Schmitt:" + EMAIL_LINE_BREAK +
            "Hätte gerne Urlaub" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Informationen zur Abwesenheit:" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    Mitarbeiter:         Kai Schmitt" + EMAIL_LINE_BREAK +
            "    Zeitraum:            22.05.2023 bis 22.05.2023, ganztägig" + EMAIL_LINE_BREAK +
            "    Art der Abwesenheit: Erholungsurlaub" + EMAIL_LINE_BREAK +
            "    Grund:               " + EMAIL_LINE_BREAK +
            "    Vertretung:          " + EMAIL_LINE_BREAK +
            "    Anschrift/Telefon:   " + EMAIL_LINE_BREAK +
            "    Erstellungsdatum:    22.05.2023" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Überschneidende Abwesenheiten in der Abteilung des Antragsstellers:" + EMAIL_LINE_BREAK +
            "    " + EMAIL_LINE_BREAK +
            "    Kai Schmitt: 22.05.2023 bis 22.05.2023" + EMAIL_LINE_BREAK +
            "    " + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen.");

        // no email sent to department head
        final MimeMessage[] inboxOfDepartmentHead = greenMail.getReceivedMessagesForDomain(departmentHead.getEmail());
        final Message msgDepartmentHead = inboxOfDepartmentHead[0];
        assertThat(msgDepartmentHead.getSubject()).isEqualTo("Neue zu genehmigende Abwesenheit für Kai Schmitt eingereicht");
        assertThat(new InternetAddress(departmentHead.getEmail())).isEqualTo(msgDepartmentHead.getAllRecipients()[0]);
        assertThat(inboxOfDepartmentHead.length).isOne();
        assertThat(msgDepartmentHead.getContent()).isEqualTo("Hallo Senior Kopf," + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "es liegt ein neuer zu genehmigender Antrag vor." + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    https://localhost:8080/web/application/1234" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Kommentar von Kai Schmitt:" + EMAIL_LINE_BREAK +
            "Hätte gerne Urlaub" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Informationen zur Abwesenheit:" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    Mitarbeiter:         Kai Schmitt" + EMAIL_LINE_BREAK +
            "    Zeitraum:            22.05.2023 bis 22.05.2023, ganztägig" + EMAIL_LINE_BREAK +
            "    Art der Abwesenheit: Erholungsurlaub" + EMAIL_LINE_BREAK +
            "    Grund:               " + EMAIL_LINE_BREAK +
            "    Vertretung:          " + EMAIL_LINE_BREAK +
            "    Anschrift/Telefon:   " + EMAIL_LINE_BREAK +
            "    Erstellungsdatum:    22.05.2023" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Überschneidende Abwesenheiten in der Abteilung des Antragsstellers:" + EMAIL_LINE_BREAK +
            "    " + EMAIL_LINE_BREAK +
            "    Kai Schmitt: 22.05.2023 bis 22.05.2023" + EMAIL_LINE_BREAK +
            "    " + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/2/notifications anpassen.");
    }

    @Test
    void ensureNotificationAboutNewApplicationOfDepartmentHeadIsSentToSecondaryStageAuthority() throws MessagingException, IOException {

        final Person boss = new Person("boss", "Boss", "Hugo", "boss@example.org");
        boss.setId(1);
        boss.setPermissions(List.of(BOSS));
        boss.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED));

        final Person secondStage = new Person("manager", "Schmitt", "Kai", "manager@example.org");
        secondStage.setId(2);
        secondStage.setPermissions(List.of(SECOND_STAGE_AUTHORITY));
        secondStage.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED));

        final Person departmentHead = new Person("departmentHead", "Kopf", "Senior", "head@example.org");
        departmentHead.setPermissions(List.of(DEPARTMENT_HEAD));

        final ApplicationComment comment = new ApplicationComment(departmentHead, clock);
        comment.setText("Hätte gerne Urlaub");

        final Application application = createApplication(departmentHead);
        application.setStartDate(LocalDate.of(2023, 5, 22));
        application.setEndDate(LocalDate.of(2023, 5, 22));
        application.setApplicationDate(LocalDate.of(2023, 5, 22));

        when(departmentService.getApplicationsForLeaveOfMembersInDepartmentsOfPerson(departmentHead, application.getStartDate(), application.getEndDate())).thenReturn(List.of(application));
        when(mailRecipientService.getRecipientsOfInterest(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED)).thenReturn(asList(boss, secondStage));

        sut.sendAppliedNotificationToManagement(application, comment);

        // was email sent to boss?
        final MimeMessage[] inboxOfBoss = greenMail.getReceivedMessagesForDomain(boss.getEmail());
        assertThat(inboxOfBoss.length).isOne();
        final Message msgBoss = inboxOfBoss[0];
        assertThat(msgBoss.getContent()).isEqualTo("Hallo Hugo Boss," + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "es liegt ein neuer zu genehmigender Antrag vor." + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    https://localhost:8080/web/application/1234" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Kommentar von Senior Kopf:" + EMAIL_LINE_BREAK +
            "Hätte gerne Urlaub" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Informationen zur Abwesenheit:" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    Mitarbeiter:         Senior Kopf" + EMAIL_LINE_BREAK +
            "    Zeitraum:            22.05.2023 bis 22.05.2023, ganztägig" + EMAIL_LINE_BREAK +
            "    Art der Abwesenheit: Erholungsurlaub" + EMAIL_LINE_BREAK +
            "    Grund:               " + EMAIL_LINE_BREAK +
            "    Vertretung:          " + EMAIL_LINE_BREAK +
            "    Anschrift/Telefon:   " + EMAIL_LINE_BREAK +
            "    Erstellungsdatum:    22.05.2023" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Überschneidende Abwesenheiten in der Abteilung des Antragsstellers:" + EMAIL_LINE_BREAK +
            "    " + EMAIL_LINE_BREAK +
            "    Senior Kopf: 22.05.2023 bis 22.05.2023" + EMAIL_LINE_BREAK +
            "    " + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen.");

        // was email sent to secondary stage?
        final MimeMessage[] inboxOfSecondaryStage = greenMail.getReceivedMessagesForDomain(secondStage.getEmail());
        assertThat(inboxOfSecondaryStage.length).isOne();
        final Message msgSecondaryStage = inboxOfSecondaryStage[0];
        assertThat(msgSecondaryStage.getContent()).isEqualTo("Hallo Kai Schmitt," + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "es liegt ein neuer zu genehmigender Antrag vor." + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    https://localhost:8080/web/application/1234" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Kommentar von Senior Kopf:" + EMAIL_LINE_BREAK +
            "Hätte gerne Urlaub" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Informationen zur Abwesenheit:" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    Mitarbeiter:         Senior Kopf" + EMAIL_LINE_BREAK +
            "    Zeitraum:            22.05.2023 bis 22.05.2023, ganztägig" + EMAIL_LINE_BREAK +
            "    Art der Abwesenheit: Erholungsurlaub" + EMAIL_LINE_BREAK +
            "    Grund:               " + EMAIL_LINE_BREAK +
            "    Vertretung:          " + EMAIL_LINE_BREAK +
            "    Anschrift/Telefon:   " + EMAIL_LINE_BREAK +
            "    Erstellungsdatum:    22.05.2023" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Überschneidende Abwesenheiten in der Abteilung des Antragsstellers:" + EMAIL_LINE_BREAK +
            "    " + EMAIL_LINE_BREAK +
            "    Senior Kopf: 22.05.2023 bis 22.05.2023" + EMAIL_LINE_BREAK +
            "    " + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/2/notifications anpassen.");
    }

    @Test
    void ensureNotificationAboutNewApplicationWithoutOverlappingVacations() throws MessagingException, IOException {

        final Person holidayReplacement = new Person("pennyworth", "Pennyworth", "Alfred", "pennyworth@example.org");

        final Person boss = new Person("boss", "Boss", "Hugo", "boss@example.org");
        boss.setId(1);
        boss.setPermissions(List.of(BOSS));
        boss.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED));

        final Person person = new Person("lieschen", "Müller", "Lieschen", "mueller@example.org");
        person.setPermissions(List.of(USER));

        final Application application = createApplication(person);
        application.setApplicationDate(LocalDate.of(2021, APRIL, 12));
        application.setStartDate(LocalDate.of(2021, APRIL, 16));
        application.setEndDate(LocalDate.of(2021, APRIL, 16));

        final HolidayReplacementEntity holidayReplacementEntity = new HolidayReplacementEntity();
        holidayReplacementEntity.setPerson(holidayReplacement);

        application.setHolidayReplacements(List.of(holidayReplacementEntity));

        when(departmentService.getApplicationsForLeaveOfMembersInDepartmentsOfPerson(person, application.getStartDate(), application.getEndDate())).thenReturn(List.of());
        when(mailRecipientService.getRecipientsOfInterest(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED)).thenReturn(List.of(boss));

        sut.sendAppliedNotificationToManagement(application, new ApplicationComment(clock));

        MimeMessage[] messages = greenMail.getReceivedMessagesForDomain(boss.getEmail());
        assertThat(messages.length).isOne();

        Message message = messages[0];
        assertThat(message.getContent()).isEqualTo("Hallo Hugo Boss," + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "es liegt ein neuer zu genehmigender Antrag vor." + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    https://localhost:8080/web/application/1234" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Informationen zur Abwesenheit:" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    Mitarbeiter:         Lieschen Müller" + EMAIL_LINE_BREAK +
            "    Zeitraum:            16.04.2021 bis 16.04.2021, ganztägig" + EMAIL_LINE_BREAK +
            "    Art der Abwesenheit: Erholungsurlaub" + EMAIL_LINE_BREAK +
            "    Grund:               " + EMAIL_LINE_BREAK +
            "    Vertretung:          Alfred Pennyworth" + EMAIL_LINE_BREAK +
            "    Anschrift/Telefon:   " + EMAIL_LINE_BREAK +
            "    Erstellungsdatum:    12.04.2021" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Überschneidende Abwesenheiten in der Abteilung des Antragsstellers:" + EMAIL_LINE_BREAK +
            "    " + EMAIL_LINE_BREAK +
            "    Keine" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen."
        );
    }

    @Test
    void ensureNotificationAboutNewApplicationWithMultipleOverlappingVacations() throws MessagingException, IOException {

        final Person holidayReplacement = new Person("pennyworth", "Pennyworth", "Alfred", "pennyworth@example.org");

        final Person boss = new Person("boss", "Boss", "Hugo", "boss@example.org");
        boss.setId(1);
        boss.setPermissions(List.of(BOSS));
        boss.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED));

        final Person person = new Person("lieschen", "Müller", "Lieschen", "mueller@example.org");
        person.setPermissions(List.of(USER));

        final Application application = createApplication(person);
        application.setApplicationDate(LocalDate.of(2021, APRIL, 12));
        application.setStartDate(LocalDate.of(2021, APRIL, 16));
        application.setEndDate(LocalDate.of(2021, APRIL, 16));

        final Application applicationSecond = createApplication(person);
        applicationSecond.setApplicationDate(LocalDate.of(2021, APRIL, 12));
        applicationSecond.setStartDate(LocalDate.of(2021, APRIL, 17));
        applicationSecond.setEndDate(LocalDate.of(2021, APRIL, 17));

        final HolidayReplacementEntity holidayReplacementEntity = new HolidayReplacementEntity();
        holidayReplacementEntity.setPerson(holidayReplacement);

        application.setHolidayReplacements(List.of(holidayReplacementEntity));

        when(departmentService.getApplicationsForLeaveOfMembersInDepartmentsOfPerson(person, application.getStartDate(), application.getEndDate())).thenReturn(List.of(application, applicationSecond));
        when(mailRecipientService.getRecipientsOfInterest(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED)).thenReturn(List.of(boss));

        sut.sendAppliedNotificationToManagement(application, new ApplicationComment(clock));

        MimeMessage[] messages = greenMail.getReceivedMessagesForDomain(boss.getEmail());
        assertThat(messages.length).isOne();

        Message message = messages[0];
        assertThat(message.getContent()).isEqualTo("Hallo Hugo Boss," + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "es liegt ein neuer zu genehmigender Antrag vor." + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    https://localhost:8080/web/application/1234" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Informationen zur Abwesenheit:" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    Mitarbeiter:         Lieschen Müller" + EMAIL_LINE_BREAK +
            "    Zeitraum:            16.04.2021 bis 16.04.2021, ganztägig" + EMAIL_LINE_BREAK +
            "    Art der Abwesenheit: Erholungsurlaub" + EMAIL_LINE_BREAK +
            "    Grund:               " + EMAIL_LINE_BREAK +
            "    Vertretung:          Alfred Pennyworth" + EMAIL_LINE_BREAK +
            "    Anschrift/Telefon:   " + EMAIL_LINE_BREAK +
            "    Erstellungsdatum:    12.04.2021" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Überschneidende Abwesenheiten in der Abteilung des Antragsstellers:" + EMAIL_LINE_BREAK +
            "    " + EMAIL_LINE_BREAK +
            "    Lieschen Müller: 16.04.2021 bis 16.04.2021" + EMAIL_LINE_BREAK +
            "    Lieschen Müller: 17.04.2021 bis 17.04.2021" + EMAIL_LINE_BREAK +
            "    " + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen."
        );
    }

    @Test
    void ensureNotificationAboutNewApplicationOfDepartmentHeadIsSentWithOneReplacement() throws MessagingException, IOException {

        final Person holidayReplacement = new Person("pennyworth", "Pennyworth", "Alfred", "pennyworth@example.org");

        final Person boss = new Person("boss", "Boss", "Hugo", "boss@example.org");
        boss.setId(1);
        boss.setPermissions(List.of(BOSS));
        boss.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED));

        final Person person = new Person("lieschen", "Müller", "Lieschen", "mueller@example.org");
        person.setPermissions(List.of(USER));

        final Application application = createApplication(person);
        application.setApplicationDate(LocalDate.of(2021, APRIL, 12));
        application.setStartDate(LocalDate.of(2021, APRIL, 16));
        application.setEndDate(LocalDate.of(2021, APRIL, 16));

        final HolidayReplacementEntity holidayReplacementEntity = new HolidayReplacementEntity();
        holidayReplacementEntity.setPerson(holidayReplacement);

        application.setHolidayReplacements(List.of(holidayReplacementEntity));

        when(departmentService.getApplicationsForLeaveOfMembersInDepartmentsOfPerson(person, application.getStartDate(), application.getEndDate())).thenReturn(List.of(application));
        when(mailRecipientService.getRecipientsOfInterest(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED)).thenReturn(List.of(boss));

        sut.sendAppliedNotificationToManagement(application, new ApplicationComment(clock));

        MimeMessage[] messages = greenMail.getReceivedMessagesForDomain(boss.getEmail());
        assertThat(messages.length).isOne();

        Message message = messages[0];
        assertThat(message.getContent()).isEqualTo("Hallo Hugo Boss," + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "es liegt ein neuer zu genehmigender Antrag vor." + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    https://localhost:8080/web/application/1234" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Informationen zur Abwesenheit:" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    Mitarbeiter:         Lieschen Müller" + EMAIL_LINE_BREAK +
            "    Zeitraum:            16.04.2021 bis 16.04.2021, ganztägig" + EMAIL_LINE_BREAK +
            "    Art der Abwesenheit: Erholungsurlaub" + EMAIL_LINE_BREAK +
            "    Grund:               " + EMAIL_LINE_BREAK +
            "    Vertretung:          Alfred Pennyworth" + EMAIL_LINE_BREAK +
            "    Anschrift/Telefon:   " + EMAIL_LINE_BREAK +
            "    Erstellungsdatum:    12.04.2021" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Überschneidende Abwesenheiten in der Abteilung des Antragsstellers:" + EMAIL_LINE_BREAK +
            "    " + EMAIL_LINE_BREAK +
            "    Lieschen Müller: 16.04.2021 bis 16.04.2021" + EMAIL_LINE_BREAK +
            "    " + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen.");
    }

    @Test
    void ensureNotificationAboutNewApplicationOfDepartmentHeadIsSentWithMultipleReplacements() throws MessagingException, IOException {

        final Person holidayReplacementOne = new Person("pennyworth", "Pennyworth", "Alfred", "pennyworth@example.org");
        final Person holidayReplacementTwo = new Person("rob", "", "Robin", "robin@example.org");

        final Person boss = new Person("boss", "Boss", "Hugo", "boss@example.org");
        boss.setId(1);
        boss.setPermissions(List.of(BOSS));
        boss.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED));

        final Person person = new Person("lieschen", "Müller", "Lieschen", "mueller@example.org");
        person.setPermissions(List.of(USER));

        final Application application = createApplication(person);
        application.setApplicationDate(LocalDate.of(2021, APRIL, 12));
        application.setStartDate(LocalDate.of(2021, APRIL, 16));
        application.setEndDate(LocalDate.of(2021, APRIL, 16));

        final HolidayReplacementEntity holidayReplacementOneEntity = new HolidayReplacementEntity();
        holidayReplacementOneEntity.setPerson(holidayReplacementOne);

        final HolidayReplacementEntity holidayReplacementTwoEntity = new HolidayReplacementEntity();
        holidayReplacementTwoEntity.setPerson(holidayReplacementTwo);

        application.setHolidayReplacements(List.of(holidayReplacementOneEntity, holidayReplacementTwoEntity));

        when(departmentService.getApplicationsForLeaveOfMembersInDepartmentsOfPerson(person, application.getStartDate(), application.getEndDate())).thenReturn(List.of(application));
        when(mailRecipientService.getRecipientsOfInterest(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED)).thenReturn(List.of(boss));

        sut.sendAppliedNotificationToManagement(application, new ApplicationComment(clock));

        final MimeMessage[] messages = greenMail.getReceivedMessagesForDomain(boss.getEmail());
        assertThat(messages.length).isOne();

        final Message message = messages[0];
        assertThat(message.getContent()).isEqualTo("Hallo Hugo Boss," + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "es liegt ein neuer zu genehmigender Antrag vor." + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    https://localhost:8080/web/application/1234" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Informationen zur Abwesenheit:" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    Mitarbeiter:         Lieschen Müller" + EMAIL_LINE_BREAK +
            "    Zeitraum:            16.04.2021 bis 16.04.2021, ganztägig" + EMAIL_LINE_BREAK +
            "    Art der Abwesenheit: Erholungsurlaub" + EMAIL_LINE_BREAK +
            "    Grund:               " + EMAIL_LINE_BREAK +
            "    Vertretung:          Alfred Pennyworth, Robin" + EMAIL_LINE_BREAK +
            "    Anschrift/Telefon:   " + EMAIL_LINE_BREAK +
            "    Erstellungsdatum:    12.04.2021" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Überschneidende Abwesenheiten in der Abteilung des Antragsstellers:" + EMAIL_LINE_BREAK +
            "    " + EMAIL_LINE_BREAK +
            "    Lieschen Müller: 16.04.2021 bis 16.04.2021" + EMAIL_LINE_BREAK +
            "    " + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen.");
    }

    @Test
    void ensureNotificationAboutTemporaryAllowedApplicationIsSentToSecondStageAuthoritiesAndToPerson()
        throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setId(1);
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_TEMPORARY_ALLOWED));

        final Person secondStage = new Person("manager", "Schmitt", "Kai", "manager@example.org");
        secondStage.setId(2);
        secondStage.setPermissions(List.of(SECOND_STAGE_AUTHORITY));
        secondStage.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_TEMPORARY_ALLOWED));

        final ApplicationComment comment = new ApplicationComment(secondStage, clock);
        comment.setText("OK, spricht von meiner Seite aus nix dagegen");

        final Application application = createApplication(person);
        application.setApplicationDate(LocalDate.of(2021, APRIL, 12));
        application.setStartDate(LocalDate.of(2021, APRIL, 16));
        application.setEndDate(LocalDate.of(2021, APRIL, 16));

        when(departmentService.getApplicationsForLeaveOfMembersInDepartmentsOfPerson(person, application.getStartDate(), application.getEndDate())).thenReturn(List.of(application));
        when(mailRecipientService.getRecipientsOfInterest(person, NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_TEMPORARY_ALLOWED)).thenReturn(List.of(secondStage));

        sut.sendTemporaryAllowedNotification(application, comment);

        // were both emails sent?
        MimeMessage[] inboxSecondStage = greenMail.getReceivedMessagesForDomain(secondStage.getEmail());
        assertThat(inboxSecondStage.length).isOne();

        MimeMessage[] inboxUser = greenMail.getReceivedMessagesForDomain(person.getEmail());
        assertThat(inboxUser.length).isOne();

        // get email user
        Message msg = inboxUser[0];
        assertThat(msg.getSubject()).isEqualTo("Deine zu genehmigende Abwesenheit wurde vorläufig genehmigt");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of user email
        String contentUser = (String) msg.getContent();
        assertThat(contentUser).isEqualTo("Hallo Lieschen Müller," + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "deine am 12.04.2021 gestellte Abwesenheit von 16.04.2021 bis 16.04.2021, ganztägig wurde vorläufig genehmigt." + EMAIL_LINE_BREAK +
            "Bitte beachte, dass diese von einem entsprechenden Verantwortlichen freigegeben werden muss." + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    https://localhost:8080/web/application/1234" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Kommentar von Kai Schmitt:" + EMAIL_LINE_BREAK +
            "OK, spricht von meiner Seite aus nix dagegen" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen.");

        // get email office
        Message msgSecondStage = inboxSecondStage[0];
        assertThat(msgSecondStage.getSubject()).isEqualTo("Eine zu genehmigende Abwesenheit wurde vorläufig genehmigt");
        assertThat(new InternetAddress(secondStage.getEmail())).isEqualTo(msgSecondStage.getAllRecipients()[0]);

        // check content of office email
        assertThat(msgSecondStage.getContent()).isEqualTo("Hallo Kai Schmitt," + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "es liegt eine neue zu genehmigende Abwesenheit vor." + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    https://localhost:8080/web/application/1234" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Die Abwesenheit wurde bereits vorläufig genehmigt und muss nun noch endgültig freigegeben werden." + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Kommentar von Kai Schmitt:" + EMAIL_LINE_BREAK +
            "OK, spricht von meiner Seite aus nix dagegen" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Informationen zur Abwesenheit:" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    Mitarbeiter:         Lieschen Müller" + EMAIL_LINE_BREAK +
            "    Zeitraum:            16.04.2021 bis 16.04.2021, ganztägig" + EMAIL_LINE_BREAK +
            "    Art der Abwesenheit: Erholungsurlaub" + EMAIL_LINE_BREAK +
            "    Grund:               " + EMAIL_LINE_BREAK +
            "    Vertretung:          " + EMAIL_LINE_BREAK +
            "    Anschrift/Telefon:   " + EMAIL_LINE_BREAK +
            "    Erstellungsdatum:    12.04.2021" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Überschneidende Abwesenheiten in der Abteilung des Antragsstellers:" + EMAIL_LINE_BREAK +
            "    " + EMAIL_LINE_BREAK +
            "    Lieschen Müller: 16.04.2021 bis 16.04.2021" + EMAIL_LINE_BREAK +
            "    " + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/2/notifications anpassen.");
    }

    @Test
    void ensureNotificationAboutTemporaryAllowedApplicationIsSentToSecondStageAuthoritiesAndToPersonWithOneReplacement()
        throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_TEMPORARY_ALLOWED));
        final Person holidayReplacement = new Person("pennyworth", "Pennyworth", "Alfred", "pennyworth@example.org");

        final Person secondStage = new Person("manager", "Schmitt", "Kai", "manager@example.org");
        secondStage.setId(1);
        secondStage.setPermissions(List.of(SECOND_STAGE_AUTHORITY));
        secondStage.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_TEMPORARY_ALLOWED));

        final ApplicationComment comment = new ApplicationComment(secondStage, clock);
        comment.setText("OK, spricht von meiner Seite aus nix dagegen");

        final Application application = createApplication(person);
        application.setApplicationDate(LocalDate.of(2021, APRIL, 12));
        application.setStartDate(LocalDate.of(2021, APRIL, 16));
        application.setEndDate(LocalDate.of(2021, APRIL, 16));

        final HolidayReplacementEntity holidayReplacementEntity = new HolidayReplacementEntity();
        holidayReplacementEntity.setPerson(holidayReplacement);

        application.setHolidayReplacements(List.of(holidayReplacementEntity));

        when(departmentService.getApplicationsForLeaveOfMembersInDepartmentsOfPerson(person, application.getStartDate(), application.getEndDate()))
            .thenReturn(List.of(application));

        when(mailRecipientService.getRecipientsOfInterest(person, NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_TEMPORARY_ALLOWED)).thenReturn(List.of(secondStage));

        sut.sendTemporaryAllowedNotification(application, comment);

        // were both emails sent?
        MimeMessage[] inboxSecondStage = greenMail.getReceivedMessagesForDomain(secondStage.getEmail());
        assertThat(inboxSecondStage.length).isOne();

        MimeMessage[] inboxUser = greenMail.getReceivedMessagesForDomain(person.getEmail());
        assertThat(inboxUser.length).isOne();

        // get email user
        Message msg = inboxUser[0];
        assertThat(msg.getSubject()).isEqualTo("Deine zu genehmigende Abwesenheit wurde vorläufig genehmigt");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // get email office
        final Message msgSecondStage = inboxSecondStage[0];
        assertThat(msgSecondStage.getSubject()).isEqualTo("Eine zu genehmigende Abwesenheit wurde vorläufig genehmigt");
        assertThat(new InternetAddress(secondStage.getEmail())).isEqualTo(msgSecondStage.getAllRecipients()[0]);

        // check content of office email
        assertThat(msgSecondStage.getContent()).isEqualTo("Hallo Kai Schmitt," + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "es liegt eine neue zu genehmigende Abwesenheit vor." + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    https://localhost:8080/web/application/1234" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Die Abwesenheit wurde bereits vorläufig genehmigt und muss nun noch endgültig freigegeben werden." + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Kommentar von Kai Schmitt:" + EMAIL_LINE_BREAK +
            "OK, spricht von meiner Seite aus nix dagegen" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Informationen zur Abwesenheit:" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    Mitarbeiter:         Lieschen Müller" + EMAIL_LINE_BREAK +
            "    Zeitraum:            16.04.2021 bis 16.04.2021, ganztägig" + EMAIL_LINE_BREAK +
            "    Art der Abwesenheit: Erholungsurlaub" + EMAIL_LINE_BREAK +
            "    Grund:               " + EMAIL_LINE_BREAK +
            "    Vertretung:          Alfred Pennyworth" + EMAIL_LINE_BREAK +
            "    Anschrift/Telefon:   " + EMAIL_LINE_BREAK +
            "    Erstellungsdatum:    12.04.2021" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Überschneidende Abwesenheiten in der Abteilung des Antragsstellers:" + EMAIL_LINE_BREAK +
            "    " + EMAIL_LINE_BREAK +
            "    Lieschen Müller: 16.04.2021 bis 16.04.2021" + EMAIL_LINE_BREAK +
            "    " + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen.");
    }

    @Test
    void ensureNotificationAboutTemporaryAllowedApplicationIsSentToSecondStageAuthoritiesAndToPersonWithMultipleReplacements()
        throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_TEMPORARY_ALLOWED));
        final Person holidayReplacementOne = new Person("pennyworth", "Pennyworth", "Alfred", "pennyworth@example.org");
        final Person holidayReplacementTwo = new Person("rob", "", "Robin", "robin@example.org");

        final Person secondStage = new Person("manager", "Schmitt", "Kai", "manager@example.org");
        secondStage.setId(1);
        secondStage.setPermissions(List.of(SECOND_STAGE_AUTHORITY));
        secondStage.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_TEMPORARY_ALLOWED));

        final ApplicationComment comment = new ApplicationComment(secondStage, clock);
        comment.setText("OK, spricht von meiner Seite aus nix dagegen");

        final Application application = createApplication(person);
        application.setApplicationDate(LocalDate.of(2021, APRIL, 12));
        application.setStartDate(LocalDate.of(2021, APRIL, 16));
        application.setEndDate(LocalDate.of(2021, APRIL, 16));

        final HolidayReplacementEntity holidayReplacementOneEntity = new HolidayReplacementEntity();
        holidayReplacementOneEntity.setPerson(holidayReplacementOne);

        final HolidayReplacementEntity holidayReplacementTwoEntity = new HolidayReplacementEntity();
        holidayReplacementTwoEntity.setPerson(holidayReplacementTwo);

        application.setHolidayReplacements(List.of(holidayReplacementOneEntity, holidayReplacementTwoEntity));

        when(departmentService.getApplicationsForLeaveOfMembersInDepartmentsOfPerson(person, application.getStartDate(), application.getEndDate()))
            .thenReturn(List.of(application));

        when(mailRecipientService.getRecipientsOfInterest(person, NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_TEMPORARY_ALLOWED)).thenReturn(List.of(secondStage));

        sut.sendTemporaryAllowedNotification(application, comment);

        // were both emails sent?
        MimeMessage[] inboxSecondStage = greenMail.getReceivedMessagesForDomain(secondStage.getEmail());
        assertThat(inboxSecondStage.length).isOne();

        MimeMessage[] inboxUser = greenMail.getReceivedMessagesForDomain(person.getEmail());
        assertThat(inboxUser.length).isOne();

        // get email user
        Message msg = inboxUser[0];
        assertThat(msg.getSubject()).isEqualTo("Deine zu genehmigende Abwesenheit wurde vorläufig genehmigt");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // get email office
        final Message msgSecondStage = inboxSecondStage[0];
        assertThat(msgSecondStage.getSubject()).isEqualTo("Eine zu genehmigende Abwesenheit wurde vorläufig genehmigt");
        assertThat(new InternetAddress(secondStage.getEmail())).isEqualTo(msgSecondStage.getAllRecipients()[0]);

        // check content of office email
        assertThat(msgSecondStage.getContent()).isEqualTo("Hallo Kai Schmitt," + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "es liegt eine neue zu genehmigende Abwesenheit vor." + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    https://localhost:8080/web/application/1234" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Die Abwesenheit wurde bereits vorläufig genehmigt und muss nun noch endgültig freigegeben werden." + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Kommentar von Kai Schmitt:" + EMAIL_LINE_BREAK +
            "OK, spricht von meiner Seite aus nix dagegen" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Informationen zur Abwesenheit:" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    Mitarbeiter:         Lieschen Müller" + EMAIL_LINE_BREAK +
            "    Zeitraum:            16.04.2021 bis 16.04.2021, ganztägig" + EMAIL_LINE_BREAK +
            "    Art der Abwesenheit: Erholungsurlaub" + EMAIL_LINE_BREAK +
            "    Grund:               " + EMAIL_LINE_BREAK +
            "    Vertretung:          Alfred Pennyworth, Robin" + EMAIL_LINE_BREAK +
            "    Anschrift/Telefon:   " + EMAIL_LINE_BREAK +
            "    Erstellungsdatum:    12.04.2021" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Überschneidende Abwesenheiten in der Abteilung des Antragsstellers:" + EMAIL_LINE_BREAK +
            "    " + EMAIL_LINE_BREAK +
            "    Lieschen Müller: 16.04.2021 bis 16.04.2021" + EMAIL_LINE_BREAK +
            "    " + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen.");
    }

    @Test
    void ensureNotificationAboutTemporaryAllowedApplicationWithMultipleOverlappingVacations() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_TEMPORARY_ALLOWED));

        final Person secondStage = new Person("manager", "Schmitt", "Kai", "manager@example.org");
        secondStage.setId(1);
        secondStage.setPermissions(List.of(SECOND_STAGE_AUTHORITY));
        secondStage.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_TEMPORARY_ALLOWED));

        final ApplicationComment comment = new ApplicationComment(secondStage, clock);
        comment.setText("OK, spricht von meiner Seite aus nix dagegen");

        final Application application = createApplication(person);
        application.setApplicationDate(LocalDate.of(2021, APRIL, 12));
        application.setStartDate(LocalDate.of(2021, APRIL, 16));
        application.setEndDate(LocalDate.of(2021, APRIL, 16));

        final Application applicationSecond = createApplication(person);
        applicationSecond.setApplicationDate(LocalDate.of(2021, APRIL, 12));
        applicationSecond.setStartDate(LocalDate.of(2021, APRIL, 17));
        applicationSecond.setEndDate(LocalDate.of(2021, APRIL, 17));

        when(departmentService.getApplicationsForLeaveOfMembersInDepartmentsOfPerson(person, application.getStartDate(), application.getEndDate())).thenReturn(List.of(application, applicationSecond));
        when(mailRecipientService.getRecipientsOfInterest(person, NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_TEMPORARY_ALLOWED)).thenReturn(List.of(secondStage));

        sut.sendTemporaryAllowedNotification(application, comment);

        // were both emails sent?
        MimeMessage[] inboxSecondStage = greenMail.getReceivedMessagesForDomain(secondStage.getEmail());
        assertThat(inboxSecondStage.length).isOne();

        MimeMessage[] inboxUser = greenMail.getReceivedMessagesForDomain(person.getEmail());
        assertThat(inboxUser.length).isOne();

        // get email user
        Message msg = inboxUser[0];
        assertThat(msg.getSubject()).isEqualTo("Deine zu genehmigende Abwesenheit wurde vorläufig genehmigt");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // get email office
        final Message msgSecondStage = inboxSecondStage[0];
        assertThat(msgSecondStage.getSubject()).isEqualTo("Eine zu genehmigende Abwesenheit wurde vorläufig genehmigt");
        assertThat(new InternetAddress(secondStage.getEmail())).isEqualTo(msgSecondStage.getAllRecipients()[0]);

        // check content of office email
        assertThat(msgSecondStage.getContent()).isEqualTo("Hallo Kai Schmitt," + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "es liegt eine neue zu genehmigende Abwesenheit vor." + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    https://localhost:8080/web/application/1234" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Die Abwesenheit wurde bereits vorläufig genehmigt und muss nun noch endgültig freigegeben werden." + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Kommentar von Kai Schmitt:" + EMAIL_LINE_BREAK +
            "OK, spricht von meiner Seite aus nix dagegen" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Informationen zur Abwesenheit:" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    Mitarbeiter:         Lieschen Müller" + EMAIL_LINE_BREAK +
            "    Zeitraum:            16.04.2021 bis 16.04.2021, ganztägig" + EMAIL_LINE_BREAK +
            "    Art der Abwesenheit: Erholungsurlaub" + EMAIL_LINE_BREAK +
            "    Grund:               " + EMAIL_LINE_BREAK +
            "    Vertretung:          " + EMAIL_LINE_BREAK +
            "    Anschrift/Telefon:   " + EMAIL_LINE_BREAK +
            "    Erstellungsdatum:    12.04.2021" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Überschneidende Abwesenheiten in der Abteilung des Antragsstellers:" + EMAIL_LINE_BREAK +
            "    " + EMAIL_LINE_BREAK +
            "    Lieschen Müller: 16.04.2021 bis 16.04.2021" + EMAIL_LINE_BREAK +
            "    Lieschen Müller: 17.04.2021 bis 17.04.2021" + EMAIL_LINE_BREAK +
            "    " + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen.");
    }

    @Test
    void ensureNotificationAboutTemporaryAllowedApplicationWithoutOverlappingVacations() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_TEMPORARY_ALLOWED));

        final Person secondStage = new Person("manager", "Schmitt", "Kai", "manager@example.org");
        secondStage.setId(1);
        secondStage.setPermissions(List.of(SECOND_STAGE_AUTHORITY));
        secondStage.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_TEMPORARY_ALLOWED));

        final ApplicationComment comment = new ApplicationComment(secondStage, clock);
        comment.setText("OK, spricht von meiner Seite aus nix dagegen");

        final Application application = createApplication(person);
        application.setApplicationDate(LocalDate.of(2021, APRIL, 12));
        application.setStartDate(LocalDate.of(2021, APRIL, 16));
        application.setEndDate(LocalDate.of(2021, APRIL, 16));

        when(departmentService.getApplicationsForLeaveOfMembersInDepartmentsOfPerson(person, application.getStartDate(), application.getEndDate())).thenReturn(List.of());
        when(mailRecipientService.getRecipientsOfInterest(person, NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_TEMPORARY_ALLOWED)).thenReturn(List.of(secondStage));

        sut.sendTemporaryAllowedNotification(application, comment);

        // were both emails sent?
        MimeMessage[] inboxSecondStage = greenMail.getReceivedMessagesForDomain(secondStage.getEmail());
        assertThat(inboxSecondStage.length).isOne();

        MimeMessage[] inboxUser = greenMail.getReceivedMessagesForDomain(person.getEmail());
        assertThat(inboxUser.length).isOne();

        // get email user
        Message msg = inboxUser[0];
        assertThat(msg.getSubject()).isEqualTo("Deine zu genehmigende Abwesenheit wurde vorläufig genehmigt");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // get email office
        final Message msgSecondStage = inboxSecondStage[0];
        assertThat(msgSecondStage.getSubject()).isEqualTo("Eine zu genehmigende Abwesenheit wurde vorläufig genehmigt");
        assertThat(new InternetAddress(secondStage.getEmail())).isEqualTo(msgSecondStage.getAllRecipients()[0]);

        // check content of office email
        assertThat(msgSecondStage.getContent()).isEqualTo("Hallo Kai Schmitt," + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "es liegt eine neue zu genehmigende Abwesenheit vor." + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    https://localhost:8080/web/application/1234" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Die Abwesenheit wurde bereits vorläufig genehmigt und muss nun noch endgültig freigegeben werden." + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Kommentar von Kai Schmitt:" + EMAIL_LINE_BREAK +
            "OK, spricht von meiner Seite aus nix dagegen" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Informationen zur Abwesenheit:" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    Mitarbeiter:         Lieschen Müller" + EMAIL_LINE_BREAK +
            "    Zeitraum:            16.04.2021 bis 16.04.2021, ganztägig" + EMAIL_LINE_BREAK +
            "    Art der Abwesenheit: Erholungsurlaub" + EMAIL_LINE_BREAK +
            "    Grund:               " + EMAIL_LINE_BREAK +
            "    Vertretung:          " + EMAIL_LINE_BREAK +
            "    Anschrift/Telefon:   " + EMAIL_LINE_BREAK +
            "    Erstellungsdatum:    12.04.2021" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Überschneidende Abwesenheiten in der Abteilung des Antragsstellers:" + EMAIL_LINE_BREAK +
            "    " + EMAIL_LINE_BREAK +
            "    Keine" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen.");
    }

    @Test
    void ensureBossesAndDepartmentHeadsGetRemindMail() throws MessagingException, IOException {

        final Person boss = new Person("boss", "Boss", "Hugo", "boss@example.org");
        boss.setPermissions(List.of(BOSS));

        final Person departmentHead = new Person("departmentHead", "Kopf", "Senior", "head@example.org");
        departmentHead.setPermissions(List.of(DEPARTMENT_HEAD));

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");

        final ApplicationComment comment = new ApplicationComment(person, clock);
        comment.setText("OK, spricht von meiner Seite aus nix dagegen");

        final Application application = createApplication(person);

        when(mailRecipientService.getResponsibleManagersOf(application.getPerson())).thenReturn(asList(boss, departmentHead));

        sut.sendRemindNotificationToManagement(application);

        // was email sent to boss?
        MimeMessage[] inboxOfBoss = greenMail.getReceivedMessagesForDomain(boss.getEmail());
        assertThat(inboxOfBoss.length).isOne();

        // was email sent to department head?
        MimeMessage[] inboxOfDepartmentHead = greenMail.getReceivedMessagesForDomain(departmentHead.getEmail());
        assertThat(inboxOfDepartmentHead.length).isOne();

        // has mail correct attributes?
        Message msg = inboxOfBoss[0];
        assertThat(msg.getSubject()).isEqualTo("Erinnerung auf wartende zu genehmigende Abwesenheit");
        assertThat(new InternetAddress(boss.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        String content = (String) msg.getContent();
        assertThat(content).contains("Hallo Hugo Boss");
        assertThat(content).contains("/web/application/1234");
    }

    @Test
    void ensureSendRemindForWaitingApplicationsReminderNotification() throws Exception {

        // PERSONs
        final Person personA = new Person("personA", "Mahler", "Max", "mahler@example.org");
        personA.setId(1);
        final Person personB = new Person("personB", "Förster", "Frederik", "förster@example.org");
        personB.setId(2);
        final Person personC = new Person("personC", "Schuster", "Peter", "schuster@example.org");
        personC.setId(3);

        // APPLICATIONs
        final Application applicationA = createApplication(personA);
        applicationA.setId(1);
        applicationA.setApplicationDate(LocalDate.of(2022, APRIL, 17));
        applicationA.setStartDate(LocalDate.of(2022, APRIL, 20));
        applicationA.setEndDate(LocalDate.of(2022, APRIL, 21));

        final Application applicationAA = createApplication(personA);
        applicationAA.setId(4);
        applicationAA.setApplicationDate(LocalDate.of(2021, MAY, 12));
        applicationAA.setStartDate(LocalDate.of(2021, MAY, 16));
        applicationAA.setEndDate(LocalDate.of(2021, MAY, 16));

        final Application applicationB = createApplication(personB);
        applicationB.setId(2);
        applicationB.setApplicationDate(LocalDate.of(2023, DECEMBER, 12));
        applicationB.setStartDate(LocalDate.of(2023, DECEMBER, 24));
        applicationB.setEndDate(LocalDate.of(2023, DECEMBER, 31));

        final Application applicationC = createApplication(personC);
        applicationC.setId(3);
        applicationC.setApplicationDate(LocalDate.of(2021, NOVEMBER, 13));
        applicationC.setStartDate(LocalDate.of(2021, NOVEMBER, 30));
        applicationC.setEndDate(LocalDate.of(2021, NOVEMBER, 30));

        // DEPARTMENT HEADs
        final Person boss = new Person("boss", "Boss", "Hugo", "boss@example.org");
        boss.setId(1);
        boss.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_WAITING_REMINDER));
        final Person departmentHeadA = new Person("headAC", "Wurst", "Heinz", "headAC@example.org");
        departmentHeadA.setId(2);
        departmentHeadA.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_WAITING_REMINDER));
        final Person departmentHeadB = new Person("headB", "Mustermann", "Michel", "headB@example.org");
        departmentHeadB.setId(3);
        departmentHeadB.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_WAITING_REMINDER));

        when(mailRecipientService.getRecipientsOfInterest(personA, NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_WAITING_REMINDER)).thenReturn(asList(boss, departmentHeadA));
        when(mailRecipientService.getRecipientsOfInterest(personB, NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_WAITING_REMINDER)).thenReturn(asList(boss, departmentHeadB));
        when(mailRecipientService.getRecipientsOfInterest(personC, NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_WAITING_REMINDER)).thenReturn(asList(boss, departmentHeadA));

        sut.sendRemindForWaitingApplicationsReminderNotification(asList(applicationAA, applicationA, applicationB, applicationC));

        // were all emails sent?
        final MimeMessage[] bossInbox = greenMail.getReceivedMessagesForDomain(boss.getEmail());
        assertThat(bossInbox.length).isOne();

        final MimeMessage[] departmentHeadAInbox = greenMail.getReceivedMessagesForDomain(departmentHeadA.getEmail());
        assertThat(departmentHeadAInbox.length).isOne();

        final MimeMessage[] departmentHeadBInbox = greenMail.getReceivedMessagesForDomain(departmentHeadB.getEmail());
        assertThat(departmentHeadBInbox.length).isOne();

        // get email boss
        final Message msgBoss = bossInbox[0];
        assertThat(msgBoss.getSubject()).isEqualTo("Erinnerung für 4 wartende zu genehmigende Abwesenheiten");
        assertThat(new InternetAddress(boss.getEmail())).isEqualTo(msgBoss.getAllRecipients()[0]);

        // check content of boss email
        assertThat(msgBoss.getContent()).isEqualTo("Hallo Hugo Boss," + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "die folgenden 4 Abwesenheiten warten auf deine Bearbeitung:" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Anträge von Max Mahler:" + EMAIL_LINE_BREAK +
            "  Erholungsurlaub vom 20.04.2022 bis 21.04.2022, ganztägig. https://localhost:8080/web/application/1" + EMAIL_LINE_BREAK +
            "  Erholungsurlaub vom 16.05.2021 bis 16.05.2021, ganztägig. https://localhost:8080/web/application/4" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Antrag von Frederik Förster:" + EMAIL_LINE_BREAK +
            "  Erholungsurlaub vom 24.12.2023 bis 31.12.2023, ganztägig. https://localhost:8080/web/application/2" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Antrag von Peter Schuster:" + EMAIL_LINE_BREAK +
            "  Erholungsurlaub vom 30.11.2021 bis 30.11.2021, ganztägig. https://localhost:8080/web/application/3" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Überblick aller wartenden Abwesenheitsanträge findest du unter https://localhost:8080/web/application#waiting-requests" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen."
        );

        // get email department head A
        final Message msgDepartmentHeadA = departmentHeadAInbox[0];
        assertThat(msgDepartmentHeadA.getSubject()).isEqualTo("Erinnerung für 3 wartende zu genehmigende Abwesenheiten");
        assertThat(new InternetAddress(departmentHeadA.getEmail())).isEqualTo(msgDepartmentHeadA.getAllRecipients()[0]);

        // check content of boss email
        assertThat(msgDepartmentHeadA.getContent()).isEqualTo("Hallo Heinz Wurst," + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "die folgenden 3 Abwesenheiten warten auf deine Bearbeitung:" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Anträge von Max Mahler:" + EMAIL_LINE_BREAK +
            "  Erholungsurlaub vom 20.04.2022 bis 21.04.2022, ganztägig. https://localhost:8080/web/application/1" + EMAIL_LINE_BREAK +
            "  Erholungsurlaub vom 16.05.2021 bis 16.05.2021, ganztägig. https://localhost:8080/web/application/4" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Antrag von Peter Schuster:" + EMAIL_LINE_BREAK +
            "  Erholungsurlaub vom 30.11.2021 bis 30.11.2021, ganztägig. https://localhost:8080/web/application/3" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Überblick aller wartenden Abwesenheitsanträge findest du unter https://localhost:8080/web/application#waiting-requests" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/2/notifications anpassen."
        );

        // get email department head A
        final Message msgDepartmentHeadB = departmentHeadBInbox[0];
        assertThat(msgDepartmentHeadB.getSubject()).isEqualTo("Erinnerung für eine wartende zu genehmigende Abwesenheit");
        assertThat(new InternetAddress(departmentHeadB.getEmail())).isEqualTo(msgDepartmentHeadB.getAllRecipients()[0]);

        // check content of boss email
        assertThat(msgDepartmentHeadB.getContent()).isEqualTo("Hallo Michel Mustermann," + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "die folgende Abwesenheit wartet auf deine Bearbeitung:" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Antrag von Frederik Förster:" + EMAIL_LINE_BREAK +
            "  Erholungsurlaub vom 24.12.2023 bis 31.12.2023, ganztägig. https://localhost:8080/web/application/2" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Überblick aller wartenden Abwesenheitsanträge findest du unter https://localhost:8080/web/application#waiting-requests" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/3/notifications anpassen."
        );
    }

    @Test
    void sendEditedApplicationNotification() throws Exception {

        final Person editor = new Person("editor", "Muster", "Max", "mustermann@example.org");
        editor.setId(1);
        editor.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_EDITED));

        final Application application = createApplication(editor);
        application.setPerson(editor);

        final Person relevantPerson = new Person("relevantPerson", "Relevant", "Person", "relevantPerson@example.org");
        relevantPerson.setId(2);
        relevantPerson.setPermissions(List.of(BOSS));
        relevantPerson.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_EDITED));
        when(mailRecipientService.getRecipientsOfInterest(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_EDITED)).thenReturn(List.of(relevantPerson));

        sut.sendEditedNotification(application, editor);

        // check editor email
        final MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(editor.getEmail());
        assertThat(inbox.length).isOne();
        final Message msg = inbox[0];
        assertThat(msg.getSubject()).isEqualTo("Zu genehmigende Abwesenheit von Max Muster wurde erfolgreich bearbeitet");
        assertThat(new InternetAddress(editor.getEmail())).isEqualTo(msg.getAllRecipients()[0]);
        assertThat(msg.getContent()).isEqualTo(
            "Hallo Max Muster," + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "die Abwesenheit von Max Muster wurde bearbeitet." + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "    https://localhost:8080/web/application/1234" + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK + EMAIL_LINE_BREAK +
                "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen."
        );

        // check relevant person email
        final MimeMessage[] inboxRelevantPerson = greenMail.getReceivedMessagesForDomain(relevantPerson.getEmail());
        assertThat(inboxRelevantPerson.length).isOne();
        final Message msgRelevantPerson = inboxRelevantPerson[0];
        assertThat(msgRelevantPerson.getSubject()).isEqualTo("Zu genehmigende Abwesenheit von Max Muster wurde erfolgreich bearbeitet");
        assertThat(new InternetAddress(relevantPerson.getEmail())).isEqualTo(msgRelevantPerson.getAllRecipients()[0]);
        assertThat(msgRelevantPerson.getContent()).isEqualTo(
            "Hallo Person Relevant," + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "die Abwesenheit von Max Muster wurde bearbeitet." + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "    https://localhost:8080/web/application/1234" + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK + EMAIL_LINE_BREAK +
                "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/2/notifications anpassen."
        );
    }

    @Test
    void ensurePersonGetsANotificationForUpcomingApplicationToday() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setId(1);
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_UPCOMING));
        final Person holidayReplacement = new Person("holidayReplacement", "holiday", "replacement", "holidayreplacement@example.org");

        final Application application = createApplication(person);
        application.setStartDate(LocalDate.now(clock));
        application.setEndDate(LocalDate.now(clock));

        final HolidayReplacementEntity holidayReplacementEntity = new HolidayReplacementEntity();
        holidayReplacementEntity.setPerson(holidayReplacement);
        holidayReplacementEntity.setNote("Some notes");
        application.setHolidayReplacements(List.of(holidayReplacementEntity));

        sut.sendRemindForUpcomingApplicationsReminderNotification(List.of(application));

        // was email sent?
        MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(person.getEmail());
        assertThat(inbox.length).isOne();

        Message msg = inbox[0];
        assertThat(msg.getSubject()).contains("Erinnerung an deine Abwesenheit");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        assertThat(msg.getContent()).isEqualTo(
            "Hallo Lieschen Müller," + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "heute beginnt deine Abwesenheit" + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "    https://localhost:8080/web/application/1234" + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "und du wirst vertreten durch:" + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "- replacement holiday" + EMAIL_LINE_BREAK +
                "  \"Some notes\"" + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "Da du vom 01.01.2022 bis zum 01.01.2022 nicht anwesend bist, denke bitte an die Übergabe." + EMAIL_LINE_BREAK +
                "Dazu gehören z.B. Abwesenheitsnotiz, E-Mail- & Telefon-Weiterleitung, Zeiterfassung, etc." + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen."
        );
    }

    @Test
    void ensurePersonGetsANotificationForUpcomingApplication() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setId(1);
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_UPCOMING));
        final Person holidayReplacement = new Person("holidayReplacement", "holiday", "replacement", "holidayreplacement@example.org");

        final Application application = createApplication(person);
        application.setStartDate(LocalDate.of(2022, Month.JANUARY, 2));
        application.setEndDate(LocalDate.of(2022, Month.JANUARY, 2));

        final HolidayReplacementEntity holidayReplacementEntity = new HolidayReplacementEntity();
        holidayReplacementEntity.setPerson(holidayReplacement);
        holidayReplacementEntity.setNote("Some notes");
        application.setHolidayReplacements(List.of(holidayReplacementEntity));

        sut.sendRemindForUpcomingApplicationsReminderNotification(List.of(application));

        // was email sent?
        MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(person.getEmail());
        assertThat(inbox.length).isOne();

        Message msg = inbox[0];
        assertThat(msg.getSubject()).contains("Erinnerung an deine Abwesenheit");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        assertThat(msg.getContent()).isEqualTo(
            "Hallo Lieschen Müller," + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "morgen beginnt deine Abwesenheit" + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "    https://localhost:8080/web/application/1234" + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "und du wirst vertreten durch:" + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "- replacement holiday" + EMAIL_LINE_BREAK +
                "  \"Some notes\"" + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "Da du vom 02.01.2022 bis zum 02.01.2022 nicht anwesend bist, denke bitte an die Übergabe." + EMAIL_LINE_BREAK +
                "Dazu gehören z.B. Abwesenheitsnotiz, E-Mail- & Telefon-Weiterleitung, Zeiterfassung, etc." + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen."
        );
    }

    @Test
    void ensurePersonGetsANotificationForUpcomingApplicationMoreThanOneDay() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setId(1);
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_UPCOMING));
        final Person holidayReplacement = new Person("holidayReplacement", "holiday", "replacement", "holidayreplacement@example.org");

        final Application application = createApplication(person);
        application.setStartDate(LocalDate.of(2022, Month.JANUARY, 3));
        application.setEndDate(LocalDate.of(2022, Month.JANUARY, 3));
        final HolidayReplacementEntity holidayReplacementEntity = new HolidayReplacementEntity();
        holidayReplacementEntity.setPerson(holidayReplacement);
        holidayReplacementEntity.setNote("Some notes");
        application.setHolidayReplacements(List.of(holidayReplacementEntity));

        sut.sendRemindForUpcomingApplicationsReminderNotification(List.of(application));

        // was email sent?
        MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(person.getEmail());
        assertThat(inbox.length).isOne();

        Message msg = inbox[0];
        assertThat(msg.getSubject()).contains("Erinnerung an deine Abwesenheit");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        assertThat(msg.getContent()).isEqualTo(
            "Hallo Lieschen Müller," + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "in 2 Tagen beginnt deine Abwesenheit" + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "    https://localhost:8080/web/application/1234" + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "und du wirst vertreten durch:" + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "- replacement holiday" + EMAIL_LINE_BREAK +
                "  \"Some notes\"" + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "Da du vom 03.01.2022 bis zum 03.01.2022 nicht anwesend bist, denke bitte an die Übergabe." + EMAIL_LINE_BREAK +
                "Dazu gehören z.B. Abwesenheitsnotiz, E-Mail- & Telefon-Weiterleitung, Zeiterfassung, etc." + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen."
        );
    }

    @Test
    void ensurePersonGetsANotificationForUpcomingApplicationWithoutReplacementToday() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setId(1);
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_UPCOMING));
        final Application application = createApplication(person);
        application.setStartDate(LocalDate.now(clock));
        application.setEndDate(LocalDate.now(clock));

        sut.sendRemindForUpcomingApplicationsReminderNotification(List.of(application));

        // was email sent?
        MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(person.getEmail());
        assertThat(inbox.length).isOne();

        Message msg = inbox[0];
        assertThat(msg.getSubject()).contains("Erinnerung an deine Abwesenheit");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        assertThat(msg.getContent()).isEqualTo(
            "Hallo Lieschen Müller," + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "heute beginnt deine Abwesenheit" + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "    https://localhost:8080/web/application/1234" + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "Da du vom 01.01.2022 bis zum 01.01.2022 nicht anwesend bist, denke bitte an die Übergabe." + EMAIL_LINE_BREAK +
                "Dazu gehören z.B. Abwesenheitsnotiz, E-Mail- & Telefon-Weiterleitung, Zeiterfassung, etc." + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen."
        );
    }

    @Test
    void ensurePersonGetsANotificationForUpcomingApplicationWithoutReplacementTomorrow() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setId(1);
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_UPCOMING));
        final Application application = createApplication(person);
        application.setStartDate(LocalDate.now(clock).plusDays(1));
        application.setEndDate(LocalDate.now(clock).plusDays(1));

        sut.sendRemindForUpcomingApplicationsReminderNotification(List.of(application));

        // was email sent?
        MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(person.getEmail());
        assertThat(inbox.length).isOne();

        Message msg = inbox[0];
        assertThat(msg.getSubject()).contains("Erinnerung an deine Abwesenheit");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        assertThat(msg.getContent()).isEqualTo(
            "Hallo Lieschen Müller," + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "morgen beginnt deine Abwesenheit" + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "    https://localhost:8080/web/application/1234" + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "Da du vom 02.01.2022 bis zum 02.01.2022 nicht anwesend bist, denke bitte an die Übergabe." + EMAIL_LINE_BREAK +
                "Dazu gehören z.B. Abwesenheitsnotiz, E-Mail- & Telefon-Weiterleitung, Zeiterfassung, etc." + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen."
        );
    }

    @Test
    void ensurePersonGetsANotificationForUpcomingApplicationWithoutReplacement() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setId(1);
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_UPCOMING));
        final Application application = createApplication(person);
        application.setStartDate(LocalDate.of(2022, Month.JANUARY, 31));
        application.setEndDate(LocalDate.of(2022, Month.JANUARY, 31));

        sut.sendRemindForUpcomingApplicationsReminderNotification(List.of(application));

        // was email sent?
        MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(person.getEmail());
        assertThat(inbox.length).isOne();

        Message msg = inbox[0];
        assertThat(msg.getSubject()).contains("Erinnerung an deine Abwesenheit");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        assertThat(msg.getContent()).isEqualTo(
            "Hallo Lieschen Müller," + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "in 30 Tagen beginnt deine Abwesenheit" + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "    https://localhost:8080/web/application/1234" + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "Da du vom 31.01.2022 bis zum 31.01.2022 nicht anwesend bist, denke bitte an die Übergabe." + EMAIL_LINE_BREAK +
                "Dazu gehören z.B. Abwesenheitsnotiz, E-Mail- & Telefon-Weiterleitung, Zeiterfassung, etc." + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen."
        );
    }

    @Test
    void ensurePersonGetsANotificationForUpcomingApplicationWithoutReplacementWithMoreThanOneDay() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setId(1);
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_UPCOMING));
        final Application application = createApplication(person);
        application.setStartDate(LocalDate.of(2022, Month.JANUARY, 31));
        application.setEndDate(LocalDate.of(2022, Month.JANUARY, 31));

        sut.sendRemindForUpcomingApplicationsReminderNotification(List.of(application));

        // was email sent?
        MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(person.getEmail());
        assertThat(inbox.length).isOne();

        Message msg = inbox[0];
        assertThat(msg.getSubject()).contains("Erinnerung an deine Abwesenheit");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        assertThat(msg.getContent()).isEqualTo(
            "Hallo Lieschen Müller," + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "in 30 Tagen beginnt deine Abwesenheit" + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "    https://localhost:8080/web/application/1234" + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "Da du vom 31.01.2022 bis zum 31.01.2022 nicht anwesend bist, denke bitte an die Übergabe." + EMAIL_LINE_BREAK +
                "Dazu gehören z.B. Abwesenheitsnotiz, E-Mail- & Telefon-Weiterleitung, Zeiterfassung, etc." + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen."
        );
    }

    @Test
    void ensurePersonGetsANotificationForUpcomingApplicationWithOneReplacementWithoutNote() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setId(1);
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_UPCOMING));
        final Person holidayReplacement = new Person("pennyworth", "Pennyworth", "Alfred", "pennyworth@example.org");

        final Application application = createApplication(person);
        application.setStartDate(LocalDate.of(2022, Month.JANUARY, 2));
        application.setEndDate(LocalDate.of(2022, Month.JANUARY, 2));

        final HolidayReplacementEntity holidayReplacementEntity = new HolidayReplacementEntity();
        holidayReplacementEntity.setPerson(holidayReplacement);
        application.setHolidayReplacements(List.of(holidayReplacementEntity));

        sut.sendRemindForUpcomingApplicationsReminderNotification(List.of(application));

        // was email sent?
        MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(person.getEmail());
        assertThat(inbox.length).isOne();

        Message msg = inbox[0];
        assertThat(msg.getSubject()).contains("Erinnerung an deine Abwesenheit");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        assertThat(msg.getContent()).isEqualTo(
            "Hallo Lieschen Müller," + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "morgen beginnt deine Abwesenheit" + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "    https://localhost:8080/web/application/1234" + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "und du wirst vertreten durch:" + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "- Alfred Pennyworth" + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "Da du vom 02.01.2022 bis zum 02.01.2022 nicht anwesend bist, denke bitte an die Übergabe." + EMAIL_LINE_BREAK +
                "Dazu gehören z.B. Abwesenheitsnotiz, E-Mail- & Telefon-Weiterleitung, Zeiterfassung, etc." + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen."
        );
    }

    @Test
    void ensurePersonGetsANotificationForUpcomingApplicationWithOneReplacementWithNote() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setId(1);
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_UPCOMING));
        final Person holidayReplacement = new Person("pennyworth", "Pennyworth", "Alfred", "pennyworth@example.org");

        final Application application = createApplication(person);
        application.setStartDate(LocalDate.of(2022, Month.JANUARY, 2));
        application.setEndDate(LocalDate.of(2022, Month.JANUARY, 2));

        final HolidayReplacementEntity holidayReplacementEntity = new HolidayReplacementEntity();
        holidayReplacementEntity.setPerson(holidayReplacement);
        holidayReplacementEntity.setNote("Hey Alfred, denke bitte an Pinguin, danke dir!");

        application.setHolidayReplacements(List.of(holidayReplacementEntity));

        sut.sendRemindForUpcomingApplicationsReminderNotification(List.of(application));

        // was email sent?
        MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(person.getEmail());
        assertThat(inbox.length).isOne();

        Message msg = inbox[0];
        assertThat(msg.getSubject()).contains("Erinnerung an deine Abwesenheit");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        assertThat(msg.getContent()).isEqualTo(
            "Hallo Lieschen Müller," + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "morgen beginnt deine Abwesenheit" + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "    https://localhost:8080/web/application/1234" + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "und du wirst vertreten durch:" + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "- Alfred Pennyworth" + EMAIL_LINE_BREAK +
                "  \"Hey Alfred, denke bitte an Pinguin, danke dir!\"" + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "Da du vom 02.01.2022 bis zum 02.01.2022 nicht anwesend bist, denke bitte an die Übergabe." + EMAIL_LINE_BREAK +
                "Dazu gehören z.B. Abwesenheitsnotiz, E-Mail- & Telefon-Weiterleitung, Zeiterfassung, etc." + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen."
        );
    }

    @Test
    void ensurePersonGetsANotificationForUpcomingApplicationWithMultipleReplacementsWithoutNote() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setId(1);
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_UPCOMING));
        final Person holidayReplacementOne = new Person("pennyworth", "Pennyworth", "Alfred", "pennyworth@example.org");
        final Person holidayReplacementTwo = new Person("rob", "", "Robin", "robin@example.org");

        final Application application = createApplication(person);
        application.setStartDate(LocalDate.of(2022, Month.JANUARY, 2));
        application.setEndDate(LocalDate.of(2022, Month.JANUARY, 2));

        final HolidayReplacementEntity holidayReplacementOneEntity = new HolidayReplacementEntity();
        holidayReplacementOneEntity.setPerson(holidayReplacementOne);
        holidayReplacementOneEntity.setNote("Hey Alfred, denke bitte an Pinguin, danke dir!");

        final HolidayReplacementEntity holidayReplacementTwoEntity = new HolidayReplacementEntity();
        holidayReplacementTwoEntity.setPerson(holidayReplacementTwo);
        holidayReplacementTwoEntity.setNote("Uffbasse Rob. Ich sehe dich.");

        application.setHolidayReplacements(List.of(holidayReplacementOneEntity, holidayReplacementTwoEntity));

        sut.sendRemindForUpcomingApplicationsReminderNotification(List.of(application));

        // was email sent?
        MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(person.getEmail());
        assertThat(inbox.length).isOne();

        Message msg = inbox[0];
        assertThat(msg.getSubject()).contains("Erinnerung an deine Abwesenheit");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        assertThat(msg.getContent()).isEqualTo(
            "Hallo Lieschen Müller," + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "morgen beginnt deine Abwesenheit" + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "    https://localhost:8080/web/application/1234" + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "und du wirst vertreten durch:" + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "- Alfred Pennyworth" + EMAIL_LINE_BREAK +
                "  \"Hey Alfred, denke bitte an Pinguin, danke dir!\"" + EMAIL_LINE_BREAK +
                "- Robin" + EMAIL_LINE_BREAK +
                "  \"Uffbasse Rob. Ich sehe dich.\"" + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "Da du vom 02.01.2022 bis zum 02.01.2022 nicht anwesend bist, denke bitte an die Übergabe." + EMAIL_LINE_BREAK +
                "Dazu gehören z.B. Abwesenheitsnotiz, E-Mail- & Telefon-Weiterleitung, Zeiterfassung, etc." + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen."
        );
    }

    @Test
    void ensurePersonGetsANotificationForUpcomingApplicationWithMultipleReplacementsWithNote() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setId(1);
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_UPCOMING));
        final Person holidayReplacementOne = new Person("pennyworth", "Pennyworth", "Alfred", "pennyworth@example.org");
        final Person holidayReplacementTwo = new Person("rob", "", "Robin", "robin@example.org");

        final Application application = createApplication(person);
        application.setStartDate(LocalDate.of(2022, Month.JANUARY, 2));
        application.setEndDate(LocalDate.of(2022, Month.JANUARY, 2));

        final HolidayReplacementEntity holidayReplacementOneEntity = new HolidayReplacementEntity();
        holidayReplacementOneEntity.setPerson(holidayReplacementOne);

        final HolidayReplacementEntity holidayReplacementTwoEntity = new HolidayReplacementEntity();
        holidayReplacementTwoEntity.setPerson(holidayReplacementTwo);

        application.setHolidayReplacements(List.of(holidayReplacementOneEntity, holidayReplacementTwoEntity));

        sut.sendRemindForUpcomingApplicationsReminderNotification(List.of(application));

        // was email sent?
        MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(person.getEmail());
        assertThat(inbox.length).isOne();

        Message msg = inbox[0];
        assertThat(msg.getSubject()).contains("Erinnerung an deine Abwesenheit");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        assertThat(msg.getContent()).isEqualTo(
            "Hallo Lieschen Müller," + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "morgen beginnt deine Abwesenheit" + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "    https://localhost:8080/web/application/1234" + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "und du wirst vertreten durch:" + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "- Alfred Pennyworth" + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "- Robin" + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "Da du vom 02.01.2022 bis zum 02.01.2022 nicht anwesend bist, denke bitte an die Übergabe." + EMAIL_LINE_BREAK +
                "Dazu gehören z.B. Abwesenheitsnotiz, E-Mail- & Telefon-Weiterleitung, Zeiterfassung, etc." + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen."
        );
    }

    @Test
    void ensurePersonGetsANotificationForUpcomingApplicationWithReplacementWithoutNoteWithMoreThanOneDay() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setId(1);
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_UPCOMING));
        final Person holidayReplacement = new Person("holidayReplacement", "holiday", "replacement", "holidayreplacement@example.org");

        final Application application = createApplication(person);
        application.setStartDate(LocalDate.of(2022, Month.JANUARY, 31));
        application.setEndDate(LocalDate.of(2022, Month.JANUARY, 31));

        final HolidayReplacementEntity holidayReplacementEntity = new HolidayReplacementEntity();
        holidayReplacementEntity.setPerson(holidayReplacement);
        application.setHolidayReplacements(List.of(holidayReplacementEntity));

        sut.sendRemindForUpcomingApplicationsReminderNotification(List.of(application));

        // was email sent?
        MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(person.getEmail());
        assertThat(inbox.length).isOne();

        Message msg = inbox[0];
        assertThat(msg.getSubject()).contains("Erinnerung an deine Abwesenheit");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        assertThat(msg.getContent()).isEqualTo(
            "Hallo Lieschen Müller," + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "in 30 Tagen beginnt deine Abwesenheit" + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "    https://localhost:8080/web/application/1234" + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "und du wirst vertreten durch:" + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "- replacement holiday" + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "Da du vom 31.01.2022 bis zum 31.01.2022 nicht anwesend bist, denke bitte an die Übergabe." + EMAIL_LINE_BREAK +
                "Dazu gehören z.B. Abwesenheitsnotiz, E-Mail- & Telefon-Weiterleitung, Zeiterfassung, etc." + EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                EMAIL_LINE_BREAK +
                "Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen."
        );
    }

    @Test
    void ensurePersonGetsANotificationForUpcomingHolidayReplacementToday() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        final Person holidayReplacement = new Person("holidayReplacement", "holiday", "replacement", "holidayreplacement@example.org");
        holidayReplacement.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT_UPCOMING));

        final Application application = createApplication(person);
        application.setStartDate(LocalDate.now(clock));
        application.setEndDate(LocalDate.now(clock));

        final HolidayReplacementEntity holidayReplacementEntity = new HolidayReplacementEntity();
        holidayReplacementEntity.setPerson(holidayReplacement);
        holidayReplacementEntity.setNote("Some notes");
        application.setHolidayReplacements(List.of(holidayReplacementEntity));

        sut.sendRemindForUpcomingHolidayReplacement(List.of(application));

        // was email sent?
        MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(holidayReplacement.getEmail());
        assertThat(inbox.length).isOne();

        Message msg = inbox[0];
        assertThat(msg.getSubject()).contains("Erinnerung an deine bevorstehende Vertretung für Lieschen Müller");
        assertThat(new InternetAddress(holidayReplacement.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        String content = (String) msg.getContent();
        assertThat(content).contains("Hallo replacement holiday");
        assertThat(content).contains("deine Vertretung für Lieschen Müller vom 01.01.2022 bis zum 01.01.2022 beginnt heute.");
        assertThat(content).contains("Notiz:");
        assertThat(content).contains("Some notes");
        assertThat(content).contains("Einen Überblick deiner aktuellen und zukünftigen Vertretungen findest du unter");
        assertThat(content).contains("/web/application/replacement");
    }

    @Test
    void ensurePersonGetsANotificationForUpcomingHolidayReplacement() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        final Person holidayReplacement = new Person("holidayReplacement", "holiday", "replacement", "holidayreplacement@example.org");
        holidayReplacement.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT_UPCOMING));

        final Application application = createApplication(person);
        application.setStartDate(LocalDate.of(2022, Month.JANUARY, 2));
        application.setEndDate(LocalDate.of(2022, Month.JANUARY, 2));

        final HolidayReplacementEntity holidayReplacementEntity = new HolidayReplacementEntity();
        holidayReplacementEntity.setPerson(holidayReplacement);
        holidayReplacementEntity.setNote("Some notes");
        application.setHolidayReplacements(List.of(holidayReplacementEntity));

        sut.sendRemindForUpcomingHolidayReplacement(List.of(application));

        // was email sent?
        MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(holidayReplacement.getEmail());
        assertThat(inbox.length).isOne();

        Message msg = inbox[0];
        assertThat(msg.getSubject()).contains("Erinnerung an deine bevorstehende Vertretung für Lieschen Müller");
        assertThat(new InternetAddress(holidayReplacement.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        String content = (String) msg.getContent();
        assertThat(content).contains("Hallo replacement holiday");
        assertThat(content).contains("deine Vertretung für Lieschen Müller vom 02.01.2022 bis zum 02.01.2022 beginnt morgen.");
        assertThat(content).contains("Notiz:");
        assertThat(content).contains("Some notes");
        assertThat(content).contains("Einen Überblick deiner aktuellen und zukünftigen Vertretungen findest du unter");
        assertThat(content).contains("/web/application/replacement");
    }

    @Test
    void ensurePersonGetsANotificationForUpcomingHolidayReplacementMoreThanOneDay() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        final Person holidayReplacement = new Person("holidayReplacement", "holiday", "replacement", "holidayreplacement@example.org");
        holidayReplacement.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT_UPCOMING));

        final Application application = createApplication(person);
        application.setStartDate(LocalDate.of(2022, Month.JANUARY, 4));
        application.setEndDate(LocalDate.of(2022, Month.JANUARY, 5));

        final HolidayReplacementEntity holidayReplacementEntity = new HolidayReplacementEntity();
        holidayReplacementEntity.setPerson(holidayReplacement);
        holidayReplacementEntity.setNote("Some notes");
        application.setHolidayReplacements(List.of(holidayReplacementEntity));

        sut.sendRemindForUpcomingHolidayReplacement(List.of(application));

        // was email sent?
        MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(holidayReplacement.getEmail());
        assertThat(inbox.length).isOne();

        Message msg = inbox[0];
        assertThat(msg.getSubject()).contains("Erinnerung an deine bevorstehende Vertretung für Lieschen Müller");
        assertThat(new InternetAddress(holidayReplacement.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        String content = (String) msg.getContent();
        assertThat(content).contains("Hallo replacement holiday");
        assertThat(content).contains("deine Vertretung für Lieschen Müller vom 04.01.2022 bis zum 05.01.2022 beginnt in 3 Tagen.");
        assertThat(content).contains("Notiz:");
        assertThat(content).contains("Some notes");
        assertThat(content).contains("Einen Überblick deiner aktuellen und zukünftigen Vertretungen findest du unter");
        assertThat(content).contains("/web/application/replacement");
    }

    @Test
    void ensurePersonGetsANotificationForUpcomingHolidayReplacementWithoutNote() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        final Person holidayReplacement = new Person("holidayReplacement", "holiday", "replacement", "holidayreplacement@example.org");
        holidayReplacement.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT_UPCOMING));

        final Application application = createApplication(person);
        application.setStartDate(LocalDate.of(2022, Month.JANUARY, 2));
        application.setEndDate(LocalDate.of(2022, Month.JANUARY, 2));

        final HolidayReplacementEntity holidayReplacementEntity = new HolidayReplacementEntity();
        holidayReplacementEntity.setPerson(holidayReplacement);
        holidayReplacementEntity.setNote("");
        application.setHolidayReplacements(List.of(holidayReplacementEntity));

        sut.sendRemindForUpcomingHolidayReplacement(List.of(application));

        // was email sent?
        MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(holidayReplacement.getEmail());
        assertThat(inbox.length).isOne();

        Message msg = inbox[0];
        assertThat(msg.getSubject()).contains("Erinnerung an deine bevorstehende Vertretung für Lieschen Müller");
        assertThat(new InternetAddress(holidayReplacement.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        String content = (String) msg.getContent();
        assertThat(content).contains("Hallo replacement holiday");
        assertThat(content).contains("deine Vertretung für Lieschen Müller vom 02.01.2022 bis zum 02.01.2022 beginnt morgen.");
        assertThat(content).doesNotContain("Notiz:");
        assertThat(content).contains("Einen Überblick deiner aktuellen und zukünftigen Vertretungen findest du unter");
        assertThat(content).contains("/web/application/replacement");
    }

    private Application createApplication(Person person) {

        final LocalDate now = LocalDate.now(UTC);

        final Application application = new Application();
        application.setId(1234);
        application.setPerson(person);
        application.setVacationType(createVacationTypeEntity(HOLIDAY, "application.data.vacationType.holiday"));
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
