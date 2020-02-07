package org.synyx.urlaubsverwaltung.dev;

import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.util.CalcUtil;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.time.ZonedDateTime;


/**
 * Helper class to check durations.
 */
class DurationChecker {

    private final WorkDaysService workDaysService;
    private final Clock clock;

    DurationChecker(WorkDaysService workDaysService, Clock clock) {

        this.workDaysService = workDaysService;
        this.clock = clock;
    }

    /**
     * Check if the given dates are in the current year.
     *
     * @param start to be checked if in the current year
     * @param end   to be checked if in the current year
     * @return {@code true} if both dates are in the current year, else {@code false}
     */
    boolean startAndEndDatesAreInCurrentYear(LocalDate start, LocalDate end) {

        int currentYear = Year.now(clock).getValue();

        return start.getYear() == currentYear && end.getYear() == currentYear;
    }


    /**
     * Check if the period between the given start and end date is greater than zero days, the custom working time of
     * the given person is concerned.
     *
     * @param start  of the period
     * @param end    of the period
     * @param person to use the working time for calculation
     * @return {@code true} if the period duration is greater than zero, else {@code false}
     */
    boolean durationIsGreaterThanZero(LocalDate start, LocalDate end, Person person) {

        BigDecimal workDays = workDaysService.getWorkDays(DayLength.FULL, start, end, person);

        return CalcUtil.isPositive(workDays);
    }
}
