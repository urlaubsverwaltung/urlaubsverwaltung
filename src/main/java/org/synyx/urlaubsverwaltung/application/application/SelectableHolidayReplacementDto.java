package org.synyx.urlaubsverwaltung.application.application;

public class SelectableHolidayReplacementDto {

    private Integer personId;
    private String displayName;

    public Integer getPersonId() {
        return personId;
    }

    public void setPersonId(Integer personId) {
        this.personId = personId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
