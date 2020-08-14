package org.synyx.urlaubsverwaltung.overview.calendar;


import java.util.List;


class VacationListResponse {

    private List<VacationResponse> vacations;

    VacationListResponse(List<VacationResponse> vacations) {

        this.vacations = vacations;
    }

    public List<VacationResponse> getVacations() {

        return vacations;
    }

    public void setVacations(List<VacationResponse> vacations) {

        this.vacations = vacations;
    }
}
