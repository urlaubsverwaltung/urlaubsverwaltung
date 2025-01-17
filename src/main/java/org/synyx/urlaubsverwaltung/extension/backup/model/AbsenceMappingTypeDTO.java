package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.synyx.urlaubsverwaltung.calendarintegration.AbsenceMappingType;

public enum AbsenceMappingTypeDTO {
    VACATION,
    SICKNOTE;

    public AbsenceMappingType toAbsenceMappingType() {
        return AbsenceMappingType.valueOf(this.name());
    }
}
