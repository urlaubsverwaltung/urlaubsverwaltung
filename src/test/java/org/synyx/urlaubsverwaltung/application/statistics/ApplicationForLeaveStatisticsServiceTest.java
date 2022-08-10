package org.synyx.urlaubsverwaltung.application.statistics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeService;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedata;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedataService;
import org.synyx.urlaubsverwaltung.search.PageableSearchQuery;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory.HOLIDAY;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor.YELLOW;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@ExtendWith(MockitoExtension.class)
class ApplicationForLeaveStatisticsServiceTest {

    private ApplicationForLeaveStatisticsService sut;

    @Mock
    private PersonService personService;
    @Mock
    private PersonBasedataService personBasedataService;
    @Mock
    private DepartmentService departmentService;
    @Mock
    private ApplicationForLeaveStatisticsBuilder applicationForLeaveStatisticsBuilder;
    @Mock
    private VacationTypeService vacationTypeService;

    @BeforeEach
    void setUp() {
        sut = new ApplicationForLeaveStatisticsService(personService, personBasedataService, departmentService, applicationForLeaveStatisticsBuilder, vacationTypeService);
    }

    @Test
    void getStatisticsForUser() {

        final LocalDate startDate = LocalDate.parse("2018-01-01");
        final LocalDate endDate = LocalDate.parse("2018-12-31");
        final FilterPeriod filterPeriod = new FilterPeriod(startDate, endDate);

        final Person user = new Person();
        user.setId(1);
        user.setPermissions(List.of(USER));

        final PageRequest pageRequest = PageRequest.ofSize(10);
        final PageableSearchQuery searchQuery = new PageableSearchQuery(Pageable.unpaged(), "");

        when(departmentService.getManagedMembersOfPerson(user, searchQuery)).thenReturn(new PageImpl<>(List.of()));

        final VacationType vacationType = new VacationType(1, true, HOLIDAY, "message_key", true, YELLOW, false);
        when(vacationTypeService.getActiveVacationTypes()).thenReturn(List.of(vacationType));

        verifyNoMoreInteractions(personService);
        verifyNoInteractions(personBasedataService);
        verifyNoInteractions(departmentService);
        verifyNoInteractions(applicationForLeaveStatisticsBuilder);

        final Page<ApplicationForLeaveStatistics> statisticsPage = sut.getStatistics(user, filterPeriod, pageRequest);
        assertThat(statisticsPage.getContent()).isEmpty();
    }

    @Test
    void getStatisticsForOffice() {

        final LocalDate startDate = LocalDate.parse("2018-01-01");
        final LocalDate endDate = LocalDate.parse("2018-12-31");
        final FilterPeriod filterPeriod = new FilterPeriod(startDate, endDate);

        final Person office = new Person();
        office.setId(1);
        office.setPermissions(List.of(USER, OFFICE));

        final Person anyPerson = new Person();
        anyPerson.setId(2);
        anyPerson.setPermissions(List.of(USER));

        when(personService.getActivePersons()).thenReturn(List.of(anyPerson));

        final VacationType vacationType = new VacationType(1, true, HOLIDAY, "message_key", true, YELLOW, false);
        final List<VacationType> vacationTypes = List.of(vacationType);
        when(vacationTypeService.getActiveVacationTypes()).thenReturn(vacationTypes);

        when(applicationForLeaveStatisticsBuilder.build(anyPerson, startDate, endDate, vacationTypes)).thenReturn(new ApplicationForLeaveStatistics(anyPerson));

        final Page<ApplicationForLeaveStatistics> statisticsPage = sut.getStatistics(office, filterPeriod, PageRequest.ofSize(10));
        assertThat(statisticsPage.getContent()).hasSize(1);
        assertThat(statisticsPage.getContent().get(0).getPerson()).isEqualTo(anyPerson);
    }

