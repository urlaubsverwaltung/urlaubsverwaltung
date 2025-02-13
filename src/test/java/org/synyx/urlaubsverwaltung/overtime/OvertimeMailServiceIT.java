package org.synyx.urlaubsverwaltung.overtime;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.SingleTenantTestContainersBase;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;
import static org.synyx.urlaubsverwaltung.overtime.OvertimeCommentAction.CREATED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_OVERTIME_APPLIED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_OVERTIME_APPLIED_BY_MANAGEMENT;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_OVERTIME_MANAGEMENT_APPLIED;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@SpringBootTest(properties = {"spring.mail.port=3025", "spring.mail.host=localhost"})
@Transactional
class OvertimeMailServiceIT extends SingleTenantTestContainersBase {

    @RegisterExtension
    static final GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP_IMAP);

    @Autowired
    private OvertimeMailService sut;
    @Autowired
    private PersonService personService;
    @Autowired
    private Clock clock;

    @Test
    void ensureManagerWithOvertimeNotificationGetMailIfOvertimeRecorded() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen12@example.org");
        person.setId(1L);

        final LocalDate startDate = LocalDate.of(2020, 4, 16);
        final LocalDate endDate = LocalDate.of(2020, 4, 23);
        final Overtime overtime = new Overtime(person, startDate, endDate, Duration.parse("P1DT30H72M"));
        overtime.setId(1L);

        final OvertimeComment overtimeComment = new OvertimeComment(person, overtime, CREATED, clock);

        final Person office = personService.create("office", "Marlene", "Muster", "office@example.org", List.of(NOTIFICATION_EMAIL_OVERTIME_MANAGEMENT_APPLIED), List.of(USER, OFFICE));

        sut.sendOvertimeNotificationToManagement(overtime, overtimeComment);

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain(office.getEmail())).hasSize(1));

        // check attributes
        final Message msg = greenMail.getReceivedMessagesForDomain(office.getEmail())[0];
        assertThat(msg.getSubject()).contains("Lieschen Müller hat Überstunden eingetragen");
        assertThat(new InternetAddress(office.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        assertThat(readPlainContent(msg)).isEqualTo("""
            Hallo Marlene Muster,

            für Lieschen Müller wurden Überstunden eingetragen.

                https://localhost:8080/web/overtime/1

            Informationen zu den Überstunden:

                Mitarbeiter: Lieschen Müller
                Zeitraum:    16.04.2020 bis 23.04.2020
                Dauer:       55 Std. 12 Min.


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/%s/notifications anpassen.""".formatted(office.getId()));
    }

    @Test
    void ensureApplicantWithOvertimeNotificationGetMailIfOvertimeRecordedFromManagement() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setId(1L);
        person.setNotifications(List.of(NOTIFICATION_EMAIL_OVERTIME_APPLIED_BY_MANAGEMENT));

        final Person author = personService.create("office", "Marlene", "Muster", "office@example.org", List.of(), List.of(USER, OFFICE));

        final LocalDate startDate = LocalDate.of(2020, 4, 16);
        final LocalDate endDate = LocalDate.of(2020, 4, 23);
        final Overtime overtime = new Overtime(person, startDate, endDate, Duration.parse("P1DT30H72M"));
        overtime.setId(1L);

        final OvertimeComment overtimeComment = new OvertimeComment(author, overtime, CREATED, clock);

        sut.sendOvertimeNotificationToApplicantFromManagement(overtime, overtimeComment, author);

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain(person.getEmail())).hasSize(1));

        // check attributes
        final Message msg = greenMail.getReceivedMessagesForDomain(person.getEmail())[0];
        assertThat(msg.getSubject()).contains("Es wurden Überstunden für dich eingetragen");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        assertThat(readPlainContent(msg)).isEqualTo("""
            Hallo Lieschen Müller,

            Marlene Muster hat für dich Überstunden eingetragen.

                https://localhost:8080/web/overtime/1

            Informationen zu den Überstunden:

                Zeitraum:    16.04.2020 bis 23.04.2020
                Dauer:       55 Std. 12 Min.


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/1/notifications anpassen.""");
    }

    @Test
    void ensureApplicantWithOvertimeNotificationGetMailIfOvertimeRecordedFromApplicant() throws MessagingException, IOException {

        final Person author = personService.create("user", "Lieschen", "Müller", "lieschen@example.org", List.of(NOTIFICATION_EMAIL_OVERTIME_APPLIED), List.of(USER));

        final LocalDate startDate = LocalDate.of(2020, 4, 16);
        final LocalDate endDate = LocalDate.of(2020, 4, 23);
        final Overtime overtime = new Overtime(author, startDate, endDate, Duration.parse("P1DT30H72M"));
        overtime.setId(1L);

        final OvertimeComment overtimeComment = new OvertimeComment(author, overtime, CREATED, clock);

        sut.sendOvertimeNotificationToApplicantFromApplicant(overtime, overtimeComment);

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain(author.getEmail())).hasSize(1));

        // check attributes
        final Message msg = greenMail.getReceivedMessagesForDomain(author.getEmail())[0];
        assertThat(msg.getSubject()).contains("Du hast Überstunden eingetragen");
        assertThat(new InternetAddress(author.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        assertThat(readPlainContent(msg)).isEqualTo("""
            Hallo Lieschen Müller,

            du hast folgende Überstunden eingetragen.

                https://localhost:8080/web/overtime/1

            Informationen zu den Überstunden:

                Zeitraum:    16.04.2020 bis 23.04.2020
                Dauer:       55 Std. 12 Min.


            Deine E-Mail-Benachrichtigungen kannst du unter https://localhost:8080/web/person/%s/notifications anpassen.""".formatted(author.getId()));
    }

    private String readPlainContent(Message message) throws MessagingException, IOException {
        return message.getContent().toString().replaceAll("\\r", "");
    }
}
