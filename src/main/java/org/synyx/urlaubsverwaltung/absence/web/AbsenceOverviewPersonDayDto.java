package org.synyx.urlaubsverwaltung.absence.web;

public class AbsenceOverviewPersonDayDto {

    private final AbsenceOverviewDayType type;
    private final boolean weekend;
    private final boolean workday;

    AbsenceOverviewPersonDayDto(AbsenceOverviewDayType type, boolean weekend, boolean workday) {
        this.type = type;
        this.weekend = weekend;
        this.workday = workday;
    }

    public AbsenceOverviewDayType getType() {
        return type;
    }

    public boolean isWeekend() {
        return weekend;
    }

    public boolean isWorkday() {
        return workday;
    }
}
