package org.synyx.urlaubsverwaltung.core.sync;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.synyx.urlaubsverwaltung.core.settings.CalendarSettings;
import org.synyx.urlaubsverwaltung.core.settings.ExchangeCalendarSettings;
import org.synyx.urlaubsverwaltung.core.settings.Settings;
import org.synyx.urlaubsverwaltung.core.settings.SettingsService;
import org.synyx.urlaubsverwaltung.core.sync.absence.Absence;
import org.synyx.urlaubsverwaltung.core.sync.providers.exchange.ExchangeCalendarProvider;

import static org.mockito.Mockito.when;


/**
 * Unit test for {@link CalendarSyncServiceImpl}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class CalendarSyncServiceImplTest {

    private SettingsService settingsService;
    private CalendarService calendarService;

    private CalendarSyncService calendarSyncService;
    private Settings settings;

    @Before
    public void setUp() {

        settingsService = Mockito.mock(SettingsService.class);
        settings = new Settings();
        settings.setCalendarSettings(new CalendarSettings());
        when(settingsService.getSettings()).thenReturn(settings);

        calendarService = Mockito.mock(CalendarService.class);

        when(calendarService.getCalendarProvider()).thenReturn(Mockito.mock(ExchangeCalendarProvider.class));

        calendarSyncService = new CalendarSyncServiceImpl(settingsService, calendarService);

    }


    @Test
    public void ensureAddsAbsenceToExchangeCalendar() {

        ExchangeCalendarSettings calendarSettings = settings.getCalendarSettings().getExchangeCalendarSettings();

        Absence absence = Mockito.mock(Absence.class);

        calendarSyncService.addAbsence(absence);

        Mockito.verify(calendarService.getCalendarProvider())
            .add(Mockito.eq(absence), Mockito.eq(settings.getCalendarSettings()));
    }


    @Test
    public void ensureUpdatesAbsenceInExchangeCalendar() {

        ExchangeCalendarSettings calendarSettings = settings.getCalendarSettings().getExchangeCalendarSettings();

        Absence absence = Mockito.mock(Absence.class);
        String eventId = "event-1";

        calendarSyncService.update(absence, eventId);

        Mockito.verify(calendarService.getCalendarProvider())
            .update(Mockito.eq(absence), Mockito.eq(eventId), Mockito.eq(settings.getCalendarSettings()));
    }


    @Test
    public void ensureDeletedAbsenceInExchangeCalendar() {

        ExchangeCalendarSettings calendarSettings = settings.getCalendarSettings().getExchangeCalendarSettings();

        String eventId = "event-1";

        calendarSyncService.deleteAbsence(eventId);

        Mockito.verify(calendarService.getCalendarProvider())
            .delete(Mockito.eq(eventId), Mockito.eq(settings.getCalendarSettings()));
    }


    @Test
    public void ensureChecksExchangeCalendarSettings() {

        ExchangeCalendarSettings calendarSettings = settings.getCalendarSettings().getExchangeCalendarSettings();

        calendarSyncService.checkCalendarSyncSettings();

        Mockito.verify(calendarService.getCalendarProvider())
                .checkCalendarSyncSettings(Mockito.any(CalendarSettings.class));
    }


}
