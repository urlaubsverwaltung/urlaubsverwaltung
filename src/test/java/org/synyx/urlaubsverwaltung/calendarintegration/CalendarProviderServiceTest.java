package org.synyx.urlaubsverwaltung.calendarintegration;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CalendarProviderServiceTest {

    static Stream<Arguments> data() {
        return Stream.of(
            arguments(GoogleCalendarSyncProvider.class, GoogleCalendarSyncProvider.class)
        );
    }

    @ParameterizedTest
    @MethodSource("data")
    void testResult(Class input, Class expected) {
        final CalendarSettingsService settingsService = getPreparedSettingsServiceForProvider(input);
        final List<CalendarProvider> calendarProviders = getTypicalProviderList();
        final CalendarProviderService sut = new CalendarProviderService(calendarProviders, settingsService);

        assertThat(sut.getCalendarProvider().get().getClass().getName()).isEqualTo(expected.getName());
    }

    private CalendarSettingsService getPreparedSettingsServiceForProvider(Class provider) {
        final CalendarSettingsService calendarSettingsService = mock(CalendarSettingsService.class);
        final CalendarSettings calendarSettings = mock(CalendarSettings.class);
        when(calendarSettingsService.getCalendarSettings()).thenReturn(calendarSettings);

        when(calendarSettings.getProvider()).thenReturn(provider.getSimpleName());

        return calendarSettingsService;
    }

    private List<CalendarProvider> getTypicalProviderList() {
        return List.of(
            new GoogleCalendarSyncProvider(null)
        );
    }
}
