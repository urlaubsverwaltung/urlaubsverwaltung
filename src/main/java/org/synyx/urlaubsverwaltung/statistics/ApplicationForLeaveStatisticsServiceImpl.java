package org.synyx.urlaubsverwaltung.statistics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;

import java.util.List;
import java.util.stream.Collectors;

import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;

@Service
class ApplicationForLeaveStatisticsServiceImpl implements ApplicationForLeaveStatisticsService {

    private final PersonService personService;
    private final DepartmentService departmentService;
    private final ApplicationForLeaveStatisticsBuilder applicationForLeaveStatisticsBuilder;

    @Autowired
    public ApplicationForLeaveStatisticsServiceImpl(PersonService personService, DepartmentService departmentService,
                                                    ApplicationForLeaveStatisticsBuilder applicationForLeaveStatisticsBuilder) {
        this.personService = personService;
        this.departmentService = departmentService;
        this.applicationForLeaveStatisticsBuilder = applicationForLeaveStatisticsBuilder;
    }

    @Override
    public List<ApplicationForLeaveStatistics> getStatistics(FilterPeriod period) {
        return getRelevantPersons().stream()
            .map(person -> applicationForLeaveStatisticsBuilder.build(person, period.getStartDate(), period.getEndDate()))
            .collect(Collectors.toList());
    }

    private List<Person> getRelevantPersons() {

        final Person signedInUser = personService.getSignedInUser();
        if (signedInUser.hasRole(DEPARTMENT_HEAD)) {
            return departmentService.getManagedMembersOfDepartmentHead(signedInUser);
        }

        return personService.getActivePersons();
    }
}
