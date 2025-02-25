package org.synyx.urlaubsverwaltung.application.application;

import org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor;

public record ApplicationForLeaveDetailVacationTypeDto(
    String label,
    VacationCategory category,
    VacationTypeColor color,
    boolean requiresApprovalToCancel
) {
}
