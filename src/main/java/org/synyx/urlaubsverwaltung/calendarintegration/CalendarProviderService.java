package org.synyx.urlaubsverwaltung.calendarintegration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.util.List;
import java.util.Optional;

@Service
public class CalendarProviderService {

    private final List<CalendarProvider> calendarProviders;
    private final SettingsService settingsService;

    @Autowired
    CalendarProviderService(List<CalendarProvider> calendarProviders, SettingsService settingsService) {
        this.calendarProviders = calendarProviders;
        this.settingsService = settingsService;
    }

    /**
     * Returns the configured {@link CalendarProvider}
     *
     * @return configured {@link CalendarProvider} or an empty optional if none is configured
     */
    Optional<CalendarProvider> getCalendarProvider() {
        final String configuredCalendarProvider = settingsService.getSettings().getCalendarSettings().getProvider();
        return calendarProviders.stream()
            .filter(calendarProvider -> calendarProvider.getClass().getSimpleName().equals(configuredCalendarProvider))
            .findFirst();
    }
}
