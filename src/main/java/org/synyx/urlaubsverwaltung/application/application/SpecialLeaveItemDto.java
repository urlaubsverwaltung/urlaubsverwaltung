package org.synyx.urlaubsverwaltung.application.application;

import java.util.Objects;

public class SpecialLeaveItemDto {

    private boolean active;
    private String messageKey;
    private int days;

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
        SpecialLeaveItemDto that = (SpecialLeaveItemDto) o;
        return active == that.active && days == that.days && Objects.equals(messageKey, that.messageKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(active, messageKey, days);
    }

    @Override
    public String toString() {
        return "SpecialLeaveItemDto{" +
            "active=" + active +
            ", messageKey='" + messageKey + '\'' +
            ", days=" + days +
            '}';
    }
}
