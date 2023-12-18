package org.synyx.urlaubsverwaltung.calendarintegration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CalendarProviderService {

    private final List<CalendarProvider> calendarProviders;
    private final CalendarSettingsService calendarSettingsService;

    @Autowired
    CalendarProviderService(List<CalendarProvider> calendarProviders, CalendarSettingsService calendarSettingsService) {
        this.calendarProviders = calendarProviders;
        this.calendarSettingsService = calendarSettingsService;
    }

    /**
     * Returns the configured {@link CalendarProvider}
     *
     * @return configured {@link CalendarProvider} or an empty optional if none is configured
     */
    Optional<CalendarProvider> getCalendarProvider() {
        final String configuredCalendarProvider = calendarSettingsService.getCalendarSettings().getProvider();
        return calendarProviders.stream()
            .filter(calendarProvider -> calendarProvider.getClass().getSimpleName().equals(configuredCalendarProvider))
            .findFirst();
    }
}
