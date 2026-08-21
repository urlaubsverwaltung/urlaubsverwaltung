package org.synyx.urlaubsverwaltung.absence.statistics;

import java.math.BigDecimal;

/**
 * Result of {@link VacationDaysTaken#calculate}, for a single stichtag.
 *
 * @param vacationDaysTaken             sum of taken/planned vacation days across all considered persons
 * @param validEntitlement              sum of the still-valid entitlement (expired remainder already excluded)
 * @param percentage                    {@code vacationDaysTaken} relative to {@code validEntitlement}, as a value
 *                                       between 0 and 100, {@link BigDecimal#ZERO} when the entitlement sum is zero
 * @param expiredRemainingVacationDays  sum of remaining vacation days that have expired unused
 */
record VacationDaysTakenResult(
    BigDecimal vacationDaysTaken,
    BigDecimal validEntitlement,
    BigDecimal percentage,
    BigDecimal expiredRemainingVacationDays
) {
}
