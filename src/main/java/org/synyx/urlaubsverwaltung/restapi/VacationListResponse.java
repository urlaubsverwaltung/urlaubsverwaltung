package org.synyx.urlaubsverwaltung.restapi;

import java.util.List;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
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
