package org.synyx.urlaubsverwaltung.absence.web;

public class AbsenceOverviewMonthDayDto {

    private final AbsenceOverviewDayType type;
    private final String dayOfMonth;
    private final boolean weekend;

    AbsenceOverviewMonthDayDto(AbsenceOverviewDayType type, String dayOfMonth, boolean weekend) {
        this.type = type;
        this.dayOfMonth = dayOfMonth;
        this.weekend = weekend;
    }

    public String getType() {
        return type == null ? "" : type.getIdentifier();
    }

    public String getDayOfMonth() {
        return dayOfMonth;
    }

    public boolean isWeekend() {
        return weekend;
    }
}
