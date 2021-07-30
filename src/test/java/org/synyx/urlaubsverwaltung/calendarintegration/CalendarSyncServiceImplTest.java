package org.synyx.urlaubsverwaltung.calendarintegration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.absence.Absence;
import org.synyx.urlaubsverwaltung.calendarintegration.providers.exchange.ExchangeCalendarProvider;
import org.synyx.urlaubsverwaltung.calendarintegration.settings.CalendarSettingsEntity;
import org.synyx.urlaubsverwaltung.calendarintegration.settings.CalendarSettingsService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Unit test for {@link CalendarSyncServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class CalendarSyncServiceImplTest {

    private CalendarSyncService calendarSyncService;
    @Mock
    private CalendarSettingsService calendarSettingsService;
    @Mock
    private ExchangeCalendarProvider exchangeCalendarProvider;

    CalendarSettingsEntity calendarSettingsEntity = new CalendarSettingsEntity();

    @BeforeEach
    void setUp() {

        calendarSettingsEntity.setProvider(exchangeCalendarProvider.getClass().getSimpleName());
        when(calendarSettingsService.getSettings()).thenReturn(calendarSettingsEntity);

        calendarSyncService = new CalendarSyncServiceImpl(calendarSettingsService, List.of(exchangeCalendarProvider));
    }

    @Test
    void ensureAddsAbsenceToExchangeCalendar() {

        Absence absence = mock(Absence.class);

        calendarSyncService.addAbsence(absence);

        verify(exchangeCalendarProvider).add(absence, calendarSettingsService.getSettings());
    }

    @Test
    void ensureUpdatesAbsenceInExchangeCalendar() {

        Absence absence = mock(Absence.class);
        String eventId = "event-1";

        calendarSyncService.update(absence, eventId);

        verify(exchangeCalendarProvider).update(absence, eventId, calendarSettingsService.getSettings());
    }

    @Test
    void ensureDeletedAbsenceInExchangeCalendar() {

        String eventId = "event-1";

        calendarSyncService.deleteAbsence(eventId);

        verify(exchangeCalendarProvider).delete(eventId, calendarSettingsService.getSettings());
    }

    @Test
    void ensureChecksExchangeCalendarSettings() {

        calendarSyncService.checkCalendarSyncSettings();

        verify(exchangeCalendarProvider).checkCalendarSyncSettings(any(CalendarSettingsEntity.class));
    }
}
