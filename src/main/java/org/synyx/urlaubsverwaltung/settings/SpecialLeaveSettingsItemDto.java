package org.synyx.urlaubsverwaltung.settings;

import jakarta.validation.constraints.Min;

public class SpecialLeaveSettingsItemDto {

    private int id;
    private boolean active;
    private String messageKey;
    @Min(0)
    private int days;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }
}
