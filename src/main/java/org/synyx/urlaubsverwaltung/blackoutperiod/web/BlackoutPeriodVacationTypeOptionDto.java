package org.synyx.urlaubsverwaltung.blackoutperiod.web;

import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor;

/**
 * A selectable vacation type, rendered as a checkbox in the blackout period form.
 */
record BlackoutPeriodVacationTypeOptionDto(Long id, String label, boolean selected, VacationTypeColor color) {
}
