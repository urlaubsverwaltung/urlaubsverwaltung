package org.synyx.urlaubsverwaltung.person.basedata;

public class PersonBasedata {

    private final int personId;
    private final String personnelNumber;
    private final String additionalInformation;

    private final String niceName;
    private final String gravatarURL;
    private final String email;

    public PersonBasedata(int personId, String personnelNumber, String additionalInformation, String niceName,
                          String gravatarURL, String email) {
        this.personId = personId;
        this.personnelNumber = personnelNumber;
        this.additionalInformation = additionalInformation;
        this.niceName = niceName;
        this.gravatarURL = gravatarURL;
        this.email = email;
    }

    static PersonBasedata.Builder builder() {
        return new PersonBasedata.Builder();
    }

    static class Builder {
        private int personId;
        private String personnelNumber;
        private String additionalInformation;

        private String niceName;
        private String gravatarURL;
        private String email;

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

        public Builder withNiceName(String niceName) {
            this.niceName = niceName;
            return this;
        }

        public Builder withGravatarURL(String gravatarURL) {
            this.gravatarURL = gravatarURL;
            return this;
        }

        public Builder withEmail(String email) {
            this.email = email;
            return this;
        }

        public PersonBasedata build() {
            return new PersonBasedata(personId, personnelNumber, additionalInformation, niceName, gravatarURL, email);
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

    public String getNiceName() {
        return niceName;
    }

    public String getGravatarURL() {
        return gravatarURL;
    }

    public String getEmail() {
        return email;
    }
}
