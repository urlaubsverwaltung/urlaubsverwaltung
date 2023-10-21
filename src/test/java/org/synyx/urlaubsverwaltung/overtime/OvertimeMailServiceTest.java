package org.synyx.urlaubsverwaltung.overtime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.mail.Mail;
import org.synyx.urlaubsverwaltung.mail.MailRecipientService;
import org.synyx.urlaubsverwaltung.mail.MailService;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Clock;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Locale.GERMAN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_OVERTIME_APPLIED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_OVERTIME_APPLIED_BY_MANAGEMENT;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_OVERTIME_MANAGEMENT_APPLIED;

@ExtendWith(MockitoExtension.class)
class OvertimeMailServiceTest {

    private OvertimeMailService sut;

    @Mock
    private MailService mailService;
    @Mock
    private MailRecipientService mailRecipientService;

    @BeforeEach
    void setUp() {
        sut = new OvertimeMailService(mailService, mailRecipientService);
    }

    @Test
    void ensureToSendOvertimeNotificationToApplicantFromApplicant() {

        final Person submitter = new Person("submitter", "submitter", "submitter", "submitter@example.org");
        submitter.setNotifications(List.of(NOTIFICATION_EMAIL_OVERTIME_APPLIED));

        final Overtime overtime = new Overtime();
        overtime.setPerson(submitter);
        overtime.setDuration(Duration.parse("P1DT30H72M"));
        final OvertimeComment overtimeComment = new OvertimeComment(Clock.systemUTC());

        final Map<String, Object> model = new HashMap<>();
        model.put("overtime", overtime);
        model.put("overtimeDurationHours", "55 Std.");
        model.put("overtimeDurationMinutes", "12 Min.");
        model.put("comment", overtimeComment);

        sut.sendOvertimeNotificationToApplicantFromApplicant(overtime, overtimeComment);

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService).send(argument.capture());
        final Mail mails = argument.getValue();
        assertThat(mails.getMailAddressRecipients()).hasValue(List.of(submitter));
        assertThat(mails.getSubjectMessageKey()).isEqualTo("subject.overtime.created.applicant");
        assertThat(mails.getTemplateName()).isEqualTo("overtime_to_applicant_from_applicant");
        assertThat(mails.getTemplateModel(GERMAN)).isEqualTo(model);
    }

    @Test
    void ensureToSendOvertimeNotificationToApplicantFromManagement() {

        final Person author = new Person("author", "author", "author", "author@example.org");

        final Person person = new Person("submitter", "submitter", "submitter", "submitter@example.org");
        person.setNotifications(List.of(NOTIFICATION_EMAIL_OVERTIME_APPLIED_BY_MANAGEMENT));

        final Overtime overtime = new Overtime();
        overtime.setPerson(person);
        overtime.setDuration(Duration.parse("P1DT30H72M"));
        final OvertimeComment overtimeComment = new OvertimeComment(Clock.systemUTC());

        final Map<String, Object> model = new HashMap<>();
        model.put("overtime", overtime);
        model.put("overtimeDurationHours", "55 Std.");
        model.put("overtimeDurationMinutes", "12 Min.");
        model.put("comment", overtimeComment);
        model.put("author", author);

        sut.sendOvertimeNotificationToApplicantFromManagement(overtime, overtimeComment, author);

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService).send(argument.capture());
        final Mail mails = argument.getValue();
        assertThat(mails.getMailAddressRecipients()).hasValue(List.of(person));
        assertThat(mails.getSubjectMessageKey()).isEqualTo("subject.overtime.created.applicant_from_management");
        assertThat(mails.getTemplateName()).isEqualTo("overtime_to_applicant_from_management");
        assertThat(mails.getTemplateModel(GERMAN)).isEqualTo(model);
    }

    @Test
    void ensureToSendOvertimeNotificationToManagement() {

        final Person submitter = new Person("submitter", "submitter", "submitter", "submitter@example.org");

        final Overtime overtime = new Overtime();
        overtime.setPerson(submitter);
        overtime.setDuration(Duration.parse("P1DT30H72M"));
        final OvertimeComment overtimeComment = new OvertimeComment(Clock.systemUTC());

        final Person office = new Person("muster", "Muster", "Marlene", "muster@example.org");
        when(mailRecipientService.getRecipientsOfInterest(overtime.getPerson(), NOTIFICATION_EMAIL_OVERTIME_MANAGEMENT_APPLIED)).thenReturn(List.of(office));

        final Map<String, Object> model = new HashMap<>();
        model.put("overtime", overtime);
        model.put("overtimeDurationHours", "55 Std.");
        model.put("overtimeDurationMinutes", "12 Min.");
        model.put("comment", overtimeComment);

        sut.sendOvertimeNotificationToManagement(overtime, overtimeComment);

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService).send(argument.capture());
        final Mail mails = argument.getValue();
        assertThat(mails.getMailAddressRecipients()).hasValue(List.of(office));
        assertThat(mails.getSubjectMessageKey()).isEqualTo("subject.overtime.created.management");
        assertThat(mails.getTemplateName()).isEqualTo("overtime_to_management");
        assertThat(mails.getTemplateModel(GERMAN)).isEqualTo(model);
    }
}
