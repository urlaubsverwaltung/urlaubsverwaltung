package org.synyx.urlaubsverwaltung.person.account;

public class PersonAccountBasedataDto {

    private final String personnelNumber;
    private final String additionalInformation;

    PersonAccountBasedataDto(String personnelNumber, String additionalInformation) {
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
