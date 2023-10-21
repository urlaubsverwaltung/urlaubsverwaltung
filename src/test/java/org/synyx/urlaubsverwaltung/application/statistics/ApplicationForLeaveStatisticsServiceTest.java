package org.synyx.urlaubsverwaltung.application.statistics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.synyx.urlaubsverwaltung.application.vacationtype.ProvidedVacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeService;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedata;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedataService;
import org.synyx.urlaubsverwaltung.search.PageableSearchQuery;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
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

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"BOSS", "OFFICE"})
    void getStatisticsForUserWithRole(Role role) {

        final LocalDate startDate = LocalDate.parse("2018-01-01");
        final LocalDate endDate = LocalDate.parse("2018-12-31");
        final FilterPeriod filterPeriod = new FilterPeriod(startDate, endDate);

        final Person personsWithRole = new Person();
        personsWithRole.setId(1L);
        personsWithRole.setPermissions(List.of(USER, role));

        final Person anyPerson = new Person();
        anyPerson.setId(2L);
        anyPerson.setPermissions(List.of(USER));

        final PageRequest personPageRequest = PageRequest.of(0, 10, Sort.Direction.ASC, "firstName");
        final PageableSearchQuery personSearchQuery = new PageableSearchQuery(personPageRequest, "");

        when(personService.getActivePersons(personSearchQuery)).thenReturn(new PageImpl<>(List.of(anyPerson)));

        final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource()).build();
//        final VacationType<?> vacationType = new VacationType(1L, true, HOLIDAY, "message_key", true, true, YELLOW, false);
        final List<VacationType<?>> activeVacationTypes = List.of(vacationType);
        when(vacationTypeService.getActiveVacationTypes()).thenReturn(activeVacationTypes);

        when(applicationForLeaveStatisticsBuilder.build(List.of(anyPerson), startDate, endDate, activeVacationTypes))
            .thenReturn(Map.of(anyPerson, new ApplicationForLeaveStatistics(anyPerson, activeVacationTypes)));

        final PageRequest pageRequest = PageRequest.of(0, 10, Sort.Direction.ASC, "person.firstName");
        final PageableSearchQuery statisticsPageableSearchQuery = new PageableSearchQuery(pageRequest, "");

        final Page<ApplicationForLeaveStatistics> statisticsPage = sut.getStatistics(personsWithRole, filterPeriod, statisticsPageableSearchQuery);

        assertThat(statisticsPage.getContent()).hasSize(1);
        assertThat(statisticsPage.getContent().get(0).getPerson()).isEqualTo(anyPerson);
    }

    @Test
    void getStatisticsForOfficeWithPersonWithPersonBasedata() {

        final LocalDate startDate = LocalDate.parse("2018-01-01");
        final LocalDate endDate = LocalDate.parse("2018-12-31");
        final FilterPeriod filterPeriod = new FilterPeriod(startDate, endDate);

        final Person office = new Person();
        office.setId(1L);
        office.setPermissions(List.of(USER, OFFICE));

        final Person person = new Person();
        person.setId(2L);
        person.setPermissions(List.of(USER));

        final PageRequest personPageRequest = PageRequest.of(0, 10, Sort.Direction.ASC, "firstName");
        final PageableSearchQuery personSearchQuery = new PageableSearchQuery(personPageRequest, "");

        when(personService.getActivePersons(personSearchQuery)).thenReturn(new PageImpl<>(List.of(person)));

        final PersonBasedata personBasedata = new PersonBasedata(new PersonId(2L), "42", "additional information");
        when(personBasedataService.getBasedataByPersonId(List.of(2L))).thenReturn(Map.of(new PersonId(2L), personBasedata));

        final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource()).build();
