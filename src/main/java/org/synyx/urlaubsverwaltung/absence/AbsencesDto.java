package org.synyx.urlaubsverwaltung.absence;

import java.util.List;

public class AbsencesDto {

    private final List<AbsenceDto> absences;

    AbsencesDto(List<AbsenceDto> absences) {
        this.absences = absences;
    }

    public List<AbsenceDto> getAbsences() {
        return absences;
    }
}
