package org.synyx.urlaubsverwaltung.absence.statistics;

import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar;

import java.math.BigDecimal;
import java.time.Year;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Splits absence applications into working days per {@link VacationType} and calendar month, for a given
 * {@link Year}.
 *
 * <p>
 * A pure calculation without Spring or database concerns of its own — applications and working time calendars are
 * handed in already resolved. Application status filtering happens before this class is called; every application
 * passed in is counted.
 */
final class MonthlyAbsenceDays {

    private static final int MONTHS_PER_YEAR = 12;

    private MonthlyAbsenceDays() {
        // static calculator, not meant to be instantiated
    }

    /**
     * Splits the given applications into working days per {@link VacationType} and month for the given year.
     *
     * <p>
     * Applications spanning a month or year boundary are split day-precise via
     * {@link WorkingTimeCalendar#workingTimeInDateRage(Application, DateRange)}, called once per month the
     * application overlaps; days outside the requested year are dropped. Vacation types without a single day in
     * the given year are absent from the result.
     *
     * @param year                         year to calculate the monthly absence days for
     * @param applications                 applications to consider, already filtered to the relevant statuses
     * @param workingTimeCalendarsByPerson working time calendar per person; applications of a person missing here
     *                                     are skipped
     * @return monthly absence days and year sum per vacation type that has at least one day in the given year
     */
    static Map<VacationType<?>, MonthlyAbsenceDaysByType> calculate(Year year, List<Application> applications,
                                                              Map<Person, WorkingTimeCalendar> workingTimeCalendarsByPerson) {

        final DateRange yearRange = DateRange.ofYear(year);
        final Map<VacationType<?>, BigDecimal[]> daysByTypeAndMonth = new LinkedHashMap<>();

        for (Application application : applications) {

            final WorkingTimeCalendar workingTimeCalendar = workingTimeCalendarsByPerson.get(application.getPerson());
            if (workingTimeCalendar == null) {
                continue;
            }

            final Optional<DateRange> overlapWithYear = yearRange.overlap(application.getDateRange());
            if (overlapWithYear.isEmpty()) {
                continue;
            }

            addMonthlyDays(daysByTypeAndMonth, application, workingTimeCalendar, year, overlapWithYear.get());
        }

        final Map<VacationType<?>, MonthlyAbsenceDaysByType> result = new LinkedHashMap<>();
        daysByTypeAndMonth.forEach((vacationType, monthlyDays) -> result.put(vacationType, toMonthlyAbsenceDaysByType(monthlyDays)));

        return result;
    }

    private static void addMonthlyDays(Map<VacationType<?>, BigDecimal[]> daysByTypeAndMonth, Application application,
                                        WorkingTimeCalendar workingTimeCalendar, Year year, DateRange overlapWithYear) {

        final int firstMonth = overlapWithYear.startDate().getMonthValue();
        final int lastMonth = overlapWithYear.endDate().getMonthValue();

        for (int month = firstMonth; month <= lastMonth; month++) {

            final YearMonth yearMonth = YearMonth.of(year.getValue(), month);
            final DateRange monthRange = new DateRange(yearMonth.atDay(1), yearMonth.atEndOfMonth());
            final BigDecimal days = workingTimeCalendar.workingTimeInDateRage(application, monthRange);
            if (days.signum() == 0) {
                continue;
            }

            final BigDecimal[] monthlyDays = daysByTypeAndMonth.computeIfAbsent(application.getVacationType(), type -> newEmptyMonths());
            monthlyDays[month - 1] = monthlyDays[month - 1].add(days);
        }
    }

    private static MonthlyAbsenceDaysByType toMonthlyAbsenceDaysByType(BigDecimal[] monthlyDays) {

        final List<BigDecimal> daysByMonth = List.of(monthlyDays);
        final BigDecimal yearSum = daysByMonth.stream().reduce(BigDecimal.ZERO, BigDecimal::add);

        return new MonthlyAbsenceDaysByType(daysByMonth, yearSum);
    }

    private static BigDecimal[] newEmptyMonths() {
        final BigDecimal[] months = new BigDecimal[MONTHS_PER_YEAR];
        Arrays.fill(months, BigDecimal.ZERO);
        return months;
    }
}
