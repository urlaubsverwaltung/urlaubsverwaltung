package org.synyx.urlaubsverwaltung.sicknote;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.TestContainersBase;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;

@SpringBootTest(properties = {"spring.mail.port=3025", "spring.mail.host=localhost"})
@Transactional
class SickNoteMailServiceIT extends TestContainersBase {

    @RegisterExtension
    public final GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP_IMAP);

    @Autowired
    private SickNoteMailService sickNoteMailService;
    @Autowired
    private PersonService personService;

    @MockBean
    private SickNoteService sickNoteService;

    @Test
    void sendEndOfSickPayNotification() throws MessagingException, IOException {

        final Person office = new Person("office", "Muster", "Marlene", "office@firma.test");
        office.setPermissions(singletonList(OFFICE));
        office.setNotifications(singletonList(NOTIFICATION_OFFICE));
        personService.save(office);

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@firma.test");

        final SickNote sickNote = new SickNote();
        sickNote.setPerson(person);
        sickNote.setStartDate(LocalDate.of(2020, 2, 1));
        sickNote.setEndDate(LocalDate.of(2020, 4, 1));

        final List<SickNote> sickNotes = List.of(sickNote);
        when(sickNoteService.getSickNotesReachingEndOfSickPay()).thenReturn(sickNotes);

        sickNoteMailService.sendEndOfSickPayNotification();

        // Where both mails sent?
        MimeMessage[] inboxOffice = greenMail.getReceivedMessagesForDomain(office.getEmail());
        assertThat(inboxOffice.length).isOne();

        final MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(person.getEmail());
        assertThat(inbox.length).isOne();

        // check email of user
        final Message msgUser = inbox[0];
        assertThat(msgUser.getSubject()).isEqualTo("Ende der Lohnfortzahlung");

        final String content = (String) msgUser.getContent();
        assertThat(content).contains("Hallo Lieschen Müller");
        assertThat(content).contains("Die Krankmeldung von Lieschen Müller");
        assertThat(content).contains("für den Zeitraum 01.02.2020 - 01.04.2020 erreicht in Kürze die 42 Tag(e) Grenze");

        // check email of office
        Message msgOffice = inboxOffice[0];
        assertThat(msgOffice.getSubject()).isEqualTo("Ende der Lohnfortzahlung");

        // check content of office email
        String contentOfficeMail = (String) msgOffice.getContent();
        assertThat(contentOfficeMail).contains("Hallo Marlene Muster");
        assertThat(contentOfficeMail).contains("Die Krankmeldung von Lieschen Müller");
        assertThat(contentOfficeMail).contains("für den Zeitraum 01.02.2020 - 01.04.2020 erreicht in Kürze die 42 Tag(e) Grenze");

    }
}
