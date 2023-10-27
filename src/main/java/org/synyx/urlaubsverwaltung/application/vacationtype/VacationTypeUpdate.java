package org.synyx.urlaubsverwaltung.application.vacationtype;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class VacationTypeUpdate {

    private final Long id;
    private final boolean active;
    private final boolean requiresApprovalToApply;
    private final boolean requiresApprovalToCancel;
    private final VacationTypeColor color;
    private final boolean visibleToEveryone;
    private final List<VacationTypeLabel> labels;

    public VacationTypeUpdate(Long id, boolean active, boolean requiresApprovalToApply, boolean requiresApprovalToCancel,
                              VacationTypeColor color, boolean visibleToEveryone, List<VacationTypeLabel> labels) {
        this.id = id;
        this.active = active;
        this.requiresApprovalToApply = requiresApprovalToApply;
        this.requiresApprovalToCancel = requiresApprovalToCancel;
        this.color = color;
        this.visibleToEveryone = visibleToEveryone;
        this.labels = labels;
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

    public Optional<List<VacationTypeLabel>> getLabels() {
        return Optional.ofNullable(labels);
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
            && color == that.color
            && Objects.equals(labels, that.labels);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, active, requiresApprovalToApply, requiresApprovalToCancel, color, visibleToEveryone, labels);
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
            ", labels=" + labels +
            '}';
    }
}
