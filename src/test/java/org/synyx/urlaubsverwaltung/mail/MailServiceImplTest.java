package org.synyx.urlaubsverwaltung.mail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_USER;
import static org.synyx.urlaubsverwaltung.person.MailNotification.OVERTIME_NOTIFICATION_OFFICE;

@ExtendWith(MockitoExtension.class)
class MailServiceImplTest {

    private MailServiceImpl sut;

    @Mock
    private MessageSource messageSource;
    @Mock
    private MailContentBuilder mailContentBuilder;
    @Mock
    private MailSenderService mailSenderService;
    @Mock
    private MailProperties mailProperties;
    @Mock
    private PersonService personService;

    @BeforeEach
    void setUp() {

        when(messageSource.getMessage(any(), any(), any())).thenReturn("subject");
        when(mailContentBuilder.buildMailBody(any(), any(), any())).thenReturn("emailBody");
        when(mailProperties.getSender()).thenReturn("no-reply@firma.test");

        sut = new MailServiceImpl(messageSource, mailContentBuilder, mailSenderService, mailProperties, personService);
    }

    @Test
    void sendMailToWithNotification() {

        setupMockServletRequest();

        final Person person = new Person();
        person.setEmail("mail@example.org");
        final List<Person> persons = singletonList(person);
        when(personService.getPersonsWithNotificationType(OVERTIME_NOTIFICATION_OFFICE)).thenReturn(persons);

        final Map<String, Object> model = new HashMap<>();
        model.put("someModel", "something");

        final String subjectMessageKey = "subject.overtime.created";
        final String templateName = "overtime_office";
        final Mail mail = Mail.builder()
            .withRecipient(OVERTIME_NOTIFICATION_OFFICE)
            .withSubject(subjectMessageKey)
            .withTemplate(templateName, model)
            .build();

        sut.send(mail);

        verify(mailSenderService).sendEmail(eq("no-reply@firma.test"), eq(List.of("mail@example.org")), eq("subject"), eq("emailBody"));
    }

    @Test
    void sendMailToWithPerson() {

        setupMockServletRequest();

        final Person hans = new Person();
        hans.setEmail("hans@firma.test");

        final String subjectMessageKey = "subject.overtime.created";
        final String templateName = "overtime_office";

        final Mail mail = Mail.builder()
            .withRecipient(hans)
            .withSubject(subjectMessageKey)
            .withTemplate(templateName, new HashMap<>())
            .build();

        sut.send(mail);

        verify(mailSenderService).sendEmail(eq("no-reply@firma.test"), eq(List.of("hans@firma.test")), eq("subject"), eq("emailBody"));
    }

    @Test
    void sendMailToEachPerson() {

        setupMockServletRequest();

        final Person hans = new Person();
        hans.setEmail("hans@firma.test");

        final Person franz = new Person();
        franz.setEmail("franz@firma.test");
        final List<Person> persons = asList(hans, franz);

        final String subjectMessageKey = "subject.overtime.created";
        final String templateName = "overtime_office";

        final Mail mail = Mail.builder()
            .withRecipient(persons)
            .withSubject(subjectMessageKey)
            .withTemplate(templateName, new HashMap<>())
            .build();

        sut.send(mail);

        verify(mailSenderService).sendEmail(eq("no-reply@firma.test"), eq(singletonList("hans@firma.test")), eq("subject"), eq("emailBody"));
        verify(mailSenderService).sendEmail(eq("no-reply@firma.test"), eq(singletonList("franz@firma.test")), eq("subject"), eq("emailBody"));
    }

