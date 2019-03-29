package org.synyx.urlaubsverwaltung.overview.calendar;

import org.synyx.urlaubsverwaltung.restapi.absence.AbsenceResponse;

import java.util.List;


class VacationListResponse {

    private List<AbsenceResponse> vacations;

    VacationListResponse(List<AbsenceResponse> vacations) {

        this.vacations = vacations;
    }

    public List<AbsenceResponse> getVacations() {

        return vacations;
    }


    public void setVacations(List<AbsenceResponse> vacations) {

        this.vacations = vacations;
    }
}
