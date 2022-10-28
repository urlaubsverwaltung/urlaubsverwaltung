package org.synyx.urlaubsverwaltung.person.web;

public class PersonDetailsBasedataDto {

    private final String personnelNumber;
    private final String additionalInformation;

    PersonDetailsBasedataDto(String personnelNumber, String additionalInformation) {
        this.personnelNumber = personnelNumber;
        this.additionalInformation = additionalInformation;
    }

    public String getPersonnelNumber() {
        return personnelNumber;
    }

    public String getAdditionalInformation() {
        return additionalInformation;
    }
}
