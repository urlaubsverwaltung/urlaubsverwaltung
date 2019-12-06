package org.synyx.urlaubsverwaltung.calendarintegration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.calendarintegration.providers.CalendarProvider;
import org.synyx.urlaubsverwaltung.calendarintegration.providers.noop.NoopCalendarSyncProvider;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.util.List;
import java.util.Optional;

@Service
public class CalendarService {

    private final List<CalendarProvider> calendarProviders;
    private final SettingsService settingsService;

    @Autowired
    public CalendarService(List<CalendarProvider> calendarProviders, SettingsService settingsService) {
        this.calendarProviders = calendarProviders;
        this.settingsService = settingsService;
    }

    /**
     * @return configured CalendarProvider or NoopCalendarSyncProvider in case of problems
     */
    public CalendarProvider getCalendarProvider() {
        String calenderProvider = settingsService.getSettings().getCalendarSettings().getProvider();

        Optional<CalendarProvider> option = calendarProviders.stream()
            .filter(calendarProvider -> calendarProvider.getClass().getSimpleName().equals(calenderProvider))
            .findFirst();

        return option.orElse(new NoopCalendarSyncProvider());
    }
}
