package org.synyx.urlaubsverwaltung.person.web;

import org.synyx.urlaubsverwaltung.person.Person;

final class PersonBasedataDtoMapper {

    private PersonBasedataDtoMapper() {
    }

    static PersonBasedataDto mapToPersonBasedataDto(Person person) {
        final PersonBasedataDto personBasedataDto = new PersonBasedataDto();
        personBasedataDto.setId(person.getId());
        personBasedataDto.setNiceName(person.getNiceName());
        personBasedataDto.setGravatarURL(person.getGravatarURL());
        personBasedataDto.setEmail(person.getEmail());
        // todo
//        personBasedataDto.setPersonnelNummber();
//        personBasedataDto.setAdditionalInfo();
        return personBasedataDto;
    }

    static Person merge(Person person, PersonBasedataDto personBasedataDto) {
        // todo
//        person.setPersonnelNumber(personMasterdataDto.getPersonnelNumber());
//        person.setAdditionalInfo(personMasterdataDto.getAdditionalInfo());
        return person;
    }
}
