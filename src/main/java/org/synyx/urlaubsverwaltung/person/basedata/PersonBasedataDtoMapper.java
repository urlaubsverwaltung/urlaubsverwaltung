package org.synyx.urlaubsverwaltung.person.basedata;

import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;

final class PersonBasedataDtoMapper {

    private PersonBasedataDtoMapper() {
    }

    static PersonBasedataDto mapToPersonBasedataDto(PersonBasedata personBasedata, Person person) {
        final PersonBasedataDto personBasedataDto = new PersonBasedataDto();
        personBasedataDto.setPersonId(personBasedata.getPersonId().getValue());
        personBasedataDto.setPersonnelNumber(personBasedata.getPersonnelNumber());
        personBasedataDto.setAdditionalInfo(personBasedata.getAdditionalInformation());
        personBasedataDto.setNiceName(person.getNiceName());
        personBasedataDto.setGravatarURL(person.getGravatarURL());
        personBasedataDto.setEmail(person.getEmail());
        personBasedataDto.setIsInactive(person.isInactive());
        return personBasedataDto;
    }

    static PersonBasedata mapToPersonBasedata(PersonBasedataDto personBasedataDto) {
        return new PersonBasedata(
            new PersonId(personBasedataDto.getPersonId()),
            personBasedataDto.getPersonnelNumber(),
            personBasedataDto.getAdditionalInfo());
    }
}
