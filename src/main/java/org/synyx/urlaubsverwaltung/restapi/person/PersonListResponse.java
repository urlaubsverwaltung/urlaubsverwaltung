package org.synyx.urlaubsverwaltung.restapi.person;

import java.util.List;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class PersonListResponse {

    private List<PersonResponse> persons;

    public PersonListResponse(List<PersonResponse> persons) {

        this.persons = persons;
    }

    public List<PersonResponse> getPersons() {

        return persons;
    }


    public void setPersons(List<PersonResponse> persons) {

        this.persons = persons;
    }
}
