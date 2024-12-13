package org.synyx.urlaubsverwaltung.extension.backup.model;

import java.util.List;

public record ApplicationBackupDTO(List<VacationTypeDTO> vacationTypes, List<ApplicationDTO> applications) {
}
