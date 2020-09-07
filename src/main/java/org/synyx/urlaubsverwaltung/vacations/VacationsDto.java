package org.synyx.urlaubsverwaltung.vacations;

import java.util.List;

class VacationsDto {

    private List<VacationDto> vacations;

    VacationsDto(List<VacationDto> vacations) {
        this.vacations = vacations;
    }

    public List<VacationDto> getVacations() {
        return vacations;
    }

    public void setVacations(List<VacationDto> vacations) {
        this.vacations = vacations;
    }
}
