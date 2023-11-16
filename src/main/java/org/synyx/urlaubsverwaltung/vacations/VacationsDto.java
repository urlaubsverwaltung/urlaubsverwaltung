package org.synyx.urlaubsverwaltung.vacations;

import org.springframework.hateoas.RepresentationModel;

import java.util.List;

public class VacationsDto extends RepresentationModel<VacationsDto> {

    private final List<VacationDto> vacations;

    VacationsDto(List<VacationDto> vacations) {
        this.vacations = vacations;
    }

    public List<VacationDto> getVacations() {
        return vacations;
    }
}
