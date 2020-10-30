package org.synyx.urlaubsverwaltung.absence.web;

public class AbsenceOverviewMonthDayDto {

    private final String dayOfMonth;
    private final boolean weekend;
    private final boolean holiday;

    AbsenceOverviewMonthDayDto(String dayOfMonth, boolean weekend, boolean holiday) {
        this.dayOfMonth = dayOfMonth;
        this.weekend = weekend;
        this.holiday = holiday;
    }

    public String getDayOfMonth() {
        return dayOfMonth;
    }

    public boolean isWeekend() {
        return weekend;
    }

    public boolean isHoliday() {
        return holiday;
    }
}
