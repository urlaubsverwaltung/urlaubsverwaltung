package org.synyx.urlaubsverwaltung.person.web;

import org.synyx.urlaubsverwaltung.person.Person;

import java.util.List;

final class PersonMasterdataDtoMapper {

    private PersonMasterdataDtoMapper() {
    }

    static PersonMasterdataDto mapToPersonMasterdataDto(Person person) {
        final PersonMasterdataDto personPermissionsDto = new PersonMasterdataDto();
        personPermissionsDto.setId(person.getId());
        personPermissionsDto.setNiceName(person.getNiceName());
        personPermissionsDto.setGravatarURL(person.getGravatarURL());
        personPermissionsDto.setEmail(person.getEmail());
        // todo
//        personPermissionsDto.setPersonnelNummber();
//        personPermissionsDto.setAdditionalInfo();
        return personPermissionsDto;
    }

    static Person merge(Person person, PersonMasterdataDto personMasterdataDto) {
        // todo
//        person.setPersonnelNumber(personMasterdataDto.getPersonnelNumber());
//        person.setAdditionalInfo(personMasterdataDto.getAdditionalInfo());
        return person;
    }
}
