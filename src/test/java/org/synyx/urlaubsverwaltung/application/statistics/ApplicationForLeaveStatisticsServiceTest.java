package org.synyx.urlaubsverwaltung.application.statistics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeService;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@ExtendWith(MockitoExtension.class)
class ApplicationForLeaveStatisticsServiceTest {

    private ApplicationForLeaveStatisticsService sut;

    @Mock
    private PersonService personService;
    @Mock
    private DepartmentService departmentService;
    @Mock
    private ApplicationForLeaveStatisticsBuilder applicationForLeaveStatisticsBuilder;
    @Mock
    private VacationTypeService vacationTypeService;

    @BeforeEach
    void setUp() {
        sut = new ApplicationForLeaveStatisticsService(personService, departmentService, applicationForLeaveStatisticsBuilder, vacationTypeService);
    }

    @Test
    void getStatisticsForUser() {

        final LocalDate startDate = LocalDate.parse("2018-01-01");
        final LocalDate endDate = LocalDate.parse("2018-12-31");
        final FilterPeriod filterPeriod = new FilterPeriod(startDate, endDate);

        final Person office = new Person();
        office.setPermissions(List.of(USER));
        when(personService.getSignedInUser()).thenReturn(office);

        final VacationType vacationType = new VacationType(1, true, HOLIDAY, "message_key", true);
        when(vacationTypeService.getActiveVacationTypes()).thenReturn(List.of(vacationType));

        verifyNoMoreInteractions(personService);
        verifyNoInteractions(departmentService);
        verifyNoInteractions(applicationForLeaveStatisticsBuilder);

        final List<ApplicationForLeaveStatistics> statistics = sut.getStatistics(filterPeriod);
        assertThat(statistics).isEmpty();
    }

    @Test
    void getStatisticsForOffice() {

        final LocalDate startDate = LocalDate.parse("2018-01-01");
        final LocalDate endDate = LocalDate.parse("2018-12-31");
        final FilterPeriod filterPeriod = new FilterPeriod(startDate, endDate);

        final Person office = new Person();
        office.setPermissions(List.of(USER, OFFICE));
        when(personService.getSignedInUser()).thenReturn(office);

        final Person anyPerson = new Person();
        anyPerson.setId(1);
        anyPerson.setPermissions(List.of(USER));
        when(personService.getActivePersons()).thenReturn(List.of(anyPerson));

        final VacationType vacationType = new VacationType(1, true, HOLIDAY, "message_key", true);
        final List<VacationType> vacationTypes = List.of(vacationType);
        when(vacationTypeService.getActiveVacationTypes()).thenReturn(vacationTypes);

        when(applicationForLeaveStatisticsBuilder.build(anyPerson, startDate, endDate, vacationTypes)).thenReturn(new ApplicationForLeaveStatistics(anyPerson));

        final List<ApplicationForLeaveStatistics> statistics = sut.getStatistics(filterPeriod);
        assertThat(statistics).hasSize(1);
        assertThat(statistics.get(0).getPerson()).isEqualTo(anyPerson);
    }

    @Test
    void getStatisticsForBoss() {

        final LocalDate startDate = LocalDate.parse("2018-01-01");
        final LocalDate endDate = LocalDate.parse("2018-12-31");
        final FilterPeriod filterPeriod = new FilterPeriod(startDate, endDate);

        final Person office = new Person();
        office.setPermissions(List.of(USER, BOSS));
        when(personService.getSignedInUser()).thenReturn(office);

        final Person anyPerson = new Person();
        anyPerson.setId(1);
        anyPerson.setPermissions(List.of(USER));
        when(personService.getActivePersons()).thenReturn(List.of(anyPerson));

        final VacationType vacationType = new VacationType(1, true, HOLIDAY, "message_key", true);
        final List<VacationType> vacationTypes = List.of(vacationType);
        when(vacationTypeService.getActiveVacationTypes()).thenReturn(vacationTypes);

        when(applicationForLeaveStatisticsBuilder.build(anyPerson, startDate, endDate, vacationTypes)).thenReturn(new ApplicationForLeaveStatistics(anyPerson));

        final List<ApplicationForLeaveStatistics> statistics = sut.getStatistics(filterPeriod);
        assertThat(statistics).hasSize(1);
        assertThat(statistics.get(0).getPerson()).isEqualTo(anyPerson);
    }

