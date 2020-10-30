package org.synyx.urlaubsverwaltung.absence.web;

public class AbsenceOverviewPersonDayDto {

    private final AbsenceOverviewDayType type;
    private final boolean weekend;
    private final boolean holiday;

    AbsenceOverviewPersonDayDto(AbsenceOverviewDayType type, boolean weekend, boolean holiday) {
        this.type = type;
        this.weekend = weekend;
        this.holiday = holiday;
    }

    public String getType() {
        return type == null ? "" : type.getIdentifier();
    }

    public boolean isWeekend() {
        return weekend;
    }

    public boolean isHoliday() {
        return holiday;
    }
}
