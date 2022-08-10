package org.synyx.urlaubsverwaltung.application.statistics;

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
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedata;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedataService;
import org.synyx.urlaubsverwaltung.search.PageableSearchQuery;
import org.synyx.urlaubsverwaltung.search.SortComparator;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;

@Service
class ApplicationForLeaveStatisticsService {

    private final PersonService personService;
    private final PersonBasedataService personBasedataService;
    private final DepartmentService departmentService;
    private final ApplicationForLeaveStatisticsBuilder applicationForLeaveStatisticsBuilder;
    private final VacationTypeService vacationTypeService;

    @Autowired
    ApplicationForLeaveStatisticsService(PersonService personService, PersonBasedataService personBasedataService, DepartmentService departmentService,
                                         ApplicationForLeaveStatisticsBuilder applicationForLeaveStatisticsBuilder, VacationTypeService vacationTypeService) {
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
     * @param person person to restrict the returned page content
     * @param period filter result set for a given period of time
     * @param pageable the page request
     *
     * @return filtered page of {@link ApplicationForLeaveStatistics}
     */
    Page<ApplicationForLeaveStatistics> getStatistics(Person person, FilterPeriod period, Pageable pageable) {
        final List<VacationType> activeVacationTypes = vacationTypeService.getActiveVacationTypes();
        final List<Person> relevantPersons = getAllRelevantPersons(person);
        final List<Integer> personIdValues = relevantPersons.stream().map(Person::getId).collect(toList());
        final Map<PersonId, PersonBasedata> basedataByPersonId = personBasedataService.getBasedataByPersonId(personIdValues);

        final List<ApplicationForLeaveStatistics> content = relevantPersons.stream()
            .map(relevantPerson -> statistics(period, activeVacationTypes, relevantPerson, basedataByPersonId.getOrDefault(new PersonId(relevantPerson.getId()), null)))
            .sorted(new SortComparator<>(ApplicationForLeaveStatistics.class, pageable.getSort()))
            .skip((long) pageable.getPageNumber() * pageable.getPageSize())
            .limit(pageable.getPageSize())
            .collect(toList());

        return new PageImpl<>(content, pageable, relevantPersons.size());
    }

    private ApplicationForLeaveStatistics statistics(FilterPeriod period, List<VacationType> vacationTypes, Person person, PersonBasedata personBasedata) {
        if (personBasedata == null) {
            return applicationForLeaveStatisticsBuilder.build(person, period.getStartDate(), period.getEndDate(), vacationTypes);
        } else {
            return applicationForLeaveStatisticsBuilder.build(person, personBasedata, period.getStartDate(), period.getEndDate(), vacationTypes);
        }
    }

    private List<Person> getAllRelevantPersons(Person person) {
        if (person.hasRole(BOSS) || person.hasRole(OFFICE)) {
            return personService.getActivePersons();
        }

        final PageableSearchQuery query = new PageableSearchQuery(Pageable.unpaged());
        return departmentService.getManagedMembersOfPerson(person, query).getContent();
    }
}
