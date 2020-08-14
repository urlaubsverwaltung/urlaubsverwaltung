package org.synyx.urlaubsverwaltung.person.api;

import java.util.List;


public class PersonsDto {

    private List<PersonDto> persons;

    public PersonsDto(List<PersonDto> persons) {
        this.persons = persons;
    }

    public List<PersonDto> getPersons() {
        return persons;
    }

    public void setPersons(List<PersonDto> persons) {
        this.persons = persons;
    }
}
