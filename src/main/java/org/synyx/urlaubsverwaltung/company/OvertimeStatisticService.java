package org.synyx.urlaubsverwaltung.company;

import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.overtime.Overtime;
import org.synyx.urlaubsverwaltung.overtime.OvertimeService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;

@Component
class OvertimeStatisticService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

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
    OvertimeStatistic getOvertimeStatistics(Person viewer, LocalDate from, LocalDate to) {

        if (!viewer.isPrivileged()) {
            // TODO create statistics for viewer?
            LOG.info("person id={} is not privileged. Returning empty overtime statistics.", viewer.getId());
            return OvertimeStatistic.empty();
        }

        final List<Person> persons = getRelevantPersons(viewer);
        final List<PersonId> personIds = persons.stream().map(Person::getIdAsPersonId).toList();

        final Map<PersonId, List<Overtime>> overtimeByPerson = overtimeService.getOvertimeForPersonsInDateRange(personIds, from, to);

        // TODO how to handle active/inactive considering the date range?
        return new OvertimeStatistic(overtimeByPerson);
    }

    private List<Person> getRelevantPersons(Person viewer) {
        if (viewer.hasAnyRole(OFFICE, BOSS)) {
            return personService.getAllPersons();
        }

        final List<Person> persons = viewer.isDepartmentPrivileged()
            ? departmentService.getManagedActiveMembersOfPerson(viewer)
            : List.of();

        return Stream.concat(persons.stream(), Stream.of(viewer)).toList();
    }
}
