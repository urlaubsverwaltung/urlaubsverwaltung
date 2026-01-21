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
     * Get the matching page sorted by given person criteria.
     *
     * @param person person to restrict the returned page content
     * @param filterPeriod filter result set for a given period of time
     * @param personPageable person pageable criteria
     * @param query optional person query, to search for firstname for instance
     * @return filtered page of {@link ApplicationForLeaveStatistics}
     */
    Page<ApplicationForLeaveStatistics> getStatisticsSortedByPerson(Person person, FilterPeriod filterPeriod, PersonPageRequest personPageable, String query) {
        final Page<Person> relevantPersonsPage = getAllRelevantPersons(person, personPageable, query);
        return getStatistics(filterPeriod, relevantPersonsPage, personPageable);
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
        final String query = pageableSearchQuery.getQuery();

        // TODO this is actually wrong! statistics is relevant for pagination, not person.
        final Page<Person> relevantPersonsPage = getAllRelevantPersons(person, pageable, query);

        return getStatistics(period, relevantPersonsPage, pageable);
    }

    private Page<ApplicationForLeaveStatistics> getStatistics(FilterPeriod period, Page<Person> relevantPersonsPage, Pageable originalPageable) {

        final List<VacationType<?>> activeVacationTypes = vacationTypeService.getActiveVacationTypes();
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
                .skip((long) originalPageable.getPageNumber() * originalPageable.getPageSize())
                .limit(originalPageable.getPageSize());
        }

        final List<ApplicationForLeaveStatistics> content = statisticsStream
            .sorted(new SortComparator<>(ApplicationForLeaveStatistics.class, originalPageable.getSort()))
            .toList();

        return new PageImpl<>(content, originalPageable, relevantPersonsPage.getTotalElements());
    }

    private Page<Person> getAllRelevantPersons(Person person, Pageable pageable, String query) {

        PersonPageRequest pageRequest = PersonPageRequest.ofApiPageable(pageable);

        // this has been / is a bug, we don't want to fix right now...
        // the pageNumber and pageSize must actually NOT be considered when the pageable doesn't contain info for person pagination
        if (pageRequest.isUnpaged()) {
            LOG.error("reached buggy path of fetching paginated persons for statistics, despite person should not be paginated.");
            pageRequest = PersonPageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        }

        return getAllRelevantPersons(person, pageRequest, query);
    }

    private Page<Person> getAllRelevantPersons(Person person, PersonPageRequest pageRequest, String query) {

        if (person.hasRole(OFFICE) || person.hasRole(BOSS)) {
            return personService.getActivePersons(pageRequest, query);
        }

        if (person.isDepartmentPrivileged()) {
            return departmentService.getManagedMembersOfPerson(person, pageRequest, query);
        }

        return new PageImpl<>(List.of(person), pageRequest.toPageable(), 1);
    }
}
