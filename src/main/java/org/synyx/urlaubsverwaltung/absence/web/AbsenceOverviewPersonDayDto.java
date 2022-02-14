package org.synyx.urlaubsverwaltung.absence.web;

public class AbsenceOverviewPersonDayDto {

    private final AbsenceOverviewDayType type;
    private final boolean workday;

    AbsenceOverviewPersonDayDto(AbsenceOverviewDayType type, boolean workday) {
        this.type = type;
        this.workday = workday;
    }

    public AbsenceOverviewDayType getType() {
        return type;
    }

    public boolean isWorkday() {
        return workday;
    }
}
