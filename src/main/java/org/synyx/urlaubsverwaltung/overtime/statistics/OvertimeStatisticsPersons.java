package org.synyx.urlaubsverwaltung.overtime.statistics;

import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;

import java.time.Year;
import java.util.List;

import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;

/**
 * Resolves the persons that the overtime statistics are aggregated over, from the perspective of a signed-in person.
 *
 * <p>
 * {@link Role#OFFICE} and {@link Role#BOSS} get everyone, {@link Role#DEPARTMENT_HEAD} and
 * {@link Role#SECOND_STAGE_AUTHORITY} get the members they manage, everyone else gets nobody. Which of the two
 * methods applies depends on the question a block of the page asks, not on the role.
 */
@Service
class OvertimeStatisticsPersons {

    private final PersonService personService;
    private final DepartmentService departmentService;

    OvertimeStatisticsPersons(PersonService personService, DepartmentService departmentService) {
        this.personService = personService;
        this.departmentService = departmentService;
    }

    /**
     * The persons of a single year. For office and boss everyone who had an account in that year, which keeps past
     * years stable: someone who left the company stays part of the years they worked in. A manager gets the members
     * of the departments they were responsible for in that year and still are, as far as those members are still
     * part of the department - see {@link DepartmentService#getManagedMembersOfPerson(Person, Year)}.
     *
     * @param signedInUser person requesting the statistics
     * @param year         year to resolve the persons for
     * @return persons relevant for the given year, empty if the signed-in person is responsible for nobody
     */
    List<Person> relevantPersonsOfYear(Person signedInUser, Year year) {

        if (signedInUser.hasAnyRole(OFFICE, BOSS)) {
            return personService.getAllPersonsHavingAccountInYear(year);
        }

        if (signedInUser.hasAnyRole(DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY)) {
            return departmentService.getManagedMembersOfPerson(signedInUser, year);
        }

        return List.of();
    }

    /**
     * The persons of the whole history, without any reference to a year. The persons currently employed, respectively
     * the members currently managed - someone who left is not part of the overtime that is still open, which is
     * exactly the question those figures answer.
     *
     * @param signedInUser person requesting the statistics
     * @return persons relevant over the whole history, empty if the signed-in person is responsible for nobody
     */
    List<Person> relevantPersonsOfWholeHistory(Person signedInUser) {

        if (signedInUser.hasAnyRole(OFFICE, BOSS)) {
            return personService.getActivePersons();
        }

        if (signedInUser.hasAnyRole(DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY)) {
            return departmentService.getManagedActiveMembersOfPerson(signedInUser);
        }

        return List.of();
    }
}
