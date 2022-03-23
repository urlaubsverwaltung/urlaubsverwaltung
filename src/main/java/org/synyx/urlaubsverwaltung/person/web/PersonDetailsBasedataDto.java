package org.synyx.urlaubsverwaltung.person.web;

public class PersonDetailsBasedataDto {

    private final String personnelNumber;
    private final String additionalInfo;

    PersonDetailsBasedataDto(String personnelNumber, String additionalInfo) {
        this.personnelNumber = personnelNumber;
        this.additionalInfo = additionalInfo;
    }

    public String getPersonnelNumber() {
        return personnelNumber;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }
}
