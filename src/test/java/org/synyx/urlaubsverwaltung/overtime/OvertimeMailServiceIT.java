package org.synyx.urlaubsverwaltung.overtime;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.User;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.TestContainersBase;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.synyx.urlaubsverwaltung.overtime.OvertimeCommentAction.CREATED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_OVERTIME_APPLIED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_OVERTIME_APPLIED_BY_MANAGEMENT;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_OVERTIME_MANAGEMENT_APPLIED;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@SpringBootTest(properties = {"spring.mail.port=3025", "spring.mail.host=localhost"})
@Transactional
class OvertimeMailServiceIT extends TestContainersBase {

    private static final String EMAIL_LINE_BREAK = "\r\n";

    @RegisterExtension
    public final GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP_IMAP);

    @Autowired
    private OvertimeMailService sut;
    @Autowired
    private PersonService personService;
    @Autowired
    private Clock clock;

    @Test
    void ensureManagerWithOvertimeNotificationGetMailIfOvertimeRecorded() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen12@example.org");

        final LocalDate startDate = LocalDate.of(2020, 4, 16);
        final LocalDate endDate = LocalDate.of(2020, 4, 23);
        final Overtime overtime = new Overtime(person, startDate, endDate, Duration.parse("P1DT30H72M"));
        overtime.setId(1);

        final OvertimeComment overtimeComment = new OvertimeComment(person, overtime, CREATED, clock);

        final Person office = personService.create("office", "Marlene", "Muster", "office@example.org", List.of(NOTIFICATION_EMAIL_OVERTIME_MANAGEMENT_APPLIED), List.of(OFFICE));

        sut.sendOvertimeNotificationToManagement(overtime, overtimeComment);

        // was email sent to office?
        assertThat(greenMail.getReceivedMessagesForDomain(office.getEmail()).length).isOne();

        // check attributes
        final Message msg = greenMail.getReceivedMessagesForDomain(office.getEmail())[0];
        assertThat(msg.getSubject()).contains("Lieschen Müller hat Überstunden eingetragen");
        assertThat(new InternetAddress(office.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        assertThat(msg.getContent()).isEqualTo("Hallo Marlene Muster," + EMAIL_LINE_BREAK +
            "" + EMAIL_LINE_BREAK +
            "es wurden für Lieschen Müller Überstunden erfasst." + EMAIL_LINE_BREAK +
            "" + EMAIL_LINE_BREAK +
            "    https://localhost:8080/web/overtime/1" + EMAIL_LINE_BREAK +
            "" + EMAIL_LINE_BREAK +
            "Informationen zu den Überstunden:" + EMAIL_LINE_BREAK +
            "" + EMAIL_LINE_BREAK +
            "    Mitarbeiter: Lieschen Müller" + EMAIL_LINE_BREAK +
            "    Zeitraum:    16.04.2020 - 23.04.2020" + EMAIL_LINE_BREAK +
            "    Dauer:       55 Std. 12 Min.");
    }

    @Test
    void ensureApplicantWithOvertimeNotificationGetMailIfOvertimeRecordedFromManagement() throws MessagingException, IOException {

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");
        person.setNotifications(List.of(NOTIFICATION_EMAIL_OVERTIME_APPLIED_BY_MANAGEMENT));

        final Person author = personService.create("office", "Marlene", "Muster", "office@example.org", List.of(), List.of(USER, OFFICE));

        final LocalDate startDate = LocalDate.of(2020, 4, 16);
        final LocalDate endDate = LocalDate.of(2020, 4, 23);
        final Overtime overtime = new Overtime(person, startDate, endDate, Duration.parse("P1DT30H72M"));
        overtime.setId(1);

        final OvertimeComment overtimeComment = new OvertimeComment(author, overtime, CREATED, clock);

        sut.sendOvertimeNotificationToApplicantFromManagement(overtime, overtimeComment, author);

        // was email sent to office?
        assertThat(greenMail.getReceivedMessagesForDomain(person.getEmail()).length).isOne();

        // check attributes
        final Message msg = greenMail.getReceivedMessagesForDomain(person.getEmail())[0];
        assertThat(msg.getSubject()).contains("Es wurden Überstunden für dich eingetragen");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        assertThat(msg.getContent()).isEqualTo("Hallo Lieschen Müller," + EMAIL_LINE_BREAK +
            "" + EMAIL_LINE_BREAK +
            "es wurden für dich von Marlene Muster Überstunden erfasst." + EMAIL_LINE_BREAK +
            "" + EMAIL_LINE_BREAK +
            "    https://localhost:8080/web/overtime/1" + EMAIL_LINE_BREAK +
            "" + EMAIL_LINE_BREAK +
            "Informationen zu den Überstunden:" + EMAIL_LINE_BREAK +
            "" + EMAIL_LINE_BREAK +
            "    Zeitraum:    16.04.2020 - 23.04.2020" + EMAIL_LINE_BREAK +
            "    Dauer:       55 Std. 12 Min.");
    }

    @Test
    void ensureApplicantWithOvertimeNotificationGetMailIfOvertimeRecordedFromApplicant() throws MessagingException, IOException {

        final Person author = personService.create("user", "Lieschen", "Müller", "lieschen@example.org", List.of(NOTIFICATION_EMAIL_OVERTIME_APPLIED), List.of(USER));

        final LocalDate startDate = LocalDate.of(2020, 4, 16);
        final LocalDate endDate = LocalDate.of(2020, 4, 23);
        final Overtime overtime = new Overtime(author, startDate, endDate, Duration.parse("P1DT30H72M"));
        overtime.setId(1);

        final OvertimeComment overtimeComment = new OvertimeComment(author, overtime, CREATED, clock);

        sut.sendOvertimeNotificationToApplicantFromApplicant(overtime, overtimeComment);

        // was email sent to office?
        assertThat(greenMail.getReceivedMessagesForDomain(author.getEmail()).length).isOne();

        // check attributes
        final Message msg = greenMail.getReceivedMessagesForDomain(author.getEmail())[0];
        assertThat(msg.getSubject()).contains("Du hast Überstunden eingetragen");
        assertThat(new InternetAddress(author.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        assertThat(msg.getContent()).isEqualTo("Hallo Lieschen Müller," + EMAIL_LINE_BREAK +
            "" + EMAIL_LINE_BREAK +
            "du hast folgende Überstunden erfasst." + EMAIL_LINE_BREAK +
            "" + EMAIL_LINE_BREAK +
            "    https://localhost:8080/web/overtime/1" + EMAIL_LINE_BREAK +
            "" + EMAIL_LINE_BREAK +
            "Informationen zu den Überstunden:" + EMAIL_LINE_BREAK +
            "" + EMAIL_LINE_BREAK +
            "    Zeitraum:    16.04.2020 - 23.04.2020" + EMAIL_LINE_BREAK +
            "    Dauer:       55 Std. 12 Min.");
    }
}
