package org.synyx.urlaubsverwaltung.person.api;

import java.util.List;


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
