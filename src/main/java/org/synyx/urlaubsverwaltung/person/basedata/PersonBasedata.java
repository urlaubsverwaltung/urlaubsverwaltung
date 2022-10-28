package org.synyx.urlaubsverwaltung.person.basedata;

import org.synyx.urlaubsverwaltung.person.PersonId;

public class PersonBasedata {

    private final PersonId personId;
    private final String personnelNumber;
    private final String additionalInformation;

    public PersonBasedata(PersonId personId, String personnelNumber, String additionalInformation) {
        this.personId = personId;
        this.personnelNumber = personnelNumber;
        this.additionalInformation = additionalInformation;
    }

    public PersonId getPersonId() {
        return personId;
    }

    public String getPersonnelNumber() {
        return personnelNumber;
    }

    public String getAdditionalInformation() {
        return additionalInformation;
    }
}
