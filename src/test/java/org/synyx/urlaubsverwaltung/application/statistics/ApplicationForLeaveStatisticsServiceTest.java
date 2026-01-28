package org.synyx.urlaubsverwaltung.application.statistics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.synyx.urlaubsverwaltung.application.vacationtype.ProvidedVacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeService;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.PersonPageRequest;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.PersonSortProperty;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedata;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedataService;
import org.synyx.urlaubsverwaltung.web.FilterPeriod;

import java.math.BigDecimal;
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

    @Nested
    class GetStatisticsSortedByPerson {

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

            final PersonPageRequest personPageRequest = PersonPageRequest.of(0, 10, Sort.by("firstName"));
            when(personService.getActivePersons(personPageRequest, "")).thenReturn(new PageImpl<>(List.of(anyPerson)));

            final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource()).build();
            final List<VacationType<?>> activeVacationTypes = List.of(vacationType);
            when(vacationTypeService.getActiveVacationTypes()).thenReturn(activeVacationTypes);

            when(applicationForLeaveStatisticsBuilder.build(List.of(anyPerson), startDate, endDate, activeVacationTypes))
                .thenReturn(Map.of(anyPerson, new ApplicationForLeaveStatistics(anyPerson, activeVacationTypes)));

            final Page<ApplicationForLeaveStatistics> statisticsPage = sut.getStatisticsSortedByPerson(personsWithRole, filterPeriod, personPageRequest, "");

            assertThat(statisticsPage.getContent()).hasSize(1);
            assertThat(statisticsPage.getContent().get(0).getPerson()).isEqualTo(anyPerson);
        }

        @ParameterizedTest
        @EnumSource(value = Role.class, names = {"BOSS", "OFFICE"})
        void getStatisticsSortedByPersonForUserWithRole(Role role) {

            final LocalDate startDate = LocalDate.parse("2018-01-01");
            final LocalDate endDate = LocalDate.parse("2018-12-31");
            final FilterPeriod filterPeriod = new FilterPeriod(startDate, endDate);

            final Person personsWithRole = new Person();
            personsWithRole.setId(1L);
            personsWithRole.setPermissions(List.of(USER, role));

            final Person anyPerson = new Person();
            anyPerson.setId(2L);
            anyPerson.setPermissions(List.of(USER));

            final PersonPageRequest personPageRequest = PersonPageRequest.of(0, 10, Sort.by("firstName"));
            when(personService.getActivePersons(personPageRequest, "")).thenReturn(new PageImpl<>(List.of(anyPerson)));

            final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource()).build();
            final List<VacationType<?>> activeVacationTypes = List.of(vacationType);
            when(vacationTypeService.getActiveVacationTypes()).thenReturn(activeVacationTypes);

            when(applicationForLeaveStatisticsBuilder.build(List.of(anyPerson), startDate, endDate, activeVacationTypes))
                .thenReturn(Map.of(anyPerson, new ApplicationForLeaveStatistics(anyPerson, activeVacationTypes)));

            final Page<ApplicationForLeaveStatistics> statisticsPage = sut.getStatisticsSortedByPerson(personsWithRole, filterPeriod, personPageRequest, "");

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

            final PersonPageRequest personPageRequest = PersonPageRequest.of(0, 10, Sort.by("firstName"));
            when(personService.getActivePersons(personPageRequest, "")).thenReturn(new PageImpl<>(List.of(person)));

            final PersonBasedata personBasedata = new PersonBasedata(new PersonId(2L), "42", "additional information");
            when(personBasedataService.getBasedataByPersonId(List.of(2L))).thenReturn(Map.of(new PersonId(2L), personBasedata));

            final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource()).build();
            final List<VacationType<?>> vacationTypes = List.of(vacationType);
            when(vacationTypeService.getActiveVacationTypes()).thenReturn(vacationTypes);

            final ApplicationForLeaveStatistics applicationForLeaveStatistics = new ApplicationForLeaveStatistics(person, vacationTypes);
            when(applicationForLeaveStatisticsBuilder.build(List.of(person), startDate, endDate, vacationTypes)).thenReturn(Map.of(person, applicationForLeaveStatistics));

            final Page<ApplicationForLeaveStatistics> statisticsPage = sut.getStatisticsSortedByPerson(office, filterPeriod, personPageRequest, "");
            assertThat(statisticsPage.getContent()).hasSize(1);

            final ApplicationForLeaveStatistics applicationForLeaveStatisticsOfPerson = statisticsPage.getContent().get(0);
            assertThat(applicationForLeaveStatisticsOfPerson.getPerson()).isEqualTo(person);
            assertThat(applicationForLeaveStatisticsOfPerson.getPersonBasedata()).hasValue(personBasedata);
        }

        @Test
        void getStatisticsSortedByPersonForOfficeWithPersonWithPersonBasedata() {

            final LocalDate startDate = LocalDate.parse("2018-01-01");
            final LocalDate endDate = LocalDate.parse("2018-12-31");
            final FilterPeriod filterPeriod = new FilterPeriod(startDate, endDate);

            final Person office = new Person();
            office.setId(1L);
            office.setPermissions(List.of(USER, OFFICE));

            final Person person = new Person();
            person.setId(2L);
            person.setPermissions(List.of(USER));

            final PersonPageRequest personPageRequest = PersonPageRequest.of(0, 10, Sort.by(PersonSortProperty.FIRST_NAME_KEY));
            when(personService.getActivePersons(personPageRequest, "")).thenReturn(new PageImpl<>(List.of(person)));

            final PersonBasedata personBasedata = new PersonBasedata(new PersonId(2L), "42", "additional information");
            when(personBasedataService.getBasedataByPersonId(List.of(2L))).thenReturn(Map.of(new PersonId(2L), personBasedata));

            final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource()).build();
            final List<VacationType<?>> vacationTypes = List.of(vacationType);
            when(vacationTypeService.getActiveVacationTypes()).thenReturn(vacationTypes);

            final ApplicationForLeaveStatistics applicationForLeaveStatistics = new ApplicationForLeaveStatistics(person, vacationTypes);
            when(applicationForLeaveStatisticsBuilder.build(List.of(person), startDate, endDate, vacationTypes)).thenReturn(Map.of(person, applicationForLeaveStatistics));

            final Page<ApplicationForLeaveStatistics> statisticsPage = sut.getStatisticsSortedByPerson(office, filterPeriod, personPageRequest, "");
            assertThat(statisticsPage.getContent()).hasSize(1);

            final ApplicationForLeaveStatistics applicationForLeaveStatisticsOfPerson = statisticsPage.getContent().get(0);
            assertThat(applicationForLeaveStatisticsOfPerson.getPerson()).isEqualTo(person);
            assertThat(applicationForLeaveStatisticsOfPerson.getPersonBasedata()).hasValue(personBasedata);
        }

        @ParameterizedTest
        @EnumSource(value = Role.class, names = {"DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY"})
        void getStatisticsForPersonWithDepartmentPrivileges(Role role) {

            final LocalDate startDate = LocalDate.parse("2018-01-01");
            final LocalDate endDate = LocalDate.parse("2018-12-31");
            final FilterPeriod filterPeriod = new FilterPeriod(startDate, endDate);

            final Person departmentHead = new Person();
            departmentHead.setId(1L);
            departmentHead.setPermissions(List.of(USER, role));

            final Person departmentMember = new Person();
            departmentMember.setId(2L);
            departmentMember.setPermissions(List.of(USER));

            final PersonPageRequest personPageRequest = PersonPageRequest.of(0, 10, Sort.by("firstName"));
            when(departmentService.getManagedMembersOfPerson(departmentHead, personPageRequest, ""))
                .thenReturn(new PageImpl<>(List.of(departmentMember)));

            final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource()).build();
            final List<VacationType<?>> vacationTypes = List.of(vacationType);
            when(vacationTypeService.getActiveVacationTypes()).thenReturn(vacationTypes);

            when(applicationForLeaveStatisticsBuilder.build(List.of(departmentMember), startDate, endDate, vacationTypes))
                .thenReturn(Map.of(departmentMember, new ApplicationForLeaveStatistics(departmentMember, vacationTypes)));

            final Page<ApplicationForLeaveStatistics> statisticsPage = sut.getStatisticsSortedByPerson(departmentHead, filterPeriod, personPageRequest, "");
            assertThat(statisticsPage.getContent()).hasSize(1);
            assertThat(statisticsPage.getContent().get(0).getPerson()).isEqualTo(departmentMember);
        }

        @ParameterizedTest
        @EnumSource(value = Role.class, names = {"DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY"})
        void getStatisticsSortedByPersonWithDepartmentPrivileges(Role role) {

            final LocalDate startDate = LocalDate.parse("2018-01-01");
            final LocalDate endDate = LocalDate.parse("2018-12-31");
            final FilterPeriod filterPeriod = new FilterPeriod(startDate, endDate);

            final Person departmentHead = new Person();
            departmentHead.setId(1L);
            departmentHead.setPermissions(List.of(USER, role));

            final Person departmentMember = new Person();
            departmentMember.setId(2L);
            departmentMember.setPermissions(List.of(USER));

            final PersonPageRequest personPageRequest = PersonPageRequest.of(0, 10, Sort.by("firstName"));
            when(departmentService.getManagedMembersOfPerson(departmentHead, personPageRequest, ""))
                .thenReturn(new PageImpl<>(List.of(departmentMember)));

            final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource()).build();
            final List<VacationType<?>> vacationTypes = List.of(vacationType);
            when(vacationTypeService.getActiveVacationTypes()).thenReturn(vacationTypes);

            when(applicationForLeaveStatisticsBuilder.build(List.of(departmentMember), startDate, endDate, vacationTypes))
                .thenReturn(Map.of(departmentMember, new ApplicationForLeaveStatistics(departmentMember, vacationTypes)));

            final Page<ApplicationForLeaveStatistics> statisticsPage = sut.getStatisticsSortedByPerson(departmentHead, filterPeriod, personPageRequest, "");
            assertThat(statisticsPage.getContent()).hasSize(1);
            assertThat(statisticsPage.getContent().get(0).getPerson()).isEqualTo(departmentMember);
        }

        @Test
        void getStatisticsForUser() {

            final LocalDate startDate = LocalDate.parse("2018-01-01");
            final LocalDate endDate = LocalDate.parse("2018-12-31");
            final FilterPeriod filterPeriod = new FilterPeriod(startDate, endDate);

            final Person user = new Person();
            user.setId(1L);
            user.setPermissions(List.of(USER));

            final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource()).build();
            final List<VacationType<?>> vacationTypes = List.of(vacationType);
            when(vacationTypeService.getActiveVacationTypes()).thenReturn(vacationTypes);

            when(applicationForLeaveStatisticsBuilder.build(List.of(user), startDate, endDate, vacationTypes))
                .thenReturn(Map.of(user, new ApplicationForLeaveStatistics(user, vacationTypes)));

            final PersonPageRequest pageRequest = PersonPageRequest.of(0, 10, Sort.by("firstName"));
            final Page<ApplicationForLeaveStatistics> statisticsPage = sut.getStatisticsSortedByPerson(user, filterPeriod, pageRequest, "");
            assertThat(statisticsPage.getContent()).hasSize(1);
            assertThat(statisticsPage.getContent().get(0).getPerson()).isEqualTo(user);
        }
    }

    @Nested
    class GetStatisticsSortedByStatistics {

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

            // TODO this is actually wrong! the sut search sorts by statistics, persons must be fetched to the absence entity result set
            when(personService.getActivePersons(PersonPageRequest.of(0, 10), ""))
                .thenReturn(new PageImpl<>(List.of(anyPerson)));

            final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource()).build();
            final List<VacationType<?>> vacationTypes = List.of(vacationType);
            when(vacationTypeService.getActiveVacationTypes()).thenReturn(vacationTypes);

            when(applicationForLeaveStatisticsBuilder.build(List.of(anyPerson), startDate, endDate, vacationTypes))
                .thenReturn(Map.of(anyPerson, new ApplicationForLeaveStatistics(anyPerson, vacationTypes)));

            final ApplicationForLeaveStatisticsPageRequest pageRequest = ApplicationForLeaveStatisticsPageRequest.of(0, 10, Sort.by("leftVacationDaysForYear"));
            final Page<ApplicationForLeaveStatistics> statisticsPage = sut.getStatisticsSortedByStatistics(personWithRole, filterPeriod, pageRequest, "");

            assertThat(statisticsPage.getContent()).hasSize(1);
            assertThat(statisticsPage.getContent().get(0).getPerson()).isEqualTo(anyPerson);
        }

        @ParameterizedTest
        @EnumSource(value = Role.class, names = {"DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY"})
        void ensureAllActivePersonsAreRequestedWhenSortedByStatisticsAttributeByDepartmentPrivileged(final Role role) {

            final LocalDate startDate = LocalDate.parse("2018-01-01");
            final LocalDate endDate = LocalDate.parse("2018-12-31");
            final FilterPeriod filterPeriod = new FilterPeriod(startDate, endDate);

            final Person departmentManagement = new Person();
            departmentManagement.setId(1L);
            departmentManagement.setPermissions(List.of(USER, role));

            final Person departmentMember = new Person();
            departmentMember.setId(2L);
            departmentMember.setPermissions(List.of(USER));
            departmentMember.setFirstName("Anton");

            final Person departmentMemberTwo = new Person();
            departmentMemberTwo.setId(3L);
            departmentMemberTwo.setPermissions(List.of(USER));
            departmentMemberTwo.setFirstName("Bernd");

            // TODO this is actually wrong! the sut search sorts by statistics, persons must be fetched to the absence entity result set
            when(departmentService.getManagedMembersOfPerson(departmentManagement, PersonPageRequest.of(0, 10), ""))
                // note different sorting of persons to requested statistics sorting
                // departmentMember must be second in the expected result
                .thenReturn(new PageImpl<>(List.of(departmentMember, departmentMemberTwo)));

            final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource()).build();
            final List<VacationType<?>> vacationTypes = List.of(vacationType);
            when(vacationTypeService.getActiveVacationTypes()).thenReturn(vacationTypes);

            final ApplicationForLeaveStatistics statistics1 = new ApplicationForLeaveStatistics(departmentMember, vacationTypes);
            statistics1.setLeftVacationDaysForYear(BigDecimal.TEN);

            final ApplicationForLeaveStatistics statistics2 = new ApplicationForLeaveStatistics(departmentMemberTwo, vacationTypes);
            statistics2.setLeftVacationDaysForYear(BigDecimal.ZERO);

            when(applicationForLeaveStatisticsBuilder.build(List.of(departmentMember, departmentMemberTwo), startDate, endDate, vacationTypes))
                .thenReturn(Map.of(
                    departmentMember, statistics1,
                    departmentMemberTwo, statistics2
                ));

            final ApplicationForLeaveStatisticsPageRequest pageRequest = ApplicationForLeaveStatisticsPageRequest.of(0, 10, Sort.by("leftVacationDaysForYear"));
            final Page<ApplicationForLeaveStatistics> statisticsPage = sut.getStatisticsSortedByStatistics(departmentManagement, filterPeriod, pageRequest, "");

            assertThat(statisticsPage.getContent()).hasSize(2);
            assertThat(statisticsPage.getContent().get(0).getPerson()).isEqualTo(departmentMemberTwo);
            assertThat(statisticsPage.getContent().get(0).getLeftVacationDaysForYear()).isEqualTo(BigDecimal.ZERO);
            assertThat(statisticsPage.getContent().get(1).getPerson()).isEqualTo(departmentMember);
            assertThat(statisticsPage.getContent().get(1).getLeftVacationDaysForYear()).isEqualTo(BigDecimal.TEN);
        }
    }
}
