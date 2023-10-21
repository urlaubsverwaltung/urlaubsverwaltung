package org.synyx.urlaubsverwaltung.application.application;

import org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor;

public class ApplicationForLeaveDetailVacationTypeDto {

    private final String messageKey;
    private final VacationCategory category;
    private final VacationTypeColor color;
    private final boolean requiresApprovalToCancel;

    public ApplicationForLeaveDetailVacationTypeDto(String messageKey, VacationCategory category, VacationTypeColor color,
                                                    boolean requiresApprovalToCancel) {
        this.messageKey = messageKey;
        this.category = category;
        this.color = color;
        this.requiresApprovalToCancel = requiresApprovalToCancel;
    }

    public String getMessageKey() {
        return messageKey;
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
            "messageKey='" + messageKey + '\'' +
            ", category=" + category +
            ", color=" + color +
            ", requiresApprovalToCancel=" + requiresApprovalToCancel +
            '}';
    }
}
