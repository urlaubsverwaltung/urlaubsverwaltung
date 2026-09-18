package org.synyx.urlaubsverwaltung.blackoutperiod.web;

import java.time.LocalDate;

/**
 * A blackout period as rendered in the blackout period overview list, with locale-resolved display labels
 * already applied.
 */
record BlackoutPeriodListDto(Long id, String title, LocalDate startDate, LocalDate endDate, String scopeLabel, String vacationTypesLabel) {
}
