package org.synyx.urlaubsverwaltung.overtime.statistics;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.overtime.Overtime;
import org.synyx.urlaubsverwaltung.overtime.OvertimeService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendarService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendarSupplier;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.time.Duration.ZERO;
import static java.time.ZoneOffset.UTC;
import static java.util.Collections.nCopies;
import static java.util.stream.Collectors.toMap;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.activeStatuses;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.OVERTIME;
import static org.synyx.urlaubsverwaltung.overtime.statistics.OvertimeStatistics.MONTHS_PER_YEAR;

/**
 * Creates the company wide {@link OvertimeStatistics}.
 */
@Service
@Transactional(readOnly = true)
class OvertimeStatisticsService {

    private final OvertimeService overtimeService;
    private final PersonService personService;
    private final ApplicationService applicationService;
    private final WorkingTimeCalendarService workingTimeCalendarService;

    OvertimeStatisticsService(
        OvertimeService overtimeService,
        PersonService personService,
        ApplicationService applicationService,
        WorkingTimeCalendarService workingTimeCalendarService
    ) {
        this.overtimeService = overtimeService;
        this.personService = personService;
        this.applicationService = applicationService;
        this.workingTimeCalendarService = workingTimeCalendarService;
    }

    /**
     * Creates the company wide overtime figures of the given year.
     *
     * <p>
     * Aggregated over everyone who had an account in that year. Using the cohort of the year instead of the currently
     * active persons keeps past years stable: someone who left stays part of the years they worked in, and drops out
     * of the years afterwards.
     *
     * <p>
     * Reduction covers both ways it can happen in this application: a negative overtime record and an application of
     * category overtime. Leaving the applications out would make the company balance disagree with what every person
     * sees as their own remaining overtime.
     *
     * @param year to create the statistics for
     * @return company wide overtime figures of the given year
     */
    OvertimeStatistics getStatistics(Year year) {

        final List<Person> persons = personService.getAllPersonsHavingAccountInYear(year);
        if (persons.isEmpty()) {
            return OvertimeStatistics.empty(year);
        }

        final List<Duration> accrued = new ArrayList<>(nCopies(MONTHS_PER_YEAR, ZERO));
        final List<Duration> reduction = new ArrayList<>(nCopies(MONTHS_PER_YEAR, ZERO));

        addOvertimeRecords(year, persons, accrued, reduction);
        addOvertimeReductionApplications(year, persons, reduction);

        return new OvertimeStatistics(year, List.copyOf(accrued), List.copyOf(reduction));
    }

    /**
     * Creates the company wide overtime figures over the whole history, without any reference to a year.
     *
     * <p>
     * Aggregated over the persons currently employed. Someone who left is not part of the overtime the company still
     * has open, which is exactly the question these figures answer.
     *
     * <p>
     * Without a date boundary nothing has to be spread pro rata, so this needs neither the monthly split nor the
     * working time calendars - the reduction of an application counts as a whole, no matter which year it falls into.
     *
     * @return company wide overtime figures over the whole history
     */
    OvertimeTotals getTotals() {

        final List<Person> persons = personService.getActivePersons();
        if (persons.isEmpty()) {
            return OvertimeTotals.empty();
        }

        final List<PersonId> personIds = persons.stream().map(Person::getIdAsPersonId).toList();

        Duration accrued = ZERO;
        Duration reductionByRecords = ZERO;

        for (List<Overtime> overtimes : overtimeService.getAllOvertimesByPersonIds(personIds).values()) {
            for (Overtime overtime : overtimes) {
                if (overtime.duration().isNegative()) {
                    reductionByRecords = reductionByRecords.plus(overtime.duration().negated());
                } else {
                    accrued = accrued.plus(overtime.duration());
                }
            }
        }

        final Duration reductionByApplications = applicationService.getTotalOvertimeReductionOfPersons(persons);

        return new OvertimeTotals(accrued, reductionByRecords.plus(reductionByApplications));
    }

    private void addOvertimeRecords(Year year, List<Person> persons, List<Duration> accrued, List<Duration> reduction) {

        final List<PersonId> personIds = persons.stream().map(Person::getIdAsPersonId).toList();
        final List<Overtime> overtimes = overtimeService
            .getOvertimeForPersonsInDateRange(personIds, startOfYear(year), endOfYear(year))
            .values().stream()
            .flatMap(List::stream)
            .toList();

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
    }

    private void addOvertimeReductionApplications(Year year, List<Person> persons, List<Duration> reduction) {

        final List<Application> applications = applicationService
            .getForStatesAndPerson(activeStatuses(), persons, year.atDay(1), year.atDay(year.length()))
            .stream()
            .filter(OvertimeStatisticsService::isOvertimeReduction)
            .toList();

        if (applications.isEmpty()) {
            return;
        }

        final WorkingTimeCalendarSupplier workingTimeCalendars = workingTimeCalendars(applications);

        for (Application application : applications) {
            for (int month = 1; month <= MONTHS_PER_YEAR; month++) {

                // asking the application itself how much of it falls into a month keeps that rule where it belongs.
                // It recalculates its daily shares per call, which is fine: a reduction application rarely spans more
                // than two months, so the repeated work is negligible compared to spreading the rule over two classes.
                final Duration share = application.getOvertimeReductionShareFor(rangeOfMonth(year, month), workingTimeCalendars);
                if (share.isZero()) {
                    continue;
                }

                final int index = month - 1;
                reduction.set(index, reduction.get(index).plus(share));
            }
        }
    }

    /**
     * Loads the working time calendars of every person involved in one query. The range has to cover the applications
     * completely and not only the selected year, because the reduction of an application is spread over all of its
     * working days - a range cut off at the turn of the year would distort the share of every day.
     */
    private WorkingTimeCalendarSupplier workingTimeCalendars(List<Application> applications) {

        final Set<Person> applicants = new HashSet<>();
        LocalDate from = LocalDate.MAX;
        LocalDate to = LocalDate.MIN;

        for (Application application : applications) {
            from = application.getStartDate().isBefore(from) ? application.getStartDate() : from;
            to = application.getEndDate().isAfter(to) ? application.getEndDate() : to;
            applicants.add(application.getPerson());
        }

        final Map<PersonId, WorkingTimeCalendar> calendarByPersonId =
            workingTimeCalendarService.getWorkingTimesByPersons(applicants, new DateRange(from, to))
                .entrySet().stream()
                .collect(toMap(entry -> entry.getKey().getIdAsPersonId(), Map.Entry::getValue));

        return (personId, _) -> calendarByPersonId.get(personId);
    }

    private static boolean isOvertimeReduction(Application application) {
        return application.getVacationType() != null && application.getVacationType().isOfCategory(OVERTIME);
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
