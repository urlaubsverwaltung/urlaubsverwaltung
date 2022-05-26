package org.synyx.urlaubsverwaltung.application.vacationtype;

import java.util.Objects;

/**
 * Describes a type of vacation.
 */
public class VacationType {

    private Integer id;
    private boolean active;
    private VacationCategory category;
    private String messageKey;
    private boolean requiresApproval;
    private VacationTypeColor color;

    public VacationType() {
        // ok
    }

    public VacationType(Integer id, boolean active, VacationCategory category, String messageKey, boolean requiresApproval, VacationTypeColor color) {
        this.id = id;
        this.active = active;
        this.category = category;
        this.messageKey = messageKey;
        this.requiresApproval = requiresApproval;
        this.color = color;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean enabled) {
        this.active = enabled;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public VacationCategory getCategory() {
        return category;
    }

    public void setCategory(VacationCategory category) {
        this.category = category;
    }

    public boolean isOfCategory(VacationCategory category) {
        return getCategory().equals(category);
    }

    public String getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    public boolean isRequiresApproval() {
        return requiresApproval;
    }

    public void setRequiresApproval(boolean requiresApproval) {
        this.requiresApproval = requiresApproval;
    }

    public VacationTypeColor getColor() {
        return this.color;
    }

    public void setColor(VacationTypeColor color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return "VacationType{" +
            "id=" + id +
            ", active=" + active +
            ", category=" + category +
            ", messageKey='" + messageKey + '\'' +
            ", requiresApproval='" + requiresApproval + '\'' +
            ", color='" + color + '\'' +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VacationType that = (VacationType) o;
        return active == that.active && requiresApproval == that.requiresApproval && category == that.category && Objects.equals(messageKey, that.messageKey)
            && Objects.equals(color, that.color);
    }

    @Override
    public int hashCode() {
        return Objects.hash(active, category, messageKey, requiresApproval, color);
    }
}
