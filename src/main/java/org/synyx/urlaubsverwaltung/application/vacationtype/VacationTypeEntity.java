package org.synyx.urlaubsverwaltung.application.vacationtype;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

import static jakarta.persistence.EnumType.STRING;

/**
 * Describes a type of vacation.
 *
 * @since 2.15.0
 */
@Entity(name = "vacation_type")
public class VacationTypeEntity {

    @Id
    private Long id;

    @NotNull
    private boolean active;

    @NotNull
    @Enumerated(STRING)
    private VacationCategory category;

    @NotNull
    private String messageKey;

    @NotNull
    private boolean requiresApprovalToApply;

    @NotNull
    private boolean requiresApprovalToCancel;

    @NotNull
    @Enumerated(STRING)
    private VacationTypeColor color;

    @NotNull
    private boolean visibleToEveryone;

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean enabled) {
        this.active = enabled;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public boolean isRequiresApprovalToApply() {
        return requiresApprovalToApply;
    }

    public void setRequiresApprovalToApply(boolean requiresApprovalToApply) {
        this.requiresApprovalToApply = requiresApprovalToApply;
    }

    public boolean isRequiresApprovalToCancel() {
        return requiresApprovalToCancel;
    }

    public void setRequiresApprovalToCancel(boolean requiresApprovalToCancel) {
        this.requiresApprovalToCancel = requiresApprovalToCancel;
    }

    public void setColor(VacationTypeColor color) {
        this.color = color;
    }

    public VacationTypeColor getColor() {
        return this.color;
    }

    public boolean isVisibleToEveryone() {
        return visibleToEveryone;
    }

    public void setVisibleToEveryone(boolean visibleToEveryone) {
        this.visibleToEveryone = visibleToEveryone;
    }

    @Override
    public String toString() {
        return "VacationTypeEntity{" +
            "id=" + id +
            ", active=" + active +
            ", category=" + category +
            ", messageKey='" + messageKey + '\'' +
            ", requiresApprovalToApply=" + requiresApprovalToApply +
            ", requiresApprovalToCancel=" + requiresApprovalToCancel +
            ", color=" + color +
            ", visibleToEveryone=" + visibleToEveryone +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final VacationTypeEntity that = (VacationTypeEntity) o;
        return null != this.getId() && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
