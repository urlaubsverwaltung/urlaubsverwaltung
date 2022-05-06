package org.synyx.urlaubsverwaltung.person.account;

import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedata;

final class PersonAccountBasedataDtoMapper {

    private PersonAccountBasedataDtoMapper() {
    }

    static PersonAccountBasedataDto mapToPersonDetailsBasedataDto(PersonBasedata basedata) {
        return new PersonAccountBasedataDto(basedata.getPersonnelNumber(), basedata.getAdditionalInformation());
    }
}
