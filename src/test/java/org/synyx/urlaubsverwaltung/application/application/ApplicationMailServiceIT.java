package org.synyx.urlaubsverwaltung.application.application;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.simplejavamail.api.email.AttachmentResource;
import org.simplejavamail.converter.EmailConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.SingleTenantTestContainersBase;
import org.synyx.urlaubsverwaltung.application.comment.ApplicationComment;
import org.synyx.urlaubsverwaltung.application.comment.ApplicationCommentAction;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.mail.MailProperties;
import org.synyx.urlaubsverwaltung.mail.MailRecipientService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

import static java.time.Month.APRIL;
import static java.time.Month.DECEMBER;
import static java.time.Month.FEBRUARY;
import static java.time.Month.MAY;
import static java.time.Month.NOVEMBER;
import static java.time.ZoneOffset.UTC;
import static java.util.Arrays.asList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createVacationType;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.WAITING;
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
class ApplicationMailServiceIT extends SingleTenantTestContainersBase {

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
    @Autowired
    private MessageSource messageSource;

    @MockitoBean
    private MailRecipientService mailRecipientService;
    @MockitoBean
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
        person.setId(3L);
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_ALLOWED));

        final Person office = personService.create("office", "Marlene", "Muster", "office@example.org", List.of(NOTIFICATION_EMAIL_APPLICATION_ALLOWED, NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALLOWED), List.of(OFFICE));

        final Person boss = new Person("boss", "Boss", "Hugo", "boss@example.org");
        boss.setId(2L);
        boss.setPermissions(List.of(BOSS));
        boss.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALLOWED));

        final Application application = createApplication(person);
        application.setApplicationDate(LocalDate.of(2021, APRIL, 12));
        application.setStartDate(LocalDate.of(2021, APRIL, 16));
        application.setEndDate(LocalDate.of(2021, APRIL, 17));
        application.setBoss(boss);

        final ApplicationComment comment = new ApplicationComment(
            1L, Instant.now(clock), application, ApplicationCommentAction.ALLOWED, boss, "OK, Urlaub kann genommen werden");

        when(mailRecipientService.getRecipientsOfInterest(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALLOWED)).thenReturn(List.of(boss, office));

        final Person colleague = new Person("colleague", "Dampf", "Hans", "dampf@example.org");
        colleague.setId(42L);
        when(mailRecipientService.getColleagues(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_COLLEAGUES_ALLOWED)).thenReturn(List.of(colleague));

        sut.sendAllowedNotification(application, comment);

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> {
                assertThat(greenMail.getReceivedMessagesForDomain(office.getEmail())).hasSize(1);
                assertThat(greenMail.getReceivedMessagesForDomain(person.getEmail())).hasSize(1);
                assertThat(greenMail.getReceivedMessagesForDomain(boss.getEmail())).hasSize(1);
                assertThat(greenMail.getReceivedMessagesForDomain(colleague.getEmail())).hasSize(1);
            });

        final MimeMessage[] inboxOffice = greenMail.getReceivedMessagesForDomain(office.getEmail());
        final MimeMessage[] inboxUser = greenMail.getReceivedMessagesForDomain(person.getEmail());
        final MimeMessage[] inboxBoss = greenMail.getReceivedMessagesForDomain(boss.getEmail());
        final MimeMessage[] inboxColleague = greenMail.getReceivedMessagesForDomain(colleague.getEmail());

        // check email user attributes
        final MimeMessage msg = inboxUser[0];
        assertThat(msg.getSubject()).isEqualTo("Deine Abwesenheit wurde genehmigt");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of user email
        final MimeMessage msgUser = inboxUser[0];
        assertThat(msgUser.getSubject()).isEqualTo("Deine Abwesenheit wurde genehmigt");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msgUser.getAllRecipients()[0]);
        assertThat(readPlainContent(msgUser)).isEqualTo("""
            Hallo Lieschen Mueller,

            deine Abwesenheit vom 16.04.2021 bis zum 17.04.2021 wurde von Hugo Boss genehmigt.

                https://localhost:8080/web/application/1234

            Kommentar von Hugo Boss:
            OK, Urlaub kann genommen werden

            Informationen zur Abwesenheit:

                Zeitraum:            16.04.2021 bis 17.04.2021, ganztägig
                Art der Abwesenheit: Erholungsurlaub
                Grund:              \s
                Vertretung:         \s
                Anschrift/Telefon:  \s
                Erstellungsdatum:    12.04.2021


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/3/notifications anpassen.
            """);

        final List<AttachmentResource> attachmentsUser = getAttachments(msgUser);
        assertThat(attachmentsUser.getFirst().getName()).contains("calendar.ics");

        // check email office attributes
        final MimeMessage msgOffice = inboxOffice[0];
        assertThat(msgOffice.getSubject()).isEqualTo("Neue genehmigte Abwesenheit von Lieschen Mueller");
        assertThat(new InternetAddress(office.getEmail())).isEqualTo(msgOffice.getAllRecipients()[0]);
        assertThat(readPlainContent(msgOffice)).isEqualTo("""
            Hallo Marlene Muster,

            folgende Abwesenheit von Lieschen Mueller wurde von Hugo Boss genehmigt.

                https://localhost:8080/web/application/1234

            Kommentar von Hugo Boss:
            OK, Urlaub kann genommen werden

            Informationen zur Abwesenheit:

                Mitarbeiter:         Lieschen Mueller
                Zeitraum:            16.04.2021 bis 17.04.2021, ganztägig
                Art der Abwesenheit: Erholungsurlaub
                Grund:              \s
                Vertretung:         \s
                Anschrift/Telefon:  \s
                Erstellungsdatum:    12.04.2021


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/%s/notifications anpassen.
            """.formatted(office.getId()));

        final List<AttachmentResource> attachmentsOffice = getAttachments(msgOffice);
        assertThat(attachmentsOffice.getFirst().getName()).contains("calendar.ics");

        // check email management attributes
        final MimeMessage msgBoss = inboxBoss[0];
        assertThat(msgBoss.getSubject()).isEqualTo("Neue genehmigte Abwesenheit von Lieschen Mueller");
        assertThat(new InternetAddress(boss.getEmail())).isEqualTo(msgBoss.getAllRecipients()[0]);
        assertThat(readPlainContent(msgBoss)).isEqualTo("""
            Hallo Hugo Boss,

            folgende Abwesenheit von Lieschen Mueller wurde von Hugo Boss genehmigt.

                https://localhost:8080/web/application/1234

            Kommentar von Hugo Boss:
            OK, Urlaub kann genommen werden

            Informationen zur Abwesenheit:

                Mitarbeiter:         Lieschen Mueller
                Zeitraum:            16.04.2021 bis 17.04.2021, ganztägig
                Art der Abwesenheit: Erholungsurlaub
                Grund:              \s
                Vertretung:         \s
                Anschrift/Telefon:  \s
                Erstellungsdatum:    12.04.2021


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/2/notifications anpassen.
            """);

        final List<AttachmentResource> attachmentsBoss = getAttachments(msgBoss);
        assertThat(attachmentsBoss.getFirst().getName()).contains("calendar.ics");

        // check email colleague
        final MimeMessage msgColleague = inboxColleague[0];
        assertThat(msgColleague.getSubject()).isEqualTo("Neue Abwesenheit von Lieschen Mueller");
        assertThat(new InternetAddress(colleague.getEmail())).isEqualTo(msgColleague.getAllRecipients()[0]);
        assertThat(readPlainContent(msgColleague)).isEqualTo("""
            Hallo Hans Dampf,

            eine Abwesenheit von Lieschen Mueller wurde erstellt:

                Zeitraum: 16.04.2021 bis 17.04.2021, ganztägig

            Link zur Abwesenheitsübersicht: https://localhost:8080/web/absences


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/42/notifications anpassen.
            """);

        final List<AttachmentResource> attachmentsColleague = getAttachments(msgColleague);
        assertThat(attachmentsColleague.getFirst().getName()).contains("calendar.ics");
    }

    @Test
    void ensureNotificationAboutAllowedApplicationIsSentToOfficeAndThePersonWithOneReplacement() throws Exception {

        final Person person = new Person("user", "Mueller", "Lieschen", "lieschen@example.org");
        person.setId(1L);
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

        final ApplicationComment comment = new ApplicationComment(
            1L, Instant.now(clock), application, ApplicationCommentAction.ALLOWED, boss, "OK, Urlaub kann genommen werden");

        when(mailRecipientService.getRecipientsOfInterest(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALLOWED)).thenReturn(List.of(office));

        sut.sendAllowedNotification(application, comment);

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> {
                assertThat(greenMail.getReceivedMessagesForDomain(office.getEmail())).hasSize(1);
                assertThat(greenMail.getReceivedMessagesForDomain(person.getEmail())).hasSize(1);
            });

        // were both emails sent?
        final MimeMessage[] inboxOffice = greenMail.getReceivedMessagesForDomain(office.getEmail());
        final MimeMessage[] inboxUser = greenMail.getReceivedMessagesForDomain(person.getEmail());

        // check email user attributes
        final MimeMessage msgUser = inboxUser[0];
        assertThat(msgUser.getSubject()).isEqualTo("Deine Abwesenheit wurde genehmigt");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msgUser.getAllRecipients()[0]);
        assertThat(readPlainContent(msgUser)).isEqualTo("""
            Hallo Lieschen Mueller,

            deine Abwesenheit vom 16.04.2021 wurde von Hugo Boss genehmigt.

                https://localhost:8080/web/application/1234

            Kommentar von Hugo Boss:
            OK, Urlaub kann genommen werden

            Informationen zur Abwesenheit:

                Zeitraum:            16.04.2021, ganztägig
                Art der Abwesenheit: Erholungsurlaub
                Grund:              \s
                Vertretung:          Alfred Pennyworth
                Anschrift/Telefon:  \s
                Erstellungsdatum:    12.04.2021


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen.
            """);

        final List<AttachmentResource> attachmentsUser = getAttachments(msgUser);
        assertThat(attachmentsUser.getFirst().getName()).contains("calendar.ics");

        // check email office attributes
        final MimeMessage msgOffice = inboxOffice[0];
        assertThat(msgOffice.getSubject()).isEqualTo("Neue genehmigte Abwesenheit von Lieschen Mueller");
        assertThat(new InternetAddress(office.getEmail())).isEqualTo(msgOffice.getAllRecipients()[0]);
        assertThat(readPlainContent(msgOffice)).isEqualTo("""
            Hallo Marlene Muster,

            folgende Abwesenheit von Lieschen Mueller wurde von Hugo Boss genehmigt.

                https://localhost:8080/web/application/1234

            Kommentar von Hugo Boss:
            OK, Urlaub kann genommen werden

            Informationen zur Abwesenheit:

                Mitarbeiter:         Lieschen Mueller
                Zeitraum:            16.04.2021, ganztägig
                Art der Abwesenheit: Erholungsurlaub
                Grund:              \s
                Vertretung:          Alfred Pennyworth
                Anschrift/Telefon:  \s
                Erstellungsdatum:    12.04.2021


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/%s/notifications anpassen.
            """.formatted(office.getId()));

        final List<AttachmentResource> attachmentsOffice = getAttachments(msgOffice);
        assertThat(attachmentsOffice.getFirst().getName()).contains("calendar.ics");
    }

    @Test
    void ensureNotificationAboutAllowedApplicationIsSentToOfficeAndThePersonWithMultipleReplacements() throws Exception {

        final Person person = new Person("user", "Mueller", "Lieschen", "lieschen@example.org");
        person.setId(1L);
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

        final ApplicationComment comment = new ApplicationComment(
            1L, Instant.now(clock), application, ApplicationCommentAction.ALLOWED, boss, "OK, Urlaub kann genommen werden");

        when(mailRecipientService.getRecipientsOfInterest(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALLOWED)).thenReturn(List.of(office));

        sut.sendAllowedNotification(application, comment);

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> {
                assertThat(greenMail.getReceivedMessagesForDomain(office.getEmail())).hasSize(1);
                assertThat(greenMail.getReceivedMessagesForDomain(person.getEmail())).hasSize(1);
            });

        // were both emails sent?
        final MimeMessage[] inboxOffice = greenMail.getReceivedMessagesForDomain(office.getEmail());
        final MimeMessage[] inboxUser = greenMail.getReceivedMessagesForDomain(person.getEmail());

        // check content of user email
        final MimeMessage msgUser = inboxUser[0];
        assertThat(msgUser.getSubject()).isEqualTo("Deine Abwesenheit wurde genehmigt");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msgUser.getAllRecipients()[0]);
        assertThat(readPlainContent(msgUser)).isEqualTo("""
            Hallo Lieschen Mueller,

            deine Abwesenheit vom 16.04.2021 wurde von Hugo Boss genehmigt.

                https://localhost:8080/web/application/1234

            Kommentar von Hugo Boss:
            OK, Urlaub kann genommen werden

            Informationen zur Abwesenheit:

                Zeitraum:            16.04.2021, ganztägig
                Art der Abwesenheit: Erholungsurlaub
                Grund:              \s
                Vertretung:          Alfred Pennyworth, Robin
                Anschrift/Telefon:  \s
                Erstellungsdatum:    12.04.2021


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen.
            """);

        final List<AttachmentResource> attachmentsUser = getAttachments(msgUser);
        assertThat(attachmentsUser.getFirst().getName()).contains("calendar.ics");

        // check email office attributes
        final MimeMessage msgOffice = inboxOffice[0];
        assertThat(msgOffice.getSubject()).isEqualTo("Neue genehmigte Abwesenheit von Lieschen Mueller");
        assertThat(new InternetAddress(office.getEmail())).isEqualTo(msgOffice.getAllRecipients()[0]);
        assertThat(readPlainContent(msgOffice)).isEqualTo("""
            Hallo Marlene Muster,

            folgende Abwesenheit von Lieschen Mueller wurde von Hugo Boss genehmigt.

                https://localhost:8080/web/application/1234

            Kommentar von Hugo Boss:
            OK, Urlaub kann genommen werden

            Informationen zur Abwesenheit:

                Mitarbeiter:         Lieschen Mueller
                Zeitraum:            16.04.2021, ganztägig
                Art der Abwesenheit: Erholungsurlaub
                Grund:              \s
                Vertretung:          Alfred Pennyworth, Robin
                Anschrift/Telefon:  \s
                Erstellungsdatum:    12.04.2021


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/%s/notifications anpassen.
            """.formatted(office.getId()));

        final List<AttachmentResource> attachmentsOffice = getAttachments(msgOffice);
        assertThat(attachmentsOffice.getFirst().getName()).contains("calendar.ics");
    }

    @Test
    void ensureNotificationAboutRejectedApplicationIsSentToApplierAndRelevantPersons() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setId(1L);
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_REJECTED));

        final Person boss = new Person("boss", "Boss", "Hugo", "boss@example.org");
        boss.setId(2L);
        boss.setPermissions(List.of(BOSS));
        boss.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_REJECTED));

        final Application application = createApplication(person);
        application.setStartDate(LocalDate.of(2023, 5, 22));
        application.setEndDate(LocalDate.of(2023, 5, 22));
        application.setApplicationDate(LocalDate.of(2023, 5, 22));
        application.setBoss(boss);

        final ApplicationComment comment = new ApplicationComment(
            1L, Instant.now(clock), application, ApplicationCommentAction.REJECTED, boss, "Geht leider nicht zu dem Zeitraum");

        final Person departmentHead = new Person("departmentHead", "Head", "Department", "dh@example.org");
        departmentHead.setId(3L);
        departmentHead.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_REJECTED));
        when(mailRecipientService.getRecipientsOfInterest(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_REJECTED)).thenReturn(List.of(boss, departmentHead));

        sut.sendRejectedNotification(application, comment);

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> {
                assertThat(greenMail.getReceivedMessagesForDomain(person.getEmail())).hasSize(1);
                assertThat(greenMail.getReceivedMessagesForDomain(boss.getEmail())).hasSize(1);
                assertThat(greenMail.getReceivedMessagesForDomain(departmentHead.getEmail())).hasSize(1);
            });

        // was email sent?
        MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(person.getEmail());

        // check content of user email
        Message msg = inbox[0];
        assertThat(msg.getSubject()).isEqualTo("Deine zu genehmigende Abwesenheit wurde abgelehnt");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        assertThat(readPlainContent(msg)).isEqualTo("""
            Hallo Lieschen Müller,

            dein am 22.05.2023 gestellte Abwesenheit wurde leider von Hugo Boss abgelehnt.

                https://localhost:8080/web/application/1234

            Begründung:
            Geht leider nicht zu dem Zeitraum


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen.""");

        // was email sent to boss
        MimeMessage[] inboxBoss = greenMail.getReceivedMessagesForDomain(boss.getEmail());
        Message msgBoss = inboxBoss[0];
        assertThat(msgBoss.getSubject()).isEqualTo("Eine zu genehmigende Abwesenheit wurde abgelehnt");
        assertThat(readPlainContent(msgBoss)).isEqualTo("""
            Hallo Hugo Boss,

            der von Lieschen Müller am 22.05.2023 gestellte Antrag wurde von Hugo Boss abgelehnt.

                https://localhost:8080/web/application/1234

            Begründung:
            Geht leider nicht zu dem Zeitraum


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/2/notifications anpassen.""");

        // was email sent to departmentHead
        MimeMessage[] inboxDepartmentHead = greenMail.getReceivedMessagesForDomain(departmentHead.getEmail());
        Message msgDepartmentHead = inboxDepartmentHead[0];
        assertThat(msgDepartmentHead.getSubject()).isEqualTo("Eine zu genehmigende Abwesenheit wurde abgelehnt");
        assertThat(readPlainContent(msgDepartmentHead)).isEqualTo("""
            Hallo Department Head,

            der von Lieschen Müller am 22.05.2023 gestellte Antrag wurde von Hugo Boss abgelehnt.

                https://localhost:8080/web/application/1234

            Begründung:
            Geht leider nicht zu dem Zeitraum


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/3/notifications anpassen.""");
    }

    @Test
    void ensureCorrectReferMail() throws MessagingException, IOException {

        final Person recipient = new Person("recipient", "Muster", "Max", "mustermann@example.org");
        recipient.setId(1L);
        final Person sender = new Person("sender", "Grimes", "Rick", "rick@grimes.com");

        final Application application = createApplication(recipient);
        application.setApplicationDate(LocalDate.of(2022, 5, 19));
        application.setStartDate(LocalDate.of(2022, 5, 20));
        application.setEndDate(LocalDate.of(2022, 5, 29));

        sut.sendReferredToManagementNotification(application, recipient, sender);

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain(recipient.getEmail())).hasSize(1));

        final MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(recipient.getEmail());

        // check content of user email
        final Message msg = inbox[0];
        assertThat(msg.getSubject()).isEqualTo("Hilfe bei der Entscheidung über eine zu genehmigende Abwesenheit");
        assertThat(new InternetAddress(recipient.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        assertThat(readPlainContent(msg)).isEqualTo("""
            Hallo Max Muster,

            Rick Grimes bittet dich um Hilfe bei der Bearbeitung eines Antrags von Max Muster.
            Bitte kümmere dich um die Bearbeitung dieses Antrags oder halte ggf. nochmals Rücksprache mit Rick Grimes.

                https://localhost:8080/web/application/1234

            Informationen zur Abwesenheit:

                Mitarbeiter:         Max Muster
                Zeitraum:            20.05.2022 bis 29.05.2022, ganztägig
                Art der Abwesenheit: Erholungsurlaub
                Grund:              \s
                Vertretung:         \s
                Anschrift/Telefon:  \s
                Erstellungsdatum:    19.05.2022
                Weitergeleitet von:  Rick Grimes


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen.""");
    }

    @Test
    void sendDeclinedCancellationRequestApplicationNotification() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setId(10L);
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_CANCELLATION));

        final Person office = personService.create("office", "Marlene", "Muster", "office@example.org", List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION_REQUESTED), List.of(OFFICE));

        final Application application = createApplication(person);
        application.setStatus(ApplicationStatus.ALLOWED);
        application.setStartDate(LocalDate.of(2020, 5, 29));
        application.setEndDate(LocalDate.of(2020, 5, 29));

        final ApplicationComment comment = new ApplicationComment(
            1L, Instant.now(clock), application, ApplicationCommentAction.CANCEL_REQUESTED_DECLINED, office, "Stornierung abgelehnt!");

        final Person relevantPerson = new Person("relevant", "Person", "Relevant", "relevantperson@example.org");
        relevantPerson.setId(1337L);
        relevantPerson.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION));
        when(mailRecipientService.getRecipientsOfInterest(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION_REQUESTED)).thenReturn(List.of(relevantPerson, office));

        sut.sendDeclinedCancellationRequestApplicationNotification(application, comment);

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> {
                assertThat(greenMail.getReceivedMessagesForDomain(person.getEmail())).hasSize(1);
                assertThat(greenMail.getReceivedMessagesForDomain(office.getEmail())).hasSize(1);
                assertThat(greenMail.getReceivedMessagesForDomain(relevantPerson.getEmail())).hasSize(1);
            });

        MimeMessage[] inboxPerson = greenMail.getReceivedMessagesForDomain(person.getEmail());
        Message msgPerson = inboxPerson[0];
        assertThat(msgPerson.getSubject()).contains("Stornierungsantrag abgelehnt");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msgPerson.getAllRecipients()[0]);
        assertThat(readPlainContent(msgPerson)).isEqualTo("""
            Hallo Lieschen Müller,

            dein Stornierungsantrag der genehmigten Abwesenheit vom 29.05.2020 wurde abgelehnt.

                https://localhost:8080/web/application/1234

            Kommentar von Marlene Muster:
            Stornierung abgelehnt!


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/%s/notifications anpassen.""".formatted(person.getId()));

        // send mail to office
        final MimeMessage[] inboxOffice = greenMail.getReceivedMessagesForDomain(office.getEmail());
        final Message msg = inboxOffice[0];
        assertThat(msg.getSubject()).contains("Stornierungsantrag abgelehnt");
        assertThat(new InternetAddress(office.getEmail())).isEqualTo(msg.getAllRecipients()[0]);
        assertThat(readPlainContent(msg)).isEqualTo("""
            Hallo Marlene Muster,

            der Stornierungsantrag von Lieschen Müller der genehmigten Abwesenheit vom 29.05.2020 wurde abgelehnt.

                https://localhost:8080/web/application/1234

            Kommentar von Marlene Muster:
            Stornierung abgelehnt!


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/%s/notifications anpassen.""".formatted(office.getId()));

        // was email sent to relevant person
        MimeMessage[] inboxRelevantPerson = greenMail.getReceivedMessagesForDomain(relevantPerson.getEmail());
        Message msgRelevantPerson = inboxRelevantPerson[0];
        assertThat(msgRelevantPerson.getSubject()).isEqualTo("Stornierungsantrag abgelehnt");
        assertThat(new InternetAddress(relevantPerson.getEmail())).isEqualTo(msgRelevantPerson.getAllRecipients()[0]);
        assertThat(readPlainContent(msgRelevantPerson)).isEqualTo("""
            Hallo Relevant Person,

            der Stornierungsantrag von Lieschen Müller der genehmigten Abwesenheit vom 29.05.2020 wurde abgelehnt.

                https://localhost:8080/web/application/1234

            Kommentar von Marlene Muster:
            Stornierung abgelehnt!


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1337/notifications anpassen.""");
    }

    @Test
    void ensureApplicantAndOfficeGetsMailAboutCancellationRequest() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setId(1L);
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_CANCELLATION));

        final Person office = personService.create("office", "Marlene", "Muster", "office@example.org", List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION_REQUESTED), List.of(OFFICE));

        final Application application = createApplication(person);
        application.setStartDate(LocalDate.of(2020, 5, 29));
        application.setEndDate(LocalDate.of(2020, 5, 29));

        final ApplicationComment comment = new ApplicationComment(
            1L, Instant.now(clock), application, ApplicationCommentAction.CANCEL_REQUESTED, person, "Bitte stornieren!");

        when(mailRecipientService.getRecipientsOfInterest(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION_REQUESTED)).thenReturn(List.of(office));

        sut.sendCancellationRequest(application, comment);

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> {
                assertThat(greenMail.getReceivedMessagesForDomain(office.getEmail())).hasSize(1);
                assertThat(greenMail.getReceivedMessagesForDomain(person.getEmail())).hasSize(1);
            });

        MimeMessage[] inboxPerson = greenMail.getReceivedMessagesForDomain(person.getEmail());
        final Message msgPerson = inboxPerson[0];
        assertThat(msgPerson.getSubject()).contains("Stornierung wurde beantragt");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msgPerson.getAllRecipients()[0]);
        assertThat(readPlainContent(msgPerson)).isEqualTo("""
            Hallo Lieschen Müller,

            dein Antrag zum Stornieren deines bereits genehmigten Antrags vom 29.05.2020 wurde eingereicht.

                https://localhost:8080/web/application/1234

            Kommentar von Lieschen Müller:
            Bitte stornieren!


            Überblick deiner offenen Stornierungsanträge findest du unter https://localhost:8080/web/application#cancellation-requests


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen.""");

        // send mail to all relevant persons?
        final MimeMessage[] inboxOffice = greenMail.getReceivedMessagesForDomain(office.getEmail());
        final Message msgOffice = inboxOffice[0];
        assertThat(msgOffice.getSubject()).isEqualTo("Ein Benutzer beantragt die Stornierung einer genehmigten Abwesenheit");
        assertThat(new InternetAddress(office.getEmail())).isEqualTo(msgOffice.getAllRecipients()[0]);
        assertThat(readPlainContent(msgOffice)).isEqualTo("""
            Hallo Marlene Muster,

            Lieschen Müller möchte die bereits genehmigte Abwesenheit vom 29.05.2020 stornieren.

                https://localhost:8080/web/application/1234

            Kommentar von Lieschen Müller:
            Bitte stornieren!


            Überblick aller offenen Stornierungsanträge findest du unter https://localhost:8080/web/application#cancellation-requests


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/%s/notifications anpassen.""".formatted(office.getId()));
    }

    @Test
    void ensurePersonGetsMailIfApplicationForLeaveHasBeenConvertedToSickNote() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setId(1L);
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_CONVERTED));

        final Person office = new Person("office", "Muster", "Marlene", "office@example.org");
        office.setPermissions(List.of(OFFICE));

        final Application application = createApplication(person);
        application.setApplier(office);
        application.setStartDate(LocalDate.of(2023, FEBRUARY, 2));
        application.setEndDate(LocalDate.of(2023, FEBRUARY, 4));

        final Person relevantPerson = new Person("relevantPerson", "Relevant", "Person", "relevantPerson@example.org");
        relevantPerson.setId(2L);
        relevantPerson.setPermissions(List.of(BOSS));
        relevantPerson.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CONVERTED));
        when(mailRecipientService.getRecipientsOfInterest(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CONVERTED)).thenReturn(List.of(relevantPerson));

        sut.sendSickNoteConvertedToVacationNotification(application);

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> {
                assertThat(greenMail.getReceivedMessagesForDomain(person.getEmail())).hasSize(1);
                assertThat(greenMail.getReceivedMessagesForDomain(relevantPerson.getEmail())).hasSize(1);
            });

        final MimeMessage[] inboxApplicant = greenMail.getReceivedMessagesForDomain(person.getEmail());
        final Message msgApplicant = inboxApplicant[0];
        assertThat(msgApplicant.getSubject()).contains("Deine Krankmeldung wurde in eine Abwesenheit umgewandelt");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msgApplicant.getAllRecipients()[0]);
        assertThat(readPlainContent(msgApplicant)).isEqualTo("""
            Hallo Lieschen Müller,

            Marlene Muster hat deine Krankmeldung vom 02.02.2023 bis 04.02.2023 zu Urlaub umgewandelt.

                https://localhost:8080/web/application/1234


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen.""");

        // Was email sent to management
        final MimeMessage[] inboxManagement = greenMail.getReceivedMessagesForDomain(relevantPerson.getEmail());
        final Message msgManagement = inboxManagement[0];
        assertThat(msgManagement.getSubject()).contains("Die Krankmeldung von Lieschen Müller wurde in eine Abwesenheit umgewandelt");
        assertThat(new InternetAddress(relevantPerson.getEmail())).isEqualTo(msgManagement.getAllRecipients()[0]);
        assertThat(readPlainContent(msgManagement)).isEqualTo("""
            Hallo Person Relevant,

            Marlene Muster hat die Krankmeldung von Lieschen Müller vom 02.02.2023 bis 04.02.2023 zu Urlaub umgewandelt.

                https://localhost:8080/web/application/1234


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/2/notifications anpassen.""");
    }

    @Test
    void ensureNotificationAboutConfirmationAllowedDirectlySent() throws Exception {

        final Person person = new Person("user", "Mueller", "Lieschen", "lieschen@example.org");
        person.setId(1L);
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_ALLOWED));

        final Application application = createApplication(person);
        application.setApplicationDate(LocalDate.of(2021, APRIL, 12));
        application.setStartDate(LocalDate.of(2021, APRIL, 16));
        application.setEndDate(LocalDate.of(2021, APRIL, 16));

        final ApplicationComment comment = new ApplicationComment(
            1L, Instant.now(clock), application, ApplicationCommentAction.ALLOWED, person, "OK, Urlaub kann genommen werden");

        final Person colleague = new Person("colleague", "colleague", "colleague", "colleague@example.org");
        colleague.setId(2L);
        colleague.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_COLLEAGUES_ALLOWED));
        when(mailRecipientService.getColleagues(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_COLLEAGUES_ALLOWED)).thenReturn(List.of(colleague));

        sut.sendConfirmationAllowedDirectly(application, comment);

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> {
                assertThat(greenMail.getReceivedMessagesForDomain(person.getEmail())).hasSize(1);
                assertThat(greenMail.getReceivedMessagesForDomain(colleague.getEmail())).hasSize(1);
            });

        // email sent to applicant
        final MimeMessage[] inboxUser = greenMail.getReceivedMessagesForDomain(person.getEmail());
        final Message contentUser = inboxUser[0];
        assertThat(contentUser.getSubject()).isEqualTo("Deine Abwesenheit wurde erstellt");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(contentUser.getAllRecipients()[0]);
        assertThat(readPlainContent(contentUser)).isEqualTo("""
            Hallo Lieschen Mueller,

            dein Abwesenheitsantrag wurde erfolgreich erstellt.

                https://localhost:8080/web/application/1234

            Kommentar von Lieschen Mueller:
            OK, Urlaub kann genommen werden

            Informationen zur Abwesenheit:

                Zeitraum:            16.04.2021, ganztägig
                Art der Abwesenheit: Erholungsurlaub
                Grund:              \s
                Vertretung:         \s
                Anschrift/Telefon:  \s
                Erstellungsdatum:    12.04.2021


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen.""");

        // email sent to colleague
        final MimeMessage[] inboxColleague = greenMail.getReceivedMessagesForDomain(colleague.getEmail());
        final Message contentColleague = inboxColleague[0];
        assertThat(contentColleague.getSubject()).isEqualTo("Neue Abwesenheit von Lieschen Mueller");
        assertThat(new InternetAddress(colleague.getEmail())).isEqualTo(contentColleague.getAllRecipients()[0]);
        assertThat(readPlainContent(contentColleague)).isEqualTo("""
            Hallo colleague colleague,

            eine Abwesenheit von Lieschen Mueller wurde erstellt:

                Zeitraum: 16.04.2021, ganztägig

            Link zur Abwesenheitsübersicht: https://localhost:8080/web/absences


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/2/notifications anpassen.""");
    }

    @Test
    void ensureConfirmationAllowedDirectlyByOfficeSent() throws Exception {

        final Person person = new Person("user", "Mueller", "Lieschen", "lieschen@example.org");
        person.setId(1L);
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_ALLOWED));

        final Person office = personService.create("office", "Marlene", "Muster", "office@example.org", List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION), List.of(OFFICE));

        final Application application = createApplication(person);
        application.setApplier(office);
        application.setApplicationDate(LocalDate.of(2021, APRIL, 12));
        application.setStartDate(LocalDate.of(2021, APRIL, 16));
        application.setEndDate(LocalDate.of(2021, APRIL, 16));

        final ApplicationComment comment = new ApplicationComment(
            1L, Instant.now(clock), application, ApplicationCommentAction.ALLOWED, person, "OK, Urlaub kann genommen werden");

        final Person colleague = new Person("colleague", "colleague", "colleague", "colleague@example.org");
        colleague.setId(2L);
        colleague.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_COLLEAGUES_ALLOWED));
        when(mailRecipientService.getColleagues(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_COLLEAGUES_ALLOWED)).thenReturn(List.of(colleague));

        sut.sendConfirmationAllowedDirectlyByManagement(application, comment);

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> {
                assertThat(greenMail.getReceivedMessagesForDomain(person.getEmail())).hasSize(1);
                assertThat(greenMail.getReceivedMessagesForDomain(colleague.getEmail())).hasSize(1);
            });

        final MimeMessage[] inboxUser = greenMail.getReceivedMessagesForDomain(person.getEmail());
        final Message contentUser = inboxUser[0];
        assertThat(contentUser.getSubject()).isEqualTo("Eine Abwesenheit wurde für dich erstellt");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(contentUser.getAllRecipients()[0]);
        assertThat(readPlainContent(contentUser)).isEqualTo("""
            Hallo Lieschen Mueller,

            Marlene Muster hat eine Abwesenheit für dich erstellt.

                https://localhost:8080/web/application/1234

            Kommentar von Lieschen Mueller:
            OK, Urlaub kann genommen werden

            Informationen zur Abwesenheit:

                Mitarbeiter:         Lieschen Mueller
                Zeitraum:            16.04.2021, ganztägig
                Art der Abwesenheit: Erholungsurlaub
                Grund:              \s
                Vertretung:         \s
                Anschrift/Telefon:  \s
                Erstellungsdatum:    12.04.2021


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen.""");

        // email sent to colleague
        final MimeMessage[] inboxColleague = greenMail.getReceivedMessagesForDomain(colleague.getEmail());
        final Message contentColleague = inboxColleague[0];
        assertThat(contentColleague.getSubject()).isEqualTo("Neue Abwesenheit von Lieschen Mueller");
        assertThat(new InternetAddress(colleague.getEmail())).isEqualTo(contentColleague.getAllRecipients()[0]);
        assertThat(readPlainContent(contentColleague)).isEqualTo("""
            Hallo colleague colleague,

            eine Abwesenheit von Lieschen Mueller wurde erstellt:

                Zeitraum: 16.04.2021, ganztägig

            Link zur Abwesenheitsübersicht: https://localhost:8080/web/absences


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/2/notifications anpassen.""");
    }

    @Test
    void ensureNewDirectlyAllowedApplicationNotificationSent() throws Exception {

        final Person person = new Person("user", "Mueller", "Lieschen", "lieschen@example.org");

        final Application application = createApplication(person);
        application.setApplicationDate(LocalDate.of(2021, APRIL, 12));
        application.setStartDate(LocalDate.of(2021, APRIL, 16));
        application.setEndDate(LocalDate.of(2021, APRIL, 16));

        final Person relevantPerson = new Person("relevant", "Person", "Relevant", "relevantperson@example.org");
        relevantPerson.setId(1L);
        relevantPerson.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALLOWED));
        when(mailRecipientService.getRecipientsOfInterest(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALLOWED)).thenReturn(List.of(relevantPerson));

        final ApplicationComment comment = new ApplicationComment(
            1L, Instant.now(clock), application, ApplicationCommentAction.ALLOWED, person, "OK, Urlaub kann genommen werden");

        sut.sendDirectlyAllowedNotificationToManagement(application, comment);

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain(relevantPerson.getEmail())).hasSize(1));

        // email sent?
        final MimeMessage[] inboxUser = greenMail.getReceivedMessagesForDomain(relevantPerson.getEmail());
        final Message contentRelevantPerson = inboxUser[0];
        assertThat(contentRelevantPerson.getSubject()).isEqualTo("Neue Abwesenheit wurde von Lieschen Mueller erstellt");
        assertThat(new InternetAddress(relevantPerson.getEmail())).isEqualTo(contentRelevantPerson.getAllRecipients()[0]);
        assertThat(readPlainContent(contentRelevantPerson)).isEqualTo("""
            Hallo Relevant Person,

            es wurde eine neue Abwesenheit erstellt (diese muss nicht genehmigt werden).

                https://localhost:8080/web/application/1234

            Kommentar von Lieschen Mueller:
            OK, Urlaub kann genommen werden

            Informationen zur Abwesenheit:

                Mitarbeiter:         Lieschen Mueller
                Zeitraum:            16.04.2021, ganztägig
                Art der Abwesenheit: Erholungsurlaub
                Grund:              \s
                Vertretung:         \s
                Anschrift/Telefon:  \s
                Erstellungsdatum:    12.04.2021


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen.""");
    }

    @Test
    void ensureNotifyHolidayReplacementAboutDirectlyAllowedApplicationSent() throws Exception {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        final Application application = createApplication(person);
        application.setStartDate(LocalDate.of(2020, 12, 18));
        application.setEndDate(LocalDate.of(2020, 12, 18));

        final Person holidayReplacement = new Person("replacement", "Teria", "Mar", "replacement@example.org");
        holidayReplacement.setId(1L);
        holidayReplacement.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT));
        final HolidayReplacementEntity replacementEntity = new HolidayReplacementEntity();
        replacementEntity.setPerson(holidayReplacement);
        replacementEntity.setNote("Eine Nachricht an die Vertretung");

        application.setHolidayReplacements(List.of(replacementEntity));

        sut.notifyHolidayReplacementAboutDirectlyAllowedApplication(replacementEntity, application);

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain(holidayReplacement.getEmail())).hasSize(1));

        // was email sent?
        final MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(holidayReplacement.getEmail());
        final MimeMessage msg = inbox[0];
        assertThat(msg.getSubject()).contains("Eine Vertretung für Lieschen Müller wurde eingetragen");
        assertThat(new InternetAddress(holidayReplacement.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        assertThat(readPlainContent(msg)).isEqualTo("""
            Hallo Mar Teria,

            eine Abwesenheit von Lieschen Müller wurde erstellt und
            du wurdest für den Zeitraum vom 18.12.2020 ganztägig als Vertretung eingetragen.

            Notiz von Lieschen Müller an dich:
            Eine Nachricht an die Vertretung

            Einen Überblick deiner aktuellen und zukünftigen Vertretungen findest du unter https://localhost:8080/web/application/replacement


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen.
            """);

        final List<AttachmentResource> attachments = getAttachments(msg);
        assertThat(attachments.getFirst().getName()).contains("calendar.ics");
    }

    @Test
    void ensureCorrectHolidayReplacementApplyMailIsSent() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        final Application application = createApplication(person);
        application.setStartDate(LocalDate.of(2020, 12, 18));
        application.setEndDate(LocalDate.of(2020, 12, 18));

        final Person holidayReplacement = new Person("replacement", "Teria", "Mar", "replacement@example.org");
        holidayReplacement.setId(1L);
        holidayReplacement.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT));
        final HolidayReplacementEntity replacementEntity = new HolidayReplacementEntity();
        replacementEntity.setPerson(holidayReplacement);
        replacementEntity.setNote("Eine Nachricht an die Vertretung");

        application.setHolidayReplacements(List.of(replacementEntity));

        sut.notifyHolidayReplacementForApply(replacementEntity, application);

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain(holidayReplacement.getEmail())).hasSize(1));

        // was email sent?
        final MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(holidayReplacement.getEmail());
        final Message msg = inbox[0];
        assertThat(msg.getSubject()).contains("Deine vorläufig geplante Vertretung für Lieschen Müller");
        assertThat(new InternetAddress(holidayReplacement.getEmail())).isEqualTo(msg.getAllRecipients()[0]);
        assertThat(readPlainContent(msg)).isEqualTo("""
            Hallo Mar Teria,

            Lieschen Müller hat dich bei einer Abwesenheit als Vertretung vorgesehen.
            Es handelt sich um den Zeitraum vom 18.12.2020 ganztägig.

            Notiz von Lieschen Müller an dich:
            Eine Nachricht an die Vertretung

            Einen Überblick deiner aktuellen und zukünftigen Vertretungen findest du unter https://localhost:8080/web/application/replacement


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/%s/notifications anpassen.""".formatted(holidayReplacement.getId()));
    }

    @Test
    void ensureCorrectHolidayReplacementAllowMailIsSent() throws Exception {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        final Application application = createApplication(person);
        application.setStartDate(LocalDate.of(2020, 5, 29));
        application.setEndDate(LocalDate.of(2020, 5, 29));

        final Person holidayReplacement = new Person("replacement", "Teria", "Mar", "replacement@example.org");
        holidayReplacement.setId(1L);
        holidayReplacement.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT));
        final HolidayReplacementEntity replacementEntity = new HolidayReplacementEntity();
        replacementEntity.setPerson(holidayReplacement);
        replacementEntity.setNote("Eine Nachricht an die Vertretung");

        application.setHolidayReplacements(List.of(replacementEntity));

        sut.notifyHolidayReplacementAllow(replacementEntity, application);

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain(holidayReplacement.getEmail())).hasSize(1));

        MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(holidayReplacement.getEmail());
        MimeMessage msg = inbox[0];
        assertThat(msg.getSubject()).contains("Deine Vertretung für Lieschen Müller wurde eingeplant");
        assertThat(new InternetAddress(holidayReplacement.getEmail())).isEqualTo(msg.getAllRecipients()[0]);
        assertThat(readPlainContent(msg)).isEqualTo("""
            Hallo Mar Teria,

            die Abwesenheit von Lieschen Müller wurde genehmigt.
            Du wurdest damit für den Zeitraum vom 29.05.2020 ganztägig als Vertretung eingetragen.

            Notiz von Lieschen Müller an dich:
            Eine Nachricht an die Vertretung

            Einen Überblick deiner aktuellen und zukünftigen Vertretungen findest du unter https://localhost:8080/web/application/replacement


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/%s/notifications anpassen.
            """.formatted(holidayReplacement.getId()));

        final List<AttachmentResource> attachments = getAttachments(msg);
        assertThat(attachments.getFirst().getName()).contains("calendar.ics");
    }

    @Test
    void ensureCorrectHolidayReplacementCancellationMailIsSent() throws Exception {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        final Application application = createApplication(person);
        application.setStartDate(LocalDate.of(2020, 12, 18));
        application.setEndDate(LocalDate.of(2020, 12, 18));

        final Person holidayReplacement = new Person("replacement", "Teria", "Mar", "replacement@example.org");
        holidayReplacement.setId(1L);
        holidayReplacement.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT));
        final HolidayReplacementEntity replacementEntity = new HolidayReplacementEntity();
        replacementEntity.setPerson(holidayReplacement);

        application.setHolidayReplacements(List.of(replacementEntity));

        sut.notifyHolidayReplacementAboutCancellation(replacementEntity, application);

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain(holidayReplacement.getEmail())).hasSize(1));

        final MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(holidayReplacement.getEmail());
        final MimeMessage msg = inbox[0];
        assertThat(msg.getSubject()).contains("Deine vorläufig geplante Vertretung für Lieschen Müller wurde zurückgezogen");
        assertThat(new InternetAddress(holidayReplacement.getEmail())).isEqualTo(msg.getAllRecipients()[0]);
        assertThat(readPlainContent(msg)).isEqualTo("""
            Hallo Mar Teria,

            du bist für die Abwesenheit von Lieschen Müller vom 18.12.2020 nicht mehr als Vertretung vorgesehen.

            Einen Überblick deiner aktuellen und zukünftigen Vertretungen findest du unter https://localhost:8080/web/application/replacement


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/%s/notifications anpassen.
            """.formatted(holidayReplacement.getId()));

        final List<AttachmentResource> attachments = getAttachments(msg);
        assertThat(attachments.getFirst().getName()).contains("calendar.ics");
    }

    @Test
    void ensureCorrectHolidayReplacementEditMailIsSentIfStatusIsWaiting() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        final Application application = createApplication(person);
        application.setStatus(WAITING);
        application.setStartDate(LocalDate.of(2020, 12, 18));
        application.setEndDate(LocalDate.of(2020, 12, 18));

        final Person holidayReplacement = new Person("replacement", "Teria", "Mar", "replacement@example.org");
        holidayReplacement.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT));
        final HolidayReplacementEntity replacementEntity = new HolidayReplacementEntity();
        replacementEntity.setPerson(holidayReplacement);
        replacementEntity.setNote("Eine Nachricht an die Vertretung");

        application.setHolidayReplacements(List.of(replacementEntity));

        sut.notifyHolidayReplacementAboutEdit(replacementEntity, application);

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain(holidayReplacement.getEmail())).hasSize(1));

        // was email sent?
        final MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(holidayReplacement.getEmail());
        final Message msg = inbox[0];
        assertThat(msg.getSubject()).contains("Deine vorläufig geplante Vertretung für Lieschen Müller wurde bearbeitet");
        assertThat(new InternetAddress(holidayReplacement.getEmail())).isEqualTo(msg.getAllRecipients()[0]);
        assertThat(readPlainContent(msg)).isEqualTo("""
            Hallo Mar Teria,

            der Zeitraum für die Abwesenheit von Lieschen Müller bei dem du als Vertretung vorgesehen bist, hat sich geändert.
            Der neue Zeitraum ist vom 18.12.2020 bis zum 18.12.2020 ganztägig.

            Notiz von Lieschen Müller an dich:
            Eine Nachricht an die Vertretung

            Einen Überblick deiner aktuellen und zukünftigen Vertretungen findest du unter https://localhost:8080/web/application/replacement


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/null/notifications anpassen.""");
    }

    @Test
    void ensureCorrectHolidayReplacementEditMailIsSentIfStatusIsAllowed() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        final Application application = createApplication(person);
        application.setStatus(ALLOWED);
        application.setStartDate(LocalDate.of(2020, 12, 18));
        application.setEndDate(LocalDate.of(2020, 12, 18));

        final Person holidayReplacement = new Person("replacement", "Teria", "Mar", "replacement@example.org");
        holidayReplacement.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT));
        final HolidayReplacementEntity replacementEntity = new HolidayReplacementEntity();
        replacementEntity.setPerson(holidayReplacement);
        replacementEntity.setNote("Eine Nachricht an die Vertretung");

        application.setHolidayReplacements(List.of(replacementEntity));

        sut.notifyHolidayReplacementAboutEdit(replacementEntity, application);

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain(holidayReplacement.getEmail())).hasSize(1));

        final MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(holidayReplacement.getEmail());
        final Message msg = inbox[0];
        assertThat(msg.getSubject()).contains("Deine geplante Vertretung für Lieschen Müller wurde bearbeitet");
        assertThat(new InternetAddress(holidayReplacement.getEmail())).isEqualTo(msg.getAllRecipients()[0]);
        assertThat(readPlainContent(msg)).isEqualTo("""
            Hallo Mar Teria,

            der Zeitraum für die Abwesenheit von Lieschen Müller bei dem du als Vertretung vorgesehen bist, hat sich geändert.
            Der neue Zeitraum ist vom 18.12.2020 bis zum 18.12.2020 ganztägig.

            Notiz von Lieschen Müller an dich:
            Eine Nachricht an die Vertretung

            Einen Überblick deiner aktuellen und zukünftigen Vertretungen findest du unter https://localhost:8080/web/application/replacement


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/null/notifications anpassen.""");
    }

    @Test
    void ensureToSendAppliedNotificationWhereFromIsNotNull() throws MessagingException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_APPLIED));

        final Application application = createApplication(person);
        final ApplicationComment comment = new ApplicationComment(
            1L, Instant.now(clock), application, ApplicationCommentAction.APPLIED, person, "Hätte gerne Urlaub");

        sut.sendAppliedNotification(application, comment);

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain(person.getEmail())).hasSize(1));

        final MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(person.getEmail());
        final Message msg = inbox[0];
        final Address[] from = msg.getFrom();
        assertThat(from).isNotNull();
        assertThat(from).hasSize(1);
        assertThat(from[0]).hasToString(String.format("%s <%s>", mailProperties.getFromDisplayName(), mailProperties.getFrom()));
    }

    @Test
    void ensureAfterApplyingForLeaveAConfirmationNotificationIsSentToPerson() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_APPLIED));

        final Application application = createApplication(person);
        application.setStartDate(LocalDate.of(2024, 8, 8));
        application.setEndDate(LocalDate.of(2024, 8, 8));
        application.setApplicationDate(LocalDate.of(2024, 8, 8));

        final ApplicationComment comment = new ApplicationComment(
            1L, Instant.now(clock), application, ApplicationCommentAction.APPLIED, person, "Hätte gerne Urlaub");

        sut.sendAppliedNotification(application, comment);

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain(person.getEmail())).hasSize(1));

        final MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(person.getEmail());
        final Message msg = inbox[0];
        assertThat(msg.getSubject()).contains("Antragsstellung");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);
        assertThat(readPlainContent(msg)).isEqualTo("""
            Hallo Lieschen Müller,

            deine Abwesenheit wurde erfolgreich eingereicht.

                https://localhost:8080/web/application/1234

            Kommentar von Lieschen Müller:
            Hätte gerne Urlaub

            Informationen zur Abwesenheit:

                Zeitraum:            08.08.2024, ganztägig
                Art der Abwesenheit: Erholungsurlaub
                Grund:              \s
                Vertretung:         \s
                Anschrift/Telefon:  \s
                Erstellungsdatum:    08.08.2024


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/null/notifications anpassen.""");
    }

    @Test
    void ensureAfterApplyingForLeaveAConfirmationNotificationIsSentToPersonWithOneReplacement() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setId(1L);
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_APPLIED));
        final Person holidayReplacement = new Person("pennyworth", "Pennyworth", "Alfred", "pennyworth@example.org");

        final HolidayReplacementEntity holidayReplacementEntity = new HolidayReplacementEntity();
        holidayReplacementEntity.setPerson(holidayReplacement);

        final Application application = createApplication(person);
        application.setApplicationDate(LocalDate.of(2021, APRIL, 12));
        application.setStartDate(LocalDate.of(2021, APRIL, 16));
        application.setEndDate(LocalDate.of(2021, APRIL, 16));
        application.setHolidayReplacements(List.of(holidayReplacementEntity));

        final ApplicationComment comment = new ApplicationComment(
            1L, Instant.now(clock), application, ApplicationCommentAction.APPLIED, person, "Hätte gerne Urlaub");

        sut.sendAppliedNotification(application, comment);

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain(person.getEmail())).hasSize(1));

        // was email sent?
        final MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(person.getEmail());
        final Message msg = inbox[0];
        assertThat(msg.getSubject()).contains("Antragsstellung");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        assertThat(readPlainContent(msg)).isEqualTo("""
            Hallo Lieschen Müller,

            deine Abwesenheit wurde erfolgreich eingereicht.

                https://localhost:8080/web/application/1234

            Kommentar von Lieschen Müller:
            Hätte gerne Urlaub

            Informationen zur Abwesenheit:

                Zeitraum:            16.04.2021, ganztägig
                Art der Abwesenheit: Erholungsurlaub
                Grund:              \s
                Vertretung:          Alfred Pennyworth
                Anschrift/Telefon:  \s
                Erstellungsdatum:    12.04.2021


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen.""");
    }

    @Test
    void ensureAfterApplyingForLeaveAConfirmationNotificationIsSentToPersonWithMultipleReplacements() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setId(1L);
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

        final ApplicationComment comment = new ApplicationComment(
            1L, Instant.now(clock), application, ApplicationCommentAction.APPLIED, person, "Hätte gerne Urlaub");

        sut.sendAppliedNotification(application, comment);

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain(person.getEmail())).hasSize(1));

        // was email sent?
        final MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(person.getEmail());
        final Message msg = inbox[0];
        assertThat(msg.getSubject()).contains("Antragsstellung");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        assertThat(readPlainContent(msg)).isEqualTo("""
            Hallo Lieschen Müller,

            deine Abwesenheit wurde erfolgreich eingereicht.

                https://localhost:8080/web/application/1234

            Kommentar von Lieschen Müller:
            Hätte gerne Urlaub

            Informationen zur Abwesenheit:

                Zeitraum:            16.04.2021, ganztägig
                Art der Abwesenheit: Erholungsurlaub
                Grund:              \s
                Vertretung:          Alfred Pennyworth, Robin
                Anschrift/Telefon:  \s
                Erstellungsdatum:    12.04.2021


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen.""");
    }

    @Test
    void ensurePersonGetsANotificationIfAnOfficeMemberAppliedForLeaveForThisPerson() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_APPLIED));

        final Application application = createApplication(person);
        application.setStartDate(LocalDate.of(2024, 8, 8));
        application.setEndDate(LocalDate.of(2024, 8, 8));
        application.setApplicationDate(LocalDate.of(2024, 8, 8));

        final ApplicationComment comment = new ApplicationComment(
            1L, Instant.now(clock), application, ApplicationCommentAction.APPLIED, person, "Habe das mal für dich beantragt");

        final Person office = new Person("office", "Muster", "Marlene", "office@example.org");
        office.setPermissions(List.of(OFFICE));
        application.setApplier(office);

        sut.sendAppliedByManagementNotification(application, comment);

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain(person.getEmail())).hasSize(1));

        // was email sent?
        final MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(person.getEmail());
        final Message msg = inbox[0];
        assertThat(msg.getSubject()).isEqualTo("Für dich wurde eine zu genehmigende Abwesenheit eingereicht");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);
        assertThat(readPlainContent(msg)).isEqualTo("""
            Hallo Lieschen Müller,

            Marlene Muster hat eine Abwesenheit für dich gestellt.

                https://localhost:8080/web/application/1234

            Kommentar von Lieschen Müller:
            Habe das mal für dich beantragt

            Informationen zur Abwesenheit:

                Zeitraum:            08.08.2024, ganztägig
                Art der Abwesenheit: Erholungsurlaub
                Grund:              \s
                Vertretung:         \s
                Anschrift/Telefon:  \s
                Erstellungsdatum:    08.08.2024


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/null/notifications anpassen.""");
    }

    @Test
    void ensurePersonGetsANotificationIfAnOfficeMemberAppliedForLeaveForThisPersonWithOneReplacement() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setId(1L);
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_APPLIED));

        final Person holidayReplacement = new Person("pennyworth", "Pennyworth", "Alfred", "pennyworth@example.org");
        final HolidayReplacementEntity holidayReplacementEntity = new HolidayReplacementEntity();
        holidayReplacementEntity.setPerson(holidayReplacement);

        final Application application = createApplication(person);
        application.setApplicationDate(LocalDate.of(2021, APRIL, 12));
        application.setStartDate(LocalDate.of(2021, APRIL, 16));
        application.setEndDate(LocalDate.of(2021, APRIL, 16));
        application.setHolidayReplacements(List.of(holidayReplacementEntity));

        final ApplicationComment comment = new ApplicationComment(
            1L, Instant.now(clock), application, ApplicationCommentAction.APPLIED, person, "Habe das mal für dich beantragt");

        final Person office = new Person("office", "Muster", "Marlene", "office@example.org");
        office.setPermissions(List.of(OFFICE));

        application.setApplier(office);
        sut.sendAppliedByManagementNotification(application, comment);

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain(person.getEmail())).hasSize(1));

        // was email sent?
        final MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(person.getEmail());
        final Message msg = inbox[0];
        assertThat(msg.getSubject()).isEqualTo("Für dich wurde eine zu genehmigende Abwesenheit eingereicht");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        assertThat(readPlainContent(msg)).isEqualTo("""
            Hallo Lieschen Müller,

            Marlene Muster hat eine Abwesenheit für dich gestellt.

                https://localhost:8080/web/application/1234

            Kommentar von Lieschen Müller:
            Habe das mal für dich beantragt

            Informationen zur Abwesenheit:

                Zeitraum:            16.04.2021, ganztägig
                Art der Abwesenheit: Erholungsurlaub
                Grund:              \s
                Vertretung:          Alfred Pennyworth
                Anschrift/Telefon:  \s
                Erstellungsdatum:    12.04.2021


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen.""");
    }

    @Test
    void ensurePersonGetsANotificationIfAnOfficeMemberAppliedForLeaveForThisPersonWithMultipleReplacements() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setId(1L);
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

        final ApplicationComment comment = new ApplicationComment(
            1L, Instant.now(clock), application, ApplicationCommentAction.APPLIED, person, "Habe das mal für dich beantragt");

        final Person office = new Person("office", "Muster", "Marlene", "office@example.org");
        office.setPermissions(List.of(OFFICE));

        application.setApplier(office);
        sut.sendAppliedByManagementNotification(application, comment);

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain(person.getEmail())).hasSize(1));

        final MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(person.getEmail());
        final Message msg = inbox[0];
        assertThat(msg.getSubject()).isEqualTo("Für dich wurde eine zu genehmigende Abwesenheit eingereicht");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        assertThat(readPlainContent(msg)).isEqualTo("""
            Hallo Lieschen Müller,

            Marlene Muster hat eine Abwesenheit für dich gestellt.

                https://localhost:8080/web/application/1234

            Kommentar von Lieschen Müller:
            Habe das mal für dich beantragt

            Informationen zur Abwesenheit:

                Zeitraum:            16.04.2021, ganztägig
                Art der Abwesenheit: Erholungsurlaub
                Grund:              \s
                Vertretung:          Alfred Pennyworth, Robin
                Anschrift/Telefon:  \s
                Erstellungsdatum:    12.04.2021


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen.""");
    }

    @Test
    void ensurePersonAndRelevantPersonsGetsANotificationIfPersonCancelledOneOfHisApplications() throws MessagingException,
        IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_REVOKED));

        final Application application = createApplication(person);
        application.setCanceller(person);
        application.setApplicationDate(LocalDate.of(2024, 8, 8));

        final ApplicationComment comment = new ApplicationComment(
            1L, Instant.now(clock), application, ApplicationCommentAction.REVOKED, person, "Wrong date - revoked");

        final Person relevantPerson = new Person("relevant", "Person", "Relevant", "relevantperson@example.org");
        relevantPerson.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_REVOKED));
        when(mailRecipientService.getRecipientsOfInterest(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_REVOKED)).thenReturn(List.of(relevantPerson));

        sut.sendRevokedNotifications(application, comment);

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> {
                assertThat(greenMail.getReceivedMessagesForDomain(person.getEmail())).hasSize(1);
                assertThat(greenMail.getReceivedMessagesForDomain(relevantPerson.getEmail())).hasSize(1);
            });

        // was email sent to applicant
        final MimeMessage[] inboxApplicant = greenMail.getReceivedMessagesForDomain(person.getEmail());
        final Message msg = inboxApplicant[0];
        assertThat(msg.getSubject()).isEqualTo("Deine Abwesenheit wurde erfolgreich storniert");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);
        assertThat(readPlainContent(msg)).isEqualTo("""
            Hallo Lieschen Müller,

            dein am 08.08.2024 gestellter, nicht genehmigter Antrag wurde von dir erfolgreich storniert.

                https://localhost:8080/web/application/1234

            Begründung:
            Wrong date - revoked


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/null/notifications anpassen.""");

        // was email sent to relevant person
        final MimeMessage[] inboxRelevantPerson = greenMail.getReceivedMessagesForDomain(relevantPerson.getEmail());
        final Message msgRelevantPerson = inboxRelevantPerson[0];
        assertThat(msgRelevantPerson.getSubject()).isEqualTo("Eine nicht genehmigte Abwesenheit wurde erfolgreich storniert");
        assertThat(new InternetAddress(relevantPerson.getEmail())).isEqualTo(msgRelevantPerson.getAllRecipients()[0]);
        assertThat(readPlainContent(msgRelevantPerson)).isEqualTo("""
            Hallo Relevant Person,

            der am 08.08.2024 gestellte, nicht genehmigte Antrag von Lieschen Müller wurde storniert.

                https://localhost:8080/web/application/1234

            Begründung:
            Wrong date - revoked


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/null/notifications anpassen.""");
    }

    @Test
    void ensurePersonAndRelevantPersonsGetsANotificationIfNotApplicantCancelledThisApplication() throws MessagingException,
        IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_REVOKED));

        final Application application = createApplication(person);
        application.setApplicationDate(LocalDate.of(2024, 8, 8));

        final Person office = new Person("office", "Person", "Office", "office@example.org");
        application.setCanceller(office);

        final ApplicationComment comment = new ApplicationComment(
            1L, Instant.now(clock), application, ApplicationCommentAction.REVOKED, office, "Wrong information - revoked");

        final Person relevantPerson = new Person("relevant", "Person", "Relevant", "relevantperson@example.org");
        relevantPerson.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_REVOKED));
        when(mailRecipientService.getRecipientsOfInterest(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_REVOKED)).thenReturn(List.of(relevantPerson));

        sut.sendRevokedNotifications(application, comment);

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> {
                assertThat(greenMail.getReceivedMessagesForDomain(relevantPerson.getEmail())).hasSize(1);
                assertThat(greenMail.getReceivedMessagesForDomain(person.getEmail())).hasSize(1);
            });

        final MimeMessage[] inboxApplicant = greenMail.getReceivedMessagesForDomain(person.getEmail());
        final Message msg = inboxApplicant[0];
        assertThat(msg.getSubject()).isEqualTo("Deine Abwesenheit wurde storniert");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);
        assertThat(readPlainContent(msg)).isEqualTo("""
            Hallo Lieschen Müller,

            dein am 08.08.2024 gestellter, nicht genehmigter Antrag wurde von Office Person storniert.

                https://localhost:8080/web/application/1234

            Begründung:
            Wrong information - revoked


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/null/notifications anpassen.""");

        // was email sent to relevant person
        final MimeMessage[] inboxRelevantPerson = greenMail.getReceivedMessagesForDomain(relevantPerson.getEmail());
        final Message msgRelevantPerson = inboxRelevantPerson[0];
        assertThat(msgRelevantPerson.getSubject()).isEqualTo("Eine nicht genehmigte Abwesenheit wurde erfolgreich storniert");
        assertThat(new InternetAddress(relevantPerson.getEmail())).isEqualTo(msgRelevantPerson.getAllRecipients()[0]);
        assertThat(readPlainContent(msgRelevantPerson)).isEqualTo("""
            Hallo Relevant Person,

            der am 08.08.2024 gestellte, nicht genehmigte Antrag von Lieschen Müller wurde von Office Person storniert.

                https://localhost:8080/web/application/1234

            Begründung:
            Wrong information - revoked


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/null/notifications anpassen.""");
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

        final ApplicationComment comment = new ApplicationComment(
            1L, Instant.now(clock), application, ApplicationCommentAction.CANCELLED, person, "Cancelled");

        final Person recipientOfInterest = new Person("relevant", "Person", "Relevant", "relevantperson@example.org");
        recipientOfInterest.setId(1L);
        recipientOfInterest.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION));
        when(mailRecipientService.getRecipientsOfInterest(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION)).thenReturn(List.of(recipientOfInterest));

        sut.sendCancelledDirectlyToManagement(application, comment);

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain(recipientOfInterest.getEmail())).hasSize(1));

        // was email sent to applicant?
        final MimeMessage[] inboxRecipientOfInterest = greenMail.getReceivedMessagesForDomain(recipientOfInterest.getEmail());
        final Message msg = inboxRecipientOfInterest[0];
        assertThat(msg.getSubject()).isEqualTo("Eine Abwesenheit von Lieschen Müller wurde storniert");
        assertThat(new InternetAddress(recipientOfInterest.getEmail())).isEqualTo(msg.getAllRecipients()[0]);
        assertThat(readPlainContent(msg)).isEqualTo("""
            Hallo Relevant Person,

            die Abwesenheit von Lieschen Müller vom 16.04.2021 wurde von Lieschen Müller storniert.

                https://localhost:8080/web/application/1234

            Kommentar von Lieschen Müller:
            Cancelled

            Informationen zur Abwesenheit:

                Zeitraum:            16.04.2021, ganztägig
                Art der Abwesenheit: Erholungsurlaub
                Grund:              \s
                Vertretung:         \s
                Anschrift/Telefon:  \s
                Erstellungsdatum:    16.04.2021


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen.""");
    }

    @Test
    void ensureApplicantReceivesNotificationsIfApplicantCancel() throws Exception {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setId(1L);
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_CANCELLATION));

        final Application application = createApplication(person);
        application.setApplicationDate(LocalDate.of(2021, APRIL, 16));
        application.setStartDate(LocalDate.of(2021, APRIL, 16));
        application.setEndDate(LocalDate.of(2021, APRIL, 16));
        application.setDayLength(FULL);

        final ApplicationComment comment = new ApplicationComment(
            1L, Instant.now(clock), application, ApplicationCommentAction.CANCELLED, person, "Wrong information - cancelled");

        final Person colleague = new Person("colleague", "colleague", "colleague", "colleague@example.org");
        colleague.setId(2L);
        colleague.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_COLLEAGUES_CANCELLATION));
        when(mailRecipientService.getColleagues(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_COLLEAGUES_CANCELLATION)).thenReturn(List.of(colleague));

        sut.sendCancelledDirectlyConfirmationByApplicant(application, comment);

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> {
                assertThat(greenMail.getReceivedMessagesForDomain(person.getEmail())).hasSize(1);
                assertThat(greenMail.getReceivedMessagesForDomain(colleague.getEmail())).hasSize(1);
            });

        // was email sent to applicant
        final MimeMessage[] inboxApplicant = greenMail.getReceivedMessagesForDomain(person.getEmail());
        final MimeMessage msg = inboxApplicant[0];
        assertThat(msg.getSubject()).isEqualTo("Deine Abwesenheit wurde storniert");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);
        assertThat(readPlainContent(msg)).isEqualTo("""
            Hallo Lieschen Müller,

            deine Abwesenheit vom 16.04.2021 wurde erfolgreich storniert.

                https://localhost:8080/web/application/1234

            Kommentar von Lieschen Müller:
            Wrong information - cancelled

            Informationen zur Abwesenheit:

                Zeitraum:            16.04.2021, ganztägig
                Art der Abwesenheit: Erholungsurlaub
                Grund:              \s
                Vertretung:         \s
                Anschrift/Telefon:  \s
                Erstellungsdatum:    16.04.2021


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen.
            """);

        final List<AttachmentResource> attachmentsRelevantPerson = getAttachments(msg);
        assertThat(attachmentsRelevantPerson.getFirst().getName()).contains("calendar.ics");

        // check email colleague
        final MimeMessage[] inboxColleague = greenMail.getReceivedMessagesForDomain(colleague.getEmail());
        final MimeMessage msgColleague = inboxColleague[0];
        assertThat(msgColleague.getSubject()).isEqualTo("Abwesenheit von Lieschen Müller wurde zurückgenommen");
        assertThat(new InternetAddress(colleague.getEmail())).isEqualTo(msgColleague.getAllRecipients()[0]);
        assertThat(readPlainContent(msgColleague)).isEqualTo("""
            Hallo colleague colleague,

            eine Abwesenheit von Lieschen Müller wurde zurückgenommen:

                Zeitraum: 16.04.2021, ganztägig

            Link zur Abwesenheitsübersicht: https://localhost:8080/web/absences


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/2/notifications anpassen.
            """);

        final List<AttachmentResource> attachmentsColleague = getAttachments(msgColleague);
        assertThat(attachmentsColleague.getFirst().getName()).contains("calendar.ics");
    }

    @Test
    void ensureApplicantReceivesNotificationsIfOfficeCancel() throws MessagingException,
        IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setId(1L);
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_CANCELLATION));

        final Application application = createApplication(person);
        application.setApplicationDate(LocalDate.of(2021, APRIL, 16));
        application.setStartDate(LocalDate.of(2021, APRIL, 16));
        application.setEndDate(LocalDate.of(2021, APRIL, 16));

        final Person office = new Person("office", "Person", "Office", "office@example.org");
        application.setCanceller(office);

        final ApplicationComment comment = new ApplicationComment(
            1L, Instant.now(clock), application, ApplicationCommentAction.CANCELLED, office, "Wrong information - cancelled");

        final Person colleague = new Person("colleague", "colleague", "colleague", "colleague@example.org");
        colleague.setId(2L);
        colleague.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_COLLEAGUES_CANCELLATION));
        when(mailRecipientService.getColleagues(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_COLLEAGUES_CANCELLATION)).thenReturn(List.of(colleague));

        sut.sendCancelledDirectlyConfirmationByManagement(application, comment);

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> {
                assertThat(greenMail.getReceivedMessagesForDomain(person.getEmail())).hasSize(1);
                assertThat(greenMail.getReceivedMessagesForDomain(colleague.getEmail())).hasSize(1);
            });

        // was email sent to applicant
        final MimeMessage[] inboxApplicant = greenMail.getReceivedMessagesForDomain(person.getEmail());
        final Message msg = inboxApplicant[0];
        assertThat(msg.getSubject()).isEqualTo("Eine Abwesenheit wurde für dich storniert");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        assertThat(readPlainContent(msg)).isEqualTo("""
            Hallo Lieschen Müller,

            deine Abwesenheit vom 16.04.2021 wurde von Office Person erfolgreich storniert.

                https://localhost:8080/web/application/1234

            Kommentar von Office Person:
            Wrong information - cancelled

            Informationen zur Abwesenheit:

                Zeitraum:            16.04.2021, ganztägig
                Art der Abwesenheit: Erholungsurlaub
                Grund:              \s
                Vertretung:         \s
                Anschrift/Telefon:  \s
                Erstellungsdatum:    16.04.2021


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen.""");

        // check email colleague
        final MimeMessage[] inboxColleague = greenMail.getReceivedMessagesForDomain(colleague.getEmail());
        final MimeMessage msgColleague = inboxColleague[0];
        assertThat(msgColleague.getSubject()).isEqualTo("Abwesenheit von Lieschen Müller wurde zurückgenommen");
        assertThat(new InternetAddress(colleague.getEmail())).isEqualTo(msgColleague.getAllRecipients()[0]);
        assertThat(readPlainContent(msgColleague)).isEqualTo("""
            Hallo colleague colleague,

            eine Abwesenheit von Lieschen Müller wurde zurückgenommen:

                Zeitraum: 16.04.2021, ganztägig

            Link zur Abwesenheitsübersicht: https://localhost:8080/web/absences


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/2/notifications anpassen.""");
    }

    @Test
    void ensurePersonGetsANotificationIfOfficeCancelledOneOfHisApplications() throws Exception {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setId(1L);
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_CANCELLATION));

        final Person office = new Person("office", "Muster", "Marlene", "office@example.org");
        office.setPermissions(List.of(OFFICE));
        office.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION));

        final Application application = createApplication(person);
        application.setApplicationDate(LocalDate.of(2020, 5, 29));
        application.setStartDate(LocalDate.of(2020, 6, 15));
        application.setEndDate(LocalDate.of(2020, 6, 15));
        application.setCanceller(office);
        application.setDayLength(FULL);

        final ApplicationComment comment = new ApplicationComment(
            1L, Instant.now(clock), application, ApplicationCommentAction.CANCELLED, person, "Geht leider nicht");

        final Person relevantPerson = new Person("relevant", "Person", "Relevant", "relevantperson@example.org");
        relevantPerson.setId(2L);
        relevantPerson.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION));
        when(mailRecipientService.getRecipientsOfInterest(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_CANCELLATION)).thenReturn(List.of(relevantPerson, office));

        final Person colleague = new Person("colleague", "colleague", "colleague", "colleague@example.org");
        colleague.setId(42L);
        when(mailRecipientService.getColleagues(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_COLLEAGUES_CANCELLATION)).thenReturn(List.of(colleague));

        sut.sendCancelledConfirmationByManagement(application, comment);

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> {
                assertThat(greenMail.getReceivedMessagesForDomain(person.getEmail())).hasSize(1);
                assertThat(greenMail.getReceivedMessagesForDomain(relevantPerson.getEmail())).hasSize(1);
                assertThat(greenMail.getReceivedMessagesForDomain(colleague.getEmail())).hasSize(1);
            });

        final MimeMessage[] inboxApplicant = greenMail.getReceivedMessagesForDomain(person.getEmail());
        final MimeMessage msg = inboxApplicant[0];
        assertThat(msg.getSubject()).isEqualTo("Deine zu genehmigende Abwesenheit wurde storniert");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);
        assertThat(readPlainContent(msg)).isEqualTo("""
            Hallo Lieschen Müller,

            Marlene Muster hat einen deine Abwesenheit storniert.

                https://localhost:8080/web/application/1234

            Kommentar von Lieschen Müller:
            Geht leider nicht


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen.
            """);

        final List<AttachmentResource> attachments = getAttachments(msg);
        assertThat(attachments.getFirst().getName()).contains("calendar.ics");

        // was email sent to relevant person?
        final MimeMessage[] inboxRelevantPerson = greenMail.getReceivedMessagesForDomain(relevantPerson.getEmail());
        final MimeMessage msgRelevantPerson = inboxRelevantPerson[0];
        assertThat(msgRelevantPerson.getSubject()).isEqualTo("Eine zu genehmigende Abwesenheit wurde vom Office storniert");
        assertThat(new InternetAddress(relevantPerson.getEmail())).isEqualTo(msgRelevantPerson.getAllRecipients()[0]);
        assertThat(readPlainContent(msgRelevantPerson)).isEqualTo("""
            Hallo Relevant Person,

            Marlene Muster hat die Abwesenheit von Lieschen Müller vom 29.05.2020 storniert.

                https://localhost:8080/web/application/1234

            Kommentar von Lieschen Müller:
            Geht leider nicht


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/2/notifications anpassen.
            """);

        final List<AttachmentResource> attachmentsRelevantPerson = getAttachments(msgRelevantPerson);
        assertThat(attachmentsRelevantPerson.getFirst().getName()).contains("calendar.ics");

        // was email sent to colleague?
        final MimeMessage[] inboxColleague = greenMail.getReceivedMessagesForDomain(colleague.getEmail());
        final MimeMessage msgColleague = inboxColleague[0];
        assertThat(msgColleague.getSubject()).isEqualTo("Abwesenheit von Lieschen Müller wurde zurückgenommen");
        assertThat(new InternetAddress(colleague.getEmail())).isEqualTo(msgColleague.getAllRecipients()[0]);

        assertThat(readPlainContent(msgColleague)).isEqualTo("""
            Hallo colleague colleague,

            eine Abwesenheit von Lieschen Müller wurde zurückgenommen:

                Zeitraum: 15.06.2020, ganztägig

            Link zur Abwesenheitsübersicht: https://localhost:8080/web/absences


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/42/notifications anpassen.
            """);

        final List<AttachmentResource> attachmentsColleague = getAttachments(msgColleague);
        assertThat(attachmentsColleague.getFirst().getName()).contains("calendar.ics");
    }

    @Test
    void ensureNotificationAboutNewApplicationIsSentToBossesAndDepartmentHeads() throws MessagingException, IOException {

        final Person boss = new Person("boss", "Boss", "Hugo", "boss@example.org");
        boss.setId(1L);
        boss.setPermissions(List.of(BOSS));
        boss.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED));

        final Person departmentHead = new Person("departmentHead", "Kopf", "Senior", "head@example.org");
        departmentHead.setId(2L);
        departmentHead.setPermissions(List.of(DEPARTMENT_HEAD));
        departmentHead.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED));

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");

        final Application application = createApplication(person);
        application.setStartDate(LocalDate.of(2023, 5, 22));
        application.setEndDate(LocalDate.of(2023, 5, 22));
        application.setApplicationDate(LocalDate.of(2023, 5, 22));

        final ApplicationComment comment = new ApplicationComment(
            1L, Instant.now(clock), application, ApplicationCommentAction.APPLIED, person, "Hätte gerne Urlaub");

        when(departmentService.getApplicationsFromColleaguesOf(person, application.getStartDate(), application.getEndDate())).thenReturn(List.of(application));
        when(mailRecipientService.getRecipientsOfInterest(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED)).thenReturn(asList(boss, departmentHead));

        sut.sendAppliedNotificationToManagement(application, comment);

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> {
                assertThat(greenMail.getReceivedMessagesForDomain(boss.getEmail())).hasSize(1);
                assertThat(greenMail.getReceivedMessagesForDomain(departmentHead.getEmail())).hasSize(1);
            });

        // was email sent to boss?
        final MimeMessage[] inboxOfBoss = greenMail.getReceivedMessagesForDomain(boss.getEmail());
        final Message msgBoss = inboxOfBoss[0];
        assertThat(msgBoss.getSubject()).isEqualTo("Neue zu genehmigende Abwesenheit für Lieschen Müller eingereicht");
        assertThat(new InternetAddress(boss.getEmail())).isEqualTo(msgBoss.getAllRecipients()[0]);
        assertThat(readPlainContent(msgBoss)).isEqualTo("""
            Hallo Hugo Boss,

            es liegt ein neuer zu genehmigender Antrag vor.

                https://localhost:8080/web/application/1234

            Kommentar von Lieschen Müller:
            Hätte gerne Urlaub

            Informationen zur Abwesenheit:

                Mitarbeiter:         Lieschen Müller
                Zeitraum:            22.05.2023, ganztägig
                Art der Abwesenheit: Erholungsurlaub
                Grund:              \s
                Vertretung:         \s
                Anschrift/Telefon:  \s
                Erstellungsdatum:    22.05.2023

            Überschneidende Abwesenheiten in der Abteilung des Antragsstellers:
               \s
                Lieschen Müller: 22.05.2023, ganztägig
               \s


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen.""");

        // was email sent to department head?
        final MimeMessage[] inboxOfDepartmentHead = greenMail.getReceivedMessagesForDomain(departmentHead.getEmail());
        final Message msgDepartmentHead = inboxOfDepartmentHead[0];
        assertThat(readPlainContent(msgDepartmentHead)).isEqualTo("""
            Hallo Senior Kopf,

            es liegt ein neuer zu genehmigender Antrag vor.

                https://localhost:8080/web/application/1234

            Kommentar von Lieschen Müller:
            Hätte gerne Urlaub

            Informationen zur Abwesenheit:

                Mitarbeiter:         Lieschen Müller
                Zeitraum:            22.05.2023, ganztägig
                Art der Abwesenheit: Erholungsurlaub
                Grund:              \s
                Vertretung:         \s
                Anschrift/Telefon:  \s
                Erstellungsdatum:    22.05.2023

            Überschneidende Abwesenheiten in der Abteilung des Antragsstellers:
               \s
                Lieschen Müller: 22.05.2023, ganztägig
               \s


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/2/notifications anpassen.""");
    }

    @Test
    void ensureNotificationAboutNewApplicationOfSecondStageAuthorityIsSentToBosses() throws MessagingException, IOException {

        final Person boss = new Person("boss", "Boss", "Hugo", "boss@example.org");
        boss.setId(1L);
        boss.setPermissions(List.of(BOSS));
        boss.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED));

        final Person secondStage = new Person("manager", "Schmitt", "Kai", "manager@example.org");
        secondStage.setPermissions(List.of(SECOND_STAGE_AUTHORITY));

        final Person departmentHead = new Person("departmentHead", "Kopf", "Senior", "head@example.org");
        departmentHead.setId(2L);
        departmentHead.setPermissions(List.of(DEPARTMENT_HEAD));
        departmentHead.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED));

        final Application application = createApplication(secondStage);
        application.setStartDate(LocalDate.of(2023, 5, 22));
        application.setEndDate(LocalDate.of(2023, 5, 22));
        application.setApplicationDate(LocalDate.of(2023, 5, 22));

        final ApplicationComment comment = new ApplicationComment(
            1L, Instant.now(clock), application, ApplicationCommentAction.APPLIED, secondStage, "Hätte gerne Urlaub");

        when(departmentService.getApplicationsFromColleaguesOf(secondStage, application.getStartDate(), application.getEndDate())).thenReturn(List.of(application));
        when(mailRecipientService.getRecipientsOfInterest(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED)).thenReturn(asList(boss, departmentHead));

        sut.sendAppliedNotificationToManagement(application, comment);

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> {
                assertThat(greenMail.getReceivedMessagesForDomain(boss.getEmail())).hasSize(1);
                assertThat(greenMail.getReceivedMessagesForDomain(departmentHead.getEmail())).hasSize(1);
            });

        // was email sent to boss?
        final MimeMessage[] inboxOfBoss = greenMail.getReceivedMessagesForDomain(boss.getEmail());
        final Message msgBoss = inboxOfBoss[0];
        assertThat(msgBoss.getSubject()).isEqualTo("Neue zu genehmigende Abwesenheit für Kai Schmitt eingereicht");
        assertThat(new InternetAddress(boss.getEmail())).isEqualTo(msgBoss.getAllRecipients()[0]);
        assertThat(readPlainContent(msgBoss)).isEqualTo("""
            Hallo Hugo Boss,

            es liegt ein neuer zu genehmigender Antrag vor.

                https://localhost:8080/web/application/1234

            Kommentar von Kai Schmitt:
            Hätte gerne Urlaub

            Informationen zur Abwesenheit:

                Mitarbeiter:         Kai Schmitt
                Zeitraum:            22.05.2023, ganztägig
                Art der Abwesenheit: Erholungsurlaub
                Grund:              \s
                Vertretung:         \s
                Anschrift/Telefon:  \s
                Erstellungsdatum:    22.05.2023

            Überschneidende Abwesenheiten in der Abteilung des Antragsstellers:
               \s
                Kai Schmitt: 22.05.2023, ganztägig
               \s


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen.""");

        // no email sent to department head
        final MimeMessage[] inboxOfDepartmentHead = greenMail.getReceivedMessagesForDomain(departmentHead.getEmail());
        final Message msgDepartmentHead = inboxOfDepartmentHead[0];
        assertThat(msgDepartmentHead.getSubject()).isEqualTo("Neue zu genehmigende Abwesenheit für Kai Schmitt eingereicht");
        assertThat(new InternetAddress(departmentHead.getEmail())).isEqualTo(msgDepartmentHead.getAllRecipients()[0]);
        assertThat(readPlainContent(msgDepartmentHead)).isEqualTo("""
            Hallo Senior Kopf,

            es liegt ein neuer zu genehmigender Antrag vor.

                https://localhost:8080/web/application/1234

            Kommentar von Kai Schmitt:
            Hätte gerne Urlaub

            Informationen zur Abwesenheit:

                Mitarbeiter:         Kai Schmitt
                Zeitraum:            22.05.2023, ganztägig
                Art der Abwesenheit: Erholungsurlaub
                Grund:              \s
                Vertretung:         \s
                Anschrift/Telefon:  \s
                Erstellungsdatum:    22.05.2023

            Überschneidende Abwesenheiten in der Abteilung des Antragsstellers:
               \s
                Kai Schmitt: 22.05.2023, ganztägig
               \s


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/2/notifications anpassen.""");
    }

    @Test
    void ensureNotificationAboutNewApplicationOfDepartmentHeadIsSentToSecondaryStageAuthority() throws MessagingException, IOException {

        final Person boss = new Person("boss", "Boss", "Hugo", "boss@example.org");
        boss.setId(1L);
        boss.setPermissions(List.of(BOSS));
        boss.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED));

        final Person secondStage = new Person("manager", "Schmitt", "Kai", "manager@example.org");
        secondStage.setId(2L);
        secondStage.setPermissions(List.of(SECOND_STAGE_AUTHORITY));
        secondStage.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED));

        final Person departmentHead = new Person("departmentHead", "Kopf", "Senior", "head@example.org");
        departmentHead.setPermissions(List.of(DEPARTMENT_HEAD));

        final Application application = createApplication(departmentHead);
        application.setStartDate(LocalDate.of(2023, 5, 22));
        application.setEndDate(LocalDate.of(2023, 5, 22));
        application.setApplicationDate(LocalDate.of(2023, 5, 22));

        final ApplicationComment comment = new ApplicationComment(
            1L, Instant.now(clock), application, ApplicationCommentAction.APPLIED, departmentHead, "Hätte gerne Urlaub");

        when(departmentService.getApplicationsFromColleaguesOf(departmentHead, application.getStartDate(), application.getEndDate())).thenReturn(List.of(application));
        when(mailRecipientService.getRecipientsOfInterest(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED)).thenReturn(asList(boss, secondStage));

        sut.sendAppliedNotificationToManagement(application, comment);

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> {
                assertThat(greenMail.getReceivedMessagesForDomain(boss.getEmail())).hasSize(1);
                assertThat(greenMail.getReceivedMessagesForDomain(secondStage.getEmail())).hasSize(1);
            });

        // was email sent to boss?
        final MimeMessage[] inboxOfBoss = greenMail.getReceivedMessagesForDomain(boss.getEmail());
        final Message msgBoss = inboxOfBoss[0];
        assertThat(readPlainContent(msgBoss)).isEqualTo("""
            Hallo Hugo Boss,

            es liegt ein neuer zu genehmigender Antrag vor.

                https://localhost:8080/web/application/1234

            Kommentar von Senior Kopf:
            Hätte gerne Urlaub

            Informationen zur Abwesenheit:

                Mitarbeiter:         Senior Kopf
                Zeitraum:            22.05.2023, ganztägig
                Art der Abwesenheit: Erholungsurlaub
                Grund:              \s
                Vertretung:         \s
                Anschrift/Telefon:  \s
                Erstellungsdatum:    22.05.2023

            Überschneidende Abwesenheiten in der Abteilung des Antragsstellers:
               \s
                Senior Kopf: 22.05.2023, ganztägig
               \s


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen.""");

        // was email sent to secondary stage?
        final MimeMessage[] inboxOfSecondaryStage = greenMail.getReceivedMessagesForDomain(secondStage.getEmail());
        final Message msgSecondaryStage = inboxOfSecondaryStage[0];
        assertThat(readPlainContent(msgSecondaryStage)).isEqualTo("""
            Hallo Kai Schmitt,

            es liegt ein neuer zu genehmigender Antrag vor.

                https://localhost:8080/web/application/1234

            Kommentar von Senior Kopf:
            Hätte gerne Urlaub

            Informationen zur Abwesenheit:

                Mitarbeiter:         Senior Kopf
                Zeitraum:            22.05.2023, ganztägig
                Art der Abwesenheit: Erholungsurlaub
                Grund:              \s
                Vertretung:         \s
                Anschrift/Telefon:  \s
                Erstellungsdatum:    22.05.2023

            Überschneidende Abwesenheiten in der Abteilung des Antragsstellers:
               \s
                Senior Kopf: 22.05.2023, ganztägig
               \s


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/2/notifications anpassen.""");
    }

    @Test
    void ensureNotificationAboutNewApplicationWithoutOverlappingVacations() throws MessagingException, IOException {

        final Person holidayReplacement = new Person("pennyworth", "Pennyworth", "Alfred", "pennyworth@example.org");

        final Person boss = new Person("boss", "Boss", "Hugo", "boss@example.org");
        boss.setId(1L);
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

        when(departmentService.getApplicationsFromColleaguesOf(person, application.getStartDate(), application.getEndDate())).thenReturn(List.of());
        when(mailRecipientService.getRecipientsOfInterest(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED)).thenReturn(List.of(boss));

        final ApplicationComment comment = new ApplicationComment(
            1L, Instant.now(clock), application, ApplicationCommentAction.APPLIED, person, "");

        sut.sendAppliedNotificationToManagement(application, comment);

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain(boss.getEmail())).hasSize(1));

        final MimeMessage[] messages = greenMail.getReceivedMessagesForDomain(boss.getEmail());
        final Message message = messages[0];
        assertThat(readPlainContent(message)).isEqualTo("""
            Hallo Hugo Boss,

            es liegt ein neuer zu genehmigender Antrag vor.

                https://localhost:8080/web/application/1234

            Informationen zur Abwesenheit:

                Mitarbeiter:         Lieschen Müller
                Zeitraum:            16.04.2021, ganztägig
                Art der Abwesenheit: Erholungsurlaub
                Grund:              \s
                Vertretung:          Alfred Pennyworth
                Anschrift/Telefon:  \s
                Erstellungsdatum:    12.04.2021

            Überschneidende Abwesenheiten in der Abteilung des Antragsstellers:
               \s
                Keine


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen."""
        );
    }

    @Test
    void ensureNotificationAboutNewApplicationWithMultipleOverlappingVacations() throws MessagingException, IOException {

        final Person holidayReplacement = new Person("pennyworth", "Pennyworth", "Alfred", "pennyworth@example.org");

        final Person boss = new Person("boss", "Boss", "Hugo", "boss@example.org");
        boss.setId(1L);
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
        applicationSecond.setEndDate(LocalDate.of(2021, APRIL, 18));

        final HolidayReplacementEntity holidayReplacementEntity = new HolidayReplacementEntity();
        holidayReplacementEntity.setPerson(holidayReplacement);

        application.setHolidayReplacements(List.of(holidayReplacementEntity));

        when(departmentService.getApplicationsFromColleaguesOf(person, application.getStartDate(), application.getEndDate())).thenReturn(List.of(application, applicationSecond));
        when(mailRecipientService.getRecipientsOfInterest(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED)).thenReturn(List.of(boss));

        final ApplicationComment comment = new ApplicationComment(
            1L, Instant.now(clock), application, ApplicationCommentAction.APPLIED, person, "");

        sut.sendAppliedNotificationToManagement(application, comment);

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain(boss.getEmail())).hasSize(1));

        final MimeMessage[] messages = greenMail.getReceivedMessagesForDomain(boss.getEmail());
        final Message message = messages[0];
        assertThat(readPlainContent(message)).isEqualTo("""
            Hallo Hugo Boss,

            es liegt ein neuer zu genehmigender Antrag vor.

                https://localhost:8080/web/application/1234

            Informationen zur Abwesenheit:

                Mitarbeiter:         Lieschen Müller
                Zeitraum:            16.04.2021, ganztägig
                Art der Abwesenheit: Erholungsurlaub
                Grund:              \s
                Vertretung:          Alfred Pennyworth
                Anschrift/Telefon:  \s
                Erstellungsdatum:    12.04.2021

            Überschneidende Abwesenheiten in der Abteilung des Antragsstellers:
               \s
                Lieschen Müller: 16.04.2021, ganztägig
                Lieschen Müller: 17.04.2021 bis 18.04.2021, ganztägig
               \s


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen."""
        );
    }

    @Test
    void ensureNotificationAboutNewApplicationOfDepartmentHeadIsSentWithOneReplacement() throws MessagingException, IOException {

        final Person holidayReplacement = new Person("pennyworth", "Pennyworth", "Alfred", "pennyworth@example.org");

        final Person boss = new Person("boss", "Boss", "Hugo", "boss@example.org");
        boss.setId(1L);
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

        when(departmentService.getApplicationsFromColleaguesOf(person, application.getStartDate(), application.getEndDate())).thenReturn(List.of(application));
        when(mailRecipientService.getRecipientsOfInterest(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED)).thenReturn(List.of(boss));

        final ApplicationComment comment = new ApplicationComment(
            1L, Instant.now(clock), application, ApplicationCommentAction.APPLIED, person, "");

        sut.sendAppliedNotificationToManagement(application, comment);

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain(boss.getEmail())).hasSize(1));

        final MimeMessage[] messages = greenMail.getReceivedMessagesForDomain(boss.getEmail());
        final Message message = messages[0];
        assertThat(readPlainContent(message)).isEqualTo("""
            Hallo Hugo Boss,

            es liegt ein neuer zu genehmigender Antrag vor.

                https://localhost:8080/web/application/1234

            Informationen zur Abwesenheit:

                Mitarbeiter:         Lieschen Müller
                Zeitraum:            16.04.2021, ganztägig
                Art der Abwesenheit: Erholungsurlaub
                Grund:              \s
                Vertretung:          Alfred Pennyworth
                Anschrift/Telefon:  \s
                Erstellungsdatum:    12.04.2021

            Überschneidende Abwesenheiten in der Abteilung des Antragsstellers:
               \s
                Lieschen Müller: 16.04.2021, ganztägig
               \s


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen.""");
    }

    @Test
    void ensureNotificationAboutNewApplicationOfDepartmentHeadIsSentWithMultipleReplacements() throws MessagingException, IOException {

        final Person holidayReplacementOne = new Person("pennyworth", "Pennyworth", "Alfred", "pennyworth@example.org");
        final Person holidayReplacementTwo = new Person("rob", "", "Robin", "robin@example.org");

        final Person boss = new Person("boss", "Boss", "Hugo", "boss@example.org");
        boss.setId(1L);
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

        when(departmentService.getApplicationsFromColleaguesOf(person, application.getStartDate(), application.getEndDate())).thenReturn(List.of(application));
        when(mailRecipientService.getRecipientsOfInterest(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_APPLIED)).thenReturn(List.of(boss));

        final ApplicationComment comment = new ApplicationComment(
            1L, Instant.now(clock), application, ApplicationCommentAction.APPLIED, person, "");

        sut.sendAppliedNotificationToManagement(application, comment);

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain(boss.getEmail())).hasSize(1));

        final MimeMessage[] messages = greenMail.getReceivedMessagesForDomain(boss.getEmail());
        final Message message = messages[0];
        assertThat(readPlainContent(message)).isEqualTo("""
            Hallo Hugo Boss,

            es liegt ein neuer zu genehmigender Antrag vor.

                https://localhost:8080/web/application/1234

            Informationen zur Abwesenheit:

                Mitarbeiter:         Lieschen Müller
                Zeitraum:            16.04.2021, ganztägig
                Art der Abwesenheit: Erholungsurlaub
                Grund:              \s
                Vertretung:          Alfred Pennyworth, Robin
                Anschrift/Telefon:  \s
                Erstellungsdatum:    12.04.2021

            Überschneidende Abwesenheiten in der Abteilung des Antragsstellers:
               \s
                Lieschen Müller: 16.04.2021, ganztägig
               \s


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen.""");
    }

    @Test
    void ensureNotificationAboutTemporaryAllowedApplicationIsSentToSecondStageAuthoritiesAndToPerson()
        throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setId(1L);
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_TEMPORARY_ALLOWED));

        final Person secondStage = new Person("manager", "Schmitt", "Kai", "manager@example.org");
        secondStage.setId(2L);
        secondStage.setPermissions(List.of(SECOND_STAGE_AUTHORITY));
        secondStage.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_TEMPORARY_ALLOWED));

        final Application application = createApplication(person);
        application.setApplicationDate(LocalDate.of(2021, APRIL, 12));
        application.setStartDate(LocalDate.of(2021, APRIL, 16));
        application.setEndDate(LocalDate.of(2021, APRIL, 16));

        final ApplicationComment comment = new ApplicationComment(
            1L, Instant.now(clock), application, ApplicationCommentAction.TEMPORARY_ALLOWED, secondStage, "OK, spricht von meiner Seite aus nix dagegen");

        when(departmentService.getApplicationsFromColleaguesOf(person, application.getStartDate(), application.getEndDate())).thenReturn(List.of(application));
        when(mailRecipientService.getRecipientsOfInterest(person, NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_TEMPORARY_ALLOWED)).thenReturn(List.of(secondStage));

        sut.sendTemporaryAllowedNotification(application, comment);

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> {
                assertThat(greenMail.getReceivedMessagesForDomain(secondStage.getEmail())).hasSize(1);
                assertThat(greenMail.getReceivedMessagesForDomain(person.getEmail())).hasSize(1);
            });

        final MimeMessage[] inboxSecondStage = greenMail.getReceivedMessagesForDomain(secondStage.getEmail());
        final MimeMessage[] inboxUser = greenMail.getReceivedMessagesForDomain(person.getEmail());

        final Message msg = inboxUser[0];
        assertThat(msg.getSubject()).isEqualTo("Deine zu genehmigende Abwesenheit wurde vorläufig genehmigt");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of user email
        String contentUser = readPlainContent(msg);
        assertThat(contentUser).isEqualTo("""
            Hallo Lieschen Müller,

            deine am 12.04.2021 gestellte Abwesenheit vom 16.04.2021 ganztägig wurde vorläufig genehmigt.
            Bitte beachte, dass diese von einem entsprechenden Verantwortlichen freigegeben werden muss.

                https://localhost:8080/web/application/1234

            Kommentar von Kai Schmitt:
            OK, spricht von meiner Seite aus nix dagegen


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen.""");

        // get email office
        Message msgSecondStage = inboxSecondStage[0];
        assertThat(msgSecondStage.getSubject()).isEqualTo("Eine zu genehmigende Abwesenheit wurde vorläufig genehmigt");
        assertThat(new InternetAddress(secondStage.getEmail())).isEqualTo(msgSecondStage.getAllRecipients()[0]);

        // check content of office email
        assertThat(readPlainContent(msgSecondStage)).isEqualTo("""
            Hallo Kai Schmitt,

            es liegt eine neue zu genehmigende Abwesenheit vor.

                https://localhost:8080/web/application/1234

            Die Abwesenheit wurde bereits vorläufig genehmigt und muss nun noch endgültig freigegeben werden.

            Kommentar von Kai Schmitt:
            OK, spricht von meiner Seite aus nix dagegen

            Informationen zur Abwesenheit:

                Mitarbeiter:         Lieschen Müller
                Zeitraum:            16.04.2021, ganztägig
                Art der Abwesenheit: Erholungsurlaub
                Grund:              \s
                Vertretung:         \s
                Anschrift/Telefon:  \s
                Erstellungsdatum:    12.04.2021

            Überschneidende Abwesenheiten in der Abteilung des Antragsstellers:
               \s
                Lieschen Müller: 16.04.2021, ganztägig
               \s


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/2/notifications anpassen.""");
    }

    @Test
    void ensureNotificationAboutTemporaryAllowedApplicationIsSentToSecondStageAuthoritiesAndToPersonWithOneReplacement()
        throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_TEMPORARY_ALLOWED));
        final Person holidayReplacement = new Person("pennyworth", "Pennyworth", "Alfred", "pennyworth@example.org");

        final Person secondStage = new Person("manager", "Schmitt", "Kai", "manager@example.org");
        secondStage.setId(1L);
        secondStage.setPermissions(List.of(SECOND_STAGE_AUTHORITY));
        secondStage.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_TEMPORARY_ALLOWED));

        final Application application = createApplication(person);
        application.setApplicationDate(LocalDate.of(2021, APRIL, 12));
        application.setStartDate(LocalDate.of(2021, APRIL, 16));
        application.setEndDate(LocalDate.of(2021, APRIL, 16));

        final ApplicationComment comment = new ApplicationComment(
            1L, Instant.now(clock), application, ApplicationCommentAction.TEMPORARY_ALLOWED, secondStage, "OK, spricht von meiner Seite aus nix dagegen");

        final HolidayReplacementEntity holidayReplacementEntity = new HolidayReplacementEntity();
        holidayReplacementEntity.setPerson(holidayReplacement);

        application.setHolidayReplacements(List.of(holidayReplacementEntity));

        when(departmentService.getApplicationsFromColleaguesOf(person, application.getStartDate(), application.getEndDate()))
            .thenReturn(List.of(application));

        when(mailRecipientService.getRecipientsOfInterest(person, NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_TEMPORARY_ALLOWED)).thenReturn(List.of(secondStage));

        sut.sendTemporaryAllowedNotification(application, comment);

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> {
                assertThat(greenMail.getReceivedMessagesForDomain(secondStage.getEmail())).hasSize(1);
                assertThat(greenMail.getReceivedMessagesForDomain(person.getEmail())).hasSize(1);
            });

        // were both emails sent?
        final MimeMessage[] inboxSecondStage = greenMail.getReceivedMessagesForDomain(secondStage.getEmail());
        final MimeMessage[] inboxUser = greenMail.getReceivedMessagesForDomain(person.getEmail());

        // get email user
        final Message msg = inboxUser[0];
        assertThat(msg.getSubject()).isEqualTo("Deine zu genehmigende Abwesenheit wurde vorläufig genehmigt");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // get email office
        final Message msgSecondStage = inboxSecondStage[0];
        assertThat(msgSecondStage.getSubject()).isEqualTo("Eine zu genehmigende Abwesenheit wurde vorläufig genehmigt");
        assertThat(new InternetAddress(secondStage.getEmail())).isEqualTo(msgSecondStage.getAllRecipients()[0]);

        // check content of office email
        assertThat(readPlainContent(msgSecondStage)).isEqualTo("""
            Hallo Kai Schmitt,

            es liegt eine neue zu genehmigende Abwesenheit vor.

                https://localhost:8080/web/application/1234

            Die Abwesenheit wurde bereits vorläufig genehmigt und muss nun noch endgültig freigegeben werden.

            Kommentar von Kai Schmitt:
            OK, spricht von meiner Seite aus nix dagegen

            Informationen zur Abwesenheit:

                Mitarbeiter:         Lieschen Müller
                Zeitraum:            16.04.2021, ganztägig
                Art der Abwesenheit: Erholungsurlaub
                Grund:              \s
                Vertretung:          Alfred Pennyworth
                Anschrift/Telefon:  \s
                Erstellungsdatum:    12.04.2021

            Überschneidende Abwesenheiten in der Abteilung des Antragsstellers:
               \s
                Lieschen Müller: 16.04.2021, ganztägig
               \s


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen.""");
    }

    @Test
    void ensureNotificationAboutTemporaryAllowedApplicationIsSentToSecondStageAuthoritiesAndToPersonWithMultipleReplacements()
        throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_TEMPORARY_ALLOWED));
        final Person holidayReplacementOne = new Person("pennyworth", "Pennyworth", "Alfred", "pennyworth@example.org");
        final Person holidayReplacementTwo = new Person("rob", "", "Robin", "robin@example.org");

        final Person secondStage = new Person("manager", "Schmitt", "Kai", "manager@example.org");
        secondStage.setId(1L);
        secondStage.setPermissions(List.of(SECOND_STAGE_AUTHORITY));
        secondStage.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_TEMPORARY_ALLOWED));

        final Application application = createApplication(person);
        application.setApplicationDate(LocalDate.of(2021, APRIL, 12));
        application.setStartDate(LocalDate.of(2021, APRIL, 16));
        application.setEndDate(LocalDate.of(2021, APRIL, 16));

        final ApplicationComment comment = new ApplicationComment(
            1L, Instant.now(clock), application, ApplicationCommentAction.TEMPORARY_ALLOWED, secondStage, "OK, spricht von meiner Seite aus nix dagegen");

        final HolidayReplacementEntity holidayReplacementOneEntity = new HolidayReplacementEntity();
        holidayReplacementOneEntity.setPerson(holidayReplacementOne);

        final HolidayReplacementEntity holidayReplacementTwoEntity = new HolidayReplacementEntity();
        holidayReplacementTwoEntity.setPerson(holidayReplacementTwo);

        application.setHolidayReplacements(List.of(holidayReplacementOneEntity, holidayReplacementTwoEntity));

        when(departmentService.getApplicationsFromColleaguesOf(person, application.getStartDate(), application.getEndDate()))
            .thenReturn(List.of(application));

        when(mailRecipientService.getRecipientsOfInterest(person, NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_TEMPORARY_ALLOWED)).thenReturn(List.of(secondStage));

        sut.sendTemporaryAllowedNotification(application, comment);

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> {
                assertThat(greenMail.getReceivedMessagesForDomain(secondStage.getEmail())).hasSize(1);
                assertThat(greenMail.getReceivedMessagesForDomain(person.getEmail())).hasSize(1);
            });

        final MimeMessage[] inboxSecondStage = greenMail.getReceivedMessagesForDomain(secondStage.getEmail());
        final MimeMessage[] inboxUser = greenMail.getReceivedMessagesForDomain(person.getEmail());

        // get email user
        final Message msg = inboxUser[0];
        assertThat(msg.getSubject()).isEqualTo("Deine zu genehmigende Abwesenheit wurde vorläufig genehmigt");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // get email office
        final Message msgSecondStage = inboxSecondStage[0];
        assertThat(msgSecondStage.getSubject()).isEqualTo("Eine zu genehmigende Abwesenheit wurde vorläufig genehmigt");
        assertThat(new InternetAddress(secondStage.getEmail())).isEqualTo(msgSecondStage.getAllRecipients()[0]);

        // check content of office email
        assertThat(readPlainContent(msgSecondStage)).isEqualTo("""
            Hallo Kai Schmitt,

            es liegt eine neue zu genehmigende Abwesenheit vor.

                https://localhost:8080/web/application/1234

            Die Abwesenheit wurde bereits vorläufig genehmigt und muss nun noch endgültig freigegeben werden.

            Kommentar von Kai Schmitt:
            OK, spricht von meiner Seite aus nix dagegen

            Informationen zur Abwesenheit:

                Mitarbeiter:         Lieschen Müller
                Zeitraum:            16.04.2021, ganztägig
                Art der Abwesenheit: Erholungsurlaub
                Grund:              \s
                Vertretung:          Alfred Pennyworth, Robin
                Anschrift/Telefon:  \s
                Erstellungsdatum:    12.04.2021

            Überschneidende Abwesenheiten in der Abteilung des Antragsstellers:
               \s
                Lieschen Müller: 16.04.2021, ganztägig
               \s


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen.""");
    }

    @Test
    void ensureNotificationAboutTemporaryAllowedApplicationWithMultipleOverlappingVacations() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_TEMPORARY_ALLOWED));

        final Person secondStage = new Person("manager", "Schmitt", "Kai", "manager@example.org");
        secondStage.setId(1L);
        secondStage.setPermissions(List.of(SECOND_STAGE_AUTHORITY));
        secondStage.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_TEMPORARY_ALLOWED));

        final Application application = createApplication(person);
        application.setApplicationDate(LocalDate.of(2021, APRIL, 12));
        application.setStartDate(LocalDate.of(2021, APRIL, 16));
        application.setEndDate(LocalDate.of(2021, APRIL, 16));

        final ApplicationComment comment = new ApplicationComment(
            1L, Instant.now(clock), application, ApplicationCommentAction.TEMPORARY_ALLOWED, secondStage, "OK, spricht von meiner Seite aus nix dagegen");

        final Application applicationSecond = createApplication(person);
        applicationSecond.setApplicationDate(LocalDate.of(2021, APRIL, 12));
        applicationSecond.setStartDate(LocalDate.of(2021, APRIL, 17));
        applicationSecond.setEndDate(LocalDate.of(2021, APRIL, 18));

        when(departmentService.getApplicationsFromColleaguesOf(person, application.getStartDate(), application.getEndDate())).thenReturn(List.of(application, applicationSecond));
        when(mailRecipientService.getRecipientsOfInterest(person, NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_TEMPORARY_ALLOWED)).thenReturn(List.of(secondStage));

        sut.sendTemporaryAllowedNotification(application, comment);

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> {
                assertThat(greenMail.getReceivedMessagesForDomain(secondStage.getEmail())).hasSize(1);
                assertThat(greenMail.getReceivedMessagesForDomain(person.getEmail())).hasSize(1);
            });

        final MimeMessage[] inboxUser = greenMail.getReceivedMessagesForDomain(person.getEmail());
        final Message msg = inboxUser[0];
        assertThat(msg.getSubject()).isEqualTo("Deine zu genehmigende Abwesenheit wurde vorläufig genehmigt");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        final MimeMessage[] inboxSecondStage = greenMail.getReceivedMessagesForDomain(secondStage.getEmail());
        final Message msgSecondStage = inboxSecondStage[0];
        assertThat(msgSecondStage.getSubject()).isEqualTo("Eine zu genehmigende Abwesenheit wurde vorläufig genehmigt");
        assertThat(new InternetAddress(secondStage.getEmail())).isEqualTo(msgSecondStage.getAllRecipients()[0]);

        // check content of office email
        assertThat(readPlainContent(msgSecondStage)).isEqualTo("""
            Hallo Kai Schmitt,

            es liegt eine neue zu genehmigende Abwesenheit vor.

                https://localhost:8080/web/application/1234

            Die Abwesenheit wurde bereits vorläufig genehmigt und muss nun noch endgültig freigegeben werden.

            Kommentar von Kai Schmitt:
            OK, spricht von meiner Seite aus nix dagegen

            Informationen zur Abwesenheit:

                Mitarbeiter:         Lieschen Müller
                Zeitraum:            16.04.2021, ganztägig
                Art der Abwesenheit: Erholungsurlaub
                Grund:              \s
                Vertretung:         \s
                Anschrift/Telefon:  \s
                Erstellungsdatum:    12.04.2021

            Überschneidende Abwesenheiten in der Abteilung des Antragsstellers:
               \s
                Lieschen Müller: 16.04.2021, ganztägig
                Lieschen Müller: 17.04.2021 bis 18.04.2021, ganztägig
               \s


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen.""");
    }

    @Test
    void ensureNotificationAboutTemporaryAllowedApplicationWithoutOverlappingVacations() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_TEMPORARY_ALLOWED));

        final Person secondStage = new Person("manager", "Schmitt", "Kai", "manager@example.org");
        secondStage.setId(1L);
        secondStage.setPermissions(List.of(SECOND_STAGE_AUTHORITY));
        secondStage.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_TEMPORARY_ALLOWED));

        final Application application = createApplication(person);
        application.setApplicationDate(LocalDate.of(2021, APRIL, 12));
        application.setStartDate(LocalDate.of(2021, APRIL, 16));
        application.setEndDate(LocalDate.of(2021, APRIL, 16));

        final ApplicationComment comment = new ApplicationComment(
            1L, Instant.now(clock), application, ApplicationCommentAction.TEMPORARY_ALLOWED, secondStage, "OK, spricht von meiner Seite aus nix dagegen");

        when(departmentService.getApplicationsFromColleaguesOf(person, application.getStartDate(), application.getEndDate())).thenReturn(List.of());
        when(mailRecipientService.getRecipientsOfInterest(person, NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_TEMPORARY_ALLOWED)).thenReturn(List.of(secondStage));

        sut.sendTemporaryAllowedNotification(application, comment);

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> {
                assertThat(greenMail.getReceivedMessagesForDomain(secondStage.getEmail())).hasSize(1);
                assertThat(greenMail.getReceivedMessagesForDomain(person.getEmail())).hasSize(1);
            });

        final MimeMessage[] inboxUser = greenMail.getReceivedMessagesForDomain(person.getEmail());
        final Message msg = inboxUser[0];
        assertThat(msg.getSubject()).isEqualTo("Deine zu genehmigende Abwesenheit wurde vorläufig genehmigt");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        final MimeMessage[] inboxSecondStage = greenMail.getReceivedMessagesForDomain(secondStage.getEmail());
        final Message msgSecondStage = inboxSecondStage[0];
        assertThat(msgSecondStage.getSubject()).isEqualTo("Eine zu genehmigende Abwesenheit wurde vorläufig genehmigt");
        assertThat(new InternetAddress(secondStage.getEmail())).isEqualTo(msgSecondStage.getAllRecipients()[0]);

        // check content of office email
        assertThat(readPlainContent(msgSecondStage)).isEqualTo("""
            Hallo Kai Schmitt,

            es liegt eine neue zu genehmigende Abwesenheit vor.

                https://localhost:8080/web/application/1234

            Die Abwesenheit wurde bereits vorläufig genehmigt und muss nun noch endgültig freigegeben werden.

            Kommentar von Kai Schmitt:
            OK, spricht von meiner Seite aus nix dagegen

            Informationen zur Abwesenheit:

                Mitarbeiter:         Lieschen Müller
                Zeitraum:            16.04.2021, ganztägig
                Art der Abwesenheit: Erholungsurlaub
                Grund:              \s
                Vertretung:         \s
                Anschrift/Telefon:  \s
                Erstellungsdatum:    12.04.2021

            Überschneidende Abwesenheiten in der Abteilung des Antragsstellers:
               \s
                Keine


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen.""");
    }

    @Test
    void ensureBossesAndDepartmentHeadsGetRemindMail() throws MessagingException, IOException {

        final Person boss = new Person("boss", "Boss", "Hugo", "boss@example.org");
        boss.setPermissions(List.of(BOSS));

        final Person departmentHead = new Person("departmentHead", "Kopf", "Senior", "head@example.org");
        departmentHead.setPermissions(List.of(DEPARTMENT_HEAD));

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        final Application application = createApplication(person);
        application.setApplicationDate(LocalDate.of(2024, 8, 8));

        when(mailRecipientService.getResponsibleManagersOf(application.getPerson())).thenReturn(asList(boss, departmentHead));

        sut.sendRemindNotificationToManagement(application);

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> {
                assertThat(greenMail.getReceivedMessagesForDomain(boss.getEmail())).hasSize(1);
                assertThat(greenMail.getReceivedMessagesForDomain(departmentHead.getEmail())).hasSize(1);
            });

        final MimeMessage[] inboxOfBoss = greenMail.getReceivedMessagesForDomain(boss.getEmail());
        final Message msg = inboxOfBoss[0];
        assertThat(msg.getSubject()).isEqualTo("Erinnerung auf wartende zu genehmigende Abwesenheit");
        assertThat(new InternetAddress(boss.getEmail())).isEqualTo(msg.getAllRecipients()[0]);
        assertThat(readPlainContent(msg)).isEqualTo("""
            Hallo Hugo Boss,

            Lieschen Müller bittet um die Bearbeitung der Abwesenheit vom 08.08.2024.

                https://localhost:8080/web/application/1234


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/null/notifications anpassen.""");
    }

    @Test
    void ensureSendRemindForWaitingApplicationsReminderNotification() throws Exception {

        // PERSONs
        final Person personA = new Person("personA", "Mahler", "Max", "mahler@example.org");
        personA.setId(1L);
        final Person personB = new Person("personB", "Förster", "Frederik", "förster@example.org");
        personB.setId(2L);
        final Person personC = new Person("personC", "Schuster", "Peter", "schuster@example.org");
        personC.setId(3L);

        // APPLICATIONs
        final Application applicationA = createApplication(personA);
        applicationA.setId(1L);
        applicationA.setApplicationDate(LocalDate.of(2022, APRIL, 17));
        applicationA.setStartDate(LocalDate.of(2022, APRIL, 20));
        applicationA.setEndDate(LocalDate.of(2022, APRIL, 21));

        final Application applicationAA = createApplication(personA);
        applicationAA.setId(4L);
        applicationAA.setApplicationDate(LocalDate.of(2021, MAY, 12));
        applicationAA.setStartDate(LocalDate.of(2021, MAY, 16));
        applicationAA.setEndDate(LocalDate.of(2021, MAY, 16));

        final Application applicationB = createApplication(personB);
        applicationB.setId(2L);
        applicationB.setApplicationDate(LocalDate.of(2023, DECEMBER, 12));
        applicationB.setStartDate(LocalDate.of(2023, DECEMBER, 24));
        applicationB.setEndDate(LocalDate.of(2023, DECEMBER, 31));

        final Application applicationC = createApplication(personC);
        applicationC.setId(3L);
        applicationC.setApplicationDate(LocalDate.of(2021, NOVEMBER, 13));
        applicationC.setStartDate(LocalDate.of(2021, NOVEMBER, 30));
        applicationC.setEndDate(LocalDate.of(2021, NOVEMBER, 30));

        // DEPARTMENT HEADs
        final Person boss = new Person("boss", "Boss", "Hugo", "boss@example.org");
        boss.setId(1L);
        boss.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_WAITING_REMINDER));
        final Person departmentHeadA = new Person("headAC", "Wurst", "Heinz", "headAC@example.org");
        departmentHeadA.setId(2L);
        departmentHeadA.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_WAITING_REMINDER));
        final Person departmentHeadB = new Person("headB", "Mustermann", "Michel", "headB@example.org");
        departmentHeadB.setId(3L);
        departmentHeadB.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_WAITING_REMINDER));

        when(mailRecipientService.getRecipientsOfInterest(personA, NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_WAITING_REMINDER)).thenReturn(asList(boss, departmentHeadA));
        when(mailRecipientService.getRecipientsOfInterest(personB, NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_WAITING_REMINDER)).thenReturn(asList(boss, departmentHeadB));
        when(mailRecipientService.getRecipientsOfInterest(personC, NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_WAITING_REMINDER)).thenReturn(asList(boss, departmentHeadA));

        sut.sendRemindForWaitingApplicationsReminderNotification(asList(applicationAA, applicationA, applicationB, applicationC));

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> {
                assertThat(greenMail.getReceivedMessagesForDomain(boss.getEmail())).hasSize(1);
                assertThat(greenMail.getReceivedMessagesForDomain(departmentHeadA.getEmail())).hasSize(1);
                assertThat(greenMail.getReceivedMessagesForDomain(departmentHeadB.getEmail())).hasSize(1);
            });

        // were all emails sent?
        final MimeMessage[] bossInbox = greenMail.getReceivedMessagesForDomain(boss.getEmail());
        final MimeMessage[] departmentHeadAInbox = greenMail.getReceivedMessagesForDomain(departmentHeadA.getEmail());
        final MimeMessage[] departmentHeadBInbox = greenMail.getReceivedMessagesForDomain(departmentHeadB.getEmail());

        // get email boss
        final Message msgBoss = bossInbox[0];
        assertThat(msgBoss.getSubject()).isEqualTo("Erinnerung für 4 wartende zu genehmigende Abwesenheiten");
        assertThat(new InternetAddress(boss.getEmail())).isEqualTo(msgBoss.getAllRecipients()[0]);

        // check content of boss email
        assertThat(readPlainContent(msgBoss)).isEqualTo("""
            Hallo Hugo Boss,

            die folgenden 4 Abwesenheiten warten auf deine Bearbeitung:

            Anträge von Max Mahler:
              Erholungsurlaub vom 20.04.2022 bis zum 21.04.2022 ganztägig. https://localhost:8080/web/application/1
              Erholungsurlaub vom 16.05.2021 ganztägig. https://localhost:8080/web/application/4

            Antrag von Frederik Förster:
              Erholungsurlaub vom 24.12.2023 bis zum 31.12.2023 ganztägig. https://localhost:8080/web/application/2

            Antrag von Peter Schuster:
              Erholungsurlaub vom 30.11.2021 ganztägig. https://localhost:8080/web/application/3


            Überblick aller wartenden Abwesenheitsanträge findest du unter https://localhost:8080/web/application#waiting-requests


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen."""
        );

        // get email department head A
        final Message msgDepartmentHeadA = departmentHeadAInbox[0];
        assertThat(msgDepartmentHeadA.getSubject()).isEqualTo("Erinnerung für 3 wartende zu genehmigende Abwesenheiten");
        assertThat(new InternetAddress(departmentHeadA.getEmail())).isEqualTo(msgDepartmentHeadA.getAllRecipients()[0]);

        // check content of boss email
        assertThat(readPlainContent(msgDepartmentHeadA)).isEqualTo("""
            Hallo Heinz Wurst,

            die folgenden 3 Abwesenheiten warten auf deine Bearbeitung:

            Anträge von Max Mahler:
              Erholungsurlaub vom 20.04.2022 bis zum 21.04.2022 ganztägig. https://localhost:8080/web/application/1
              Erholungsurlaub vom 16.05.2021 ganztägig. https://localhost:8080/web/application/4

            Antrag von Peter Schuster:
              Erholungsurlaub vom 30.11.2021 ganztägig. https://localhost:8080/web/application/3


            Überblick aller wartenden Abwesenheitsanträge findest du unter https://localhost:8080/web/application#waiting-requests


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/2/notifications anpassen."""
        );

        // get email department head A
        final Message msgDepartmentHeadB = departmentHeadBInbox[0];
        assertThat(msgDepartmentHeadB.getSubject()).isEqualTo("Erinnerung für eine wartende zu genehmigende Abwesenheit");
        assertThat(new InternetAddress(departmentHeadB.getEmail())).isEqualTo(msgDepartmentHeadB.getAllRecipients()[0]);

        // check content of boss email
        assertThat(readPlainContent(msgDepartmentHeadB)).isEqualTo("""
            Hallo Michel Mustermann,

            die folgende Abwesenheit wartet auf deine Bearbeitung:

            Antrag von Frederik Förster:
              Erholungsurlaub vom 24.12.2023 bis zum 31.12.2023 ganztägig. https://localhost:8080/web/application/2


            Überblick aller wartenden Abwesenheitsanträge findest du unter https://localhost:8080/web/application#waiting-requests


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/3/notifications anpassen."""
        );
    }

    @Test
    void ensureToSendEditedApplicationNotificationIfEditorIsApplicant() throws Exception {

        final Person editor = new Person("editor", "Muster", "Max", "mustermann@example.org");
        editor.setId(1L);
        editor.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_EDITED));

        final Application application = createApplication(editor);
        application.setPerson(editor);

        final Person relevantPerson = new Person("relevantPerson", "Relevant", "Person", "relevantPerson@example.org");
        relevantPerson.setId(2L);
        relevantPerson.setPermissions(List.of(BOSS));
        relevantPerson.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_EDITED));
        when(mailRecipientService.getRecipientsOfInterest(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_EDITED)).thenReturn(List.of(relevantPerson));

        sut.sendEditedNotification(application, editor);

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> {
                assertThat(greenMail.getReceivedMessagesForDomain(editor.getEmail())).hasSize(1);
                assertThat(greenMail.getReceivedMessagesForDomain(relevantPerson.getEmail())).hasSize(1);
            });

        // check editor email
        final MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(editor.getEmail());
        final Message msg = inbox[0];
        assertThat(msg.getSubject()).isEqualTo("Deine zu genehmigende Abwesenheit wurde erfolgreich bearbeitet");
        assertThat(new InternetAddress(editor.getEmail())).isEqualTo(msg.getAllRecipients()[0]);
        assertThat(readPlainContent(msg)).isEqualTo(
            """
                Hallo Max Muster,

                deine Abwesenheit wurde erfolgreich bearbeitet.

                    https://localhost:8080/web/application/1234


                Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen."""
        );

        // check relevant person email
        final MimeMessage[] inboxRelevantPerson = greenMail.getReceivedMessagesForDomain(relevantPerson.getEmail());
        final Message msgRelevantPerson = inboxRelevantPerson[0];
        assertThat(msgRelevantPerson.getSubject()).isEqualTo("Zu genehmigende Abwesenheit von Max Muster wurde erfolgreich bearbeitet");
        assertThat(new InternetAddress(relevantPerson.getEmail())).isEqualTo(msgRelevantPerson.getAllRecipients()[0]);
        assertThat(readPlainContent(msgRelevantPerson)).isEqualTo(
            """
                Hallo Person Relevant,

                die Abwesenheit von Max Muster wurde von Max Muster bearbeitet.

                    https://localhost:8080/web/application/1234


                Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/2/notifications anpassen."""
        );
    }

    @Test
    void ensureToSendEditedApplicationNotificationIfEditorIsOffice() throws Exception {

        final Person office = new Person("office", "Muster", "Marlene", "mustermann.marlene@example.org");

        final Person applicant = new Person("editor", "Muster", "Max", "mustermann@example.org");
        applicant.setId(1L);
        applicant.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_EDITED));

        final Application application = createApplication(applicant);
        application.setPerson(applicant);

        final Person relevantPerson = new Person("relevantPerson", "Relevant", "Person", "relevantPerson@example.org");
        relevantPerson.setId(2L);
        relevantPerson.setPermissions(List.of(BOSS));
        relevantPerson.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_EDITED));
        when(mailRecipientService.getRecipientsOfInterest(application.getPerson(), NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_EDITED)).thenReturn(List.of(relevantPerson));

        sut.sendEditedNotification(application, office);

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> {
                assertThat(greenMail.getReceivedMessagesForDomain(applicant.getEmail())).hasSize(1);
                assertThat(greenMail.getReceivedMessagesForDomain(relevantPerson.getEmail())).hasSize(1);
            });

        // check editor email
        final MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(applicant.getEmail());
        final Message msg = inbox[0];
        assertThat(msg.getSubject()).isEqualTo("Deine zu genehmigende Abwesenheit wurde von Marlene Muster bearbeitet");
        assertThat(new InternetAddress(applicant.getEmail())).isEqualTo(msg.getAllRecipients()[0]);
        assertThat(readPlainContent(msg)).isEqualTo(
            """
                Hallo Max Muster,

                deine Abwesenheit wurde von Marlene Muster bearbeitet.

                    https://localhost:8080/web/application/1234


                Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen."""
        );

        // check relevant person email
        final MimeMessage[] inboxRelevantPerson = greenMail.getReceivedMessagesForDomain(relevantPerson.getEmail());
        final Message msgRelevantPerson = inboxRelevantPerson[0];
        assertThat(msgRelevantPerson.getSubject()).isEqualTo("Zu genehmigende Abwesenheit von Max Muster wurde erfolgreich bearbeitet");
        assertThat(new InternetAddress(relevantPerson.getEmail())).isEqualTo(msgRelevantPerson.getAllRecipients()[0]);
        assertThat(readPlainContent(msgRelevantPerson)).isEqualTo(
            """
                Hallo Person Relevant,

                die Abwesenheit von Max Muster wurde von Marlene Muster bearbeitet.

                    https://localhost:8080/web/application/1234


                Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/2/notifications anpassen."""
        );
    }

    @Test
    void ensurePersonGetsANotificationForUpcomingApplicationToday() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setId(1L);
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

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain(person.getEmail())).hasSize(1));

        final MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(person.getEmail());
        final Message msg = inbox[0];
        assertThat(msg.getSubject()).contains("Erinnerung an deine Abwesenheit");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        assertThat(readPlainContent(msg)).isEqualTo(
            """
                Hallo Lieschen Müller,

                heute beginnt deine Abwesenheit

                    https://localhost:8080/web/application/1234

                und du wirst vertreten durch:

                - replacement holiday
                  "Some notes"

                Da du vom 01.01.2022 nicht anwesend bist, denke bitte an die Übergabe.
                Dazu gehören z.B. Abwesenheitsnotiz, E-Mail- & Telefon-Weiterleitung, Zeiterfassung, etc.


                Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen."""
        );
    }

    @Test
    void ensurePersonGetsANotificationForUpcomingApplication() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setId(1L);
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_UPCOMING));
        final Person holidayReplacement = new Person("holidayReplacement", "holiday", "replacement", "holidayreplacement@example.org");

        final Application application = createApplication(person);
        application.setStartDate(LocalDate.of(2022, Month.JANUARY, 2));
        application.setEndDate(LocalDate.of(2022, Month.JANUARY, 3));

        final HolidayReplacementEntity holidayReplacementEntity = new HolidayReplacementEntity();
        holidayReplacementEntity.setPerson(holidayReplacement);
        holidayReplacementEntity.setNote("Some notes");
        application.setHolidayReplacements(List.of(holidayReplacementEntity));

        sut.sendRemindForUpcomingApplicationsReminderNotification(List.of(application));

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain(person.getEmail())).hasSize(1));

        final MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(person.getEmail());
        final Message msg = inbox[0];
        assertThat(msg.getSubject()).contains("Erinnerung an deine Abwesenheit");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);
        assertThat(readPlainContent(msg)).isEqualTo(
            """
                Hallo Lieschen Müller,

                morgen beginnt deine Abwesenheit

                    https://localhost:8080/web/application/1234

                und du wirst vertreten durch:

                - replacement holiday
                  "Some notes"

                Da du vom 02.01.2022 bis zum 03.01.2022 nicht anwesend bist, denke bitte an die Übergabe.
                Dazu gehören z.B. Abwesenheitsnotiz, E-Mail- & Telefon-Weiterleitung, Zeiterfassung, etc.


                Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen."""
        );
    }

    @Test
    void ensurePersonGetsANotificationForUpcomingApplicationMoreThanOneDay() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setId(1L);
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

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain(person.getEmail())).hasSize(1));

        final MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(person.getEmail());
        final Message msg = inbox[0];
        assertThat(msg.getSubject()).contains("Erinnerung an deine Abwesenheit");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);
        assertThat(readPlainContent(msg)).isEqualTo(
            """
                Hallo Lieschen Müller,

                in 2 Tagen beginnt deine Abwesenheit

                    https://localhost:8080/web/application/1234

                und du wirst vertreten durch:

                - replacement holiday
                  "Some notes"

                Da du vom 03.01.2022 nicht anwesend bist, denke bitte an die Übergabe.
                Dazu gehören z.B. Abwesenheitsnotiz, E-Mail- & Telefon-Weiterleitung, Zeiterfassung, etc.


                Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen."""
        );
    }

    @Test
    void ensurePersonGetsANotificationForUpcomingApplicationWithoutReplacementToday() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setId(1L);
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_UPCOMING));
        final Application application = createApplication(person);
        application.setStartDate(LocalDate.now(clock));
        application.setEndDate(LocalDate.now(clock));

        sut.sendRemindForUpcomingApplicationsReminderNotification(List.of(application));

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain(person.getEmail())).hasSize(1));

        final MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(person.getEmail());
        final Message msg = inbox[0];
        assertThat(msg.getSubject()).contains("Erinnerung an deine Abwesenheit");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);
        assertThat(readPlainContent(msg)).isEqualTo(
            """
                Hallo Lieschen Müller,

                heute beginnt deine Abwesenheit

                    https://localhost:8080/web/application/1234



                Da du vom 01.01.2022 nicht anwesend bist, denke bitte an die Übergabe.
                Dazu gehören z.B. Abwesenheitsnotiz, E-Mail- & Telefon-Weiterleitung, Zeiterfassung, etc.


                Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen."""
        );
    }

    @Test
    void ensurePersonGetsANotificationForUpcomingApplicationWithoutReplacementTomorrow() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setId(1L);
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_UPCOMING));
        final Application application = createApplication(person);
        application.setStartDate(LocalDate.now(clock).plusDays(1));
        application.setEndDate(LocalDate.now(clock).plusDays(1));

        sut.sendRemindForUpcomingApplicationsReminderNotification(List.of(application));
        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain(person.getEmail())).hasSize(1));

        final MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(person.getEmail());
        final Message msg = inbox[0];
        assertThat(msg.getSubject()).contains("Erinnerung an deine Abwesenheit");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);
        assertThat(readPlainContent(msg)).isEqualTo(
            """
                Hallo Lieschen Müller,

                morgen beginnt deine Abwesenheit

                    https://localhost:8080/web/application/1234



                Da du vom 02.01.2022 nicht anwesend bist, denke bitte an die Übergabe.
                Dazu gehören z.B. Abwesenheitsnotiz, E-Mail- & Telefon-Weiterleitung, Zeiterfassung, etc.


                Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen."""
        );
    }

    @Test
    void ensurePersonGetsANotificationForUpcomingApplicationWithoutReplacement() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setId(1L);
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_UPCOMING));
        final Application application = createApplication(person);
        application.setStartDate(LocalDate.of(2022, Month.JANUARY, 31));
        application.setEndDate(LocalDate.of(2022, Month.JANUARY, 31));

        sut.sendRemindForUpcomingApplicationsReminderNotification(List.of(application));

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain(person.getEmail())).hasSize(1));

        final MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(person.getEmail());
        final Message msg = inbox[0];
        assertThat(msg.getSubject()).contains("Erinnerung an deine Abwesenheit");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);
        assertThat(readPlainContent(msg)).isEqualTo(
            """
                Hallo Lieschen Müller,

                in 30 Tagen beginnt deine Abwesenheit

                    https://localhost:8080/web/application/1234



                Da du vom 31.01.2022 nicht anwesend bist, denke bitte an die Übergabe.
                Dazu gehören z.B. Abwesenheitsnotiz, E-Mail- & Telefon-Weiterleitung, Zeiterfassung, etc.


                Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen."""
        );
    }

    @Test
    void ensurePersonGetsANotificationForUpcomingApplicationWithoutReplacementWithMoreThanOneDay() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setId(1L);
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_UPCOMING));
        final Application application = createApplication(person);
        application.setStartDate(LocalDate.of(2022, Month.JANUARY, 31));
        application.setEndDate(LocalDate.of(2022, Month.JANUARY, 31));

        sut.sendRemindForUpcomingApplicationsReminderNotification(List.of(application));

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain(person.getEmail())).hasSize(1));

        final MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(person.getEmail());
        final Message msg = inbox[0];
        assertThat(msg.getSubject()).contains("Erinnerung an deine Abwesenheit");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);
        assertThat(readPlainContent(msg)).isEqualTo(
            """
                Hallo Lieschen Müller,

                in 30 Tagen beginnt deine Abwesenheit

                    https://localhost:8080/web/application/1234



                Da du vom 31.01.2022 nicht anwesend bist, denke bitte an die Übergabe.
                Dazu gehören z.B. Abwesenheitsnotiz, E-Mail- & Telefon-Weiterleitung, Zeiterfassung, etc.


                Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen."""
        );
    }

    @Test
    void ensurePersonGetsANotificationForUpcomingApplicationWithOneReplacementWithoutNote() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setId(1L);
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_UPCOMING));
        final Person holidayReplacement = new Person("pennyworth", "Pennyworth", "Alfred", "pennyworth@example.org");

        final Application application = createApplication(person);
        application.setStartDate(LocalDate.of(2022, Month.JANUARY, 2));
        application.setEndDate(LocalDate.of(2022, Month.JANUARY, 2));

        final HolidayReplacementEntity holidayReplacementEntity = new HolidayReplacementEntity();
        holidayReplacementEntity.setPerson(holidayReplacement);
        application.setHolidayReplacements(List.of(holidayReplacementEntity));

        sut.sendRemindForUpcomingApplicationsReminderNotification(List.of(application));

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain(person.getEmail())).hasSize(1));

        // was email sent?
        final MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(person.getEmail());
        final Message msg = inbox[0];
        assertThat(msg.getSubject()).contains("Erinnerung an deine Abwesenheit");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        assertThat(readPlainContent(msg)).isEqualTo(
            """
                Hallo Lieschen Müller,

                morgen beginnt deine Abwesenheit

                    https://localhost:8080/web/application/1234

                und du wirst vertreten durch:

                - Alfred Pennyworth


                Da du vom 02.01.2022 nicht anwesend bist, denke bitte an die Übergabe.
                Dazu gehören z.B. Abwesenheitsnotiz, E-Mail- & Telefon-Weiterleitung, Zeiterfassung, etc.


                Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen."""
        );
    }

    @Test
    void ensurePersonGetsANotificationForUpcomingApplicationWithOneReplacementWithNote() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setId(1L);
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

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain(person.getEmail())).hasSize(1));

        // was email sent?
        final MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(person.getEmail());
        final Message msg = inbox[0];
        assertThat(msg.getSubject()).contains("Erinnerung an deine Abwesenheit");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        assertThat(readPlainContent(msg)).isEqualTo(
            """
                Hallo Lieschen Müller,

                morgen beginnt deine Abwesenheit

                    https://localhost:8080/web/application/1234

                und du wirst vertreten durch:

                - Alfred Pennyworth
                  "Hey Alfred, denke bitte an Pinguin, danke dir!"

                Da du vom 02.01.2022 nicht anwesend bist, denke bitte an die Übergabe.
                Dazu gehören z.B. Abwesenheitsnotiz, E-Mail- & Telefon-Weiterleitung, Zeiterfassung, etc.


                Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen."""
        );
    }

    @Test
    void ensurePersonGetsANotificationForUpcomingApplicationWithMultipleReplacementsWithoutNote() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setId(1L);
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

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain(person.getEmail())).hasSize(1));

        final MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(person.getEmail());
        final Message msg = inbox[0];
        assertThat(msg.getSubject()).contains("Erinnerung an deine Abwesenheit");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        assertThat(readPlainContent(msg)).isEqualTo(
            """
                Hallo Lieschen Müller,

                morgen beginnt deine Abwesenheit

                    https://localhost:8080/web/application/1234

                und du wirst vertreten durch:

                - Alfred Pennyworth
                  "Hey Alfred, denke bitte an Pinguin, danke dir!"
                - Robin
                  "Uffbasse Rob. Ich sehe dich."

                Da du vom 02.01.2022 nicht anwesend bist, denke bitte an die Übergabe.
                Dazu gehören z.B. Abwesenheitsnotiz, E-Mail- & Telefon-Weiterleitung, Zeiterfassung, etc.


                Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen."""
        );
    }

    @Test
    void ensurePersonGetsANotificationForUpcomingApplicationWithMultipleReplacementsWithNote() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setId(1L);
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

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain(person.getEmail())).hasSize(1));

        // was email sent?
        final MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(person.getEmail());
        final Message msg = inbox[0];
        assertThat(msg.getSubject()).contains("Erinnerung an deine Abwesenheit");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        assertThat(readPlainContent(msg)).isEqualTo(
            """
                Hallo Lieschen Müller,

                morgen beginnt deine Abwesenheit

                    https://localhost:8080/web/application/1234

                und du wirst vertreten durch:

                - Alfred Pennyworth

                - Robin


                Da du vom 02.01.2022 nicht anwesend bist, denke bitte an die Übergabe.
                Dazu gehören z.B. Abwesenheitsnotiz, E-Mail- & Telefon-Weiterleitung, Zeiterfassung, etc.


                Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen."""
        );
    }

    @Test
    void ensurePersonGetsANotificationForUpcomingApplicationWithReplacementWithoutNoteWithMoreThanOneDay() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setId(1L);
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_UPCOMING));
        final Person holidayReplacement = new Person("holidayReplacement", "holiday", "replacement", "holidayreplacement@example.org");

        final Application application = createApplication(person);
        application.setStartDate(LocalDate.of(2022, Month.JANUARY, 31));
        application.setEndDate(LocalDate.of(2022, Month.JANUARY, 31));

        final HolidayReplacementEntity holidayReplacementEntity = new HolidayReplacementEntity();
        holidayReplacementEntity.setPerson(holidayReplacement);
        application.setHolidayReplacements(List.of(holidayReplacementEntity));

        sut.sendRemindForUpcomingApplicationsReminderNotification(List.of(application));

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain(person.getEmail())).hasSize(1));

        // was email sent?
        final MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(person.getEmail());
        final Message msg = inbox[0];
        assertThat(msg.getSubject()).contains("Erinnerung an deine Abwesenheit");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        assertThat(readPlainContent(msg)).isEqualTo(
            """
                Hallo Lieschen Müller,

                in 30 Tagen beginnt deine Abwesenheit

                    https://localhost:8080/web/application/1234

                und du wirst vertreten durch:

                - replacement holiday


                Da du vom 31.01.2022 nicht anwesend bist, denke bitte an die Übergabe.
                Dazu gehören z.B. Abwesenheitsnotiz, E-Mail- & Telefon-Weiterleitung, Zeiterfassung, etc.


                Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen."""
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

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain(holidayReplacement.getEmail())).hasSize(1));

        // was email sent?
        final MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(holidayReplacement.getEmail());
        final Message msg = inbox[0];
        assertThat(msg.getSubject()).contains("Erinnerung an deine bevorstehende Vertretung für Lieschen Müller");
        assertThat(new InternetAddress(holidayReplacement.getEmail())).isEqualTo(msg.getAllRecipients()[0]);
        assertThat(readPlainContent(msg)).isEqualTo("""
            Hallo replacement holiday,

            deine Vertretung für Lieschen Müller vom 01.01.2022 beginnt heute.

            Notiz:
            Some notes

            Einen Überblick deiner aktuellen und zukünftigen Vertretungen findest du unter https://localhost:8080/web/application/replacement


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/null/notifications anpassen.""");
    }

    @Test
    void ensurePersonGetsANotificationForUpcomingHolidayReplacement() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        final Person holidayReplacement = new Person("holidayReplacement", "holiday", "replacement", "holidayreplacement@example.org");
        holidayReplacement.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_HOLIDAY_REPLACEMENT_UPCOMING));

        final Application application = createApplication(person);
        application.setStartDate(LocalDate.of(2022, Month.JANUARY, 2));
        application.setEndDate(LocalDate.of(2022, Month.JANUARY, 3));

        final HolidayReplacementEntity holidayReplacementEntity = new HolidayReplacementEntity();
        holidayReplacementEntity.setPerson(holidayReplacement);
        holidayReplacementEntity.setNote("Some notes");
        application.setHolidayReplacements(List.of(holidayReplacementEntity));

        sut.sendRemindForUpcomingHolidayReplacement(List.of(application));

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain(holidayReplacement.getEmail())).hasSize(1));

        // was email sent?
        final MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(holidayReplacement.getEmail());
        final Message msg = inbox[0];
        assertThat(msg.getSubject()).contains("Erinnerung an deine bevorstehende Vertretung für Lieschen Müller");
        assertThat(new InternetAddress(holidayReplacement.getEmail())).isEqualTo(msg.getAllRecipients()[0]);
        assertThat(readPlainContent(msg)).isEqualTo("""
            Hallo replacement holiday,

            deine Vertretung für Lieschen Müller vom 02.01.2022 bis zum 03.01.2022 beginnt morgen.

            Notiz:
            Some notes

            Einen Überblick deiner aktuellen und zukünftigen Vertretungen findest du unter https://localhost:8080/web/application/replacement


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/null/notifications anpassen.""");
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

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain(holidayReplacement.getEmail())).hasSize(1));

        final MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(holidayReplacement.getEmail());
        final Message msg = inbox[0];
        assertThat(msg.getSubject()).contains("Erinnerung an deine bevorstehende Vertretung für Lieschen Müller");
        assertThat(new InternetAddress(holidayReplacement.getEmail())).isEqualTo(msg.getAllRecipients()[0]);
        assertThat(readPlainContent(msg)).isEqualTo("""
            Hallo replacement holiday,

            deine Vertretung für Lieschen Müller vom 04.01.2022 bis zum 05.01.2022 beginnt in 3 Tagen.

            Notiz:
            Some notes

            Einen Überblick deiner aktuellen und zukünftigen Vertretungen findest du unter https://localhost:8080/web/application/replacement


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/null/notifications anpassen.""");
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

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain(holidayReplacement.getEmail())).hasSize(1));

        // was email sent?
        final MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(holidayReplacement.getEmail());
        final Message msg = inbox[0];
        assertThat(msg.getSubject()).contains("Erinnerung an deine bevorstehende Vertretung für Lieschen Müller");
        assertThat(new InternetAddress(holidayReplacement.getEmail())).isEqualTo(msg.getAllRecipients()[0]);
        assertThat(readPlainContent(msg)).isEqualTo("""
            Hallo replacement holiday,

            deine Vertretung für Lieschen Müller vom 02.01.2022 beginnt morgen.


            Einen Überblick deiner aktuellen und zukünftigen Vertretungen findest du unter https://localhost:8080/web/application/replacement


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/null/notifications anpassen.""");
    }

    private Application createApplication(Person person) {

        final LocalDate now = LocalDate.now(UTC);

        final Application application = new Application();
        application.setId(1234L);
        application.setPerson(person);
        application.setVacationType(createVacationType(1L, HOLIDAY, "application.data.vacationType.holiday", messageSource));
        application.setDayLength(FULL);
        application.setApplicationDate(now);
        application.setStartDate(now);
        application.setEndDate(now);
        application.setApplier(person);

        return application;
    }

    private String readPlainContent(Message message) throws MessagingException, IOException {
        return message.getContent().toString().replaceAll("\\r", "");
    }

    private String readPlainContent(MimeMessage message) {
        return Objects.requireNonNull(EmailConverter.mimeMessageToEmail(message).getPlainText()).replaceAll("\\r", "");
    }

    private List<AttachmentResource> getAttachments(MimeMessage message) {
        return EmailConverter.mimeMessageToEmail(message).getAttachments();
    }
}
