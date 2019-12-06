package org.synyx.urlaubsverwaltung.mail;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.synyx.urlaubsverwaltung.person.Person;

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
    private MailOptionProvider mailOptionProvider;
    @Mock
    private RecipientService recipientService;

    @Before
    public void setUp() {

        when(messageSource.getMessage(any(), any(), any())).thenReturn("subject");
        when(mailBuilder.buildMailBody(any(), any(), any())).thenReturn("emailBody");
        when(mailOptionProvider.getSender()).thenReturn("no-reply@firma.test");

        sut = new MailServiceImpl(messageSource, mailBuilder, mailSender, mailOptionProvider, recipientService);
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

        verify(mailSender).sendEmail(eq("no-reply@firma.test"), eq(recipients), eq("subject"), eq("emailBody"));
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

        verify(mailSender).sendEmail(eq("no-reply@firma.test"), eq(recipients), eq("subject"), eq("emailBody"));
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

        verify(mailSender).sendEmail(eq("no-reply@firma.test"), eq(singletonList(hansMail)), eq("subject"), eq("emailBody"));
        verify(mailSender).sendEmail(eq("no-reply@firma.test"), eq(singletonList(franzMail)), eq("subject"), eq("emailBody"));
    }


    @Test
    public void sendTechnicalMail() {

        final String subjectMessageKey = "subject.overtime.created";
        final String templateName = "overtime_office";
        String to = "admin@firma.test";
        when(mailOptionProvider.getAdministrator()).thenReturn(to);

        sut.sendTechnicalMail(subjectMessageKey, templateName, new HashMap<>());

        verify(mailSender).sendEmail(eq("no-reply@firma.test"), eq(singletonList(to)), eq("subject"), eq("emailBody"));
    }
}
