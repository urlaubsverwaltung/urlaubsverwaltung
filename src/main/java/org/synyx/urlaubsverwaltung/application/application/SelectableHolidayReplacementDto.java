package org.synyx.urlaubsverwaltung.application.application;

public class SelectableHolidayReplacementDto {

    private Long personId;
    private String displayName;

    public Long getPersonId() {
        return personId;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
