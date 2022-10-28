package org.synyx.urlaubsverwaltung.absence.web;

public class AbsenceOverviewMonthDayDto {

    private final AbsenceOverviewDayType type;
    private final String dayOfMonth;
    private final String dayOfWeek;
    private final boolean weekend;
    private final boolean isToday;

    AbsenceOverviewMonthDayDto(AbsenceOverviewDayType type, String dayOfMonth, String dayOfWeek, boolean weekend, boolean isToday) {
        this.type = type;
        this.dayOfMonth = dayOfMonth;
        this.dayOfWeek = dayOfWeek;
        this.weekend = weekend;
        this.isToday = isToday;
    }

    public AbsenceOverviewDayType getType() {
        return type;
    }

    public String getDayOfMonth() {
        return dayOfMonth;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public boolean isWeekend() {
        return weekend;
    }

    public boolean isToday() {
        return isToday;
    }
}
