package org.synyx.urlaubsverwaltung.calendarintegration;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.TestContainersBase;
import org.synyx.urlaubsverwaltung.absence.Absence;
import org.synyx.urlaubsverwaltung.mail.MailProperties;
import org.synyx.urlaubsverwaltung.person.Person;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.time.ZonedDateTime;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.demodatacreator.DemoDataCreator.createPerson;

@SpringBootTest(properties = {"spring.mail.port=3025", "spring.mail.host=localhost"})
@Transactional
public class CalendarMailServiceIT extends TestContainersBase {

    @Rule
    public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.SMTP_IMAP);

    @Autowired
    private CalendarMailService sut;

    @Autowired
    private MailProperties mailProperties;

    @Test
    public void ensureAdministratorGetsANotificationIfACalendarSyncErrorOccurred() throws MessagingException,
        IOException {

        final Person person = createPerson("user", "Lieschen", "Müller", "lieschen@firma.test");

        Absence absence = mock(Absence.class);
        when(absence.getPerson()).thenReturn(person);
        when(absence.getStartDate()).thenReturn(ZonedDateTime.now(UTC));
        when(absence.getEndDate()).thenReturn(ZonedDateTime.now(UTC));

        sut.sendCalendarSyncErrorNotification("Kalendername", absence, "Calendar sync failed");

        MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(mailProperties.getAdministrator());
        assertThat(inbox.length).isOne();

        Message msg = inbox[0];

        assertThat(msg.getSubject()).isEqualTo("Fehler beim Synchronisieren des Kalenders");

        String content = (String) msg.getContent();
        assertThat(content).contains("Kalendername");
        assertThat(content).contains("Calendar sync failed");
        assertThat(content).contains(person.getNiceName());
    }

    @Test
    public void ensureAdministratorGetsANotificationIfAEventUpdateErrorOccurred() throws MessagingException,
        IOException {

        final Person person = new Person();
        person.setFirstName("Henry");

        Absence absence = mock(Absence.class);
        when(absence.getPerson()).thenReturn(person);
        when(absence.getStartDate()).thenReturn(ZonedDateTime.now(UTC));
        when(absence.getEndDate()).thenReturn(ZonedDateTime.now(UTC));

        sut.sendCalendarUpdateErrorNotification("Kalendername", absence, "ID-123456", "event update failed");

        MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(mailProperties.getAdministrator());
        assertThat(inbox.length).isOne();

        Message msg = inbox[0];

        assertThat(msg.getSubject()).isEqualTo("Fehler beim Aktualisieren eines Kalendereintrags");

        String content = (String) msg.getContent();
        assertThat(content).contains("Kalendername");
        assertThat(content).contains("ID-123456");
        assertThat(content).contains("event update failed");
        assertThat(content).contains("Henry");
    }

    @Test
    public void ensureAdministratorGetsANotificationIfAnErrorOccurredDuringEventDeletion() throws MessagingException,
        IOException {

        sut.sendCalendarDeleteErrorNotification("Kalendername", "ID-123456", "event delete failed");

        MimeMessage[] inbox = greenMail.getReceivedMessagesForDomain(mailProperties.getAdministrator());
        assertThat(inbox.length).isOne();

        Message msg = inbox[0];

        assertThat(msg.getSubject()).isEqualTo("Fehler beim Löschen eines Kalendereintrags");

        String content = (String) msg.getContent();
        assertThat(content).contains("Kalendername");
        assertThat(content).contains("ID-123456");
        assertThat(content).contains("event delete failed");
    }
}
