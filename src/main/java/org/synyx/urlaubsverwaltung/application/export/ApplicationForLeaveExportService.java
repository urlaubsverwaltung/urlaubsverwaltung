package org.synyx.urlaubsverwaltung.application.export;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationForLeave;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedata;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedataService;
import org.synyx.urlaubsverwaltung.search.PageableSearchQuery;
import org.synyx.urlaubsverwaltung.search.SortComparator;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
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

    private static final String PERSON_PREFIX = "person.";

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
     * Returns a list of all application for leaves that the person is allowed to access.
     *
     * @param person to ask for the export
     * @param from   a specific date
     * @param to     a specific date
     * @return list of all {@link ApplicationForLeaveExport} that the person can access
     */
    Page<ApplicationForLeaveExport> getAll(Person person, LocalDate from, LocalDate to, PageableSearchQuery pageableSearchQuery) {

        final Pageable pageable = pageableSearchQuery.getPageable();

        final Page<Person> relevantMembersPage = getMembersForPerson(person, pageableSearchQuery);
        final List<Person> relevantMembers = relevantMembersPage.getContent();
        final List<Long> relevantPersonIds = relevantMembers.stream().map(Person::getId).toList();

        if (relevantPersonIds.isEmpty()) {
            return Page.empty();
        }

        final List<Application> applications = getApplications(person, relevantMembers, from, to);

        final Map<Person, List<Application>> applicationsByPerson = applications.stream().collect(groupingBy(Application::getPerson));
        for (Person member : relevantMembers) {
            applicationsByPerson.putIfAbsent(member, List.of());
        }


        final Map<PersonId, PersonBasedata> basedataByPersonId = personBasedataService.getBasedataByPersonId(relevantPersonIds);
        final Map<PersonId, List<String>> departmentsByPersonId = departmentService.getDepartmentNamesByMembers(relevantMembers);

        Stream<ApplicationForLeaveExport> exportsStream = applicationsByPerson.entrySet()
            .stream()
            .map(toApplicationForLeaveExport(basedataByPersonId, departmentsByPersonId));

        if (relevantMembersPage.getPageable().isUnpaged()) {
            // we don't have to restrict the statistics if persons page is paged and or sorted already.
            // otherwise we have fetched ALL persons -> therefore skip and limit statistics content.
            exportsStream = exportsStream
                .skip((long) pageable.getPageNumber() * pageable.getPageSize())
                .limit(pageable.getPageSize());
        }

        final List<ApplicationForLeaveExport> content = exportsStream
            .sorted(new SortComparator<>(ApplicationForLeaveExport.class, pageable.getSort()))
            .toList();

        return new PageImpl<>(content, pageable, relevantMembersPage.getTotalElements());
    }

    private Function<Map.Entry<Person, List<Application>>, ApplicationForLeaveExport> toApplicationForLeaveExport(Map<PersonId, PersonBasedata> basedataForPersons, Map<PersonId, List<String>> departmentsForPersons) {
        return personListEntry ->
        {
            final Person person = personListEntry.getKey();
            final PersonId personId = new PersonId(person.getId());
            final String personnelNumber = basedataForPersons.getOrDefault(personId, new PersonBasedata(personId, "", "")).personnelNumber();
            final List<String> departments = departmentsForPersons.getOrDefault(personId, List.of());
            final List<ApplicationForLeave> applicationForLeaves = personListEntry.getValue().stream().map(app -> new ApplicationForLeave(app, workDaysCountService)).toList();
            return new ApplicationForLeaveExport(personnelNumber, person.getFirstName(), person.getLastName(), applicationForLeaves, departments);
        };
    }

    private List<Application> getApplications(Person person, List<Person> members, LocalDate from, LocalDate to) {
        if (person.hasRole(OFFICE) || person.hasRole(BOSS) || person.hasRole(DEPARTMENT_HEAD) || person.hasRole(SECOND_STAGE_AUTHORITY)) {
            return applicationService.getForStatesAndPerson(List.of(ALLOWED, TEMPORARY_ALLOWED, ALLOWED_CANCELLATION_REQUESTED), members, from, to);
        }

        return List.of();
    }

    private Page<Person> getMembersForPerson(Person person, PageableSearchQuery pageableSearchQuery) {
        final Pageable pageable = pageableSearchQuery.getPageable();
        final boolean sortByPerson = isSortByPersonAttribute(pageable);

        if (person.hasRole(OFFICE) || person.hasRole(BOSS)) {
            final PageableSearchQuery query = sortByPerson
                ? new PageableSearchQuery(mapToPersonPageRequest(pageable), pageableSearchQuery.getQuery())
                : new PageableSearchQuery(PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()), pageableSearchQuery.getQuery());

            return personService.getActivePersons(query);
        }

        final PageableSearchQuery query = new PageableSearchQuery(sortByPerson ? mapToPersonPageRequest(pageable) : Pageable.unpaged(), pageableSearchQuery.getQuery());
        return departmentService.getManagedMembersOfPerson(person, query);
    }

    private boolean isSortByPersonAttribute(Pageable pageable) {
        for (Sort.Order order : pageable.getSort()) {
            if (!order.getProperty().startsWith(PERSON_PREFIX)) {
                return false;
            }
        }
        return true;
    }

    private PageRequest mapToPersonPageRequest(Pageable statisticsPageRequest) {
        Sort personSort = Sort.unsorted();

        for (Sort.Order order : statisticsPageRequest.getSort()) {
            if (order.getProperty().startsWith(PERSON_PREFIX)) {
                personSort = personSort.and(Sort.by(order.getDirection(), order.getProperty().replace(PERSON_PREFIX, "")));
            }
        }

        return PageRequest.of(statisticsPageRequest.getPageNumber(), statisticsPageRequest.getPageSize(), personSort);
    }
}
