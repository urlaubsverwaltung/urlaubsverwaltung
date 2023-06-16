package org.synyx.urlaubsverwaltung.application.vacationtype;

public class VacationTypeUpdate {

    private final Integer id;
    private final boolean active;
    private final boolean requiresApprovalToApply;
    private final boolean requiresApprovalToCancel;
    private final VacationTypeColor color;
    private final boolean visibleToEveryone;

    public VacationTypeUpdate(Integer id, boolean active, boolean requiresApprovalToApply, boolean requiresApprovalToCancel, VacationTypeColor color, boolean visibleToEveryone) {
        this.id = id;
        this.active = active;
        this.requiresApprovalToApply = requiresApprovalToApply;
        this.requiresApprovalToCancel = requiresApprovalToCancel;
        this.color = color;
        this.visibleToEveryone = visibleToEveryone;
    }

    Integer getId() {
        return id;
    }

    boolean isActive() {
        return active;
    }

    boolean isRequiresApprovalToApply() {
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
}
