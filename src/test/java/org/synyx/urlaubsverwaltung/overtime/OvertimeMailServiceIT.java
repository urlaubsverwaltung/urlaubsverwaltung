package org.synyx.urlaubsverwaltung.overtime;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.mock_javamail.Mailbox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.settings.MailSettings;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsDAO;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.synyx.urlaubsverwaltung.overtime.OvertimeAction.CREATED;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("dev")
public class OvertimeMailServiceIT {

    @Autowired
    private OvertimeMailService sut;

    @Autowired
    private PersonService personService;
    @Autowired
    private SettingsService settingsService;
    @Autowired
    private SettingsDAO settingsDAO;

    @After
    public void setUp() {
        Mailbox.clearAll();
    }

    @Test
    public void ensureOfficeWithOvertimeNotificationGetMailIfOvertimeRecorded() throws MessagingException, IOException {

        activateMailSettings();

        final Person person = TestDataCreator.createPerson("user", "Lieschen", "Müller", "lieschen@firma.test");
        final Overtime overtimeRecord = TestDataCreator.createOvertimeRecord(person);
        final OvertimeComment overtimeComment = new OvertimeComment(person, overtimeRecord, CREATED);

        final List<Person> officePeople = personService.getPersonsByRole(OFFICE);
        final Person office = officePeople.get(0);

        sut.sendOvertimeNotification(overtimeRecord, overtimeComment);

        // was email sent to office?
        List<Message> inboxOffice = Mailbox.get(office.getEmail());
        assertThat(inboxOffice.size()).isOne();

        // check attributes
        Message msg = inboxOffice.get(0);
        assertThat(msg.getSubject()).contains("Es wurden Überstunden eingetragen");
        assertThat(new InternetAddress(office.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        String text = (String) msg.getContent();
        assertThat(text).contains("Hallo Office");
        assertThat(text).contains("es wurden Überstunden erfasst");
        assertThat(text).contains("/web/overtime/1234");
    }

    private void activateMailSettings() {
        final Settings settings = settingsService.getSettings();
        final MailSettings mailSettings = settings.getMailSettings();
        mailSettings.setActive(true);
        settings.setMailSettings(mailSettings);
        settingsDAO.save(settings);
    }
}
