package org.synyx.urlaubsverwaltung.calendarintegration;

import org.springframework.stereotype.Service;

@Service
public class CalendarIntegrationSettingsImportService {

    private final CalendarSettingsRepository calendarSettingsRepository;

    CalendarIntegrationSettingsImportService(CalendarSettingsRepository calendarSettingsRepository) {
        this.calendarSettingsRepository = calendarSettingsRepository;
    }

    public void deleteAll() {
        calendarSettingsRepository.deleteAll();
    }

    public void importCalendarIntegrationSettings(CalendarSettings calendarSettings) {
        calendarSettingsRepository.save(calendarSettings);
    }
}
