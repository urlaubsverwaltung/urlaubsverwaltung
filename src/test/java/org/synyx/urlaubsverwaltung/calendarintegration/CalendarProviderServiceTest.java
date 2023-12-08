package org.synyx.urlaubsverwaltung.calendarintegration;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CalendarProviderServiceTest {

    static Stream<Arguments> data() {
        return Stream.of(
            arguments(GoogleCalendarSyncProvider.class, GoogleCalendarSyncProvider.class),
            arguments(ExchangeCalendarProvider.class, ExchangeCalendarProvider.class)
        );
    }

    @ParameterizedTest
    @MethodSource("data")
    void testResult(Class input, Class expected) {
        final SettingsService settingsService = getPreparedSettingsServiceForProvider(input);
        final List<CalendarProvider> calendarProviders = getTypicalProviderList();
        final CalendarProviderService sut = new CalendarProviderService(calendarProviders, settingsService);

        assertThat(sut.getCalendarProvider().get().getClass().getName()).isEqualTo(expected.getName());
    }

    private SettingsService getPreparedSettingsServiceForProvider(Class provider) {
        final SettingsService settingsService = mock(SettingsService.class);
        final Settings settings = mock(Settings.class);
        when(settingsService.getSettings()).thenReturn(settings);

        final CalendarSettings calendarSettings = mock(CalendarSettings.class);
        when(settings.getCalendarSettings()).thenReturn(calendarSettings);

        when(calendarSettings.getProvider()).thenReturn(provider.getSimpleName());

        return settingsService;
    }

    private List<CalendarProvider> getTypicalProviderList() {
        return List.of(
            new ExchangeCalendarProvider(null),
            new GoogleCalendarSyncProvider(null, null)
        );
    }
}
