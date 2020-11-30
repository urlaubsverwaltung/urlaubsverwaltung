package org.synyx.urlaubsverwaltung.application.statistics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;

import java.time.LocalDate;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;

@ExtendWith(MockitoExtension.class)
class ApplicationForLeaveStatisticsServiceTest {

    private ApplicationForLeaveStatisticsService sut;

    @Mock
    private PersonService personService;
    @Mock
    private DepartmentService departmentService;
    @Mock
    private ApplicationForLeaveStatisticsBuilder applicationForLeaveStatisticsBuilder;

    @BeforeEach
    void setUp() {
        sut = new ApplicationForLeaveStatisticsService(personService, departmentService, applicationForLeaveStatisticsBuilder);
    }

    @Test
    void getStatisticsForDepartmentHead() {

        final LocalDate startDate = LocalDate.parse("2018-01-01");
        final LocalDate endDate = LocalDate.parse("2018-12-31");
        final FilterPeriod filterPeriod = new FilterPeriod(startDate, endDate);

        final Person person = new Person();
        person.setPermissions(singletonList(DEPARTMENT_HEAD));
        when(personService.getSignedInUser()).thenReturn(person);

        final Person departmentMember = new Person();
        when(departmentService.getManagedMembersOfDepartmentHead(person)).thenReturn(singletonList(departmentMember));

        final List<ApplicationForLeaveStatistics> statistics = sut.getStatistics(filterPeriod);
        assertThat(statistics).hasSize(1);
    }

    @Test
    void getStatisticsForOtherThanDepartmentHead() {

        final LocalDate startDate = LocalDate.parse("2018-01-01");
        final LocalDate endDate = LocalDate.parse("2018-12-31");
        final FilterPeriod filterPeriod = new FilterPeriod(startDate, endDate);

        final Person person = new Person();
        person.setPermissions(singletonList(BOSS));
        when(personService.getSignedInUser()).thenReturn(person);

        final Person anyPerson = new Person();
        when(personService.getActivePersons()).thenReturn(singletonList(anyPerson));

        final List<ApplicationForLeaveStatistics> statistics = sut.getStatistics(filterPeriod);
        assertThat(statistics).hasSize(1);
    }
}
