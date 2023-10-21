package org.synyx.urlaubsverwaltung.application.application;

import org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor;

public class ApplicationForLeaveDetailVacationTypeDto {

    private final String label;
    private final VacationCategory category;
    private final VacationTypeColor color;
    private final boolean requiresApprovalToCancel;

    public ApplicationForLeaveDetailVacationTypeDto(String label, VacationCategory category, VacationTypeColor color,
                                                    boolean requiresApprovalToCancel) {
        this.label = label;
        this.category = category;
        this.color = color;
        this.requiresApprovalToCancel = requiresApprovalToCancel;
    }

    public String getLabel() {
        return label;
    }

    public VacationCategory getCategory() {
        return category;
    }

    public VacationTypeColor getColor() {
        return color;
    }

    public boolean isRequiresApprovalToCancel() {
        return requiresApprovalToCancel;
    }

    @Override
    public String toString() {
        return "ApplicationForLeaveDetailVacationTypeDto{" +
            "label='" + label + '\'' +
            ", category=" + category +
            ", color=" + color +
            ", requiresApprovalToCancel=" + requiresApprovalToCancel +
            '}';
    }
}
