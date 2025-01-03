package org.synyx.urlaubsverwaltung.person.api;

import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.Role;

public final class PersonMapper {

    private PersonMapper() {
        // prevents initialisation
    }

    public static PersonDto mapToDto(Person person) {
        final boolean active = !person.hasRole(Role.INACTIVE);
        return new PersonDto(person.getId(), person.getEmail(), person.getFirstName(), person.getLastName(), person.getNiceName(), active);
    }
}
