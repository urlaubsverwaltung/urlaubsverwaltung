package org.synyx.urlaubsverwaltung.application.specialleave;

public class SpecialLeaveSettingsItem {

    private final Integer id;
    private final Boolean active;
    private final String messageKey;
    private final Integer days;

    public SpecialLeaveSettingsItem(Integer id, Boolean active, String messageKey, Integer days) {
        this.id = id;
        this.active = active;
        this.messageKey = messageKey;
        this.days = days;
    }

    public Integer getId() {
        return id;
    }

    public Boolean isActive() {
        return active;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public Integer getDays() {
        return days;
    }
}
