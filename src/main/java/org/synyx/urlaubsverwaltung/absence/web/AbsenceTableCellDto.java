package org.synyx.urlaubsverwaltung.absence.web;

public class AbsenceTableCellDto {

    private final AbsenceTableCellTypeDto type;
    private final boolean workday;

    AbsenceTableCellDto(AbsenceTableCellTypeDto type, boolean workday) {
        this.type = type;
        this.workday = workday;
    }

    public AbsenceTableCellTypeDto getType() {
        return type;
    }

    public boolean isWorkday() {
        return workday;
    }
}
