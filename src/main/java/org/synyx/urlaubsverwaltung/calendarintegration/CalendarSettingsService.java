package org.synyx.urlaubsverwaltung.calendarintegration;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

@Service
class CalendarSettingsService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final CalendarSettingsRepository calendarSettingsRepository;

    @Autowired
    CalendarSettingsService(CalendarSettingsRepository calendarSettingsRepository) {
        this.calendarSettingsRepository = calendarSettingsRepository;
    }

    CalendarSettings save(CalendarSettings settings) {
        final CalendarSettings savedSettings = calendarSettingsRepository.save(settings);
        LOG.info("Updated settings: {}", savedSettings);
        return savedSettings;
    }

    CalendarSettings getCalendarSettings() {
        return calendarSettingsRepository.findById(1L)
            .orElseGet(() -> {
                final CalendarSettings settings = new CalendarSettings();
                settings.setId(1L);
                final CalendarSettings savedSettings = calendarSettingsRepository.save(settings);
                LOG.info("Saved initial calendar settings {}", savedSettings);
                return savedSettings;
            });
    }
}