    @Test
    void getStatisticsForDepartmentHead() {

        final LocalDate startDate = LocalDate.parse("2018-01-01");
        final LocalDate endDate = LocalDate.parse("2018-12-31");
        final FilterPeriod filterPeriod = new FilterPeriod(startDate, endDate);

        final Person departmentHead = new Person();
        departmentHead.setPermissions(List.of(USER, DEPARTMENT_HEAD));
        when(personService.getSignedInUser()).thenReturn(departmentHead);

        final Person departmentMember = new Person();
        departmentMember.setId(1);
        departmentMember.setPermissions(List.of(USER));
        when(departmentService.getMembersForDepartmentHead(departmentHead)).thenReturn(List.of(departmentMember));

        final VacationType vacationType = new VacationType(1, true, HOLIDAY, "message_key", true);
        final List<VacationType> vacationTypes = List.of(vacationType);
        when(vacationTypeService.getActiveVacationTypes()).thenReturn(vacationTypes);

        when(applicationForLeaveStatisticsBuilder.build(departmentMember, startDate, endDate, vacationTypes)).thenReturn(new ApplicationForLeaveStatistics(departmentMember));

        final List<ApplicationForLeaveStatistics> statistics = sut.getStatistics(filterPeriod);
        assertThat(statistics).hasSize(1);
        assertThat(statistics.get(0).getPerson()).isEqualTo(departmentMember);
    }

    @Test
    void getStatisticsForDepartmentHeadIgnoresInactivePersons() {

        final LocalDate startDate = LocalDate.parse("2018-01-01");
        final LocalDate endDate = LocalDate.parse("2018-12-31");
        final FilterPeriod filterPeriod = new FilterPeriod(startDate, endDate);

        final Person departmentHead = new Person();
        departmentHead.setPermissions(List.of(USER, DEPARTMENT_HEAD));
        when(personService.getSignedInUser()).thenReturn(departmentHead);

        final Person inactiveDepartmentMember = new Person();
        inactiveDepartmentMember.setId(1);
        inactiveDepartmentMember.setPermissions(List.of(USER, INACTIVE));

        when(departmentService.getMembersForDepartmentHead(departmentHead)).thenReturn(List.of(inactiveDepartmentMember));

        final VacationType vacationType = new VacationType(1, true, HOLIDAY, "message_key", true);
        final List<VacationType> vacationTypes = List.of(vacationType);
        when(vacationTypeService.getActiveVacationTypes()).thenReturn(vacationTypes);

        final List<ApplicationForLeaveStatistics> statistics = sut.getStatistics(filterPeriod);
        assertThat(statistics).isEmpty();

        verifyNoInteractions(applicationForLeaveStatisticsBuilder);
    }

