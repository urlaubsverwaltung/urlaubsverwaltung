package org.synyx.urlaubsverwaltung.person.masterdata;

final class PersonBasedataDtoMapper {

    private PersonBasedataDtoMapper() {
    }

    static PersonBasedataDto mapToPersonBasedataDto(PersonBasedata personBasedata) {
        final PersonBasedataDto personMasterdataDto = new PersonBasedataDto();
        personMasterdataDto.setPersonId(personBasedata.getPersonId());
        personMasterdataDto.setNiceName(personBasedata.getNiceName());
        personMasterdataDto.setGravatarURL(personBasedata.getGravatarURL());
        personMasterdataDto.setEmail(personBasedata.getEmail());
        personMasterdataDto.setPersonnelNumber(personBasedata.getPersonnelNumber());
        personMasterdataDto.setAdditionalInfo(personBasedata.getAdditionalInformation());
        return personMasterdataDto;
    }

    static PersonBasedata mapToPersonBasedata(PersonBasedataDto personMasterdataDto) {
        return new PersonBasedata(
            personMasterdataDto.getPersonId(),
            personMasterdataDto.getPersonnelNumber(),
            personMasterdataDto.getAdditionalInfo(),
            personMasterdataDto.getNiceName(),
            personMasterdataDto.getGravatarURL(),
            personMasterdataDto.getEmail());
    }
}
