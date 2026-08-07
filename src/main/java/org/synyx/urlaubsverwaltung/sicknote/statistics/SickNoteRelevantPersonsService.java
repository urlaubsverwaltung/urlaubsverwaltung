package org.synyx.urlaubsverwaltung.sicknote.statistics;

import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNotePermissionEvaluator;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.Role.SICK_NOTE_VIEW;

@Service
class SickNoteRelevantPersonsService {

    private final SickNotePermissionEvaluator sickNotePermissionEvaluator;
    private final PersonService personService;
    private final DepartmentService departmentService;
    private final Clock clock;

    SickNoteRelevantPersonsService(
        SickNotePermissionEvaluator sickNotePermissionEvaluator,
        PersonService personService,
        DepartmentService departmentService,
        Clock clock
    ) {
        this.sickNotePermissionEvaluator = sickNotePermissionEvaluator;
        this.personService = personService;
        this.departmentService = departmentService;
        this.clock = clock;
    }

    List<Person> getStatisticRelevantPersons(LocalDate from, LocalDate to, Person person) {
        return years(from, to).stream()
            .map(year -> getStatisticRelevantPersons(year, person))
            .flatMap(Collection::stream)
            .distinct()
            .toList();
    }

    private List<Person> getStatisticRelevantPersons(Year year, Person person) {

        if (sickNotePermissionEvaluator.isAllowedToViewSickNotesOfAllPersons(person)) {
            // we don't know whether a person has been active/inactive over a certain year
            // Therefore, we return all persons having an account in the given year.
            return personService.getAllPersonsHavingAccountInYear(year);
        }

        if (person.hasAnyRole(DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY)) {
            final List<Person> managedMembers = departmentService.getManagedMembersOfPerson(person, year);
            if (year.equals(Year.now(clock))) {
                // we can, however, determine it for THIS year.
                return managedMembers.stream().filter(Person::isActive).toList();
            } else {
                // Sadly, we do not know whether a person has been active or inactive in a year before this year
                return managedMembers;
            }
        }

        if (person.hasRole(SICK_NOTE_VIEW)) {
            return List.of(person);
        }

        return emptyList();
    }

    private List<Year> years(LocalDate from, LocalDate to) {
        final Year fromYear = Year.from(from);
        final Year toYear = Year.from(to);

        return Stream.iterate(fromYear, year -> !year.isAfter(toYear), year -> year.plusYears(1))
            .toList();
    }
}
