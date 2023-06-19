package org.synyx.urlaubsverwaltung.application.vacationtype;

import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.util.Objects;

import static javax.persistence.EnumType.STRING;

/**
 * Describes a type of vacation.
 *
 * @since 2.15.0
 */
@Entity(name = "vacation_type")
public class VacationTypeEntity {

    @Id
    private Integer id;

    private boolean active;

    @Enumerated(STRING)
    private VacationCategory category;

    private String messageKey;

    private boolean requiresApprovalToApply;

    private boolean requiresApprovalToCancel;

    @Enumerated(STRING)
    @NotNull
    private VacationTypeColor color;

    private boolean visibleToEveryone;

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
