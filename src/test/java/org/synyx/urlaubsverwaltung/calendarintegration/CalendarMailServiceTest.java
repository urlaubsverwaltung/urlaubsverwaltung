package org.synyx.urlaubsverwaltung.calendarintegration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.absence.Absence;
import org.synyx.urlaubsverwaltung.absence.AbsenceTimeConfiguration;
import org.synyx.urlaubsverwaltung.absence.settings.TimeSettingsEntity;
import org.synyx.urlaubsverwaltung.mail.Mail;
import org.synyx.urlaubsverwaltung.mail.MailService;
import org.synyx.urlaubsverwaltung.period.Period;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;

@ExtendWith(MockitoExtension.class)
class CalendarMailServiceTest {

    private CalendarMailService sut;

    @Mock
    private MailService mailService;

    @BeforeEach
    void setUp() {
        sut = new CalendarMailService(mailService);
    }

    @Test
    void sendRejectedNotification() {

        final String calendarName = "calendar name";
        final String exception = "Some exception";

        final AbsenceTimeConfiguration absenceTimeConfiguration = new AbsenceTimeConfiguration(new TimeSettingsEntity());

        final LocalDate startDate = LocalDate.of(2019, 5, 5);
        final LocalDate endDate = LocalDate.of(2019, 5, 10);
        final Period period = new Period(startDate, endDate, FULL);

        final Absence absence = new Absence(new Person(), period, absenceTimeConfiguration);

        Map<String, Object> model = new HashMap<>();
        model.put("calendar", calendarName);
        model.put("absence", absence);
        model.put("exception", exception);

        sut.sendCalendarSyncErrorNotification(calendarName, absence, exception);

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService).send(argument.capture());
        final Mail mails = argument.getValue();
        assertThat(mails.isSendToTechnicalMail()).isTrue();
        assertThat(mails.getSubjectMessageKey()).isEqualTo("subject.error.calendar.sync");
        assertThat(mails.getTemplateName()).isEqualTo("error_calendar_sync");
        assertThat(mails.getTemplateModel()).isEqualTo(model);
    }


    @Test
    void sendCalendarUpdateErrorNotification() {

        final String calendarName = "calendar name";
        final String exception = "Some exception";
        final String eventId = "eventId";

        final AbsenceTimeConfiguration absenceTimeConfiguration = new AbsenceTimeConfiguration(new TimeSettingsEntity());

        final LocalDate startDate = LocalDate.of(2019, 5, 5);
        final LocalDate endDate = LocalDate.of(2019, 5, 10);
        final Period period = new Period(startDate, endDate, FULL);

        final Absence absence = new Absence(new Person(), period, absenceTimeConfiguration);

        Map<String, Object> model = new HashMap<>();
        model.put("calendar", calendarName);
        model.put("absence", absence);
        model.put("eventId", eventId);
        model.put("exception", exception);

        sut.sendCalendarUpdateErrorNotification(calendarName, absence, eventId, exception);

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService).send(argument.capture());
        final Mail mails = argument.getValue();
        assertThat(mails.isSendToTechnicalMail()).isTrue();
        assertThat(mails.getSubjectMessageKey()).isEqualTo("subject.error.calendar.update");
        assertThat(mails.getTemplateName()).isEqualTo("error_calendar_update");
        assertThat(mails.getTemplateModel()).isEqualTo(model);
    }

    @Test
    void sendCalendarDeleteErrorNotification() {

        final String calendarName = "calendar name";
        final String exception = "Some exception";
        final String eventId = "eventId";

        Map<String, Object> model = new HashMap<>();
        model.put("calendar", calendarName);
        model.put("eventId", eventId);
        model.put("exception", exception);

        sut.sendCalendarDeleteErrorNotification(calendarName, eventId, exception);

        final ArgumentCaptor<Mail> argument = ArgumentCaptor.forClass(Mail.class);
        verify(mailService).send(argument.capture());
        final Mail mails = argument.getValue();
        assertThat(mails.isSendToTechnicalMail()).isTrue();
        assertThat(mails.getSubjectMessageKey()).isEqualTo("subject.error.calendar.delete");
        assertThat(mails.getTemplateName()).isEqualTo("error_calendar_delete");
        assertThat(mails.getTemplateModel()).isEqualTo(model);
    }
}
