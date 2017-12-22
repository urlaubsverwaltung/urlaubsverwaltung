package org.synyx.urlaubsverwaltung.core.sync;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;
import org.synyx.urlaubsverwaltung.core.settings.CalendarSettings;
import org.synyx.urlaubsverwaltung.core.settings.Settings;
import org.synyx.urlaubsverwaltung.core.settings.SettingsService;
import org.synyx.urlaubsverwaltung.core.sync.providers.CalendarProvider;
import org.synyx.urlaubsverwaltung.core.sync.providers.exchange.ExchangeCalendarProvider;
import org.synyx.urlaubsverwaltung.core.sync.providers.google.GoogleCalendarSyncProvider;
import org.synyx.urlaubsverwaltung.core.sync.providers.noop.NoopCalendarSyncProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;


@RunWith(Parameterized.class)
public class CalendarServiceTest {

    private Class input;
    private Class expected;

    private SettingsService getPreparedSettingsServiceForProvider(Class provider) {
        SettingsService settingsService = Mockito.mock(SettingsService.class);
        Settings settings = Mockito.mock(Settings.class);
        when(settingsService.getSettings()).thenReturn(settings);

        CalendarSettings calendarSettings = Mockito.mock(CalendarSettings.class);
        when(settings.getCalendarSettings()).thenReturn(calendarSettings);

        when(calendarSettings.getProvider()).thenReturn(provider.getSimpleName());

        return settingsService;
    }

    private List<CalendarProvider> getTypicalProviderList() {
        List<CalendarProvider> calendarProviders = new ArrayList<>();
        calendarProviders.add(new NoopCalendarSyncProvider());
        calendarProviders.add(new ExchangeCalendarProvider(null));
        calendarProviders.add(new GoogleCalendarSyncProvider(null, null));

        return calendarProviders;
    }

    public CalendarServiceTest(Class input, Class expected, String msg) {

        this.input = input;
        this.expected = expected;
    }


    @Parameterized.Parameters(name = "{2}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { GoogleCalendarSyncProvider.class, GoogleCalendarSyncProvider.class , "select GoogleCalendar"},
                { ExchangeCalendarProvider.class  , ExchangeCalendarProvider.class   , "select Exchange"},
                { NoopCalendarSyncProvider.class  , NoopCalendarSyncProvider.class   , "select NOOPCalendar"},
                { String.class                    , NoopCalendarSyncProvider.class   , "select fallback"}
        });
    }

    @Test
    public void testResult() {
        SettingsService settingsService = getPreparedSettingsServiceForProvider(this.input);

        List<CalendarProvider> calendarProviders = getTypicalProviderList();

        CalendarService cut = new CalendarService(calendarProviders, settingsService);

        assertEquals(this.expected.getName(), cut.getCalendarProvider().getClass().getName());
    }
}