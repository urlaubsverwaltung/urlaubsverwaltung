package org.synyx.urlaubsverwaltung.sicknote.sicknote;

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
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;

@SpringBootTest(properties = {"spring.mail.port=3025", "spring.mail.host=localhost"})
@Transactional
class SickNoteMailServiceIT extends TestContainersBase {

    private static final String EMAIL_LINE_BREAK = "\r\n";

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

        final Person office = new Person("office", "Muster", "Marlene", "office@example.org");
        office.setPermissions(List.of(OFFICE));
        office.setNotifications(List.of(NOTIFICATION_OFFICE));
        personService.save(office);

        final Person person = new Person("user", "Müller", "Lieschen", "lieschen@example.org");

        final SickNote sickNote = new SickNote();
        sickNote.setId(1);
        sickNote.setPerson(person);
        sickNote.setStartDate(LocalDate.of(2022, 2, 1));
        sickNote.setEndDate(LocalDate.of(2022, 4, 1));

        final List<SickNote> sickNotes = List.of(sickNote);
        when(sickNoteService.getSickNotesReachingEndOfSickPay()).thenReturn(sickNotes);

        sickNoteMailService.sendEndOfSickPayNotification();

        // Where both mails sent?
        final MimeMessage[] inboxOffice = greenMail.getReceivedMessagesForDomain(office.getEmail());
        assertThat(inboxOffice.length).isOne();

        final MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(person.getEmail());
        assertThat(inbox.length).isOne();

        // check email of user
        final Message msgUser = inbox[0];
        assertThat(msgUser.getSubject()).isEqualTo("Ende deiner Lohnfortzahlung");

        final String content = (String) msgUser.getContent();
        assertThat(content).contains("Hallo Lieschen Müller," + EMAIL_LINE_BREAK +
            "" + EMAIL_LINE_BREAK +
            "dein Anspruch auf Lohnfortzahlung von 42 Tag(en) endete am 14.03.2022." + EMAIL_LINE_BREAK +
            "" + EMAIL_LINE_BREAK +
            "    https://localhost:8080/web/sicknote/1" + EMAIL_LINE_BREAK +
            "" + EMAIL_LINE_BREAK +
            "Informationen zur Krankmeldung:" + EMAIL_LINE_BREAK +
            "" + EMAIL_LINE_BREAK +
            "    Mitarbeiter:                  Lieschen Müller" + EMAIL_LINE_BREAK +
            "    Zeitraum:                     01.02.2022 bis 01.04.2022" + EMAIL_LINE_BREAK +
            "    Anspruch auf Lohnfortzahlung: 01.02.2022 bis 14.03.2022" + EMAIL_LINE_BREAK +
            "" + EMAIL_LINE_BREAK +
            "" + EMAIL_LINE_BREAK +
            "Hinweis:" + EMAIL_LINE_BREAK +
            "Der Anspruch auf Lohnfortzahlung durch den Arbeitgeber im Krankheitsfall besteht für maximal 42 Tage" + EMAIL_LINE_BREAK +
            "(fortlaufende Kalendertage ohne Rücksicht auf die Arbeitstage des erkrankten Arbeitnehmers, Sonn- oder Feiertage)." + EMAIL_LINE_BREAK +
            "Danach wird für gesetzlich Krankenversicherte in der Regel Krankengeld von der Krankenkasse gezahlt.");

        // check email of office
        final Message msgOffice = inboxOffice[0];
        assertThat(msgOffice.getSubject()).isEqualTo("Ende der Lohnfortzahlung von Lieschen Müller");

        // check content of office email
        final String contentOfficeMail = (String) msgOffice.getContent();
        assertThat(contentOfficeMail).contains("Hallo Marlene Muster," + EMAIL_LINE_BREAK +
            "" + EMAIL_LINE_BREAK +
            "der Anspruch auf Lohnfortzahlung von Lieschen Müller von 42 Tag(en) endete am 14.03.2022." + EMAIL_LINE_BREAK +
            "" + EMAIL_LINE_BREAK +
            "    https://localhost:8080/web/sicknote/1" + EMAIL_LINE_BREAK +
            "" + EMAIL_LINE_BREAK +
            "Informationen zur Krankmeldung:" + EMAIL_LINE_BREAK +
            "" + EMAIL_LINE_BREAK +
            "    Mitarbeiter:                  Lieschen Müller" + EMAIL_LINE_BREAK +
            "    Zeitraum:                     01.02.2022 bis 01.04.2022" + EMAIL_LINE_BREAK +
            "    Anspruch auf Lohnfortzahlung: 01.02.2022 bis 14.03.2022" + EMAIL_LINE_BREAK +
            "" + EMAIL_LINE_BREAK +
            "" + EMAIL_LINE_BREAK +
            "Hinweis:" + EMAIL_LINE_BREAK +
            "Der Anspruch auf Lohnfortzahlung durch den Arbeitgeber im Krankheitsfall besteht für maximal 42 Tage" + EMAIL_LINE_BREAK +
            "(fortlaufende Kalendertage ohne Rücksicht auf die Arbeitstage des erkrankten Arbeitnehmers, Sonn- oder Feiertage)." + EMAIL_LINE_BREAK +
            "Danach wird für gesetzlich Krankenversicherte in der Regel Krankengeld von der Krankenkasse gezahlt.");

        verify(sickNoteService).setEndOfSickPayNotificationSend(sickNote);
    }
}
