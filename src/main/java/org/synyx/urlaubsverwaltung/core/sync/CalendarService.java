package org.synyx.urlaubsverwaltung.core.sync;

import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.core.settings.SettingsService;
import org.synyx.urlaubsverwaltung.core.sync.providers.CalendarProvider;
import org.synyx.urlaubsverwaltung.core.sync.providers.noop.NoopCalendarSyncProvider;

import java.util.List;
import java.util.Optional;

@Service
public class CalendarService {

    private List<CalendarProvider> calendarProviders;
    private SettingsService settingsService;

    public CalendarService(List<CalendarProvider> calendarProviders, SettingsService settingsService) {
        this.calendarProviders = calendarProviders;
        this.settingsService = settingsService;
    }

    /**
     *
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
