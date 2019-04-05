package org.synyx.urlaubsverwaltung.statistics;

import org.junit.Before;
import org.junit.Test;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.statistics.web.ApplicationForLeaveStatisticsServiceImpl;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ApplicationForLeaveStatisticsServiceImplTest {

    private PersonService personService;
    private DepartmentService departmentService;
    private ApplicationForLeaveStatisticsBuilder applicationForLeaveStatisticsBuilder;
    private ApplicationForLeaveStatisticsServiceImpl sut;

    @Before
    public void setUp() {

        personService = mock(PersonService.class);
        departmentService = mock(DepartmentService.class);
        applicationForLeaveStatisticsBuilder = mock(ApplicationForLeaveStatisticsBuilder.class);

        sut = new ApplicationForLeaveStatisticsServiceImpl(personService, departmentService, applicationForLeaveStatisticsBuilder);
    }

    @Test
    public void getStatisticsForDepartmentHead() {

        FilterPeriod filterPeriod = new FilterPeriod(java.util.Optional.ofNullable("01.01.2018"), java.util.Optional.ofNullable("31.12.2018"));

        Person person = new Person();
        person.setPermissions(Collections.singletonList(Role.DEPARTMENT_HEAD));
        when(personService.getSignedInUser()).thenReturn(person);

        Person departmentMember = new Person();
        when(departmentService.getManagedMembersOfDepartmentHead(person)).thenReturn(Collections.singletonList(departmentMember));

        when(applicationForLeaveStatisticsBuilder.build(person, filterPeriod.getStartDate(), filterPeriod.getEndDate()))
                .thenReturn(mock(ApplicationForLeaveStatistics.class));

        List<ApplicationForLeaveStatistics> statistics = sut.getStatistics(filterPeriod);

        assertThat(statistics.size(), is(1));
    }

    @Test
    public void getStatisticsForOtherThanDepartmentHead() {

        FilterPeriod filterPeriod = new FilterPeriod(java.util.Optional.ofNullable("01.01.2018"), java.util.Optional.ofNullable("31.12.2018"));

        Person person = new Person();
        person.setPermissions(Collections.singletonList(Role.BOSS));
        when(personService.getSignedInUser()).thenReturn(person);

        Person anyPerson = new Person();
        when(personService.getActivePersons()).thenReturn(Collections.singletonList(anyPerson));

        when(applicationForLeaveStatisticsBuilder.build(person, filterPeriod.getStartDate(), filterPeriod.getEndDate()))
                .thenReturn(mock(ApplicationForLeaveStatistics.class));

        List<ApplicationForLeaveStatistics> statistics = sut.getStatistics(filterPeriod);

        assertThat(statistics.size(), is(1));
    }

}
