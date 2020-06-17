package org.synyx.urlaubsverwaltung.statistics.web;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.statistics.ApplicationForLeaveStatistics;
import org.synyx.urlaubsverwaltung.statistics.ApplicationForLeaveStatisticsBuilder;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationForLeaveStatisticsServiceImplTest {

    private ApplicationForLeaveStatisticsServiceImpl sut;

    @Mock
    private PersonService personService;
    @Mock
    private DepartmentService departmentService;
    @Mock
    private ApplicationForLeaveStatisticsBuilder applicationForLeaveStatisticsBuilder;

    @Before
    public void setUp() {
        sut = new ApplicationForLeaveStatisticsServiceImpl(personService, departmentService, applicationForLeaveStatisticsBuilder);
    }

    @Test
    public void getStatisticsForDepartmentHead() {

        FilterPeriod filterPeriod = new FilterPeriod("01.01.2018", "31.12.2018");

        Person person = new Person();
        person.setPermissions(singletonList(DEPARTMENT_HEAD));
        when(personService.getSignedInUser()).thenReturn(person);

        Person departmentMember = new Person();
        when(departmentService.getManagedMembersOfDepartmentHead(person)).thenReturn(singletonList(departmentMember));

        List<ApplicationForLeaveStatistics> statistics = sut.getStatistics(filterPeriod);
        assertThat(statistics).hasSize(1);
    }

    @Test
    public void getStatisticsForOtherThanDepartmentHead() {

        FilterPeriod filterPeriod = new FilterPeriod("01.01.2018", "31.12.2018");

        Person person = new Person();
        person.setPermissions(singletonList(BOSS));
        when(personService.getSignedInUser()).thenReturn(person);

        Person anyPerson = new Person();
        when(personService.getActivePersons()).thenReturn(singletonList(anyPerson));

        List<ApplicationForLeaveStatistics> statistics = sut.getStatistics(filterPeriod);
        assertThat(statistics).hasSize(1);
    }
}