//        final VacationType<?> vacationType = new VacationType(1L, true, HOLIDAY, "message_key", true, true, YELLOW, false);
        final List<VacationType<?>> vacationTypes = List.of(vacationType);
        when(vacationTypeService.getActiveVacationTypes()).thenReturn(vacationTypes);

        final ApplicationForLeaveStatistics applicationForLeaveStatistics = new ApplicationForLeaveStatistics(person, vacationTypes);
        when(applicationForLeaveStatisticsBuilder.build(List.of(person), startDate, endDate, vacationTypes)).thenReturn(Map.of(person, applicationForLeaveStatistics));

        final PageRequest pageRequest = PageRequest.of(0, 10, Sort.Direction.ASC, "person.firstName");
        final PageableSearchQuery statisticsPageableSearchQuery = new PageableSearchQuery(pageRequest, "");

        final Page<ApplicationForLeaveStatistics> statisticsPage = sut.getStatistics(office, filterPeriod, statisticsPageableSearchQuery);
        assertThat(statisticsPage.getContent()).hasSize(1);

        final ApplicationForLeaveStatistics applicationForLeaveStatisticsOfPerson = statisticsPage.getContent().get(0);
        assertThat(applicationForLeaveStatisticsOfPerson.getPerson()).isEqualTo(person);
        assertThat(applicationForLeaveStatisticsOfPerson.getPersonBasedata()).hasValue(personBasedata);
    }

    @Test
    void getStatisticsForNotBossOrOffice() {

        final LocalDate startDate = LocalDate.parse("2018-01-01");
        final LocalDate endDate = LocalDate.parse("2018-12-31");
        final FilterPeriod filterPeriod = new FilterPeriod(startDate, endDate);

        final Person departmentHead = new Person();
        departmentHead.setId(1L);
        departmentHead.setPermissions(List.of(USER));

        final Person departmentMember = new Person();
        departmentMember.setId(2L);
        departmentMember.setPermissions(List.of(USER));

        final PageRequest personPageRequest = PageRequest.of(0, 10, Sort.Direction.ASC, "firstName");
        final PageableSearchQuery personSearchQuery = new PageableSearchQuery(personPageRequest, "");

        when(departmentService.getManagedMembersOfPerson(departmentHead, personSearchQuery)).thenReturn(new PageImpl<>(List.of(departmentMember)));

        final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource()).build();
//        final VacationType<?> vacationType = new VacationType(1L, true, HOLIDAY, "message_key", true, true, YELLOW, false);
        final List<VacationType<?>> vacationTypes = List.of(vacationType);
        when(vacationTypeService.getActiveVacationTypes()).thenReturn(vacationTypes);

        when(applicationForLeaveStatisticsBuilder.build(List.of(departmentMember), startDate, endDate, vacationTypes))
            .thenReturn(Map.of(departmentMember, new ApplicationForLeaveStatistics(departmentMember, vacationTypes)));

        final PageRequest pageRequest = PageRequest.of(0, 10, Sort.Direction.ASC, "person.firstName");
        final PageableSearchQuery statisticsPageableSearchQuery = new PageableSearchQuery(pageRequest, "");

        final Page<ApplicationForLeaveStatistics> statisticsPage = sut.getStatistics(departmentHead, filterPeriod, statisticsPageableSearchQuery);
        assertThat(statisticsPage.getContent()).hasSize(1);
        assertThat(statisticsPage.getContent().get(0).getPerson()).isEqualTo(departmentMember);
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"BOSS", "OFFICE"})
    void ensureAllActivePersonsAreRequestedWhenSortedByStatisticsAttributeByRole(Role role) {

        final LocalDate startDate = LocalDate.parse("2018-01-01");
        final LocalDate endDate = LocalDate.parse("2018-12-31");
        final FilterPeriod filterPeriod = new FilterPeriod(startDate, endDate);

        final Person personWithRole = new Person();
        personWithRole.setId(1L);
        personWithRole.setPermissions(List.of(USER, role));

        final Person anyPerson = new Person();
        anyPerson.setId(2L);
        anyPerson.setPermissions(List.of(USER));

        // unsorted PageRequest for persons expected since sut is called with sorting attribute for statistics.
        final PageRequest activePersonsPageRequest = PageRequest.of(0, 10);
        final PageableSearchQuery activePersonsPageableSearchQuery = new PageableSearchQuery(activePersonsPageRequest, "");
        when(personService.getActivePersons(activePersonsPageableSearchQuery)).thenReturn(new PageImpl<>(List.of(anyPerson)));

        final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource()).build();
