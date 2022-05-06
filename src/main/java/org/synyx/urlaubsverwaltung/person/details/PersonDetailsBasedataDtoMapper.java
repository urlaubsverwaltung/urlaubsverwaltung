package org.synyx.urlaubsverwaltung.person.details;

import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedata;

final class PersonDetailsBasedataDtoMapper {

    private PersonDetailsBasedataDtoMapper() {
    }

    static PersonDetailsBasedataDto mapToPersonDetailsBasedataDto(PersonBasedata basedata) {
        return new PersonDetailsBasedataDto(basedata.getPersonnelNumber(), basedata.getAdditionalInformation());
    }
}
