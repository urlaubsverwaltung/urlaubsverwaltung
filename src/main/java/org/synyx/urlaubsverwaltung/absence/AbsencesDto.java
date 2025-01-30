package org.synyx.urlaubsverwaltung.absence;

import org.springframework.hateoas.RepresentationModel;

import java.util.List;

public class AbsencesDto extends RepresentationModel<AbsencesDto> {

    private final List<AbsenceDto> absences;

    AbsencesDto(List<AbsenceDto> absences) {
        this.absences = absences;
    }

    public List<AbsenceDto> getAbsences() {
        return absences;
    }
}
