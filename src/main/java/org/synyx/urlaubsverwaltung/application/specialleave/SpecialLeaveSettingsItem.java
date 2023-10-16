package org.synyx.urlaubsverwaltung.application.specialleave;

import java.util.Objects;

public class SpecialLeaveSettingsItem {

    private final Long id;
    private final Boolean active;
    private final String messageKey;
    private final Integer days;

    public SpecialLeaveSettingsItem(Long id, Boolean active, String messageKey, Integer days) {
        this.id = id;
        this.active = active;
        this.messageKey = messageKey;
        this.days = days;
    }

    public Long getId() {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpecialLeaveSettingsItem that = (SpecialLeaveSettingsItem) o;
        return Objects.equals(id, that.id)
            && Objects.equals(active, that.active)
            && Objects.equals(messageKey, that.messageKey)
            && Objects.equals(days, that.days);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, active, messageKey, days);
    }

    @Override
    public String toString() {
        return "SpecialLeaveSettingsItem{" +
            "id=" + id +
            ", active=" + active +
            ", messageKey='" + messageKey + '\'' +
            ", days=" + days +
            '}';
    }
}
