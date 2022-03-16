package org.synyx.urlaubsverwaltung.person.basedata;

import org.synyx.urlaubsverwaltung.person.Person;

class PersonBasedataMapper {

    private PersonBasedataMapper() {
    }

    public static PersonBasedata mapFromEntity(PersonBasedataEntity personBasedataEntity) {

        final Person person = personBasedataEntity.getPerson();

        return new PersonBasedata(
            personBasedataEntity.getPersonId(),
            personBasedataEntity.getPersonnelNumber(),
            personBasedataEntity.getAdditionalInformation(),
            person.getNiceName(),
            person.getGravatarURL(),
            person.getEmail());
    }

    public static PersonBasedataEntity mapToEntity(PersonBasedata personBasedata) {
        PersonBasedataEntity personBasedataEntity = new PersonBasedataEntity();
        personBasedataEntity.setPersonId(personBasedata.getPersonId());
        personBasedataEntity.setPersonnelNumber(personBasedata.getPersonnelNumber());
        personBasedataEntity.setAdditionalInformation(personBasedata.getAdditionalInformation());
        return personBasedataEntity;
    }
}
