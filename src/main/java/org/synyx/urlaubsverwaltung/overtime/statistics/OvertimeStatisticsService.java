package org.synyx.urlaubsverwaltung.overtime.statistics;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.overtime.Overtime;
import org.synyx.urlaubsverwaltung.overtime.OvertimeService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;

/**
 * Service for creating company-wide {@link OvertimeStatistics}.
 */
@Service
@Transactional
public class OvertimeStatisticsService {

    private final OvertimeService overtimeService;
    private final PersonService personService;
    private final Clock clock;

    OvertimeStatisticsService(OvertimeService overtimeService, PersonService personService, Clock clock) {
        this.overtimeService = overtimeService;
        this.personService = personService;
        this.clock = clock;
    }

    /**
     * Creates a company-wide {@link OvertimeStatistics} for the given year.
     *
     * @param year the year for which the statistics should be created
     * @return a {@link OvertimeStatistics} object containing the overtime of all persons of the company
     */
    OvertimeStatistics createStatistics(Year year) {

        final List<Person> persons = personService.getAllPersonsHavingAccountInYear(year);
        final List<Overtime> overtimes = overtimeService.getOvertimeRecordsForPersonsAndDateRange(persons, DateRange.ofYear(year));

        final LocalDate referenceDate = year.equals(Year.now(clock)) ? LocalDate.now(clock) : year.atDay(year.length());

        return new OvertimeStatistics(year, referenceDate, overtimes, persons.size());
    }
}
