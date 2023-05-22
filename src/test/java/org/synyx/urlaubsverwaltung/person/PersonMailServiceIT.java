package org.synyx.urlaubsverwaltung.person;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.TestContainersBase;
import org.synyx.urlaubsverwaltung.person.web.PersonPermissionsRoleDto;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_PERSON_NEW_MANAGEMENT_ALL;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;
import static org.synyx.urlaubsverwaltung.person.web.PersonPermissionsRoleDto.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.web.PersonPermissionsRoleDto.SECOND_STAGE_AUTHORITY;

@SpringBootTest(properties = {"spring.mail.port=3025", "spring.mail.host=localhost"})
@Transactional
class PersonMailServiceIT extends TestContainersBase {

    private static final String EMAIL_LINE_BREAK = "\r\n";

    @RegisterExtension
    static final GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP_IMAP);

    @Autowired
    private PersonMailService sut;
    @Autowired
    private PersonService personService;

    @Test
    void ensureOfficeWithNotificationsGetMailNewPersonIsCreated() throws MessagingException, IOException {

        final Person createdPerson = new Person("user", "Müller", "Lieschen", "lieschen12@example.org");
        createdPerson.setId(1);

        final Person office = personService.create(
            "office",
            "Marlene", "Muster",
            "office@example.org",
            List.of(NOTIFICATION_EMAIL_PERSON_NEW_MANAGEMENT_ALL),
            List.of(OFFICE)
        );

        sut.sendPersonCreationNotification(new PersonCreatedEvent(personService, createdPerson.getId(), createdPerson.getNiceName(), createdPerson.getUsername(), createdPerson.getEmail(), createdPerson.isActive()));

        // was email sent to office?
        final MimeMessage[] inboxOffice = greenMail.getReceivedMessagesForDomain(office.getEmail());
        assertThat(inboxOffice).hasSize(2);
        assertThat(inboxOffice[0].getSubject()).isEqualTo("Ein neuer Benutzer wurde erstellt");

        // check attributes
        final Message msg = inboxOffice[1];
        assertThat(msg.getSubject()).contains("Ein neuer Benutzer wurde erstellt");
        assertThat(new InternetAddress(office.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        assertThat(msg.getContent()).isEqualTo("Hallo Marlene Muster," + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "es wurde ein neuer Benutzer erstellt." + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "    Lieschen Müller" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Du kannst unter folgender Adresse die Einstellungen des Benutzers einsehen und anpassen:" + EMAIL_LINE_BREAK +
            "https://localhost:8080/web/person/1"
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

        // was email sent to person?
        final MimeMessage[] inboxPerson = greenMail.getReceivedMessagesForDomain(person.getEmail());
        assertThat(inboxPerson).hasSize(1);

        // check attributes
        final Message msg = inboxPerson[0];
        assertThat(msg.getSubject()).isEqualTo("Du hast neue Berechtigungen erhalten");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        assertThat(msg.getContent()).isEqualTo("Hallo Marlene Muster," + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "du hast folgende neue Berechtigungen erhalten:" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "- Freigabe-Verantwortlicher" + EMAIL_LINE_BREAK +
            "- Abteilungsleiter" + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "dadurch kannst du auch weitere E-Mail-Benachrichtigungen unter https://localhost:8080/web/person/" + person.getId() + "/notifications einrichten." + EMAIL_LINE_BREAK +
            EMAIL_LINE_BREAK +
            "Eine Übersicht deiner aktuellen Berechtigungen und E-Mail-Benachrichtigungen kannst du in deinem Konto unter https://localhost:8080/web/person/" + person.getId() + " einsehen."
        );
    }
}
