package org.synyx.urlaubsverwaltung.company;

import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.overtime.Overtime;
import org.synyx.urlaubsverwaltung.overtime.OvertimeService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;

@Component
class OvertimeStatisticService {

    private final OvertimeService overtimeService;
    private final PersonService personService;
    private final DepartmentService departmentService;

    OvertimeStatisticService(OvertimeService overtimeService, PersonService personService, DepartmentService departmentService) {
        this.overtimeService = overtimeService;
        this.personService = personService;
        this.departmentService = departmentService;
    }

    /**
     *
     * @param viewer id of the person requesting statistics
     * @param from from date
     * @param to to date, inclusive
     */
    OvertimeStatistic getOvertimeStatistics(Person viewer, Instant from, Instant to) {

        final List<Person> persons = getRelevantPersons(viewer);
        final List<PersonId> personIds = persons.stream().map(Person::getIdAsPersonId).toList();

        final Map<PersonId, List<Overtime>> overtimeByPerson = overtimeService.getOvertimeForPersonsInDateRange(personIds, from, to);

        return new OvertimeStatistic(overtimeByPerson);
    }

    private List<Person> getRelevantPersons(Person viewer) {
        if (viewer.hasAnyRole(OFFICE, BOSS)) {
            return personService.getActivePersons();
        }

        final List<Person> persons = viewer.isDepartmentPrivileged()
            ? departmentService.getManagedActiveMembersOfPerson(viewer)
            : List.of();

        return Stream.concat(persons.stream(), Stream.of(viewer)).distinct().toList();
    }
}
