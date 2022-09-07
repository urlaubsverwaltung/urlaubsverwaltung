package org.synyx.urlaubsverwaltung.sicknote.statistics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedata;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedataService;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus.ACTIVE;

/**
 * Service for creating {@link SickNoteStatistics}.
 */
@Service
@Transactional
public class SickNoteStatisticsService {

    private final SickNoteService sickNoteService;
    private final WorkDaysCountService workDaysCountService;
    private final DepartmentService departmentService;
    private final PersonBasedataService personBasedataService;

    @Autowired
    SickNoteStatisticsService(SickNoteService sickNoteService, WorkDaysCountService workDaysCountService, DepartmentService departmentService, PersonBasedataService personBasedataService) {
        this.sickNoteService = sickNoteService;
        this.workDaysCountService = workDaysCountService;
        this.departmentService = departmentService;
        this.personBasedataService = personBasedataService;
    }

    SickNoteStatistics createStatisticsForPerson(Person person, Clock clock) {

        final LocalDate firstDayOfYear = Year.now(clock).atDay(1);
        final LocalDate lastDayOfYear = firstDayOfYear.with(lastDayOfYear());
        final List<SickNote> sickNotes = getSickNotes(person, firstDayOfYear, lastDayOfYear);
        return new SickNoteStatistics(clock, sickNotes, workDaysCountService);
    }

    List<SickNoteDetailedStatistics> getAllSickNotes(Person person, LocalDate from, LocalDate to) {

        final List<SickNote> sickNotes = getSickNotes(person, from, to);
        final Map<Person, List<SickNote>> sickNotesByPerson = sickNotes.stream()
            .collect(groupingBy(SickNote::getPerson));

        final List<Person> personsWithSickNotes = new ArrayList<>(sickNotesByPerson.keySet());

        final List<Integer> personIds = personsWithSickNotes.stream().map(Person::getId).collect(toList());
        Map<PersonId, PersonBasedata> basedataForPersons = personBasedataService.getBasedataByPersonId(personIds);
        Map<PersonId, List<String>> departmentsForPersons = departmentService.getDepartmentsByMembers(personsWithSickNotes);

        return sickNotesByPerson.entrySet().stream()
            .map(toSickNoteDetailedStatistics(basedataForPersons, departmentsForPersons))
            .collect(toList());
    }

    private Function<Map.Entry<Person, List<SickNote>>, SickNoteDetailedStatistics> toSickNoteDetailedStatistics(Map<PersonId, PersonBasedata> basedataForPersons, Map<PersonId, List<String>> departmentsForPersons) {
        return personListEntry ->
        {
            final Person person = personListEntry.getKey();
            final String personnelNumber = Optional.of(basedataForPersons.get(person.getId()).getPersonnelNumber()).orElse("");
            final List<String> departments = Optional.of(departmentsForPersons.get(new PersonId(person.getId()))).orElse(List.of());
            return new SickNoteDetailedStatistics(personnelNumber, person.getFirstName(), person.getLastName(), personListEntry.getValue(), departments);
        };
    }

    private List<SickNote> getSickNotes(Person person, LocalDate from, LocalDate to) {
        final List<SickNote> sickNotes;
        if (person.hasRole(DEPARTMENT_HEAD) || person.hasRole(SECOND_STAGE_AUTHORITY)) {
            final List<Person> members = person.hasRole(DEPARTMENT_HEAD) ? departmentService.getMembersForDepartmentHead(person) : departmentService.getMembersForSecondStageAuthority(person);
            sickNotes = sickNoteService.getForStatesAndPerson(List.of(ACTIVE), members, from, to);
        } else if (person.hasRole(OFFICE) || person.hasRole(BOSS)) {
            sickNotes = sickNoteService.getAllActiveByPeriod(from, to);
        } else {
            sickNotes = List.of();
        }
        return sickNotes;
    }
}
