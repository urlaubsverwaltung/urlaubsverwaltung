package org.synyx.urlaubsverwaltung.application.application;

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
    public String toString() {
        return "SpecialLeaveItemDto{" +
            "active=" + active +
            ", messageKey='" + messageKey + '\'' +
            ", days=" + days +
            '}';
    }
}
