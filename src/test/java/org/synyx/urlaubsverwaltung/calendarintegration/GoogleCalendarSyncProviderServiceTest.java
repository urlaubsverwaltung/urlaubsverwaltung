package org.synyx.urlaubsverwaltung.calendarintegration;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.absence.AbsenceTimeConfiguration;
import org.synyx.urlaubsverwaltung.absence.TimeSettings;
import org.synyx.urlaubsverwaltung.calendar.CalendarAbsence;
import org.synyx.urlaubsverwaltung.period.Period;
import org.synyx.urlaubsverwaltung.person.Person;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.period.DayLength.FULL;

@ExtendWith(MockitoExtension.class)
class GoogleCalendarSyncProviderServiceTest {

    @Mock
    private GoogleCalendarClientProvider googleCalendarClientProvider;

    @Captor
    private ArgumentCaptor<Event> eventArgumentCaptor;

    @Test
    void ensureAddAbsence() throws IOException {

        final CalendarSettings calendarSettings = prepareCalendarSettings();
        final GoogleCalendarSyncProvider sut = new GoogleCalendarSyncProvider(googleCalendarClientProvider);

        final Person person = new Person("testUser", "Hans", "Wurst", "testUser@mail.test");
        final Period period = new Period(LocalDate.parse("2022-08-25"), LocalDate.parse("2022-08-26"), FULL);

        final AbsenceTimeConfiguration config = new AbsenceTimeConfiguration(new TimeSettings());
        final CalendarAbsence absence = new CalendarAbsence(person, period, config);

        final Calendar calendarClient = mock(Calendar.class);
        when(googleCalendarClientProvider.getCalendarClient(calendarSettings.getGoogleCalendarSettings())).thenReturn(Optional.of(calendarClient));
        final Calendar.Events events = mock(Calendar.Events.class);
        when(calendarClient.events()).thenReturn(events);
        final Calendar.Events.Insert insert = mock(Calendar.Events.Insert.class);
        when(events.insert(eq("CALENDAR_ID"), any())).thenReturn(insert);
        final Event event = new Event();
        event.setId("eventId");
        when(insert.execute()).thenReturn(event);

        final Optional<String> maybeEventId = sut.add(absence, calendarSettings);
        assertThat(maybeEventId).hasValue("eventId");

        verify(events).insert(eq("CALENDAR_ID"), eventArgumentCaptor.capture());
        final Event capturedEvent = eventArgumentCaptor.getValue();
        assertThat(capturedEvent.getStart().getDate()).hasToString("2022-08-25");
        assertThat(capturedEvent.getEnd().getDate()).hasToString("2022-08-27");
        assertThat(capturedEvent.getSummary()).isEqualTo("Wurst Hans abwesend");
        assertThat(capturedEvent.getAttendees()).hasSize(1);
    }

    @Test
    void ensureUpdateAbsence() throws IOException {

        final CalendarSettings calendarSettings = prepareCalendarSettings();
        final GoogleCalendarSyncProvider sut = new GoogleCalendarSyncProvider(googleCalendarClientProvider);

        final Person person = new Person("testUser", "Hans", "Wurst", "testUser@mail.test");
        final AbsenceTimeConfiguration config = new AbsenceTimeConfiguration(new TimeSettings());

        final Calendar calendarClient = mock(Calendar.class);
        when(googleCalendarClientProvider.getCalendarClient(calendarSettings.getGoogleCalendarSettings())).thenReturn(Optional.of(calendarClient));
        final Calendar.Events events = mock(Calendar.Events.class);
        when(calendarClient.events()).thenReturn(events);

        final Calendar.Events.Get get = mock(Calendar.Events.Get.class);
        when(events.get("CALENDAR_ID", "eventId")).thenReturn(get);
        final Event event = new Event();
        when(get.execute()).thenReturn(event);

        final Calendar.Events.Patch patch = mock(Calendar.Events.Patch.class);
        when(events.patch("CALENDAR_ID", "eventId", event)).thenReturn(patch);

        final Period updatedPeriod = new Period(LocalDate.parse("2022-08-26"), LocalDate.parse("2022-08-27"), FULL);
        final CalendarAbsence absenceUpdate = new CalendarAbsence(person, updatedPeriod, config);
        sut.update(absenceUpdate, "eventId", calendarSettings);

        verify(events).patch(eq("CALENDAR_ID"), eq("eventId"), eventArgumentCaptor.capture());
        final Event capturedEvent = eventArgumentCaptor.getValue();
        assertThat(capturedEvent.getStart().getDate()).hasToString("2022-08-26");
        assertThat(capturedEvent.getEnd().getDate()).hasToString("2022-08-28");
        assertThat(capturedEvent.getSummary()).isEqualTo("Wurst Hans abwesend");
        assertThat(capturedEvent.getAttendees()).hasSize(1);
    }

    @Test
    void ensureDeleteAbsence() throws IOException {

        final CalendarSettings calendarSettings = prepareCalendarSettings();
        final GoogleCalendarSyncProvider sut = new GoogleCalendarSyncProvider(googleCalendarClientProvider);

        final Calendar calendarClient = mock(Calendar.class);
        when(googleCalendarClientProvider.getCalendarClient(calendarSettings.getGoogleCalendarSettings())).thenReturn(Optional.of(calendarClient));
        final Calendar.Events events = mock(Calendar.Events.class);
        when(calendarClient.events()).thenReturn(events);

        final Calendar.Events.Delete delete = mock(Calendar.Events.Delete.class);
        when(events.delete("CALENDAR_ID", "eventId")).thenReturn(delete);

        sut.delete("eventId", calendarSettings);

        verify(events).delete("CALENDAR_ID", "eventId");
    }

    private CalendarSettings prepareCalendarSettings() {

        final GoogleCalendarSettings googleCalendarSettings = new GoogleCalendarSettings();
        googleCalendarSettings.setCalendarId("CALENDAR_ID");
        googleCalendarSettings.setClientId("CLIENT_ID");
        googleCalendarSettings.setClientSecret("CLIENT_SECRET");
        googleCalendarSettings.setRefreshToken("REFRESH_TOKEN");

        final CalendarSettings calendarSettings = new CalendarSettings();
        calendarSettings.setGoogleCalendarSettings(googleCalendarSettings);

        return calendarSettings;
    }
}
