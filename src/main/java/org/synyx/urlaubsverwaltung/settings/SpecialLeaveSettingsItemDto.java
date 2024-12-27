package org.synyx.urlaubsverwaltung.settings;

import jakarta.validation.constraints.Min;

import java.util.Objects;

public class SpecialLeaveSettingsItemDto {

    private long id;
    private boolean active;
    private String messageKey;
    @Min(0)
    private int days;

    public long getId() {
        return id;
    }

    public void setId(long id) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpecialLeaveSettingsItemDto that = (SpecialLeaveSettingsItemDto) o;
        return id == that.id && active == that.active && days == that.days && Objects.equals(messageKey, that.messageKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, active, messageKey, days);
    }

    @Override
    public String toString() {
        return "SpecialLeaveSettingsItemDto{" +
            "id=" + id +
            ", active=" + active +
            ", messageKey='" + messageKey + '\'' +
            ", days=" + days +
            '}';
    }
}
