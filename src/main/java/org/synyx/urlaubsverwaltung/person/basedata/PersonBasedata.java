package org.synyx.urlaubsverwaltung.person.basedata;

public class PersonBasedata {

    private final int personId;
    private final String personnelNumber;
    private final String additionalInformation;

    public PersonBasedata(int personId, String personnelNumber, String additionalInformation) {
        this.personId = personId;
        this.personnelNumber = personnelNumber;
        this.additionalInformation = additionalInformation;
    }

    static PersonBasedata.Builder builder() {
        return new PersonBasedata.Builder();
    }

    static class Builder {
        private int personId;
        private String personnelNumber;
        private String additionalInformation;

        public Builder withPersonId(int personId) {
            this.personId = personId;
            return this;
        }

        public Builder withPersonnelNumber(String personnelNumber) {
            this.personnelNumber = personnelNumber;
            return this;
        }

        public Builder withAdditionalInformation(String additionalInformation) {
            this.additionalInformation = additionalInformation;
            return this;
        }

        public PersonBasedata build() {
            return new PersonBasedata(personId, personnelNumber, additionalInformation);
        }
    }

    public int getPersonId() {
        return personId;
    }

    public String getPersonnelNumber() {
        return personnelNumber;
    }

    public String getAdditionalInformation() {
        return additionalInformation;
    }
}