    @Test
    void sendMailWithAttachment() {

        setupMockServletRequest();

        final Person hans = new Person();
        hans.setEmail("hans@firma.test");

        final Person franz = new Person();
        franz.setEmail("franz@firma.test");
        final List<Person> persons = asList(hans, franz);

        final String subjectMessageKey = "subject.overtime.created";
        final String templateName = "overtime_office";

        final File iCal = new File("calendar.ics");
        iCal.deleteOnExit();

        final Mail mail = Mail.builder()
            .withRecipient(persons)
            .withSubject(subjectMessageKey)
            .withTemplate(templateName, new HashMap<>())
            .withAttachment("fileName", iCal)
            .build();

        sut.send(mail);

        verify(mailSenderService).sendEmail(eq("no-reply@firma.test"), eq(List.of("franz@firma.test")), eq("subject"), eq("emailBody"), eq(List.of(new MailAttachment("fileName", iCal))));
        verify(mailSenderService).sendEmail(eq("no-reply@firma.test"), eq(List.of("hans@firma.test")), eq("subject"), eq("emailBody"), eq(List.of(new MailAttachment("fileName", iCal))));
    }

    @Test
    void sendMailWithAttachmentToEachPerson() {

        setupMockServletRequest();

        final Person hans = new Person();
        hans.setEmail("hans@firma.test");

        final Person franz = new Person();
        franz.setEmail("franz@firma.test");
        final List<Person> persons = asList(hans, franz);

        final String subjectMessageKey = "subject.overtime.created";
        final String templateName = "overtime_office";

        final File iCal = new File("calendar.ics");
        iCal.deleteOnExit();

        final Mail mail = Mail.builder()
            .withRecipient(persons)
            .withSubject(subjectMessageKey)
            .withTemplate(templateName, new HashMap<>())
            .withAttachment("fileName", iCal)
            .build();

        sut.send(mail);

        verify(mailSenderService).sendEmail(eq("no-reply@firma.test"), eq(singletonList("hans@firma.test")), eq("subject"), eq("emailBody"), eq(List.of(new MailAttachment("fileName", iCal))));
        verify(mailSenderService).sendEmail(eq("no-reply@firma.test"), eq(singletonList("franz@firma.test")), eq("subject"), eq("emailBody"), eq(List.of(new MailAttachment("fileName", iCal))));
    }

    @Test
    void sendTechnicalMail() {

        setupMockServletRequest();

        final String subjectMessageKey = "subject.overtime.created";
        final String templateName = "overtime_office";
        String to = "admin@firma.test";
        when(mailProperties.getAdministrator()).thenReturn(to);

        final Mail mail = Mail.builder()
            .withRecipient(true)
            .withSubject(subjectMessageKey)
            .withTemplate(templateName, new HashMap<>())
            .build();
        sut.send(mail);

        verify(mailSenderService).sendEmail(eq("no-reply@firma.test"), eq(singletonList(to)), eq("subject"), eq("emailBody"));
    }

    @Test
    void sendMailToWithNotificationAndPersonsAndAdministrator() {

        when(mailProperties.getAdministrator()).thenReturn("admin@firma.test");

        setupMockServletRequest();

        final Person hans = new Person();
        hans.setEmail("hans@firma.test");
        when(personService.getPersonsWithNotificationType(NOTIFICATION_USER)).thenReturn(List.of(hans));

        final Person franz = new Person();
        franz.setEmail("franz@firma.test");

        final String subjectMessageKey = "subject.overtime.created";
        final String templateName = "overtime_office";

        final Mail mail = Mail.builder()
            .withRecipient(true)
            .withRecipient(List.of(franz))
            .withRecipient(NOTIFICATION_USER)
            .withSubject(subjectMessageKey)
            .withTemplate(templateName, new HashMap<>())
            .build();

        sut.send(mail);

        verify(mailSenderService).sendEmail(eq("no-reply@firma.test"), eq(singletonList("hans@firma.test")), eq("subject"), eq("emailBody"));
        verify(mailSenderService).sendEmail(eq("no-reply@firma.test"), eq(singletonList("franz@firma.test")), eq("subject"), eq("emailBody"));
        verify(mailSenderService).sendEmail(eq("no-reply@firma.test"), eq(singletonList("admin@firma.test")), eq("subject"), eq("emailBody"));
    }

    private void setupMockServletRequest() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }
}
