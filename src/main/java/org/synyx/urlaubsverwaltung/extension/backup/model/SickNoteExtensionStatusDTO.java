package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.synyx.urlaubsverwaltung.sicknote.sicknote.extend.SickNoteExtensionStatus;

public enum SickNoteExtensionStatusDTO {
    SUPERSEDED,
    SUBMITTED,
    ACCEPTED;

    public SickNoteExtensionStatus toSickNoteExtensionStatus() {
        return SickNoteExtensionStatus.valueOf(this.name());
    }
}
