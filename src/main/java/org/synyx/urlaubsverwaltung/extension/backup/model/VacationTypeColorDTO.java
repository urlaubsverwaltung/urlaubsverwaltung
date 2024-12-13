package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor;

public enum VacationTypeColorDTO {
    GRAY, ORANGE, YELLOW, EMERALD, CYAN, BLUE, VIOLET, PINK;

    public VacationTypeColor toVacationTypeColor() {
        return VacationTypeColor.valueOf(this.name());
    }
}
