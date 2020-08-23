package org.synyx.urlaubsverwaltung.absence;

import java.util.List;

public class DayAbsencesDto {

    private final List<DayAbsenceDto> absences;

    DayAbsencesDto(List<DayAbsenceDto> absences) {
        this.absences = absences;
    }

    public List<DayAbsenceDto> getAbsences() {
        return absences;
    }
}
