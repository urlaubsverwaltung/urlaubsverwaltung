package org.synyx.urlaubsverwaltung.person;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.SingleTenantTestContainersBase;
import org.synyx.urlaubsverwaltung.person.web.PersonPermissionsRoleDto;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_PERSON_NEW_MANAGEMENT_ALL;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;
import static org.synyx.urlaubsverwaltung.person.web.PersonPermissionsRoleDto.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.web.PersonPermissionsRoleDto.SECOND_STAGE_AUTHORITY;

@SpringBootTest(properties = {"spring.mail.port=3025", "spring.mail.host=localhost"})
@Transactional
class PersonMailServiceIT extends SingleTenantTestContainersBase {

    @RegisterExtension
    static final GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP_IMAP);

    @Autowired
    private PersonMailService sut;
    @Autowired
    private PersonService personService;

    @Test
    void ensureOfficeWithNotificationsGetMailNewPersonIsCreated() throws MessagingException, IOException {

        final Person office = personService.create(
            "office",
            "Marlene", "Muster",
            "office@example.org",
            List.of(NOTIFICATION_EMAIL_PERSON_NEW_MANAGEMENT_ALL),
            List.of(USER, OFFICE)
        );

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain(office.getEmail())).hasSize(1));

        final MimeMessage[] inboxOffice = greenMail.getReceivedMessagesForDomain(office.getEmail());
        assertThat(inboxOffice[0].getSubject()).isEqualTo("Ein neuer Benutzer wurde erstellt");
        final Message msg = inboxOffice[0];
        assertThat(msg.getSubject()).contains("Ein neuer Benutzer wurde erstellt");
        assertThat(new InternetAddress(office.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        assertThat(readPlainContent(msg)).isEqualTo("""
            Hallo Marlene Muster,

            es wurde ein neuer Benutzer erstellt.

                Marlene Muster

            Du kannst unter folgender Adresse die Einstellungen des Benutzers einsehen und anpassen:
            https://localhost:8080/web/person/%s""".formatted(office.getId())
        );
    }

    @Test
    void ensureSendsPersonGainedPermissionsNotification() throws MessagingException, IOException {

        final Person person = personService.create(
            "user",
            "Marlene",
            "Muster",
            "user@example.org",
            List.of(),
            List.of(USER)
        );

        final List<PersonPermissionsRoleDto> addedPermissions = List.of(SECOND_STAGE_AUTHORITY, DEPARTMENT_HEAD);

        sut.sendPersonGainedMorePermissionsNotification(person, addedPermissions);

        await()
            .atMost(Duration.ofSeconds(1))
            .untilAsserted(() -> assertThat(greenMail.getReceivedMessagesForDomain(person.getEmail())).hasSize(1));

        // was email sent to person?
        final MimeMessage[] inboxPerson = greenMail.getReceivedMessagesForDomain(person.getEmail());
        assertThat(inboxPerson).hasSize(1);

        // check attributes
        final Message msg = inboxPerson[0];
        assertThat(msg.getSubject()).isEqualTo("Du hast neue Berechtigungen erhalten");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        assertThat(readPlainContent(msg)).isEqualTo("""
            Hallo Marlene Muster,

            du hast folgende neue Berechtigungen erhalten:

            - Freigabe-Verantwortlicher
            - Abteilungsleiter

            dadurch kannst du auch weitere E-Mail-Benachrichtigungen unter https://localhost:8080/web/person/%s/notifications einrichten.

            Eine Übersicht deiner aktuellen Berechtigungen und E-Mail-Benachrichtigungen kannst du in deinem Konto unter https://localhost:8080/web/person/%s einsehen.""".formatted(person.getId(), person.getId())
        );
    }

    private String readPlainContent(Message message) throws MessagingException, IOException {
        return message.getContent().toString().replaceAll("\\r", "");
    }
}
