package org.synyx.urlaubsverwaltung.sicknote.statistics;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendar;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeCalendarService;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Map;

import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus.ACTIVE;

/**
 * Service for creating {@link SickNoteStatistics}.
 */
@Service
@Transactional
public class SickNoteStatisticsService {

    private final SickNoteService sickNoteService;
    private final SickNoteRelevantPersonsService sickNoteRelevantPersonsService;
    private final WorkingTimeCalendarService workingTimeCalendarService;
    private final Clock clock;

    SickNoteStatisticsService(
        SickNoteService sickNoteService,
        SickNoteRelevantPersonsService sickNoteRelevantPersonsService,
        WorkingTimeCalendarService workingTimeCalendarService,
        Clock clock
    ) {
        this.sickNoteService = sickNoteService;
        this.sickNoteRelevantPersonsService = sickNoteRelevantPersonsService;
        this.workingTimeCalendarService = workingTimeCalendarService;
        this.clock = clock;
    }

    /**
     * Creates a {@link SickNoteStatistics} for the given date range and person.
     *
     * <p>
     * The given person is relevant for the visibility of sick notes and active persons considered in the statistics.
     *
     * @param from   the first day of the year for which the statistics should be created
     * @param to     the last day of the year for which the statistics should be created
     * @param person the person for whom the statistics should be created
     * @return a {@link SickNoteStatistics} object containing sick notes and visible active persons
     */
    public SickNoteStatistics createStatisticsForPerson(LocalDate from, LocalDate to, Person person) {

        final LocalDate today = LocalDate.now(clock);
        final Year year = Year.from(from);

        final List<Person> persons = sickNoteRelevantPersonsService.getStatisticRelevantPersons(from, to, person);
        final List<SickNote> sickNotes = getSickNotes(persons, from, to);
        final Map<Person, WorkingTimeCalendar> workingTimeCalendarsByPerson = getWorkingTimeCalendarsByPerson(persons, year);

        return new SickNoteStatistics(year, today, sickNotes, persons, workingTimeCalendarsByPerson);
    }

    private List<SickNote> getSickNotes(List<Person> persons, LocalDate from, LocalDate to) {
        if (persons.isEmpty()) {
            return List.of();
        }
        return sickNoteService.getForStatesAndPerson(List.of(ACTIVE), persons, from, to);
    }

    private Map<Person, WorkingTimeCalendar> getWorkingTimeCalendarsByPerson(List<Person> persons, Year year) {
        if (persons.isEmpty()) {
            return Map.of();
        }
        return workingTimeCalendarService.getWorkingTimesByPersons(persons, year);
    }
}
