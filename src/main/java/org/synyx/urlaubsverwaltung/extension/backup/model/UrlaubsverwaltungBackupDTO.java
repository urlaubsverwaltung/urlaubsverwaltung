package org.synyx.urlaubsverwaltung.extension.backup.model;

import java.util.List;

public record UrlaubsverwaltungBackupDTO(String tenantId, String urlaubsverwaltungVersion, List<PersonDTO> persons,
                                         List<OvertimeDTO> overtimes, SickNoteBackupDTO sickNotes,
                                         ApplicationBackupDTO applications, List<DepartmentDTO> departments,
                                         CalendarBackupDTO calendars,
                                         CalendarIntegrationBackupDTO calendarIntegration,
                                         SettingsDTO settings) {
}
