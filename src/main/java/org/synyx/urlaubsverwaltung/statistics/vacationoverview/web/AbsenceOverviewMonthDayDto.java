package org.synyx.urlaubsverwaltung.statistics.vacationoverview.web;

public class AbsenceOverviewMonthDayDto {

    private final String dayOfMonth;
    private final boolean weekend;

    AbsenceOverviewMonthDayDto(String dayOfMonth, boolean weekend) {
        this.dayOfMonth = dayOfMonth;
        this.weekend = weekend;
    }

    public String getDayOfMonth() {
        return dayOfMonth;
    }

    public boolean isWeekend() {
        return weekend;
    }
}
