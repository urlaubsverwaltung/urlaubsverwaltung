package org.synyx.urlaubsverwaltung.core.sync;

import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.core.settings.SettingsService;
import org.synyx.urlaubsverwaltung.core.sync.providers.CalendarProvider;

import java.util.List;

@Service
public class CalendarService {

    private List<CalendarProvider> calendarProviders;
    private SettingsService settingsService;

    public CalendarService(List<CalendarProvider> calendarProviders, SettingsService settingsService) {
        this.calendarProviders = calendarProviders;
        this.settingsService = settingsService;
    }

    public CalendarProvider getCalendarProvider() {
        String calenderProvider = settingsService.getSettings().getCalendarSettings().getProvider();

        return calendarProviders.stream()
                .filter(calendarProvider -> calendarProvider.getClass().getSimpleName().equals(calenderProvider))
                .findFirst().get();
    }
}
