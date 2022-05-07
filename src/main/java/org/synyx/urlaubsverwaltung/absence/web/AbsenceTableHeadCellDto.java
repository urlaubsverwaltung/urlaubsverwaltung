package org.synyx.urlaubsverwaltung.absence.web;

public class AbsenceTableHeadCellDto {

    private final AbsenceTableCellTypeDto type;
    private final String dayOfMonth;
    private final String dayOfWeek;
    private final boolean weekend;
    private final boolean isToday;

    AbsenceTableHeadCellDto(AbsenceTableCellTypeDto type, String dayOfMonth, String dayOfWeek, boolean weekend, boolean isToday) {
        this.type = type;
        this.dayOfMonth = dayOfMonth;
        this.dayOfWeek = dayOfWeek;
        this.weekend = weekend;
        this.isToday = isToday;
    }

    public AbsenceTableCellTypeDto getType() {
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
