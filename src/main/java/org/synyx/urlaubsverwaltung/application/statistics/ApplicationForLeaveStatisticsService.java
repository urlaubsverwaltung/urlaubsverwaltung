package org.synyx.urlaubsverwaltung.application.statistics;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeService;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.PersonPageRequest;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedata;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedataService;
import org.synyx.urlaubsverwaltung.search.PageableSearchQuery;
import org.synyx.urlaubsverwaltung.search.SortComparator;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;

@Service
class ApplicationForLeaveStatisticsService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final PersonService personService;
    private final PersonBasedataService personBasedataService;
    private final DepartmentService departmentService;
    private final ApplicationForLeaveStatisticsBuilder applicationForLeaveStatisticsBuilder;
    private final VacationTypeService vacationTypeService;

    @Autowired
    ApplicationForLeaveStatisticsService(
        PersonService personService, PersonBasedataService personBasedataService, DepartmentService departmentService,
        ApplicationForLeaveStatisticsBuilder applicationForLeaveStatisticsBuilder, VacationTypeService vacationTypeService
    ) {
        this.personService = personService;
        this.personBasedataService = personBasedataService;
        this.departmentService = departmentService;
        this.applicationForLeaveStatisticsBuilder = applicationForLeaveStatisticsBuilder;
        this.vacationTypeService = vacationTypeService;
    }

    /**
     * Get {@link ApplicationForLeaveStatistics} the given person is allowed to see.
     * A person with {@link org.synyx.urlaubsverwaltung.person.Role} BOSS or OFFICE is allowed to see statistics of everyone for instance.
     *
     * @param person              person to restrict the returned page content
     * @param period              filter result set for a given period of time
     * @param pageableSearchQuery the page request
     * @return filtered page of {@link ApplicationForLeaveStatistics}
     */
    Page<ApplicationForLeaveStatistics> getStatistics(Person person, FilterPeriod period, PageableSearchQuery pageableSearchQuery) {

        final Pageable pageable = pageableSearchQuery.getPageable();

        final List<VacationType<?>> activeVacationTypes = vacationTypeService.getActiveVacationTypes();
        final Page<Person> relevantPersonsPage = getAllRelevantPersons(person, pageableSearchQuery);
        final List<Long> personIdValues = relevantPersonsPage.getContent().stream().map(Person::getId).toList();
        final Map<PersonId, PersonBasedata> basedataByPersonId = personBasedataService.getBasedataByPersonId(personIdValues);

        final Collection<ApplicationForLeaveStatistics> statisticsCollection = applicationForLeaveStatisticsBuilder
            .build(relevantPersonsPage.getContent(), period.startDate(), period.endDate(), activeVacationTypes).values();

        statisticsCollection.forEach(statistics -> {
            final PersonId personId = statistics.getPerson().getIdAsPersonId();
            statistics.setPersonBasedata(basedataByPersonId.getOrDefault(personId, null));
        });

        Stream<ApplicationForLeaveStatistics> statisticsStream = statisticsCollection.stream();
        if (relevantPersonsPage.getPageable().isUnpaged()) {
            // we don't have to restrict the statistics if persons page is paged and or sorted already.
            // otherwise we have fetched ALL persons -> therefore skip and limit statistics content.
            statisticsStream = statisticsStream
                .skip((long) pageable.getPageNumber() * pageable.getPageSize())
                .limit(pageable.getPageSize());
        }

        final List<ApplicationForLeaveStatistics> content = statisticsStream
            .sorted(new SortComparator<>(ApplicationForLeaveStatistics.class, pageable.getSort()))
            .toList();

        return new PageImpl<>(content, pageable, relevantPersonsPage.getTotalElements());
    }

    private Page<Person> getAllRelevantPersons(Person person, PageableSearchQuery pageableSearchQuery) {

        PersonPageRequest pageRequest = PersonPageRequest.ofApiPageable(pageableSearchQuery.getPageable());

        // this has been / is a bug, we don't want to fix right now...
        // the pageNumber and pageSize must actually NOT be considered when the pageable doesn't contain info for person pagination
        if (pageRequest.isUnpaged()) {
            LOG.error("reached buggy path of fetching paginated persons for statistics, despite person should not be paginated.");
            final Pageable pageable = pageableSearchQuery.getPageable();
            pageRequest = PersonPageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        }

        if (person.hasRole(OFFICE) || person.hasRole(BOSS)) {
            return personService.getActivePersons(pageRequest, pageableSearchQuery.getQuery());
        }

        if (person.isDepartmentPrivileged()) {
            return departmentService.getManagedMembersOfPerson(person, pageRequest, pageableSearchQuery.getQuery());
        }

        return new PageImpl<>(List.of(person), pageableSearchQuery.getPageable(), 1);
    }
}