//        final VacationType<?> vacationType = new VacationType(1L, true, HOLIDAY, "message_key", true, true, YELLOW, false);
        final List<VacationType<?>> vacationTypes = List.of(vacationType);
        when(vacationTypeService.getActiveVacationTypes()).thenReturn(vacationTypes);

        when(applicationForLeaveStatisticsBuilder.build(List.of(anyPerson), startDate, endDate, vacationTypes))
            .thenReturn(Map.of(anyPerson, new ApplicationForLeaveStatistics(anyPerson, vacationTypes)));

        final PageRequest pageRequest = PageRequest.of(0, 10, Sort.Direction.ASC, "person.firstName", "leftVacationDaysForYear");
        final PageableSearchQuery statisticsPageableSearchQuery = new PageableSearchQuery(pageRequest, "");

        final Page<ApplicationForLeaveStatistics> statisticsPage = sut.getStatistics(personWithRole, filterPeriod, statisticsPageableSearchQuery);

        assertThat(statisticsPage.getContent()).hasSize(1);
        assertThat(statisticsPage.getContent().get(0).getPerson()).isEqualTo(anyPerson);
    }

    @Test
    void ensureAllActivePersonsAreRequestedWhenSortedByStatisticsAttributeByNotBossOrOffice() {

        final LocalDate startDate = LocalDate.parse("2018-01-01");
        final LocalDate endDate = LocalDate.parse("2018-12-31");
        final FilterPeriod filterPeriod = new FilterPeriod(startDate, endDate);

        final Person notBossOrOfficePerson = new Person();
        notBossOrOfficePerson.setId(1L);

        final Person departmentMember = new Person();
        departmentMember.setId(2L);
        departmentMember.setPermissions(List.of(USER));
        departmentMember.setFirstName("Anton");

        final Person departmentMemberTwo = new Person();
        departmentMemberTwo.setId(3L);
        departmentMemberTwo.setPermissions(List.of(USER));
        departmentMemberTwo.setFirstName("Bernd");

        when(departmentService.getManagedMembersOfPerson(notBossOrOfficePerson, new PageableSearchQuery(Pageable.unpaged(), "")))
            .thenReturn(new PageImpl<>(List.of(departmentMember, departmentMemberTwo)));

        final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource()).build();
//        final VacationType<?> vacationType = new VacationType(1L, true, HOLIDAY, "message_key", true, true, YELLOW, false);
        final List<VacationType<?>> vacationTypes = List.of(vacationType);
        when(vacationTypeService.getActiveVacationTypes()).thenReturn(vacationTypes);

        when(applicationForLeaveStatisticsBuilder.build(List.of(departmentMember, departmentMemberTwo), startDate, endDate, vacationTypes))
            .thenReturn(Map.of(
                departmentMember, new ApplicationForLeaveStatistics(departmentMember, vacationTypes),
                departmentMemberTwo, new ApplicationForLeaveStatistics(departmentMemberTwo, vacationTypes)
            ));

        final PageRequest pageRequest = PageRequest.of(0, 10, Sort.Direction.ASC, "person.firstName", "leftVacationDaysForYear");
        final PageableSearchQuery statisticsPageableSearchQuery = new PageableSearchQuery(pageRequest, "");

        final Page<ApplicationForLeaveStatistics> statisticsPage = sut.getStatistics(notBossOrOfficePerson, filterPeriod, statisticsPageableSearchQuery);

        assertThat(statisticsPage.getContent()).hasSize(2);
        assertThat(statisticsPage.getContent().get(0).getPerson()).isEqualTo(departmentMember);
        assertThat(statisticsPage.getContent().get(1).getPerson()).isEqualTo(departmentMemberTwo);
    }
}
