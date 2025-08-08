package org.synyx.urlaubsverwaltung.sicknote.statistics;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.account.Account;
import org.synyx.urlaubsverwaltung.account.AccountService;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteService;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.stream.Stream;

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
    private final AccountService accountService;
    private final Clock clock;

    SickNoteStatisticsService(
        SickNoteService sickNoteService,
        DepartmentService departmentService,
        PersonService personService,
        AccountService accountService,
        Clock clock
    ) {
        this.sickNoteService = sickNoteService;
        this.departmentService = departmentService;
        this.personService = personService;
        this.accountService = accountService;
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

        final List<Person> visibleActivePersonsForPerson = getVisibleActivePersonsForPerson(person);
        final List<Person> personsToConsiderInStatistics = accountService.getHolidaysAccount(year.getValue(), visibleActivePersonsForPerson).stream()
            .map(Account::getPerson)
            .toList();

        final List<SickNote> sickNotes = getSickNotes(personsToConsiderInStatistics, firstDayOfYear, lastDayOfYear);

        return new SickNoteStatistics(year, today, sickNotes, personsToConsiderInStatistics);
    }

    private List<Person> getVisibleActivePersonsForPerson(Person person) {

        if (person.hasRole(OFFICE) || (person.hasRole(BOSS) && person.hasRole(SICK_NOTE_VIEW))) {
            return personService.getAllPersons();
        }

        if ((person.hasRole(DEPARTMENT_HEAD) || person.hasRole(SECOND_STAGE_AUTHORITY)) && person.hasRole(SICK_NOTE_VIEW)) {
            return getMembersForPerson(person);
        }

        return emptyList();
    }

    private List<SickNote> getSickNotes(List<Person> persons, LocalDate from, LocalDate to) {
           return sickNoteService.getForStatesAndPerson(List.of(ACTIVE), persons, from, to);
    }

    private List<Person> getMembersForPerson(Person person) {
        final List<Person> membersDH = person.hasRole(DEPARTMENT_HEAD) ? departmentService.getMembersForDepartmentHead(person) : List.of();
        final List<Person> membersSSA = person.hasRole(SECOND_STAGE_AUTHORITY) ? departmentService.getMembersForSecondStageAuthority(person) : List.of();
        return Stream.concat(membersDH.stream(), membersSSA.stream())
            .distinct()
            .toList();
    }
}
