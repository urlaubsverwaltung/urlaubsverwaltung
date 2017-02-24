package org.synyx.urlaubsverwaltung.restapi.vacation;

import org.synyx.urlaubsverwaltung.restapi.absence.AbsenceResponse;

import java.util.List;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
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
