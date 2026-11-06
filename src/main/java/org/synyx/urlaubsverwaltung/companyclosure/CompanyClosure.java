package org.synyx.urlaubsverwaltung.companyclosure;

import org.synyx.urlaubsverwaltung.period.DayLength;

import java.time.LocalDate;

/**
 * @param date      date of the closure
 * @param dayLength closed portion of the day, never {@link DayLength#ZERO}
 * @param name      name of the closure, e.g. "Heiligabend"
 */
public record CompanyClosure(LocalDate date, DayLength dayLength, String name) {
}
