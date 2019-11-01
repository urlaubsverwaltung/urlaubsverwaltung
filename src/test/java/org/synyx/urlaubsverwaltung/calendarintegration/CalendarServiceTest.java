package org.synyx.urlaubsverwaltung.calendarintegration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.synyx.urlaubsverwaltung.calendarintegration.providers.CalendarProvider;
import org.synyx.urlaubsverwaltung.calendarintegration.providers.exchange.ExchangeCalendarProvider;
import org.synyx.urlaubsverwaltung.calendarintegration.providers.google.GoogleCalendarSyncProvider;
import org.synyx.urlaubsverwaltung.calendarintegration.providers.noop.NoopCalendarSyncProvider;
import org.synyx.urlaubsverwaltung.settings.CalendarSettings;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(Parameterized.class)
public class CalendarServiceTest {

    private final Class input;
    private final Class expected;

    private SettingsService getPreparedSettingsServiceForProvider(Class provider) {
        SettingsService settingsService = mock(SettingsService.class);
        Settings settings = mock(Settings.class);
        when(settingsService.getSettings()).thenReturn(settings);

        CalendarSettings calendarSettings = mock(CalendarSettings.class);
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
        return Arrays.asList(new Object[][]{
            {GoogleCalendarSyncProvider.class, GoogleCalendarSyncProvider.class, "select GoogleCalendar"},
            {ExchangeCalendarProvider.class, ExchangeCalendarProvider.class, "select Exchange"},
            {NoopCalendarSyncProvider.class, NoopCalendarSyncProvider.class, "select NOOPCalendar"},
            {String.class, NoopCalendarSyncProvider.class, "select fallback"}
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
