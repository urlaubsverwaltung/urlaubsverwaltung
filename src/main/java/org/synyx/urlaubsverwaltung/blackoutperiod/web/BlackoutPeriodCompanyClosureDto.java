package org.synyx.urlaubsverwaltung.blackoutperiod.web;

import java.time.LocalDate;

/**
 * A company closure within the range of a blackout period that is about to be created or edited.
 */
record BlackoutPeriodCompanyClosureDto(LocalDate date, String name, String dayLengthLabel) {
}
