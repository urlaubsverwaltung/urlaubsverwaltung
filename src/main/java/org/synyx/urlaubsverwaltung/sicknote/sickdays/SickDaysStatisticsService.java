package org.synyx.urlaubsverwaltung.sicknote.sickdays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedata;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedataService;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
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
    private final PersonService personService;

    @Autowired
    SickDaysStatisticsService(SickNoteService sickNoteService, DepartmentService departmentService, PersonBasedataService personBasedataService, PersonService personService) {
        this.sickNoteService = sickNoteService;
        this.departmentService = departmentService;
        this.personBasedataService = personBasedataService;
        this.personService = personService;
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

        final List<Person> members = getMembersForPerson(person);
        final List<Integer> personIds = members.stream().map(Person::getId).collect(toList());

        final List<SickNote> sickNotes = getSickNotes(person, members, from, to);

        // members without sickNotes should also have a statistics object. no sickNotes just means zero sick days :shrug:
        final Map<Person, List<SickNote>> sickNotesByPerson = sickNotes.stream().collect(groupingBy(SickNote::getPerson));
        for (Person member : members) {
            sickNotesByPerson.putIfAbsent(member, List.of());
        }

        final Map<PersonId, PersonBasedata> basedataByPersonId = personBasedataService.getBasedataByPersonId(personIds);
        final Map<PersonId, List<String>> departmentsByPersonId = departmentService.getDepartmentNamesByMembers(members);

        return sickNotesByPerson.entrySet().stream()
            .map(toSickNoteDetailedStatistics(basedataByPersonId, departmentsByPersonId))
            .collect(toList());
    }

    private Function<Map.Entry<Person, List<SickNote>>, SickDaysDetailedStatistics> toSickNoteDetailedStatistics(Map<PersonId, PersonBasedata> basedataForPersons, Map<PersonId, List<String>> departmentsForPersons) {
        return personListEntry ->
        {
            final Person person = personListEntry.getKey();
            final PersonId personId = new PersonId(person.getId());
            final String personnelNumber = basedataForPersons.getOrDefault(personId, new PersonBasedata(personId, "", "")).getPersonnelNumber();
            final List<String> departments = departmentsForPersons.getOrDefault(personId, List.of());
            return new SickDaysDetailedStatistics(personnelNumber, person, personListEntry.getValue(), departments);
        };
    }

    private List<SickNote> getSickNotes(Person person, List<Person> members, LocalDate from, LocalDate to) {

        if (person.hasRole(OFFICE) || (person.hasRole(BOSS) && person.hasRole(SICK_NOTE_VIEW))) {
            return sickNoteService.getAllActiveByPeriod(from, to);
        }

        final List<SickNote> sickNotes;
        if ((person.hasRole(DEPARTMENT_HEAD) || person.hasRole(SECOND_STAGE_AUTHORITY)) && person.hasRole(SICK_NOTE_VIEW)) {
            sickNotes = sickNoteService.getForStatesAndPerson(List.of(ACTIVE), members, from, to);
        } else {
            sickNotes = List.of();
        }

        return sickNotes;
    }

    private List<Person> getMembersForPerson(Person person) {

        if (person.hasRole(OFFICE) || person.hasRole(BOSS) && person.hasRole(SICK_NOTE_VIEW)) {
            return personService.getActivePersons();
        }

        final List<Person> membersForDepartmentHead = person.hasRole(DEPARTMENT_HEAD)
                ? departmentService.getMembersForDepartmentHead(person)
                : List.of();

        final List<Person> memberForSecondStageAuthority = person.hasRole(SECOND_STAGE_AUTHORITY)
                ? departmentService.getMembersForSecondStageAuthority(person)
                : List.of();

        return Stream.concat(memberForSecondStageAuthority.stream(), membersForDepartmentHead.stream())
                .distinct()
                .sorted(comparing(Person::getFirstName).thenComparing(Person::getLastName))
                .collect(toList());
    }
}
