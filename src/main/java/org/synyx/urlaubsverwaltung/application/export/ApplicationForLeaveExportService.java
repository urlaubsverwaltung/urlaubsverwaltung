package org.synyx.urlaubsverwaltung.application.export;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationForLeave;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.PersonPageRequest;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedata;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedataService;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;

import static java.util.Comparator.comparing;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;

@Service
@Transactional
class ApplicationForLeaveExportService {

    private static final Comparator<Person> BY_NICE_NAME =
        comparing((Person person) -> person.getFirstName().toLowerCase()).thenComparing(person -> person.getLastName().toLowerCase());

    private final ApplicationService applicationService;
    private final DepartmentService departmentService;
    private final PersonBasedataService personBasedataService;
    private final PersonService personService;
    private final WorkDaysCountService workDaysCountService;

    @Autowired
    ApplicationForLeaveExportService(ApplicationService applicationService, DepartmentService departmentService,
                                     PersonBasedataService personBasedataService, PersonService personService,
                                     WorkDaysCountService workDaysCountService) {
        this.applicationService = applicationService;
        this.departmentService = departmentService;
        this.personBasedataService = personBasedataService;
        this.personService = personService;
        this.workDaysCountService = workDaysCountService;
    }

    /**
     * Returns the export of every person the given person is allowed to access.
     *
     * @param person to ask for the export
     * @param from   a specific date
     * @param to     a specific date
     * @return all {@link ApplicationForLeaveExport} the person can access, ordered by first and last name
     */
    List<ApplicationForLeaveExport> getAll(Person person, LocalDate from, LocalDate to) {

        final List<Person> members = accessibleMembers(person).stream().sorted(BY_NICE_NAME).toList();

        return exportsOf(person, members, from, to);
    }

    /**
     * Returns the export of the given persons.
     *
     * <p>
     * This export is not a statistic, therefore it cannot be sorted by statistics values. The caller decides which
     * persons to export instead - the persons visible on the statistics page, for instance, no matter how that page
     * has been sorted.
     *
     * @param person    to ask for the export
     * @param from      a specific date
     * @param to        a specific date
     * @param personIds persons to export, person ids the given person must not access are ignored
     * @return the {@link ApplicationForLeaveExport}s of the given persons, in the order of the given personIds
     */
    List<ApplicationForLeaveExport> getAllForPersons(Person person, LocalDate from, LocalDate to, List<PersonId> personIds) {

        if (personIds.isEmpty()) {
            return List.of();
        }

        final Map<PersonId, Person> accessibleMemberById = accessibleMembers(person).stream()
            .collect(toMap(Person::getIdAsPersonId, identity()));

        final List<Person> members = personIds.stream()
            .map(accessibleMemberById::get)
            .filter(Objects::nonNull)
            .toList();

        return exportsOf(person, members, from, to);
    }

    private List<ApplicationForLeaveExport> exportsOf(Person person, List<Person> members, LocalDate from, LocalDate to) {

        if (members.isEmpty()) {
            return List.of();
        }

        final List<Long> memberIds = members.stream().map(Person::getId).toList();

        final List<Application> applications = getApplications(person, members, from, to);
        final Map<Person, List<Application>> applicationsByPerson = applications.stream().collect(groupingBy(Application::getPerson));

        final Map<PersonId, PersonBasedata> basedataByPersonId = personBasedataService.getBasedataByPersonId(memberIds);
        final Map<PersonId, List<String>> departmentsByPersonId = departmentService.getDepartmentNamesByMembers(members);
        final Map<Application, SortedMap<Integer, BigDecimal>> workDaysByYearByApplication = workDaysCountService.getWorkDaysCountByYearForApplications(applications);

        return members.stream()
            .map(member -> toApplicationForLeaveExport(member, applicationsByPerson.getOrDefault(member, List.of()),
                basedataByPersonId, departmentsByPersonId, workDaysByYearByApplication))
            .toList();
    }

    private static ApplicationForLeaveExport toApplicationForLeaveExport(
        Person person, List<Application> applications, Map<PersonId, PersonBasedata> basedataForPersons,
        Map<PersonId, List<String>> departmentsForPersons, Map<Application, SortedMap<Integer, BigDecimal>> workDaysByYearByApplication) {

        final PersonId personId = person.getIdAsPersonId();
        final String personnelNumber = basedataForPersons.getOrDefault(personId, new PersonBasedata(personId, "", "")).personnelNumber();
        final List<String> departments = departmentsForPersons.getOrDefault(personId, List.of());
        final List<ApplicationForLeave> applicationForLeaves = applications.stream()
            .map(app -> new ApplicationForLeave(app, workDaysByYearByApplication.get(app))).toList();

        return new ApplicationForLeaveExport(personnelNumber, person.getFirstName(), person.getLastName(), applicationForLeaves, departments);
    }

    private List<Application> getApplications(Person person, List<Person> members, LocalDate from, LocalDate to) {
        if (person.hasRole(OFFICE) || person.hasRole(BOSS) || person.hasRole(DEPARTMENT_HEAD) || person.hasRole(SECOND_STAGE_AUTHORITY)) {
            return applicationService.getForStatesAndPerson(List.of(ALLOWED, TEMPORARY_ALLOWED, ALLOWED_CANCELLATION_REQUESTED), members, from, to);
        }

        return List.of();
    }

    /**
     * Every person the given person is allowed to see. Not paginated - the caller restricts the export itself.
     */
    private List<Person> accessibleMembers(Person person) {

        if (person.hasRole(OFFICE) || person.hasRole(BOSS)) {
            return personService.getActivePersons(PersonPageRequest.unpaged(), "").getContent();
        }

        return departmentService.getManagedActiveMembersOfPerson(person, PersonPageRequest.unpaged(), "").getContent();
    }
}
