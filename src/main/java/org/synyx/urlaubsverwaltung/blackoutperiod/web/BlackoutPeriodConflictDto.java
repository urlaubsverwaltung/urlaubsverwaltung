package org.synyx.urlaubsverwaltung.blackoutperiod.web;

import java.time.LocalDate;

/**
 * A single already existing application for leave that conflicts with a blackout period that is about to be
 * created or edited.
 */
record BlackoutPeriodConflictDto(String personName, LocalDate startDate, LocalDate endDate, String vacationTypeLabel) {
}
