package org.synyx.urlaubsverwaltung.application.vacationtype;

import java.util.Objects;

public class VacationTypeUpdate {

    private final Long id;
    private final boolean active;
    private final boolean requiresApprovalToApply;
    private final boolean requiresApprovalToCancel;
    private final VacationTypeColor color;
    private final boolean visibleToEveryone;

    public VacationTypeUpdate(Long id, boolean active, boolean requiresApprovalToApply, boolean requiresApprovalToCancel, VacationTypeColor color, boolean visibleToEveryone) {
        this.id = id;
        this.active = active;
        this.requiresApprovalToApply = requiresApprovalToApply;
        this.requiresApprovalToCancel = requiresApprovalToCancel;
        this.color = color;
        this.visibleToEveryone = visibleToEveryone;
    }

    public Long getId() {
        return id;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isRequiresApprovalToApply() {
        return requiresApprovalToApply;
    }

    public boolean isRequiresApprovalToCancel() {
        return requiresApprovalToCancel;
    }

    public VacationTypeColor getColor() {
        return color;
    }

    public boolean isVisibleToEveryone() {
        return visibleToEveryone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VacationTypeUpdate that = (VacationTypeUpdate) o;
        return active == that.active
            && requiresApprovalToApply == that.requiresApprovalToApply
            && requiresApprovalToCancel == that.requiresApprovalToCancel
            && visibleToEveryone == that.visibleToEveryone
            && Objects.equals(id, that.id)
            && color == that.color;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, active, requiresApprovalToApply, requiresApprovalToCancel, color, visibleToEveryone);
    }

    @Override
    public String toString() {
        return "VacationTypeUpdate{" +
            "id=" + id +
            ", active=" + active +
            ", requiresApprovalToApply=" + requiresApprovalToApply +
            ", requiresApprovalToCancel=" + requiresApprovalToCancel +
            ", color=" + color +
            ", visibleToEveryone=" + visibleToEveryone +
            '}';
    }
}
