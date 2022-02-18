package org.synyx.urlaubsverwaltung.person.web;

public class PersonMasterdataDto {

    private Integer id;
    private String niceName;
    private String gravatarURL;
    private String email;

    private String personnelNumber;
    private String additionalInfo;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNiceName() {
        return niceName;
    }

    public void setNiceName(String niceName) {
        this.niceName = niceName;
    }

    public String getGravatarURL() {
        return gravatarURL;
    }

    public PersonMasterdataDto setGravatarURL(String gravatarURL) {
        this.gravatarURL = gravatarURL;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public PersonMasterdataDto setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getPersonnelNumber() {
        return personnelNumber;
    }

    public void setPersonnelNumber(String personnelNumber) {
        this.personnelNumber = personnelNumber;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }
}
