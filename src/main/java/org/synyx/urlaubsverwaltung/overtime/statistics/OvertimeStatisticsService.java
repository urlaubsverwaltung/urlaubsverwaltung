package org.synyx.urlaubsverwaltung.overtime.statistics;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.overtime.Overtime;
import org.synyx.urlaubsverwaltung.overtime.OvertimeService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import static java.time.Duration.ZERO;
import static java.time.ZoneOffset.UTC;
import static java.util.Collections.nCopies;
import static org.synyx.urlaubsverwaltung.overtime.statistics.OvertimeStatistics.MONTHS_PER_YEAR;

/**
 * Creates the company wide {@link OvertimeStatistics}.
 */
@Service
@Transactional(readOnly = true)
class OvertimeStatisticsService {

    private final OvertimeService overtimeService;
    private final PersonService personService;

    OvertimeStatisticsService(OvertimeService overtimeService, PersonService personService) {
        this.overtimeService = overtimeService;
        this.personService = personService;
    }

    /**
     * Creates the company wide overtime figures of the given year.
     *
     * <p>
     * Aggregated over everyone who had an account in that year. Using the cohort of the year instead of the currently
     * active persons keeps past years stable: someone who left stays part of the years they worked in, and drops out
     * of the years afterwards.
     *
     * @param year to create the statistics for
     * @return company wide overtime figures of the given year
     */
    OvertimeStatistics getStatistics(Year year) {

        final List<Person> persons = personService.getAllPersonsHavingAccountInYear(year);
        if (persons.isEmpty()) {
            return OvertimeStatistics.empty(year);
        }

        final List<PersonId> personIds = persons.stream().map(Person::getIdAsPersonId).toList();
        final List<Overtime> overtimes = overtimeService
            .getOvertimeForPersonsInDateRange(personIds, startOfYear(year), endOfYear(year))
            .values().stream()
            .flatMap(List::stream)
            .toList();

        final List<Duration> accrued = new ArrayList<>(nCopies(MONTHS_PER_YEAR, ZERO));
        final List<Duration> reduction = new ArrayList<>(nCopies(MONTHS_PER_YEAR, ZERO));

        for (Overtime overtime : overtimes) {
            for (int month = 1; month <= MONTHS_PER_YEAR; month++) {

                // an overtime record can be spread over several days, so a record crossing a month or a year boundary
                // only contributes its share of the month in question
                final Duration share = overtime.durationForDateRange(rangeOfMonth(year, month));
                if (share.isZero()) {
                    continue;
                }

                final int index = month - 1;
                if (share.isNegative()) {
                    reduction.set(index, reduction.get(index).plus(share.negated()));
                } else {
                    accrued.set(index, accrued.get(index).plus(share));
                }
            }
        }

        return new OvertimeStatistics(year, List.copyOf(accrued), List.copyOf(reduction));
    }

    private static DateRange rangeOfMonth(Year year, int month) {
        final YearMonth yearMonth = year.atMonth(month);
        return new DateRange(yearMonth.atDay(1), yearMonth.atEndOfMonth());
    }

    private static Instant startOfYear(Year year) {
        return toInstant(year.atDay(1));
    }

    private static Instant endOfYear(Year year) {
        return toInstant(year.atDay(year.length()));
    }

    private static Instant toInstant(LocalDate date) {
        return date.atStartOfDay().toInstant(UTC);
    }
}
