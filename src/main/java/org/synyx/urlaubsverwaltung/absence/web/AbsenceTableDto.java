package org.synyx.urlaubsverwaltung.absence.web;

import java.util.List;

public class AbsenceTableDto {

    private final String nameOfMonth;
    private final List<AbsenceTableHeadCellDto> days;
    private final List<AbsenceTableRowDto> persons;

    AbsenceTableDto(String nameOfMonth, List<AbsenceTableHeadCellDto> days, List<AbsenceTableRowDto> persons) {
        this.nameOfMonth = nameOfMonth;
        this.days = days;
        this.persons = persons;
    }

    public String getNameOfMonth() {
        return nameOfMonth;
    }

    public List<AbsenceTableHeadCellDto> getDays() {
        return days;
    }

    public List<AbsenceTableRowDto> getPersons() {
        return persons;
    }
}
