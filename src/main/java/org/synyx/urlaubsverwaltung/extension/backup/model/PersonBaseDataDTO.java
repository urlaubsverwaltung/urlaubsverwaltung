package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedataEntity;

public record PersonBaseDataDTO(String personnelNumber, String additionalInformation) {

    public PersonBasedataEntity toPersonBaseDataEntity(Long personId) {
        final PersonBasedataEntity entity = new PersonBasedataEntity();
        entity.setPersonId(personId);
        entity.setPersonnelNumber(this.personnelNumber);
        entity.setAdditionalInformation(this.additionalInformation);
        return entity;
    }
}
