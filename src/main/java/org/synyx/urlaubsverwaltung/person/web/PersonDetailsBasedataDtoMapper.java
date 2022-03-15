package org.synyx.urlaubsverwaltung.person.web;

import org.synyx.urlaubsverwaltung.person.masterdata.PersonBasedata;

final class PersonDetailsBasedataDtoMapper {

    private PersonDetailsBasedataDtoMapper() {
    }

    static PersonDetailsBasedataDto mapToPersonDetailsBasedataDto(PersonBasedata basedata) {
        return  new PersonDetailsBasedataDto(basedata.getPersonnelNumber(), basedata.getAdditionalInformation());
    }
}
