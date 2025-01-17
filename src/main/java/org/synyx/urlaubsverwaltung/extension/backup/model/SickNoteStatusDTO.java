package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus;

public enum SickNoteStatusDTO {

    SUBMITTED,
    ACTIVE,
    CONVERTED_TO_VACATION,
    CANCELLED;

    public SickNoteStatus toSickNoteStatus() {
        return SickNoteStatus.valueOf(this.name());
    }

}
