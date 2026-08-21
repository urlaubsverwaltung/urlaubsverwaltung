package org.synyx.urlaubsverwaltung.absence.statistics;

import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonActivePeriod;
import org.synyx.urlaubsverwaltung.person.PersonActivePeriodService;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;

import java.time.Instant;
import java.time.Year;
import java.util.List;
import java.util.Map;

import static java.time.ZoneOffset.UTC;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;

/**
 * Resolves the persons that the absence statistics of a given {@link Year} are aggregated over, from the
 * perspective of a signed-in person.
 *
 * <p>
 * This is a pure mapping without any Spring or database concerns of its own — collaborators are handed in via
 * the constructor.
 */
class AbsenceStatisticsPersons {

    private final PersonService personService;
    private final DepartmentService departmentService;
    private final PersonActivePeriodService personActivePeriodService;

    AbsenceStatisticsPersons(PersonService personService, DepartmentService departmentService,
                             PersonActivePeriodService personActivePeriodService) {
        this.personService = personService;
        this.departmentService = departmentService;
        this.personActivePeriodService = personActivePeriodService;
    }

    /**
     * Resolves the persons relevant for the absence statistics of the given year.
     *
     * <p>
     * {@link Role#OFFICE} and {@link Role#BOSS} get every person of the system, {@link Role#DEPARTMENT_HEAD} and
     * {@link Role#SECOND_STAGE_AUTHORITY} get their managed members, everyone else gets an empty list. The result
     * is restricted to persons that have been active for at least one day of the given year, determined via
     * {@link PersonActivePeriodService} rather than the current {@code Person#isActive} flag, so that a person
     * deactivated in the meantime still counts for a year in which they were active.
     *
     * @param signedInUser person requesting the statistics
     * @param year         year to resolve the persons for
     * @return persons relevant for the given year and signed-in person, empty if the signed-in person has none
     *         of the eligible roles
     */
    List<Person> relevantPersons(Person signedInUser, Year year) {

        final List<Person> candidates = candidatesForRole(signedInUser, year);
        if (candidates.isEmpty()) {
            return List.of();
        }

        return activeInYear(candidates, year);
    }

    private List<Person> candidatesForRole(Person signedInUser, Year year) {

        if (signedInUser.hasAnyRole(OFFICE, BOSS)) {
            return personService.getAllPersons();
        }

        if (signedInUser.hasAnyRole(DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY)) {
            return departmentService.getManagedMembersOfPerson(signedInUser, year);
        }

        return List.of();
    }

    private List<Person> activeInYear(List<Person> candidates, Year year) {

        final List<PersonId> personIds = candidates.stream().map(Person::getIdAsPersonId).toList();

        final Instant from = startOfYear(year);
        final Instant to = startOfYear(year.plusYears(1));
        final Map<PersonId, List<PersonActivePeriod>> activePeriodsByPerson =
            personActivePeriodService.getActivePeriodsOverlapping(personIds, from, to);

        return candidates.stream()
            .filter(person -> !activePeriodsByPerson.getOrDefault(person.getIdAsPersonId(), List.of()).isEmpty())
            .toList();
    }

    private static Instant startOfYear(Year year) {
        return year.atDay(1).atStartOfDay(UTC).toInstant();
    }
}
