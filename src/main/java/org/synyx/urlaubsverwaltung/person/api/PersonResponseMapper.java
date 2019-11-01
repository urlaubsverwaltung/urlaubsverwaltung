package org.synyx.urlaubsverwaltung.person.api;

import org.synyx.urlaubsverwaltung.person.Person;

public final class PersonResponseMapper {

    private PersonResponseMapper() {
        // prevents initialisation
    }

    public static PersonResponse mapToResponse(Person person) {
        return new PersonResponse(person.getEmail(), person.getFirstName(), person.getLastName(), person.getNiceName());
    }
}
