package org.synyx.urlaubsverwaltung.core.sync;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.synyx.urlaubsverwaltung.core.settings.CalendarSettings;
import org.synyx.urlaubsverwaltung.core.settings.ExchangeCalendarSettings;
import org.synyx.urlaubsverwaltung.core.settings.Settings;
import org.synyx.urlaubsverwaltung.core.settings.SettingsService;
import org.synyx.urlaubsverwaltung.core.sync.absence.Absence;
import org.synyx.urlaubsverwaltung.core.sync.providers.CalendarProvider;
import org.synyx.urlaubsverwaltung.core.sync.providers.exchange.ExchangeCalendarProvider;


/**
 * Unit test for {@link CalendarSyncServiceImpl}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class CalendarSyncServiceImplTest {

    private SettingsService settingsService;
    private CalendarProvider calendarProvider;

    private CalendarSyncService calendarSyncService;
    private Settings settings;

    @Before
    public void setUp() {

        settingsService = Mockito.mock(SettingsService.class);
        settings = new Settings();
        settings.setCalendarSettings(new CalendarSettings());
        Mockito.when(settingsService.getSettings()).thenReturn(settings);

        calendarProvider = Mockito.mock(ExchangeCalendarProvider.class);

        calendarSyncService = new CalendarSyncServiceImpl(settingsService, calendarProvider);

    }


    @Test
    public void ensureAddsAbsenceToExchangeCalendarIfActivated() {

        ExchangeCalendarSettings calendarSettings = settings.getCalendarSettings().getExchangeCalendarSettings();
        calendarSettings.setActive(true);

        Absence absence = Mockito.mock(Absence.class);

        calendarSyncService.addAbsence(absence);

        Mockito.verify(calendarProvider)
            .add(Mockito.eq(absence), Mockito.eq(settings.getCalendarSettings()));
    }


    @Test
    public void ensureUpdatesAbsenceInExchangeCalendarIfActivated() {

        ExchangeCalendarSettings calendarSettings = settings.getCalendarSettings().getExchangeCalendarSettings();
        calendarSettings.setActive(true);

        Absence absence = Mockito.mock(Absence.class);
        String eventId = "event-1";

        calendarSyncService.update(absence, eventId);

        Mockito.verify(calendarProvider)
            .update(Mockito.eq(absence), Mockito.eq(eventId), Mockito.eq(settings.getCalendarSettings()));
    }


    @Test
    public void ensureDeletedAbsenceInExchangeCalendarIfActivated() {

        ExchangeCalendarSettings calendarSettings = settings.getCalendarSettings().getExchangeCalendarSettings();
        calendarSettings.setActive(true);

        String eventId = "event-1";

        calendarSyncService.deleteAbsence(eventId);

        Mockito.verify(calendarProvider)
            .delete(Mockito.eq(eventId), Mockito.eq(settings.getCalendarSettings()));
    }


    @Test
    public void ensureChecksExchangeCalendarSettingsIfActivated() {

        ExchangeCalendarSettings calendarSettings = settings.getCalendarSettings().getExchangeCalendarSettings();
        calendarSettings.setActive(true);

        calendarSyncService.checkCalendarSyncSettings();

        Mockito.verify(calendarProvider).checkCalendarSyncSettings(Mockito.any(CalendarSettings.class));
    }


}
