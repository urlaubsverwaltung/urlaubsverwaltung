package org.synyx.urlaubsverwaltung.person.basedata;

import jakarta.validation.constraints.Size;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;

@Validated
public class PersonBasedataDto {

    @Size(max = 20, message = "{person.basedata.personnelNumber.error}")
    private String personnelNumber;
    @Size(max = 500, message = "{person.basedata.additionalInformation.error}")
    private String additionalInfo;

    private long personId;
    private String niceName;
    private String initials;
    private String gravatarURL;
    private String email;
    private Boolean isInactive;

    public long getPersonId() {
        return personId;
    }

    public void setPersonId(long personId) {
        this.personId = personId;
    }

    public String getNiceName() {
        return niceName;
    }

    public void setNiceName(String niceName) {
        this.niceName = niceName;
    }

    public String getInitials() {
        return initials;
    }

    public void setInitials(String initials) {
        this.initials = initials;
    }

    public String getGravatarURL() {
        return gravatarURL;
    }

    public PersonBasedataDto setGravatarURL(String gravatarURL) {
        this.gravatarURL = gravatarURL;
        return this;
    }

    @Nullable
    public String getEmail() {
        return email;
    }

    public PersonBasedataDto setEmail(@Nullable String email) {
        this.email = email;
        return this;
    }

    public Boolean getIsInactive() {
        return isInactive;
    }

    public PersonBasedataDto setIsInactive(Boolean isInactive) {
        this.isInactive = isInactive;
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
