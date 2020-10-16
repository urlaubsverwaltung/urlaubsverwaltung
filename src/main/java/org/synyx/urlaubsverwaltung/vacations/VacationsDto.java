package org.synyx.urlaubsverwaltung.vacations;

import java.util.List;

class VacationsDto {

    private final List<VacationDto> vacations;

    VacationsDto(List<VacationDto> vacations) {
        this.vacations = vacations;
    }

    public List<VacationDto> getVacations() {
        return vacations;
    }

}
