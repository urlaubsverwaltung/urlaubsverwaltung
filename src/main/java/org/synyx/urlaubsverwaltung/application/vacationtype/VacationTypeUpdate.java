package org.synyx.urlaubsverwaltung.application.vacationtype;

public class VacationTypeUpdate {

    private final Integer id;
    private final boolean active;
    private final boolean requiresApproval;

    public VacationTypeUpdate(Integer id, boolean active, boolean requiresApproval) {
        this.id = id;
        this.active = active;
        this.requiresApproval = requiresApproval;
    }

    Integer getId() {
        return id;
    }

    boolean isActive() {
        return active;
    }

    boolean isRequiresApproval() {
        return requiresApproval;
    }
}
