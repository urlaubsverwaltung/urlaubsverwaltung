package org.synyx.urlaubsverwaltung.person.api;

import org.synyx.urlaubsverwaltung.person.Person;

public final class PersonMapper {

    private PersonMapper() {
        // prevents initialisation
    }

    public static PersonDto mapToDto(Person person) {
        return new PersonDto(person.getEmail(), person.getFirstName(), person.getLastName(), person.getNiceName());
    }
}
