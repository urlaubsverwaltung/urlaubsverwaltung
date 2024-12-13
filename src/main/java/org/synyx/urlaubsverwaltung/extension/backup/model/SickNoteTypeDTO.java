package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.synyx.urlaubsverwaltung.sicknote.sicknotetype.SickNoteType;

public record SickNoteTypeDTO(Long id, SickNoteTypeCategoryDTO category, String messageKey) {
    public SickNoteType toSickNoteEntity() {
        SickNoteType sickNoteType = new SickNoteType();
        sickNoteType.setCategory(this.category.toSickNoteTypeCategory());
        sickNoteType.setMessageKey(this.messageKey);
        return sickNoteType;
    }
}
