package org.synyx.urlaubsverwaltung.settings;

import org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor;

import java.util.Objects;

public class AbsenceTypeSettingsItemDto {

    private Long id;
    private boolean active;
    private String messageKey;
    private VacationCategory category;
    private boolean requiresApproval;
    private VacationTypeColor color;
    private boolean visibleToEveryone;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public VacationCategory getCategory() {
        return category;
    }

    public void setCategory(VacationCategory category) {
        this.category = category;
    }

    public boolean isRequiresApproval() {
        return requiresApproval;
    }

    public void setRequiresApproval(boolean requiresApproval) {
        this.requiresApproval = requiresApproval;
    }

    public VacationTypeColor getColor() {
        return color;
    }

    public void setColor(VacationTypeColor color) {
        this.color = color;
    }


    public boolean isVisibleToEveryone() {
        return visibleToEveryone;
    }

    public void setVisibleToEveryone(boolean visibleToEveryone) {
        this.visibleToEveryone = visibleToEveryone;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return "AbsenceTypeItemSettingDto{" +
            "id=" + id +
            ", active=" + active +
            ", messageKey='" + messageKey + '\'' +
            ", category='" + category + '\'' +
            ", requiresApproval=" + requiresApproval +
            ", color=" + color +
            ", visibleToEveryone=" + visibleToEveryone +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbsenceTypeSettingsItemDto that = (AbsenceTypeSettingsItemDto) o;
        return active == that.active
            && requiresApproval == that.requiresApproval
            && visibleToEveryone == that.visibleToEveryone
            && Objects.equals(id, that.id)
            && Objects.equals(messageKey, that.messageKey)
            && category == that.category
            && Objects.equals(color, that.color);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, active, messageKey, category, requiresApproval, color, visibleToEveryone);
    }

    static class Builder {
        private Long id;
        private boolean active;
        private String messageKey;
        private VacationCategory category;
        private boolean requiresApproval;
        private VacationTypeColor color;
        private boolean visibleToEveryone;

        Builder setId(Long id) {
            this.id = id;
            return this;
        }

        Builder setActive(boolean active) {
            this.active = active;
            return this;
        }

        Builder setMessageKey(String messageKey) {
            this.messageKey = messageKey;
            return this;
        }

        Builder setCategory(VacationCategory category) {
            this.category = category;
            return this;
        }

        Builder setRequiresApproval(boolean requiresApproval) {
            this.requiresApproval = requiresApproval;
            return this;
        }

        Builder setColor(VacationTypeColor color) {
            this.color = color;
            return this;
        }

        Builder setVisibleToEveryone(boolean visibleToEveryone) {
            this.visibleToEveryone = visibleToEveryone;
            return this;
        }

        AbsenceTypeSettingsItemDto build() {
            final AbsenceTypeSettingsItemDto dto = new AbsenceTypeSettingsItemDto();
            dto.setId(id);
            dto.setActive(active);
            dto.setMessageKey(messageKey);
            dto.setCategory(category);
            dto.setRequiresApproval(requiresApproval);
            dto.setColor(color);
            dto.setVisibleToEveryone(visibleToEveryone);
            return dto;
        }
    }
}
