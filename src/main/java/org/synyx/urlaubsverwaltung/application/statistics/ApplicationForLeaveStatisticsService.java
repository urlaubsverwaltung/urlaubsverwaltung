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
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedata;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedataService;
import org.synyx.urlaubsverwaltung.search.PageableSearchQuery;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

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

        final Page<Person> relevantPersons = getRelevantPersons(person, pageable);

        final List<ApplicationForLeaveStatistics> content = relevantPersons.stream()
            .map(toApplicationForLeaveStatistics(period, activeVacationTypes))
            .collect(toList());

        return new PageImpl<>(content, pageable, relevantPersons.getTotalElements());
    }

    private Function<Person, ApplicationForLeaveStatistics> toApplicationForLeaveStatistics(FilterPeriod period, List<VacationType> activeVacationTypes) {
        return person -> {
            final Optional<PersonBasedata> personBasedata = personBasedataService.getBasedataByPersonId(person.getId());
            if (personBasedata.isPresent()) {
                return applicationForLeaveStatisticsBuilder.build(person, personBasedata.get(), period.getStartDate(), period.getEndDate(), activeVacationTypes);
            } else {
                return applicationForLeaveStatisticsBuilder.build(person, period.getStartDate(), period.getEndDate(), activeVacationTypes);
            }
        };
    }

    private Page<Person> getRelevantPersons(Person person, Pageable pageable) {
        final PageableSearchQuery searchQuery = new PageableSearchQuery(pageable, "");

        if (person.hasRole(BOSS) || person.hasRole(OFFICE)) {
            return personService.getActivePersons(searchQuery);
        }

        return departmentService.getManagedMembersOfPerson(person, searchQuery);
    }
}
