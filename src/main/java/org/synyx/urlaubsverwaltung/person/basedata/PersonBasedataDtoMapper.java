package org.synyx.urlaubsverwaltung.person.basedata;

final class PersonBasedataDtoMapper {

    private PersonBasedataDtoMapper() {
    }

    static PersonBasedataDto mapToPersonBasedataDto(PersonBasedata personBasedata) {
        final PersonBasedataDto personBasedataDto = new PersonBasedataDto();
        personBasedataDto.setPersonId(personBasedata.getPersonId());
        personBasedataDto.setNiceName(personBasedata.getNiceName());
        personBasedataDto.setGravatarURL(personBasedata.getGravatarURL());
        personBasedataDto.setEmail(personBasedata.getEmail());
        personBasedataDto.setPersonnelNumber(personBasedata.getPersonnelNumber());
        personBasedataDto.setAdditionalInfo(personBasedata.getAdditionalInformation());
        return personBasedataDto;
    }

    static PersonBasedata mapToPersonBasedata(PersonBasedataDto personBasedataDto) {
        return new PersonBasedata(
            personBasedataDto.getPersonId(),
            personBasedataDto.getPersonnelNumber(),
            personBasedataDto.getAdditionalInfo(),
            personBasedataDto.getNiceName(),
            personBasedataDto.getGravatarURL(),
            personBasedataDto.getEmail());
    }
}
