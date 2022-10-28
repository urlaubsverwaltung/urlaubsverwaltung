package org.synyx.urlaubsverwaltung.person.basedata;

import org.synyx.urlaubsverwaltung.person.PersonId;

class PersonBasedataMapper {

    private PersonBasedataMapper() {
        // ok
    }

    public static PersonBasedata mapFromEntity(PersonBasedataEntity personBasedataEntity) {
        return new PersonBasedata(
            new PersonId(personBasedataEntity.getPersonId()),
            personBasedataEntity.getPersonnelNumber(),
            personBasedataEntity.getAdditionalInformation()
        );
    }

    public static PersonBasedataEntity mapToEntity(PersonBasedata personBasedata) {
        final PersonBasedataEntity personBasedataEntity = new PersonBasedataEntity();
        personBasedataEntity.setPersonId(personBasedata.getPersonId().getValue());
        personBasedataEntity.setPersonnelNumber(personBasedata.getPersonnelNumber());
        personBasedataEntity.setAdditionalInformation(personBasedata.getAdditionalInformation());
        return personBasedataEntity;
    }
}
