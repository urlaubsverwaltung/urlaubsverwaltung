package org.synyx.urlaubsverwaltung.restapi;

import java.util.List;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
class PersonListResponse {

    private List<PersonResponse> persons;

    PersonListResponse(List<PersonResponse> persons) {

        this.persons = persons;
    }

    public List<PersonResponse> getPersons() {

        return persons;
    }


    public void setPersons(List<PersonResponse> persons) {

        this.persons = persons;
    }
}
