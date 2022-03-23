package org.synyx.urlaubsverwaltung.person.basedata;

class PersonBasedataMapper {

    private PersonBasedataMapper() {
        // ok
    }

    public static PersonBasedata mapFromEntity(PersonBasedataEntity personBasedataEntity) {
        return new PersonBasedata(
            personBasedataEntity.getPersonId(),
            personBasedataEntity.getPersonnelNumber(),
            personBasedataEntity.getAdditionalInformation()
        );
    }

    public static PersonBasedataEntity mapToEntity(PersonBasedata personBasedata) {
        final PersonBasedataEntity personBasedataEntity = new PersonBasedataEntity();
        personBasedataEntity.setPersonId(personBasedata.getPersonId());
        personBasedataEntity.setPersonnelNumber(personBasedata.getPersonnelNumber());
        personBasedataEntity.setAdditionalInformation(personBasedata.getAdditionalInformation());
        return personBasedataEntity;
    }
}
