package org.synyx.urlaubsverwaltung.overtime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.mail.Mail;
import org.synyx.urlaubsverwaltung.mail.MailService;
import org.synyx.urlaubsverwaltung.mail.Recipient;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.Clock;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.person.MailNotification.OVERTIME_NOTIFICATION_OFFICE;

@ExtendWith(MockitoExtension.class)
class OvertimeMailServiceTest {

    private OvertimeMailService sut;

    @Mock
    private MailService mailService;
    @Mock
    private PersonService personService;

    @BeforeEach
    void setUp() {
        sut = new OvertimeMailService(mailService, personService);
    }

    @Test
    void sendOvertimeNotification() {
        final Overtime overtime = new Overtime();
        overtime.setDuration(Duration.parse("P1DT30H72M"));
        final OvertimeComment overtimeComment = new OvertimeComment(Clock.systemUTC());

        final Map<String, Object> model = new HashMap<>();
        model.put("overtime", overtime);
        model.put("overtimeDurationHours", "55 Std.");
        model.put("overtimeDurationMinutes", "12 Min.");
        model.put("comment", overtimeComment);

        Recipient recipient = new Recipient("officeson@office.org", "Offy McOfficeson");
        when(personService.findRecipients(OVERTIME_NOTIFICATION_OFFICE)).thenReturn(List.of(recipient));

        sut.sendOvertimeNotification(overtime, overtimeComment);

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService).send(argument.capture());
        final Mail mail = argument.getValue();
        assertThat(mail.getRecipients()).contains(List.of(recipient));
        assertThat(mail.getSubjectMessageKey()).isEqualTo("subject.overtime.created");
        assertThat(mail.getTemplateName()).isEqualTo("overtime_office");
        assertThat(mail.getTemplateModel()).isEqualTo(model);
    }
}