    @Test
    void getStatisticsForOfficeWithPersonWithPersonBasedata() {

        final LocalDate startDate = LocalDate.parse("2018-01-01");
        final LocalDate endDate = LocalDate.parse("2018-12-31");
        final FilterPeriod filterPeriod = new FilterPeriod(startDate, endDate);

        final Person office = new Person();
        office.setId(1);
        office.setPermissions(List.of(USER, OFFICE));

        final Person person = new Person();
        person.setId(2);
        person.setPermissions(List.of(USER));

        when(personService.getActivePersons()).thenReturn(List.of(person));

        final PersonBasedata personBasedata = new PersonBasedata(new PersonId(2), "42", "additional information");
        when(personBasedataService.getBasedataByPersonId(2)).thenReturn(Optional.of(personBasedata));

        final VacationType vacationType = new VacationType(1, true, HOLIDAY, "message_key", true, YELLOW, false);
        final List<VacationType> vacationTypes = List.of(vacationType);
        when(vacationTypeService.getActiveVacationTypes()).thenReturn(vacationTypes);

        final ApplicationForLeaveStatistics applicationForLeaveStatistics = new ApplicationForLeaveStatistics(person);
        applicationForLeaveStatistics.setPersonBasedata(personBasedata);

        when(applicationForLeaveStatisticsBuilder.build(person, personBasedata, startDate, endDate, vacationTypes)).thenReturn(applicationForLeaveStatistics);

        final Page<ApplicationForLeaveStatistics> statisticsPage = sut.getStatistics(office, filterPeriod, PageRequest.ofSize(10));
        assertThat(statisticsPage.getContent()).hasSize(1);

        final ApplicationForLeaveStatistics applicationForLeaveStatisticsOfPerson = statisticsPage.getContent().get(0);
        assertThat(applicationForLeaveStatisticsOfPerson.getPerson()).isEqualTo(person);
        assertThat(applicationForLeaveStatisticsOfPerson.getPersonBasedata()).hasValue(personBasedata);
    }

    @Test
    void getStatisticsForBoss() {

        final LocalDate startDate = LocalDate.parse("2018-01-01");
        final LocalDate endDate = LocalDate.parse("2018-12-31");
        final FilterPeriod filterPeriod = new FilterPeriod(startDate, endDate);

        final Person office = new Person();
        office.setId(1);
        office.setPermissions(List.of(USER, BOSS));

        final Person anyPerson = new Person();
        anyPerson.setId(2);
        anyPerson.setPermissions(List.of(USER));

        when(personService.getActivePersons()).thenReturn(List.of(anyPerson));

        final VacationType vacationType = new VacationType(1, true, HOLIDAY, "message_key", true, YELLOW, false);
        final List<VacationType> vacationTypes = List.of(vacationType);
        when(vacationTypeService.getActiveVacationTypes()).thenReturn(vacationTypes);

        when(applicationForLeaveStatisticsBuilder.build(anyPerson, startDate, endDate, vacationTypes)).thenReturn(new ApplicationForLeaveStatistics(anyPerson));

        final Page<ApplicationForLeaveStatistics> statisticsPage = sut.getStatistics(office, filterPeriod, PageRequest.ofSize(10));
        assertThat(statisticsPage.getContent()).hasSize(1);
        assertThat(statisticsPage.getContent().get(0).getPerson()).isEqualTo(anyPerson);
    }