    @Test
    void getStatisticsForSecondStageAuthority() {

        final LocalDate startDate = LocalDate.parse("2018-01-01");
        final LocalDate endDate = LocalDate.parse("2018-12-31");
        final FilterPeriod filterPeriod = new FilterPeriod(startDate, endDate);

        final Person secondStageAuthority = new Person();
        secondStageAuthority.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY));
        when(personService.getSignedInUser()).thenReturn(secondStageAuthority);

        final Person departmentMember = new Person();
        departmentMember.setId(1);
        departmentMember.setPermissions(List.of(USER));

        when(departmentService.getMembersForSecondStageAuthority(secondStageAuthority)).thenReturn(List.of(departmentMember));

        final VacationType vacationType = new VacationType(1, true, HOLIDAY, "message_key", true);
        final List<VacationType> vacationTypes = List.of(vacationType);
        when(vacationTypeService.getActiveVacationTypes()).thenReturn(vacationTypes);

        when(applicationForLeaveStatisticsBuilder.build(departmentMember, startDate, endDate, vacationTypes)).thenReturn(new ApplicationForLeaveStatistics(departmentMember));

        final List<ApplicationForLeaveStatistics> statistics = sut.getStatistics(filterPeriod);
        assertThat(statistics).hasSize(1);
        assertThat(statistics.get(0).getPerson()).isEqualTo(departmentMember);
    }

    @Test
    void getStatisticsForSecondStageAuthorityIgnoresInactivePersons() {

        final LocalDate startDate = LocalDate.parse("2018-01-01");
        final LocalDate endDate = LocalDate.parse("2018-12-31");
        final FilterPeriod filterPeriod = new FilterPeriod(startDate, endDate);

        final Person secondStageAuthority = new Person();
        secondStageAuthority.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY));
        when(personService.getSignedInUser()).thenReturn(secondStageAuthority);

        final Person inactiveDepartmentMember = new Person();
        inactiveDepartmentMember.setId(1);
        inactiveDepartmentMember.setPermissions(List.of(USER, INACTIVE));
        when(departmentService.getMembersForSecondStageAuthority(secondStageAuthority)).thenReturn(List.of(inactiveDepartmentMember));

        final VacationType vacationType = new VacationType(1, true, HOLIDAY, "message_key", true);
        final List<VacationType> vacationTypes = List.of(vacationType);
        when(vacationTypeService.getActiveVacationTypes()).thenReturn(vacationTypes);

        final List<ApplicationForLeaveStatistics> statistics = sut.getStatistics(filterPeriod);
        assertThat(statistics).isEmpty();

        verifyNoInteractions(applicationForLeaveStatisticsBuilder);
    }

    @Test
    void getStatisticsForDepartmentHeadAndSecondStageAuthority() {

        final LocalDate startDate = LocalDate.parse("2018-01-01");
        final LocalDate endDate = LocalDate.parse("2018-12-31");
        final FilterPeriod filterPeriod = new FilterPeriod(startDate, endDate);

        final Person departmentHeadAndSecondStageAuthority = new Person();
        departmentHeadAndSecondStageAuthority.setPermissions(List.of(USER, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY));
        when(personService.getSignedInUser()).thenReturn(departmentHeadAndSecondStageAuthority);

        final Person departmentMember = new Person();
        departmentMember.setId(1);
        departmentMember.setPermissions(List.of(USER));

        final Person departmentMemberTwo = new Person();
        departmentMemberTwo.setId(2);
        departmentMemberTwo.setPermissions(List.of(USER));

        final Person departmentMemberThree = new Person();
        departmentMemberThree.setId(3);
        departmentMemberThree.setPermissions(List.of(USER));

        when(departmentService.getMembersForDepartmentHead(departmentHeadAndSecondStageAuthority)).thenReturn(List.of(departmentMember));
        when(departmentService.getMembersForSecondStageAuthority(departmentHeadAndSecondStageAuthority)).thenReturn(List.of(departmentMemberTwo, departmentMemberThree));

        final VacationType vacationType = new VacationType(1, true, HOLIDAY, "message_key", true);
        final List<VacationType> vacationTypes = List.of(vacationType);
        when(vacationTypeService.getActiveVacationTypes()).thenReturn(vacationTypes);

        when(applicationForLeaveStatisticsBuilder.build(departmentMember, startDate, endDate, vacationTypes)).thenReturn(new ApplicationForLeaveStatistics(departmentMember));
        when(applicationForLeaveStatisticsBuilder.build(departmentMemberTwo, startDate, endDate, vacationTypes)).thenReturn(new ApplicationForLeaveStatistics(departmentMemberTwo));
        when(applicationForLeaveStatisticsBuilder.build(departmentMemberThree, startDate, endDate, vacationTypes)).thenReturn(new ApplicationForLeaveStatistics(departmentMemberThree));

        final List<ApplicationForLeaveStatistics> statistics = sut.getStatistics(filterPeriod);
        assertThat(statistics).hasSize(3);
        assertThat(statistics.get(0).getPerson()).isEqualTo(departmentMember);
        assertThat(statistics.get(1).getPerson()).isEqualTo(departmentMemberTwo);
        assertThat(statistics.get(2).getPerson()).isEqualTo(departmentMemberThree);
    }

    @Test
    void getStatisticsForDepartmentHeadAndSecondStageAuthorityReturnsDistinctPersons() {

        final LocalDate startDate = LocalDate.parse("2018-01-01");
        final LocalDate endDate = LocalDate.parse("2018-12-31");
        final FilterPeriod filterPeriod = new FilterPeriod(startDate, endDate);

        final Person departmentHeadAndSecondStageAuthority = new Person();
        departmentHeadAndSecondStageAuthority.setPermissions(List.of(USER, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY));
        when(personService.getSignedInUser()).thenReturn(departmentHeadAndSecondStageAuthority);

        final Person departmentMember = new Person();
        departmentMember.setId(1);
        departmentMember.setPermissions(List.of(USER));

        final Person departmentMemberTwo = new Person();
        departmentMemberTwo.setId(2);
        departmentMemberTwo.setPermissions(List.of(USER));

        when(departmentService.getMembersForDepartmentHead(departmentHeadAndSecondStageAuthority)).thenReturn(List.of(departmentMember));
        when(departmentService.getMembersForSecondStageAuthority(departmentHeadAndSecondStageAuthority)).thenReturn(List.of(departmentMember, departmentMemberTwo));

        final VacationType vacationType = new VacationType(1, true, HOLIDAY, "message_key", true);
        final List<VacationType> vacationTypes = List.of(vacationType);
        when(vacationTypeService.getActiveVacationTypes()).thenReturn(vacationTypes);

        when(applicationForLeaveStatisticsBuilder.build(departmentMember, startDate, endDate, vacationTypes)).thenReturn(new ApplicationForLeaveStatistics(departmentMember));
        when(applicationForLeaveStatisticsBuilder.build(departmentMemberTwo, startDate, endDate, vacationTypes)).thenReturn(new ApplicationForLeaveStatistics(departmentMemberTwo));

        final List<ApplicationForLeaveStatistics> statistics = sut.getStatistics(filterPeriod);
        assertThat(statistics).hasSize(2);
        assertThat(statistics.get(0).getPerson()).isEqualTo(departmentMember);
        assertThat(statistics.get(1).getPerson()).isEqualTo(departmentMemberTwo);
    }

    @Test
    void getStatisticsForDepartmentHeadAndSecondStageAuthorityIgnoresInactivePersons() {

        final LocalDate startDate = LocalDate.parse("2018-01-01");
        final LocalDate endDate = LocalDate.parse("2018-12-31");
        final FilterPeriod filterPeriod = new FilterPeriod(startDate, endDate);

        final Person departmentHeadAndSecondStageAuthority = new Person();
        departmentHeadAndSecondStageAuthority.setPermissions(List.of(USER, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY));
        when(personService.getSignedInUser()).thenReturn(departmentHeadAndSecondStageAuthority);

        final Person inactiveDepartmentMember = new Person();
        inactiveDepartmentMember.setId(1);
        inactiveDepartmentMember.setPermissions(List.of(USER));
        inactiveDepartmentMember.setPermissions(List.of(USER, INACTIVE));

        final Person inactiveDepartmentMemberTwo = new Person();
        inactiveDepartmentMemberTwo.setId(2);
        inactiveDepartmentMemberTwo.setPermissions(List.of(USER));
        inactiveDepartmentMemberTwo.setPermissions(List.of(USER, INACTIVE));

        when(departmentService.getMembersForSecondStageAuthority(departmentHeadAndSecondStageAuthority)).thenReturn(List.of(inactiveDepartmentMember, inactiveDepartmentMemberTwo));

        final VacationType vacationType = new VacationType(1, true, HOLIDAY, "message_key", true);
        final List<VacationType> vacationTypes = List.of(vacationType);
        when(vacationTypeService.getActiveVacationTypes()).thenReturn(vacationTypes);

        final List<ApplicationForLeaveStatistics> statistics = sut.getStatistics(filterPeriod);
        assertThat(statistics).isEmpty();

        verifyNoInteractions(applicationForLeaveStatisticsBuilder);
    }
}
