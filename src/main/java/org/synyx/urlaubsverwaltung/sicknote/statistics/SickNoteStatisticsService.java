package org.synyx.urlaubsverwaltung.sicknote.statistics;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteService;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;

import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static java.util.Collections.emptyList;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.Role.SICK_NOTE_VIEW;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus.ACTIVE;

/**
 * Service for creating {@link SickNoteStatistics}.
 */
@Service
@Transactional
public class SickNoteStatisticsService {

    private final SickNoteService sickNoteService;
    private final DepartmentService departmentService;
    private final PersonService personService;
    private final Clock clock;

    SickNoteStatisticsService(
        SickNoteService sickNoteService,
        DepartmentService departmentService,
        PersonService personService,
        Clock clock
    ) {
        this.sickNoteService = sickNoteService;
        this.departmentService = departmentService;
        this.personService = personService;
        this.clock = clock;
    }

    /**
     * Creates a {@link SickNoteStatistics} for the given year and person.
     *
     * <p>
     * The given person is relevant for the visibility of sick notes and active persons considered in the statistics.
     *
     * @param year   the year for which the statistics should be created
     * @param person the person for whom the statistics should be created
     * @return a {@link SickNoteStatistics} object containing sick notes and visible active persons
     */
    SickNoteStatistics createStatisticsForPerson(Year year, Person person) {

        final LocalDate today = LocalDate.now(clock);

        final LocalDate firstDayOfYear = year.atDay(1);
        final LocalDate lastDayOfYear = firstDayOfYear.with(lastDayOfYear());

        final List<Person> persons = getStatisticRelevantPersons(year, person);
        final List<SickNote> sickNotes = getSickNotes(persons, firstDayOfYear, lastDayOfYear);

        return new SickNoteStatistics(year, today, sickNotes, persons);
    }

    private List<Person> getStatisticRelevantPersons(Year year, Person person) {

        if (person.hasRole(OFFICE) || (person.hasRole(BOSS) && person.hasRole(SICK_NOTE_VIEW))) {
            // we don't know whether a person has been active/inactive over a certain year
            // Therefore, we return all persons having an account in the given year.
            return personService.getAllPersonsHavingAccountInYear(year);
        }

        if ((person.hasRole(DEPARTMENT_HEAD) || person.hasRole(SECOND_STAGE_AUTHORITY)) && person.hasRole(SICK_NOTE_VIEW)) {
            // Sadly, we neither know whether a person has been active or inactive,
            // nor do we know whether the person's holiday account has been active in the requested year.
            // (person's department membership is handled in departmentService as soon as we're able to determine it)
            final List<Person> managedMembers = departmentService.getManagedMembersOfPerson(person, year);
            if (year.equals(Year.now(clock))) {
                // we can, however, determine it for THIS year.
                return managedMembers.stream().filter(Person::isActive).toList();
            } else {
                return managedMembers;
            }
        }

        // TODO return requested person when hasRole(SICK_NOTE_VIEW) but nothing else

        return emptyList();
    }

    private List<SickNote> getSickNotes(List<Person> persons, LocalDate from, LocalDate to) {
           return sickNoteService.getForStatesAndPerson(List.of(ACTIVE), persons, from, to);
    }
}
