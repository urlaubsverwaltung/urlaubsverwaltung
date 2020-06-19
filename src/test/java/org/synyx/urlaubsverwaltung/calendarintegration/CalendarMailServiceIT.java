package org.synyx.urlaubsverwaltung.calendarintegration;


import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.mock_javamail.Mailbox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.absence.Absence;
import org.synyx.urlaubsverwaltung.mail.MailProperties;
import org.synyx.urlaubsverwaltung.person.Person;

import javax.mail.Message;
import javax.mail.MessagingException;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.demodatacreator.DemoDataCreator.createPerson;

@SpringBootTest
@RunWith(SpringRunner.class)
@Transactional
public class CalendarMailServiceIT {

    @Autowired
    private CalendarMailService sut;

    @Autowired
    private MailProperties mailProperties;

    @After
    public void tearDown() {
        Mailbox.clearAll();
    }

    @Test
    public void ensureAdministratorGetsANotificationIfACalendarSyncErrorOccurred() throws MessagingException,
        IOException {

        final Person person = createPerson("user", "Lieschen", "Müller", "lieschen@firma.test");

        Absence absence = mock(Absence.class);
        when(absence.getPerson()).thenReturn(person);
        when(absence.getStartDate()).thenReturn(ZonedDateTime.now(UTC));
        when(absence.getEndDate()).thenReturn(ZonedDateTime.now(UTC));

        sut.sendCalendarSyncErrorNotification("Kalendername", absence, "Calendar sync failed");

        List<Message> inbox = Mailbox.get(mailProperties.getAdministrator());
        assertThat(inbox.size()).isOne();

        Message msg = inbox.get(0);

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

        List<Message> inbox = Mailbox.get(mailProperties.getAdministrator());
        assertThat(inbox.size()).isOne();

        Message msg = inbox.get(0);

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

        List<Message> inbox = Mailbox.get(mailProperties.getAdministrator());
        assertThat(inbox.size()).isOne();

        Message msg = inbox.get(0);

        assertThat(msg.getSubject()).isEqualTo("Fehler beim Löschen eines Kalendereintrags");

        String content = (String) msg.getContent();
        assertThat(content).contains("Kalendername");
        assertThat(content).contains("ID-123456");
        assertThat(content).contains("event delete failed");
    }
}
