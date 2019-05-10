package org.synyx.urlaubsverwaltung.application.service;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.mock_javamail.Mailbox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationComment;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.MailSettings;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsDAO;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static java.time.ZoneOffset.UTC;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.synyx.urlaubsverwaltung.application.domain.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("dev")
public class ApplicationMailServiceIT {

    @Autowired
    private ApplicationMailService sut;

    @Autowired
    private SettingsService settingsService;
    @Autowired
    private SettingsDAO settingsDAO;

    @After
    public void setUp() {
        Mailbox.clearAll();
    }

    @Test
    public void ensureNotificationAboutAllowedApplicationIsSentToOfficeAndThePerson() throws MessagingException,
        IOException {

        activateMailSettings();

        final Person person = TestDataCreator.createPerson("user", "Lieschen", "Müller", "lieschen@firma.test");

        final Person office = TestDataCreator.createPerson("office", "Marlene", "Muster", "office@firma.test");
        office.setPermissions(singletonList(OFFICE));

        final Person boss = TestDataCreator.createPerson("boss", "Hugo", "Boss", "boss@firma.test");
        boss.setPermissions(singletonList(BOSS));

        final Application application = createApplication(person);
        application.setBoss(boss);

        final ApplicationComment comment = new ApplicationComment(boss);
        comment.setText("OK, Urlaub kann genommen werden");

        sut.sendAllowedNotification(application, comment);

        // were both emails sent?
        List<Message> inboxOffice = Mailbox.get(office.getEmail());
        assertThat(inboxOffice.size()).isOne();

        List<Message> inboxUser = Mailbox.get(person.getEmail());
        assertThat(inboxUser.size()).isOne();

        // check email user attributes
        Message msg = inboxUser.get(0);
        assertThat(msg.getSubject()).isEqualTo("Dein Urlaubsantrag wurde bewilligt");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of user email
        String contentUser = (String) msg.getContent();
        assertThat(contentUser).contains("Lieschen Müller");
        assertThat(contentUser).contains("gestellter Antrag wurde von Hugo Boss genehmigt");
        assertThat(contentUser).contains(comment.getText());
        assertThat(contentUser).contains(comment.getPerson().getNiceName());
        assertThat(contentUser).contains("/web/application/1234");

        // check email office attributes
        Message msgOffice = inboxOffice.get(0);
        assertThat(msgOffice.getSubject()).isEqualTo("Neuer bewilligter Antrag");
        assertThat(new InternetAddress(office.getEmail())).isEqualTo(msgOffice.getAllRecipients()[0]);

        // check content of office email
        String contentOfficeMail = (String) msgOffice.getContent();
        assertThat(contentOfficeMail).contains("Hallo Office");
        assertThat(contentOfficeMail).contains("es liegt ein neuer genehmigter Antrag vor");
        assertThat(contentOfficeMail).contains("Lieschen Müller");
        assertThat(contentOfficeMail).contains("Erholungsurlaub");
        assertThat(contentOfficeMail).contains(comment.getText());
        assertThat(contentOfficeMail).contains(comment.getPerson().getNiceName());
        assertThat(contentOfficeMail).contains("es liegt ein neuer genehmigter Antrag vor:");
        assertThat(contentOfficeMail).contains("/web/application/1234");
    }

    @Test
    public void ensureNotificationAboutRejectedApplicationIsSentToPerson() throws MessagingException, IOException {

        activateMailSettings();

        final Person person = TestDataCreator.createPerson("user", "Lieschen", "Müller", "lieschen@firma.test");

        final Person boss = TestDataCreator.createPerson("boss", "Hugo", "Boss", "boss@firma.test");
        boss.setPermissions(singletonList(BOSS));

        final ApplicationComment comment = new ApplicationComment(boss);
        comment.setText("Geht leider nicht zu dem Zeitraum");

        final Application application = createApplication(person);
        application.setBoss(boss);

        sut.sendRejectedNotification(application, comment);

        // was email sent?
        List<Message> inbox = Mailbox.get(person.getEmail());
        assertThat(inbox.size()).isOne();

        // check content of user email
        Message msg = inbox.get(0);
        assertThat(msg.getSubject()).isEqualTo("Dein Urlaubsantrag wurde abgelehnt");
        assertThat(new InternetAddress(person.getEmail())).isEqualTo(msg.getAllRecipients()[0]);

        // check content of email
        String content = (String) msg.getContent();
        assertThat(content).contains("Hallo Lieschen Müller");
        assertThat(content).contains("wurde leider von Hugo Boss abgelehnt");
        assertThat(content).contains("/web/application/1234");
        assertThat(content).contains(comment.getText());
        assertThat(content).contains(comment.getPerson().getNiceName());
    }

    private Application createApplication(Person person) {

        LocalDate now = LocalDate.now(UTC);

        Application application = new Application();
        application.setId(1234);
        application.setPerson(person);
        application.setVacationType(TestDataCreator.createVacationType(HOLIDAY, "application.data.vacationType.holiday"));
        application.setDayLength(FULL);
        application.setApplicationDate(now);
        application.setStartDate(now);
        application.setEndDate(now);
        application.setApplier(person);

        return application;
    }

    private void activateMailSettings() {
        final Settings settings = settingsService.getSettings();
        final MailSettings mailSettings = settings.getMailSettings();
        mailSettings.setActive(true);
        settings.setMailSettings(mailSettings);
        settingsDAO.save(settings);
    }
}
