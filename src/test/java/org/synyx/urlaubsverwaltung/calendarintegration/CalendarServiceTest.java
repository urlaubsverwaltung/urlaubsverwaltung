package org.synyx.urlaubsverwaltung.calendarintegration;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.synyx.urlaubsverwaltung.calendarintegration.providers.CalendarProvider;
import org.synyx.urlaubsverwaltung.calendarintegration.providers.exchange.ExchangeCalendarProvider;
import org.synyx.urlaubsverwaltung.calendarintegration.providers.google.GoogleCalendarSyncProvider;
import org.synyx.urlaubsverwaltung.calendarintegration.providers.noop.NoopCalendarSyncProvider;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class CalendarServiceTest {

    static Stream<Arguments> data() {
        return Stream.of(
            arguments(GoogleCalendarSyncProvider.class, GoogleCalendarSyncProvider.class),
            arguments(ExchangeCalendarProvider.class, ExchangeCalendarProvider.class),
            arguments(NoopCalendarSyncProvider.class, NoopCalendarSyncProvider.class),
            arguments(String.class, NoopCalendarSyncProvider.class)
        );
    }

    @ParameterizedTest
    @MethodSource("data")
    void testResult(Class input, Class expected) {
        SettingsService settingsService = getPreparedSettingsServiceForProvider(input);
        List<CalendarProvider> calendarProviders = getTypicalProviderList();
        CalendarService cut = new CalendarService(calendarProviders, settingsService);

        assertThat(cut.getCalendarProvider().getClass().getName()).isEqualTo(expected.getName());
    }

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
}
