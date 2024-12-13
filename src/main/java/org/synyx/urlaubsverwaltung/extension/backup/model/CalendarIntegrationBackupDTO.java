package org.synyx.urlaubsverwaltung.extension.backup.model;

import java.util.List;

public record CalendarIntegrationBackupDTO(CalendarIntegrationSettingsDTO calendarIntegrationSettings,
                                           List<AbsenceMappingDTO> absenceMappings) {
}
