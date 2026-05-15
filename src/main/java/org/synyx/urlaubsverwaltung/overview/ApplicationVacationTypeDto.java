package org.synyx.urlaubsverwaltung.overview;

import org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor;

record ApplicationVacationTypeDto(
    String label,
    VacationCategory category,
    VacationTypeColor color
) {
}
