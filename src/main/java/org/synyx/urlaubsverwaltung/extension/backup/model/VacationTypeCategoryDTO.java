package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory;

public enum VacationTypeCategoryDTO {
    HOLIDAY, SPECIALLEAVE, UNPAIDLEAVE, OVERTIME, OTHER;

    public VacationCategory toVacationCategory() {
        return VacationCategory.valueOf(this.name());
    }
}
