package org.synyx.urlaubsverwaltung.mail;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.MailSettings;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.person.MailNotification.OVERTIME_NOTIFICATION_OFFICE;


@RunWith(MockitoJUnitRunner.class)
public class MailServiceImplTest {

    private MailServiceImpl sut;

    @Mock
    private MessageSource messageSource;
    @Mock
    private MailBuilder mailBuilder;
    @Mock
    private MailSender mailSender;
    @Mock
    private RecipientService recipientService;
    @Mock
    private SettingsService settingsService;

    @Before
    public void setUp() {

        final Settings settings = new Settings();
        settings.getMailSettings().setActive(true);
        settings.getMailSettings().setAdministrator("admin@firma.test");
        when(settingsService.getSettings()).thenReturn(settings);

        when(messageSource.getMessage(any(), any(), any())).thenReturn("subject");
        when(mailBuilder.buildMailBody(any(), any(), any())).thenReturn("emailBody");

        sut = new MailServiceImpl(messageSource, mailBuilder, mailSender, recipientService, settingsService);
    }

    @Test
    public void sendMailToWithNotification() {

        final Person person = new Person();
        final List<Person> persons = singletonList(person);
        when(recipientService.getRecipientsWithNotificationType(OVERTIME_NOTIFICATION_OFFICE)).thenReturn(persons);

        final List<String> recipients = singletonList("email@firma.test");
        when(recipientService.getMailAddresses(persons)).thenReturn(recipients);

        final Map<String, Object> model = new HashMap<>();
        model.put("someModel", "something");

        final String subjectMessageKey = "subject.overtime.created";
        final String templateName = "overtime_office";

        sut.sendMailTo(OVERTIME_NOTIFICATION_OFFICE, subjectMessageKey, templateName, model);

        verify(mailSender).sendEmail(any(MailSettings.class), eq(recipients), eq("subject"), eq("emailBody"));
    }

    @Test
    public void sendMailToWithPerson() {

        final Person hans = new Person();
        final List<Person> persons = singletonList(hans);

        final List<String> recipients = singletonList("hans@firma.test");
        when(recipientService.getMailAddresses(persons)).thenReturn(recipients);

        final String subjectMessageKey = "subject.overtime.created";
        final String templateName = "overtime_office";

        sut.sendMailTo(hans, subjectMessageKey, templateName, new HashMap<>());

        verify(mailSender).sendEmail(any(MailSettings.class), eq(recipients), eq("subject"), eq("emailBody"));
    }

    @Test
    public void sendMailToEachPerson() {

        final Person hans = new Person();
        final String hansMail = "hans@firma.test";
        final List<String> recipientHans = singletonList(hansMail);
        when(recipientService.getMailAddresses(hans)).thenReturn(recipientHans);

        final Person franz = new Person();
        final String franzMail = "franz@firma.test";
        final List<String> recipientFranz = singletonList(franzMail);
        when(recipientService.getMailAddresses(franz)).thenReturn(recipientFranz);

        final List<Person> persons = asList(hans, franz);

        final String subjectMessageKey = "subject.overtime.created";
        final String templateName = "overtime_office";

        sut.sendMailToEach(persons, subjectMessageKey, templateName, new HashMap<>());

        verify(mailSender).sendEmail(any(MailSettings.class), eq(singletonList(hansMail)), eq("subject"), eq("emailBody"));
        verify(mailSender).sendEmail(any(MailSettings.class), eq(singletonList(franzMail)), eq("subject"), eq("emailBody"));
    }


    @Test
    public void sendTechnicalMail() {

        final String subjectMessageKey = "subject.overtime.created";
        final String templateName = "overtime_office";

        sut.sendTechnicalMail(subjectMessageKey, templateName, new HashMap<>());

        verify(mailSender).sendEmail(any(MailSettings.class), eq(singletonList("admin@firma.test")), eq("subject"), eq("emailBody"));
    }
}
