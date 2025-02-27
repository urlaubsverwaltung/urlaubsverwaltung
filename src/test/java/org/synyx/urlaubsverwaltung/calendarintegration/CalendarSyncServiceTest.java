package org.synyx.urlaubsverwaltung.calendarintegration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationAppliedEvent;
import org.synyx.urlaubsverwaltung.application.application.ApplicationRejectedEvent;
import org.synyx.urlaubsverwaltung.application.application.ApplicationUpdatedEvent;
import org.synyx.urlaubsverwaltung.calendar.CalendarAbsence;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.calendarintegration.AbsenceMappingType.VACATION;

@ExtendWith(MockitoExtension.class)
class CalendarSyncServiceTest {

    @Mock
    private SettingsService settingsService;
    @Mock
    private CalendarSettingsService calendarSettingsService;
    @Mock
    private CalendarProviderService calendarProviderService;
    @Mock
    private AbsenceMappingRepository absenceMappingRepository;

    private CalendarSyncService sut;

    @BeforeEach
    void setUp() {
        sut = new CalendarSyncService(settingsService, calendarSettingsService, calendarProviderService, absenceMappingRepository);
    }

    @Test
    void ensureToCreateCalendarEventOnApplicationAppliedEvent() {

        final LocalDate now = LocalDate.now();

        final Application application = new Application();
        application.setId(1L);
        application.setStartDate(now);
        application.setEndDate(now.plusDays(2));
        application.setDayLength(DayLength.FULL);
        application.setPerson(new Person());

        final ApplicationAppliedEvent event = new ApplicationAppliedEvent(UUID.randomUUID(), Instant.now(), application);

        final GoogleCalendarSyncProvider googleCalendarSyncProvider = mock(GoogleCalendarSyncProvider.class);
        when(calendarProviderService.getCalendarProvider()).thenReturn(Optional.of(googleCalendarSyncProvider));
        when(googleCalendarSyncProvider.add(any(CalendarAbsence.class), any(CalendarSettings.class))).thenReturn(Optional.of("eventId"));
        when(settingsService.getSettings()).thenReturn(new Settings());
        when(calendarSettingsService.getCalendarSettings()).thenReturn(new CalendarSettings());

        sut.consumeApplicationAppliedEvent(event);

        final ArgumentCaptor<AbsenceMapping> absenceMappingArgumentCaptor = forClass(AbsenceMapping.class);
        verify(absenceMappingRepository).save(absenceMappingArgumentCaptor.capture());
        final AbsenceMapping absenceMapping = absenceMappingArgumentCaptor.getValue();
        assertThat(absenceMapping.getEventId()).isEqualTo("eventId");
        assertThat(absenceMapping.getAbsenceId()).isEqualTo(1L);
        assertThat(absenceMapping.getAbsenceMappingType()).isEqualTo(VACATION);
    }

    @Test
    void ensureToCreateCalendarEventOnApplicationUpdatedEvent() {

        final LocalDate now = LocalDate.of(2022, 12, 10);

        final Application application = new Application();
        application.setId(1L);
        application.setStartDate(now);
        application.setEndDate(now.plusDays(2));
        application.setDayLength(DayLength.FULL);

        final Person person = new Person();
        person.setFirstName("first");
        person.setLastName("last");
        application.setPerson(person);

        final ApplicationUpdatedEvent event = new ApplicationUpdatedEvent(UUID.randomUUID(), Instant.now(), application);

        when(absenceMappingRepository.findAbsenceMappingByAbsenceIdAndAbsenceMappingType(1L, VACATION)).thenReturn(Optional.of(new AbsenceMapping(1L, VACATION, "eventId")));
        final GoogleCalendarSyncProvider googleCalendarSyncProvider = mock(GoogleCalendarSyncProvider.class);
        when(calendarProviderService.getCalendarProvider()).thenReturn(Optional.of(googleCalendarSyncProvider));
        when(settingsService.getSettings()).thenReturn(new Settings());
        final CalendarSettings calendarSettings = new CalendarSettings();
        when(calendarSettingsService.getCalendarSettings()).thenReturn(calendarSettings);

        sut.consumeApplicationUpdatedEvent(event);

        final ArgumentCaptor<CalendarAbsence> absenceArgumentCaptor = forClass(CalendarAbsence.class);
        verify(googleCalendarSyncProvider).update(absenceArgumentCaptor.capture(), eq("eventId"), eq(calendarSettings));
        final CalendarAbsence absence = absenceArgumentCaptor.getValue();
        assertThat(absence.getPerson()).isEqualTo(person);
        assertThat(absence.getCalendarAbsenceTypeMessageKey()).isEqualTo("calendar.absence.type.default");
        assertThat(absence.getStartDate()).isEqualTo(ZonedDateTime.of(2022, 12, 10, 0, 0, 0, 0, ZoneId.of("Europe/Berlin")));
        assertThat(absence.getEndDate()).isEqualTo(ZonedDateTime.of(2022, 12, 13, 0, 0, 0, 0, ZoneId.of("Europe/Berlin")));
    }

    @Test
    void ensureToCreateCalendarEventOnApplicationRejectedEvent() {

        final LocalDate now = LocalDate.of(2022, 12, 10);

        final Application application = new Application();
        application.setId(1L);
        application.setStartDate(now);
        application.setEndDate(now.plusDays(2));
        application.setDayLength(DayLength.FULL);

        final Person person = new Person();
        person.setFirstName("first");
        person.setLastName("last");
        application.setPerson(person);

        final ApplicationRejectedEvent event = new ApplicationRejectedEvent(UUID.randomUUID(), Instant.now(), application);

        when(absenceMappingRepository.findAbsenceMappingByAbsenceIdAndAbsenceMappingType(1L, VACATION)).thenReturn(Optional.of(new AbsenceMapping(1L, VACATION, "eventId")));

        final CalendarSettings calendarSettings = new CalendarSettings();
        when(calendarSettingsService.getCalendarSettings()).thenReturn(calendarSettings);

        final GoogleCalendarSyncProvider googleCalendarSyncProvider = mock(GoogleCalendarSyncProvider.class);
        when(calendarProviderService.getCalendarProvider()).thenReturn(Optional.of(googleCalendarSyncProvider));
        when(googleCalendarSyncProvider.delete("eventId", calendarSettings)).thenReturn(Optional.of("eventId"));

        sut.consumeApplicationRejectedEvent(event);

        verify(absenceMappingRepository).deleteByEventId("eventId");
    }
}