    @Test
    void getStatisticsForDepartmentHead() {

        final LocalDate startDate = LocalDate.parse("2018-01-01");
        final LocalDate endDate = LocalDate.parse("2018-12-31");
        final FilterPeriod filterPeriod = new FilterPeriod(startDate, endDate);

        final Person departmentHead = new Person();
        departmentHead.setId(1);
        departmentHead.setPermissions(List.of(USER, DEPARTMENT_HEAD));

        final Person departmentMember = new Person();
        departmentMember.setId(2);
        departmentMember.setPermissions(List.of(USER));

        final PageRequest pageRequest = PageRequest.ofSize(10);
        final PageableSearchQuery searchQuery = new PageableSearchQuery(Pageable.unpaged(), "");

        when(departmentService.getManagedMembersOfPerson(departmentHead, searchQuery)).thenReturn(new PageImpl<>(List.of(departmentMember)));

        final VacationType vacationType = new VacationType(1, true, HOLIDAY, "message_key", true, YELLOW, false);
        final List<VacationType> vacationTypes = List.of(vacationType);
        when(vacationTypeService.getActiveVacationTypes()).thenReturn(vacationTypes);

        when(applicationForLeaveStatisticsBuilder.build(departmentMember, startDate, endDate, vacationTypes)).thenReturn(new ApplicationForLeaveStatistics(departmentMember));

        final Page<ApplicationForLeaveStatistics> statisticsPage = sut.getStatistics(departmentHead, filterPeriod, pageRequest);
        assertThat(statisticsPage.getContent()).hasSize(1);
        assertThat(statisticsPage.getContent().get(0).getPerson()).isEqualTo(departmentMember);
    }

