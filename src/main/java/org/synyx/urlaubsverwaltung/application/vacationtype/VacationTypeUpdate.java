package org.synyx.urlaubsverwaltung.application.vacationtype;

public class VacationTypeUpdate {

    private final Integer id;
    private final boolean active;
    private final boolean requiresApproval;
    private final String color;

    public VacationTypeUpdate(Integer id, boolean active, boolean requiresApproval, String color) {
        this.id = id;
        this.active = active;
        this.requiresApproval = requiresApproval;
        this.color = color;
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

    public String getColor() {
        return color;
    }
}
