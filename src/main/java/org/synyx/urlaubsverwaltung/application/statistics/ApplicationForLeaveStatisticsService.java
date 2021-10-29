package org.synyx.urlaubsverwaltung.application.statistics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.application.service.VacationTypeService;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;

@Service
class ApplicationForLeaveStatisticsService {

    private final PersonService personService;
    private final DepartmentService departmentService;
    private final ApplicationForLeaveStatisticsBuilder applicationForLeaveStatisticsBuilder;
    private final VacationTypeService vacationTypeService;

    @Autowired
    ApplicationForLeaveStatisticsService(PersonService personService, DepartmentService departmentService,
                                         ApplicationForLeaveStatisticsBuilder applicationForLeaveStatisticsBuilder, VacationTypeService vacationTypeService) {
        this.personService = personService;
        this.departmentService = departmentService;
        this.applicationForLeaveStatisticsBuilder = applicationForLeaveStatisticsBuilder;
        this.vacationTypeService = vacationTypeService;
    }

    List<ApplicationForLeaveStatistics> getStatistics(FilterPeriod period) {
        final List<VacationType> activeVacationTypes = vacationTypeService.getActiveVacationTypes();
        return getRelevantPersons().stream()
            .map(person -> applicationForLeaveStatisticsBuilder.build(person, period.getStartDate(), period.getEndDate(), activeVacationTypes))
            .collect(toList());
    }

    private List<Person> getRelevantPersons() {

        final Person signedInUser = personService.getSignedInUser();
        if (signedInUser.hasRole(DEPARTMENT_HEAD)) {
            return departmentService.getManagedMembersOfDepartmentHead(signedInUser);
        }

        return personService.getActivePersons();
    }
}
