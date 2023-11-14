package org.synyx.urlaubsverwaltung.person.api;

import org.springframework.hateoas.RepresentationModel;

import java.util.List;

public class PersonsDto extends RepresentationModel<PersonsDto> {

    private final List<PersonDto> persons;

    public PersonsDto(List<PersonDto> persons) {
        this.persons = persons;
    }

    public List<PersonDto> getPersons() {
        return persons;
    }
}
