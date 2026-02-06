package org.synyx.urlaubsverwaltung.application.statistics;

import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeService;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.PersonPageRequest;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedata;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedataService;
import org.synyx.urlaubsverwaltung.search.SortComparator;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.activeStatuses;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;

@Service
class ApplicationForLeaveStatisticsService {

    private final PersonService personService;
    private final PersonBasedataService personBasedataService;
    private final DepartmentService departmentService;
    private final ApplicationService applicationService;
    private final ApplicationForLeaveStatisticsBuilder applicationForLeaveStatisticsBuilder;
    private final VacationTypeService vacationTypeService;

    @Autowired
    ApplicationForLeaveStatisticsService(
        PersonService personService,
        PersonBasedataService personBasedataService,
        DepartmentService departmentService,
        ApplicationService applicationService,
        ApplicationForLeaveStatisticsBuilder applicationForLeaveStatisticsBuilder,
        VacationTypeService vacationTypeService
    ) {
        this.personService = personService;
        this.personBasedataService = personBasedataService;
        this.departmentService = departmentService;
        this.applicationService = applicationService;
        this.applicationForLeaveStatisticsBuilder = applicationForLeaveStatisticsBuilder;
        this.vacationTypeService = vacationTypeService;
    }

    /**
     * Get the matching page sorted by given person criteria.
     *
     * @param person         person to restrict the returned page content
     * @param filterPeriod   filter result set for a given period of time
     * @param personPageable person pageable criteria
     * @param query          optional person query, to search for firstname for instance
     * @return filtered page of {@link ApplicationForLeaveStatistics}
     */
    Page<ApplicationForLeaveStatistics> getStatisticsSortedByPerson(Person person, FilterPeriod filterPeriod, PersonPageRequest personPageable, String query) {

        final Page<Person> personPage = getAllRelevantPersons(person, personPageable, query);
        final List<VacationType<?>> vacationTypes = vacationTypeService.getActiveVacationTypes();

        final Iterable<ApplicationForLeaveStatistics> statistics =
            getStatistics(filterPeriod, personPage.getContent(), vacationTypes, null);

        final List<ApplicationForLeaveStatistics> content = StreamSupport.stream(statistics.spliterator(), false).toList();

        return new PageImpl<>(content, personPageable.toPageable(), personPage.getTotalElements());
    }

    /**
     * Get {@link ApplicationForLeaveStatistics} the given person is allowed to see.
     * A person with {@link org.synyx.urlaubsverwaltung.person.Role} BOSS or OFFICE is allowed to see statistics of everyone for instance.
     *
     * @param person             person to restrict the returned page content
     * @param period             filter result set for a given period of time
     * @param statisticsPageable the page request
     * @param query              optional query to filter for person firstname for instance
     * @return filtered page of {@link ApplicationForLeaveStatistics}
     */
    Page<ApplicationForLeaveStatistics> getStatisticsSortedByStatistics(Person person, FilterPeriod period, ApplicationForLeaveStatisticsPageable statisticsPageable, String query) {

        final Pageable pageable = statisticsPageable.toPageable();
        final List<VacationType<?>> vacationTypes = vacationTypeService.getActiveVacationTypes();

        final List<Application> allApplications =
            getApplicationsOfInterest(period.startDate(), period.endDate(), vacationTypes, query);

        // fetch all allowed persons, there may be persons without applications
        final List<Person> persons = getAllRelevantPersons(person, PersonPageRequest.unpaged(), query).getContent();

        final Collection<ApplicationForLeaveStatistics> allStatistics =
            getStatistics(period, persons, vacationTypes, allApplications);

        final List<ApplicationForLeaveStatistics> paginatedStatistics = allStatistics.stream()
            .sorted(new SortComparator<>(ApplicationForLeaveStatistics.class, pageable.getSort()))
            .skip((long) pageable.getPageNumber() * pageable.getPageSize())
            .limit(pageable.getPageSize())
            .toList();

        return new PageImpl<>(paginatedStatistics, pageable, allStatistics.size());
    }

    private List<ApplicationForLeaveStatistics> getStatistics(
        FilterPeriod period, List<Person> sortedPersons, List<VacationType<?>> vacationTypes, @Nullable List<Application> applications) {

        final Map<Person, Optional<ApplicationForLeaveStatistics>> statisticsByPerson;
        if (applications == null) {
            statisticsByPerson = applicationForLeaveStatisticsBuilder.build(sortedPersons, period.startDate(), period.endDate(), vacationTypes);
        } else {
            statisticsByPerson = applicationForLeaveStatisticsBuilder.build(sortedPersons, period.startDate(), period.endDate(), vacationTypes, applications);
        }

        final List<ApplicationForLeaveStatistics> sortedStatistics = sortedPersons.stream()
            .map(statisticsByPerson::get)
            .flatMap(Optional::stream)
            .toList();

        return enrichWithPersonBaseData(sortedStatistics, sortedPersons);
    }

    private List<ApplicationForLeaveStatistics> enrichWithPersonBaseData(List<ApplicationForLeaveStatistics> statistics, List<Person> persons) {

        final List<Long> personIdValues = persons.stream().map(Person::getId).toList();
        final Map<PersonId, PersonBasedata> baseDataByPersonId = personBasedataService.getBasedataByPersonId(personIdValues);

        for (ApplicationForLeaveStatistics statistic : statistics) {
            final PersonId personId = statistic.getPerson().getIdAsPersonId();
            statistic.setPersonBasedata(baseDataByPersonId.getOrDefault(personId, null));
        }

        return statistics;
    }

    private List<Application> getApplicationsOfInterest(LocalDate from, LocalDate to, List<VacationType<?>> vacationTypes, String personQuery) {
        Assert.isTrue(from.getYear() == to.getYear(), "From and to must be in the same year");
        return applicationService.getApplicationsForACertainPeriodAndStatus(from, to, activeStatuses(), vacationTypes, personQuery);
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
