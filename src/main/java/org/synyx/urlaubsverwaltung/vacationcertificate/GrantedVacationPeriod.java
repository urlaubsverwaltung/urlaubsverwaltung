package org.synyx.urlaubsverwaltung.vacationcertificate;

import org.synyx.urlaubsverwaltung.period.DayLength;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A granted vacation period, clipped to the year of the certificate.
 *
 * @param from      first day of the period within the year
 * @param to        last day of the period within the year
 * @param dayLength day length of the application
 * @param days      working days of the period within the year
 */
record GrantedVacationPeriod(LocalDate from, LocalDate to, DayLength dayLength, BigDecimal days) {
}
