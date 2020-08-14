package org.synyx.urlaubsverwaltung.statistics.vacationoverview.web;

public class AbsenceOverviewPersonDayDto {

    private final AbsenceOverviewDayType type;
    private final boolean weekend;

    AbsenceOverviewPersonDayDto(AbsenceOverviewDayType type, boolean weekend) {
        this.type = type;
        this.weekend = weekend;
    }

    public String getType() {
        return type == null ? "" : type.getIdentifier();
    }

    public boolean isWeekend() {
        return weekend;
    }
}
