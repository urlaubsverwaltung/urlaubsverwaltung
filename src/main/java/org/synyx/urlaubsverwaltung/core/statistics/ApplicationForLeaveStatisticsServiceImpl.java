package org.synyx.urlaubsverwaltung.core.statistics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.core.department.DepartmentService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.person.Role;
import org.synyx.urlaubsverwaltung.security.SessionService;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;
import org.synyx.urlaubsverwaltung.web.statistics.ApplicationForLeaveStatistics;
import org.synyx.urlaubsverwaltung.web.statistics.ApplicationForLeaveStatisticsBuilder;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApplicationForLeaveStatisticsServiceImpl implements ApplicationForLeaveStatisticsService {

    @Autowired
    private SessionService sessionService;
    @Autowired
    private PersonService personService;
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private ApplicationForLeaveStatisticsBuilder applicationForLeaveStatisticsBuilder;

    @Override
    public List<ApplicationForLeaveStatistics> getStatistics(FilterPeriod period) {
        List<Person> persons = getRelevantPersons();

        return persons.stream()
                .map(person -> applicationForLeaveStatisticsBuilder.build(person, period.getStartDate(), period.getEndDate()))
                .collect(Collectors.toList());
    }

    private List<Person> getRelevantPersons() {

        Person signedInUser = sessionService.getSignedInUser();

        if (signedInUser.hasRole(Role.DEPARTMENT_HEAD)) {
            return departmentService.getManagedMembersOfDepartmentHead(signedInUser);
        }

        return personService.getActivePersons();
    }
}
