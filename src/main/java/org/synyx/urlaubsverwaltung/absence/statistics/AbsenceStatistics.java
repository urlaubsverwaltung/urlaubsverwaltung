package org.synyx.urlaubsverwaltung.absence.statistics;

import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;

import java.time.Year;
import java.util.Map;

/**
 * Result of {@link AbsenceStatisticsService#createStatistics}, for a single year. A value object without any
 * fetching of its own.
 *
 * @param year                        the year the statistics were calculated for
 * @param monthlyAbsenceDaysByType    monthly absence days and year sum per vacation type that has at least one day
 *                                    in {@code year}; unsorted, sorting for display is the view controller's job
 * @param vacationDaysTaken           taken/valid/percentage/expired vacation day numbers for the stichtag
 *                                    that applies to {@code year}
 */
record AbsenceStatistics(
    Year year,
    Map<VacationType<?>, MonthlyAbsenceDaysByType> monthlyAbsenceDaysByType,
    VacationDaysTakenResult vacationDaysTaken
) {
}
