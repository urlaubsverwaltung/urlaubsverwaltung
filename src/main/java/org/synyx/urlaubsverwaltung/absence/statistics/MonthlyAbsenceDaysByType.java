package org.synyx.urlaubsverwaltung.absence.statistics;

import java.math.BigDecimal;
import java.util.List;

/**
 * Monthly absence days and year sum for a single vacation type, as computed by {@link MonthlyAbsenceDays}.
 *
 * @param daysByMonth working days per month, index 0 is January and index 11 is December
 * @param yearSum     sum of {@code daysByMonth}, computed once here so callers don't have to re-derive it
 */
record MonthlyAbsenceDaysByType(List<BigDecimal> daysByMonth, BigDecimal yearSum) {
}