    @Test
    void getStatisticsForSecondStageAuthority() {
        final LocalDate startDate = LocalDate.parse("2018-01-01");
        final LocalDate endDate = LocalDate.parse("2018-12-31");
        final FilterPeriod filterPeriod = new FilterPeriod(startDate, endDate);

        final Person secondStageAuthority = new Person();
        secondStageAuthority.setId(1);
        secondStageAuthority.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY));

        final Person departmentMember = new Person();
        departmentMember.setId(2);
        departmentMember.setPermissions(List.of(USER));

        final PageRequest pageRequest = PageRequest.ofSize(10);
        final PageableSearchQuery searchQuery = new PageableSearchQuery(Pageable.unpaged(), "");
        when(departmentService.getManagedMembersOfPerson(secondStageAuthority, searchQuery))
            .thenReturn(new PageImpl<>(List.of(departmentMember)));

        final VacationType vacationType = new VacationType(1, true, HOLIDAY, "message_key", true, YELLOW, false);
        final List<VacationType> vacationTypes = List.of(vacationType);
        when(vacationTypeService.getActiveVacationTypes()).thenReturn(vacationTypes);

        when(applicationForLeaveStatisticsBuilder.build(departmentMember, startDate, endDate, vacationTypes)).thenReturn(new ApplicationForLeaveStatistics(departmentMember));

        final Page<ApplicationForLeaveStatistics> statisticsPage = sut.getStatistics(secondStageAuthority, filterPeriod, pageRequest);
        assertThat(statisticsPage.getContent()).hasSize(1);
        assertThat(statisticsPage.getContent().get(0).getPerson()).isEqualTo(departmentMember);
    }

    @Test
    void getStatisticsForDepartmentHeadAndSecondStageAuthority() {

        final LocalDate startDate = LocalDate.parse("2018-01-01");
        final LocalDate endDate = LocalDate.parse("2018-12-31");
        final FilterPeriod filterPeriod = new FilterPeriod(startDate, endDate);

        final Person departmentHeadAndSecondStageAuthority = new Person();
        departmentHeadAndSecondStageAuthority.setId(1);
        departmentHeadAndSecondStageAuthority.setPermissions(List.of(USER, DEPARTMENT_HEAD, SECOND_STAGE_AUTHORITY));

        final Person departmentMember = new Person();
        departmentMember.setId(2);
        departmentMember.setPermissions(List.of(USER));

        final Person departmentMemberTwo = new Person();
        departmentMemberTwo.setId(3);
        departmentMemberTwo.setPermissions(List.of(USER));

        final Person departmentMemberThree = new Person();
        departmentMemberThree.setId(4);
        departmentMemberThree.setPermissions(List.of(USER));

        final PageRequest pageRequest = PageRequest.ofSize(10);
        final PageableSearchQuery searchQuery = new PageableSearchQuery(Pageable.unpaged(), "");

        when(departmentService.getManagedMembersOfPerson(departmentHeadAndSecondStageAuthority, searchQuery))
            .thenReturn(new PageImpl<>(List.of(departmentMember, departmentMemberTwo, departmentMemberThree)));

        final VacationType vacationType = new VacationType(1, true, HOLIDAY, "message_key", true, YELLOW, false);
        final List<VacationType> vacationTypes = List.of(vacationType);
        when(vacationTypeService.getActiveVacationTypes()).thenReturn(vacationTypes);

        when(applicationForLeaveStatisticsBuilder.build(departmentMember, startDate, endDate, vacationTypes)).thenReturn(new ApplicationForLeaveStatistics(departmentMember));
        when(applicationForLeaveStatisticsBuilder.build(departmentMemberTwo, startDate, endDate, vacationTypes)).thenReturn(new ApplicationForLeaveStatistics(departmentMemberTwo));
        when(applicationForLeaveStatisticsBuilder.build(departmentMemberThree, startDate, endDate, vacationTypes)).thenReturn(new ApplicationForLeaveStatistics(departmentMemberThree));

        final Page<ApplicationForLeaveStatistics> statisticsPage = sut.getStatistics(departmentHeadAndSecondStageAuthority, filterPeriod, pageRequest);
        assertThat(statisticsPage.getContent()).hasSize(3);
        assertThat(statisticsPage.getContent().get(0).getPerson()).isEqualTo(departmentMember);
        assertThat(statisticsPage.getContent().get(1).getPerson()).isEqualTo(departmentMemberTwo);
        assertThat(statisticsPage.getContent().get(2).getPerson()).isEqualTo(departmentMemberThree);
    }

    @Test
    void getStatisticsForDepartmentHeadAndSecondStageAuthorityReturnsDistinctPersons() {

        final LocalDate startDate = LocalDate.parse("2018-01-01");
        final LocalDate endDate = LocalDate.parse("2018-12-31");
        final FilterPeriod filterPeriod = new FilterPeriod(startDate, endDate);

        final Person departmentHeadAndSecondStageAuthority = new Person();
        departmentHeadAndSecondStageAuthority.setId(1);

        final Person departmentMember = new Person();
        departmentMember.setId(2);
        departmentMember.setPermissions(List.of(USER));

        final Person departmentMemberTwo = new Person();
        departmentMemberTwo.setId(3);
        departmentMemberTwo.setPermissions(List.of(USER));

        final PageableSearchQuery searchQuery = new PageableSearchQuery(Pageable.unpaged(), "");

        when(departmentService.getManagedMembersOfPerson(departmentHeadAndSecondStageAuthority, searchQuery))
            .thenReturn(new PageImpl<>(List.of(departmentMember, departmentMemberTwo)));

        final VacationType vacationType = new VacationType(1, true, HOLIDAY, "message_key", true, YELLOW, false);
        final List<VacationType> vacationTypes = List.of(vacationType);
        when(vacationTypeService.getActiveVacationTypes()).thenReturn(vacationTypes);

        when(applicationForLeaveStatisticsBuilder.build(departmentMember, startDate, endDate, vacationTypes)).thenReturn(new ApplicationForLeaveStatistics(departmentMember));
        when(applicationForLeaveStatisticsBuilder.build(departmentMemberTwo, startDate, endDate, vacationTypes)).thenReturn(new ApplicationForLeaveStatistics(departmentMemberTwo));


        final Page<ApplicationForLeaveStatistics> statisticsPage = sut.getStatistics(departmentHeadAndSecondStageAuthority, filterPeriod, PageRequest.ofSize(10));
        assertThat(statisticsPage.getContent()).hasSize(2);
        assertThat(statisticsPage.getContent().get(0).getPerson()).isEqualTo(departmentMember);
        assertThat(statisticsPage.getContent().get(1).getPerson()).isEqualTo(departmentMemberTwo);
    }
}
