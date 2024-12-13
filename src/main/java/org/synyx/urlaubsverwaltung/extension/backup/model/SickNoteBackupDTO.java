package org.synyx.urlaubsverwaltung.extension.backup.model;

import java.util.List;

public record SickNoteBackupDTO(List<SickNoteTypeDTO> sickNoteTypes, List<SickNoteDTO> sickNotes) {
}
