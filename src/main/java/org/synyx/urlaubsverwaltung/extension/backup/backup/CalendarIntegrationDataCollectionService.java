package org.synyx.urlaubsverwaltung.extension.backup.backup;

import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.calendarintegration.AbsenceMappingExportService;
import org.synyx.urlaubsverwaltung.calendarintegration.CalendarSettings;
import org.synyx.urlaubsverwaltung.calendarintegration.CalendarSettingsService;
import org.synyx.urlaubsverwaltung.extension.backup.model.AbsenceMappingDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.CalendarIntegrationBackupDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.CalendarIntegrationSettingsDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.GoogleCalendarSettingsDTO;

import java.util.List;
import java.util.Optional;

@Service
@ConditionalOnBackupCreateEnabled
class CalendarIntegrationDataCollectionService {

    private final AbsenceMappingExportService absenceMappingExportService;
    private final CalendarSettingsService calendarSettingsService;

    CalendarIntegrationDataCollectionService(AbsenceMappingExportService absenceMappingExportService, CalendarSettingsService calendarSettingsService) {
        this.absenceMappingExportService = absenceMappingExportService;
        this.calendarSettingsService = calendarSettingsService;
    }

    CalendarIntegrationBackupDTO collectCalendarIntegration() {
        return getCalendarSettings()
            .map(this::createCalendarIntegrationBackupDTO)
            .orElse(null);
    }

    private CalendarIntegrationBackupDTO createCalendarIntegrationBackupDTO(CalendarSettings existingSettings) {
        final CalendarIntegrationSettingsDTO calendarIntegrationSettingsDTO = new CalendarIntegrationSettingsDTO(existingSettings.getId(), existingSettings.getProvider(), GoogleCalendarSettingsDTO.of(existingSettings.getGoogleCalendarSettings()));
        final List<AbsenceMappingDTO> absenceMappings = absenceMappingExportService.getAbsenceMappings().stream().map(AbsenceMappingDTO::of).toList();
        return new CalendarIntegrationBackupDTO(calendarIntegrationSettingsDTO, absenceMappings);
    }

    private Optional<CalendarSettings> getCalendarSettings() {
        try {
            return Optional.of(calendarSettingsService.getCalendarSettings());
        } catch (IllegalStateException e) {
            return Optional.empty();
        }
    }
}
