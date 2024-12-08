package org.synyx.urlaubsverwaltung.calendarintegration;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

@Service
public class CalendarSettingsService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final CalendarSettingsRepository calendarSettingsRepository;

    @Autowired
    CalendarSettingsService(CalendarSettingsRepository calendarSettingsRepository) {
        this.calendarSettingsRepository = calendarSettingsRepository;
    }

    public CalendarSettings save(CalendarSettings settings) {
        final CalendarSettings savedSettings = calendarSettingsRepository.save(settings);
        LOG.info("Updated settings: {}", savedSettings);
        return savedSettings;
    }

    public CalendarSettings getCalendarSettings() {
        return calendarSettingsRepository.findAll().stream().findFirst()
            .orElseThrow(() -> new IllegalStateException("No calendar settings found in database!"));
    }

    public void insertDefaultCalendarSettings() {

        final long count = calendarSettingsRepository.count();

        if (count == 0) {
            final CalendarSettings settings = new CalendarSettings();
            final CalendarSettings savedSettings = calendarSettingsRepository.save(settings);
            LOG.info("Saved initial calendar settings {}", savedSettings);
        }
    }
}
