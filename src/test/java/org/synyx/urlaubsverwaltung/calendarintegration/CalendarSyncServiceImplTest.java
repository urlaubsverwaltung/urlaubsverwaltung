package org.synyx.urlaubsverwaltung.calendarintegration;

import org.junit.Before;
import org.junit.Test;
import org.synyx.urlaubsverwaltung.absence.Absence;
import org.synyx.urlaubsverwaltung.calendarintegration.providers.exchange.ExchangeCalendarProvider;
import org.synyx.urlaubsverwaltung.settings.CalendarSettings;
import org.synyx.urlaubsverwaltung.settings.ExchangeCalendarSettings;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Unit test for {@link CalendarSyncServiceImpl}.
 */
public class CalendarSyncServiceImplTest {

    private SettingsService settingsService;
    private CalendarService calendarService;

    private CalendarSyncService calendarSyncService;
    private Settings settings;

    @Before
    public void setUp() {

        settingsService = mock(SettingsService.class);
        settings = new Settings();
        settings.setCalendarSettings(new CalendarSettings());
        when(settingsService.getSettings()).thenReturn(settings);

        calendarService = mock(CalendarService.class);

        when(calendarService.getCalendarProvider()).thenReturn(mock(ExchangeCalendarProvider.class));

        calendarSyncService = new CalendarSyncServiceImpl(settingsService, calendarService);

    }


    @Test
    public void ensureAddsAbsenceToExchangeCalendar() {

        ExchangeCalendarSettings calendarSettings = settings.getCalendarSettings().getExchangeCalendarSettings();

        Absence absence = mock(Absence.class);

        calendarSyncService.addAbsence(absence);

        verify(calendarService.getCalendarProvider())
            .add(eq(absence), eq(settings.getCalendarSettings()));
    }


    @Test
    public void ensureUpdatesAbsenceInExchangeCalendar() {

        ExchangeCalendarSettings calendarSettings = settings.getCalendarSettings().getExchangeCalendarSettings();

        Absence absence = mock(Absence.class);
        String eventId = "event-1";

        calendarSyncService.update(absence, eventId);

        verify(calendarService.getCalendarProvider())
            .update(eq(absence), eq(eventId), eq(settings.getCalendarSettings()));
    }


    @Test
    public void ensureDeletedAbsenceInExchangeCalendar() {

        ExchangeCalendarSettings calendarSettings = settings.getCalendarSettings().getExchangeCalendarSettings();

        String eventId = "event-1";

        calendarSyncService.deleteAbsence(eventId);

        verify(calendarService.getCalendarProvider())
            .delete(eq(eventId), eq(settings.getCalendarSettings()));
    }


    @Test
    public void ensureChecksExchangeCalendarSettings() {

        ExchangeCalendarSettings calendarSettings = settings.getCalendarSettings().getExchangeCalendarSettings();

        calendarSyncService.checkCalendarSyncSettings();

        verify(calendarService.getCalendarProvider())
            .checkCalendarSyncSettings(any(CalendarSettings.class));
    }


}
