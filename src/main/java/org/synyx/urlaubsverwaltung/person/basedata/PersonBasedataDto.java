package org.synyx.urlaubsverwaltung.person.basedata;

import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Size;

@Validated
public class PersonBasedataDto {

    @Size(max = 255, message = "{person.basedata.personnelNumber.error}")
    private String personnelNumber;
    @Size(max = 255, message = "{person.basedata.additionalInformation.error}")
    private String additionalInfo;

    private int personId;
    private String niceName;
    private String gravatarURL;
    private String email;

    public int getPersonId() {
        return personId;
    }

    public void setPersonId(int personId) {
        this.personId = personId;
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

    public PersonBasedataDto setGravatarURL(String gravatarURL) {
        this.gravatarURL = gravatarURL;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public PersonBasedataDto setEmail(String email) {
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
