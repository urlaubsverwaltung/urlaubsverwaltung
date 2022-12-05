package org.synyx.urlaubsverwaltung.sicknote.sickdays;

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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.Role.SICK_NOTE_VIEW;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteStatus.ACTIVE;

@Service
@Transactional
public class SickDaysStatisticsService {

    private final SickNoteService sickNoteService;
    private final DepartmentService departmentService;
    private final PersonBasedataService personBasedataService;

    @Autowired
    SickDaysStatisticsService(SickNoteService sickNoteService, DepartmentService departmentService, PersonBasedataService personBasedataService) {
        this.sickNoteService = sickNoteService;
        this.departmentService = departmentService;
        this.personBasedataService = personBasedataService;
    }

    /**
     * Returns a list of all sick notes detailed statistics that the person is allowed to access.
     *
     * @param person to ask for the statistics
     * @param from   a specific date
     * @param to     a specific date
     * @return list of all {@link SickDaysDetailedStatistics} that the person can access
     */
    List<SickDaysDetailedStatistics> getAll(Person person, LocalDate from, LocalDate to) {

        final List<SickNote> sickNotes = getSickNotes(person, from, to);
        final Map<Person, List<SickNote>> sickNotesByPerson = sickNotes.stream()
            .collect(groupingBy(SickNote::getPerson));

        final List<Person> personsWithSickNotes = new ArrayList<>(sickNotesByPerson.keySet());

        final List<Integer> personIds = personsWithSickNotes.stream().map(Person::getId).collect(toList());
        final Map<PersonId, PersonBasedata> basedataForPersons = personBasedataService.getBasedataByPersonId(personIds);
        final Map<PersonId, List<String>> departmentsForPersons = departmentService.getDepartmentNamesByMembers(personsWithSickNotes);

        return sickNotesByPerson.entrySet().stream()
            .map(toSickNoteDetailedStatistics(basedataForPersons, departmentsForPersons))
            .collect(toList());
    }

    private Function<Map.Entry<Person, List<SickNote>>, SickDaysDetailedStatistics> toSickNoteDetailedStatistics(Map<PersonId, PersonBasedata> basedataForPersons, Map<PersonId, List<String>> departmentsForPersons) {
        return personListEntry ->
        {
            final Person person = personListEntry.getKey();
            final PersonId personId = new PersonId(person.getId());
            final String personnelNumber = basedataForPersons.getOrDefault(personId, new PersonBasedata(personId, "", "")).getPersonnelNumber();
            final List<String> departments = departmentsForPersons.getOrDefault(personId, List.of());
            return new SickDaysDetailedStatistics(personnelNumber, person.getFirstName(), person.getLastName(), personListEntry.getValue(), departments);
        };
    }

    private List<SickNote> getSickNotes(Person person, LocalDate from, LocalDate to) {

        if (person.hasRole(OFFICE) || (person.hasRole(BOSS) && person.hasRole(SICK_NOTE_VIEW))) {
            return sickNoteService.getAllActiveByPeriod(from, to);
        }

        final List<SickNote> sickNotes;
        if ((person.hasRole(DEPARTMENT_HEAD) || person.hasRole(SECOND_STAGE_AUTHORITY)) && person.hasRole(SICK_NOTE_VIEW)) {
            final List<Person> members = getMembersForPerson(person);
            sickNotes = sickNoteService.getForStatesAndPerson(List.of(ACTIVE), members, from, to);
        } else {
            sickNotes = List.of();
        }

        return sickNotes;
    }

    private List<Person> getMembersForPerson(Person person) {
        final List<Person> membersDH = person.hasRole(DEPARTMENT_HEAD) ? departmentService.getMembersForDepartmentHead(person) : List.of();
        final List<Person> membersSSA = person.hasRole(SECOND_STAGE_AUTHORITY) ? departmentService.getMembersForSecondStageAuthority(person) : List.of();
        return Stream.concat(membersDH.stream(), membersSSA.stream())
            .distinct()
            .collect(toList());
    }
}
