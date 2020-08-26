package org.synyx.urlaubsverwaltung.absence.web;

import java.util.List;

public class AbsenceOverviewMonthDto {

    private final String nameOfMonth;
    private final List<AbsenceOverviewMonthDayDto> days;
    private final List<AbsenceOverviewMonthPersonDto> persons;

    AbsenceOverviewMonthDto(String nameOfMonth, List<AbsenceOverviewMonthDayDto> days, List<AbsenceOverviewMonthPersonDto> persons) {
        this.nameOfMonth = nameOfMonth;
        this.days = days;
        this.persons = persons;
    }

    public String getNameOfMonth() {
        return nameOfMonth;
    }

    public List<AbsenceOverviewMonthDayDto> getDays() {
        return days;
    }

    public List<AbsenceOverviewMonthPersonDto> getPersons() {
        return persons;
    }
}
