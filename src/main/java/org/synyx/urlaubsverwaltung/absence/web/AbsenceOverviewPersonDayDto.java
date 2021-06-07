package org.synyx.urlaubsverwaltung.absence.web;

public class AbsenceOverviewPersonDayDto {

    private final AbsenceOverviewDayType type;
    private final boolean weekend;

    AbsenceOverviewPersonDayDto(AbsenceOverviewDayType type, boolean weekend) {
        this.type = type;
        this.weekend = weekend;
    }

    public AbsenceOverviewDayType getType() {
        return type;
    }

    public boolean isWeekend() {
        return weekend;
    }
}
