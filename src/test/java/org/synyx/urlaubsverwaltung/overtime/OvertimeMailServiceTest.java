package org.synyx.urlaubsverwaltung.overtime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.mail.LegacyMail;
import org.synyx.urlaubsverwaltung.mail.MailService;

import java.time.Clock;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.synyx.urlaubsverwaltung.person.MailNotification.OVERTIME_NOTIFICATION_OFFICE;

@ExtendWith(MockitoExtension.class)
class OvertimeMailServiceTest {

    private OvertimeMailService sut;

    @Mock
    private MailService mailService;

    @BeforeEach
    void setUp() {
        sut = new OvertimeMailService(mailService);
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

        sut.sendOvertimeNotification(overtime, overtimeComment);

        final ArgumentCaptor<LegacyMail> argument = ArgumentCaptor.forClass(LegacyMail.class);
        verify(mailService).legacySend(argument.capture());
        final LegacyMail mails = argument.getValue();
        assertThat(mails.getMailNotificationRecipients()).hasValue(OVERTIME_NOTIFICATION_OFFICE);
        assertThat(mails.getSubjectMessageKey()).isEqualTo("subject.overtime.created");
        assertThat(mails.getTemplateName()).isEqualTo("overtime_office");
        assertThat(mails.getTemplateModel()).isEqualTo(model);
    }
}
