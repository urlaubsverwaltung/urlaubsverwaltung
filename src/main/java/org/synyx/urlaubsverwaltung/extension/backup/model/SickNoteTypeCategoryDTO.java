package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory;

public enum SickNoteTypeCategoryDTO {
    SICK_NOTE,
    SICK_NOTE_CHILD;

    public SickNoteCategory toSickNoteTypeCategory() {
        return SickNoteCategory.valueOf(this.name());
    }
}
