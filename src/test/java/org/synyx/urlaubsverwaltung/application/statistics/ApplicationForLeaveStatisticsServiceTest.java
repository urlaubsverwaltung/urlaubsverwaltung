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
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.application.application.ApplicationStatus;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    private ApplicationService applicationService;
    @Mock
    private ApplicationForLeaveStatisticsBuilder applicationForLeaveStatisticsBuilder;
    @Mock
    private VacationTypeService vacationTypeService;

    @BeforeEach
    void setUp() {
        sut = new ApplicationForLeaveStatisticsService(personService, personBasedataService, departmentService,
            applicationService, applicationForLeaveStatisticsBuilder, vacationTypeService);
    }

    @Nested
    class GetStatisticsSortedByPerson {

        @ParameterizedTest
        @EnumSource(value = Role.class, names = {"BOSS", "OFFICE"})
        void ensureUsingAllActivePersonsForRequestingUserWithRole(Role role) {

            final LocalDate startDate = LocalDate.parse("2018-01-01");
            final LocalDate endDate = LocalDate.parse("2018-12-31");
            final FilterPeriod filterPeriod = new FilterPeriod(startDate, endDate);

            final Person personRequestingStatistics = new Person();
            personRequestingStatistics.setId(1L);
            personRequestingStatistics.setPermissions(List.of(USER, role));

            final Person person = new Person();
            person.setId(2L);
            person.setPermissions(List.of(USER));

            final PersonPageRequest personPageRequest = PersonPageRequest.of(0, 10, Sort.by("firstName"));
            when(personService.getActivePersons(personPageRequest, "")).thenReturn(new PageImpl<>(List.of(person)));

            final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource()).build();
            final List<VacationType<?>> activeVacationTypes = List.of(vacationType);
            when(vacationTypeService.getActiveVacationTypes()).thenReturn(activeVacationTypes);

            when(applicationForLeaveStatisticsBuilder.build(List.of(person), startDate, endDate, activeVacationTypes))
                .thenReturn(Map.of(
                    person, Optional.of(new ApplicationForLeaveStatistics(person, activeVacationTypes)))
                );

            final Page<ApplicationForLeaveStatistics> actual =
                sut.getStatisticsSortedByPerson(personRequestingStatistics, filterPeriod, personPageRequest, "");

            assertThat(actual.getContent()).hasSize(1);
            assertThat(actual.getContent().get(0).getPerson()).isEqualTo(person);
        }

        @ParameterizedTest
        @EnumSource(value = Role.class, names = {"DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY"})
        void ensureUsingManagedMembersOfRequestingUserWithRole(Role role) {

            final LocalDate startDate = LocalDate.parse("2018-01-01");
            final LocalDate endDate = LocalDate.parse("2018-12-31");
            final FilterPeriod filterPeriod = new FilterPeriod(startDate, endDate);

            final Person personRequestingStatistics = new Person();
            personRequestingStatistics.setId(1L);
            personRequestingStatistics.setPermissions(List.of(USER, role));

            final Person member = new Person();
            member.setId(2L);
            member.setPermissions(List.of(USER));

            final PersonPageRequest personPageRequest = PersonPageRequest.of(0, 10, Sort.by("firstName"));
            when(departmentService.getManagedMembersOfPerson(personRequestingStatistics, personPageRequest, ""))
                .thenReturn(new PageImpl<>(List.of(member)));

            final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource()).build();
            final List<VacationType<?>> vacationTypes = List.of(vacationType);
            when(vacationTypeService.getActiveVacationTypes()).thenReturn(vacationTypes);

            when(applicationForLeaveStatisticsBuilder.build(List.of(member), startDate, endDate, vacationTypes))
                .thenReturn(Map.of(
                    member, Optional.of(new ApplicationForLeaveStatistics(member, vacationTypes)))
                );

            final Page<ApplicationForLeaveStatistics> actual =
                sut.getStatisticsSortedByPerson(personRequestingStatistics, filterPeriod, personPageRequest, "");

            assertThat(actual.getContent()).hasSize(1);
            assertThat(actual.getContent().get(0).getPerson()).isEqualTo(member);
        }

        @Test
        void ensureStatisticsRequestedForBasicUserHimself() {

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
                .thenReturn(Map.of(user, Optional.of(new ApplicationForLeaveStatistics(user, vacationTypes))));

            final PersonPageRequest pageRequest = PersonPageRequest.of(0, 10, Sort.by("firstName"));
            final Page<ApplicationForLeaveStatistics> statisticsPage = sut.getStatisticsSortedByPerson(user, filterPeriod, pageRequest, "");
            assertThat(statisticsPage.getContent()).hasSize(1);
            assertThat(statisticsPage.getContent().get(0).getPerson()).isEqualTo(user);
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
            when(applicationForLeaveStatisticsBuilder.build(List.of(person), startDate, endDate, vacationTypes))
                .thenReturn(Map.of(person, Optional.of(applicationForLeaveStatistics)));

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
            when(applicationForLeaveStatisticsBuilder.build(List.of(person), startDate, endDate, vacationTypes))
                .thenReturn(Map.of(person, Optional.of(applicationForLeaveStatistics)));

            final Page<ApplicationForLeaveStatistics> statisticsPage = sut.getStatisticsSortedByPerson(office, filterPeriod, personPageRequest, "");
            assertThat(statisticsPage.getContent()).hasSize(1);

            final ApplicationForLeaveStatistics applicationForLeaveStatisticsOfPerson = statisticsPage.getContent().get(0);
            assertThat(applicationForLeaveStatisticsOfPerson.getPerson()).isEqualTo(person);
            assertThat(applicationForLeaveStatisticsOfPerson.getPersonBasedata()).hasValue(personBasedata);
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

            when(personService.getActivePersons(any(PersonPageRequest.PersonPageRequestUnpaged.class), eq("")))
                .thenReturn(new PageImpl<>(List.of(anyPerson)));

            final VacationType<?> vacationType = ProvidedVacationType.builder(new StaticMessageSource()).build();
            final List<VacationType<?>> vacationTypes = List.of(vacationType);
            when(vacationTypeService.getActiveVacationTypes()).thenReturn(vacationTypes);

            // actual applications are not of interest. returned list just has to be passed into applicationForLeaveStatisticsBuilder
            final List<Application> applications = List.of(new Application());

            when(applicationService.getApplicationsForACertainPeriodAndStatus(startDate, endDate, ApplicationStatus.activeStatuses(), vacationTypes, ""))
                .thenReturn(applications);

            when(applicationForLeaveStatisticsBuilder.build(List.of(anyPerson), startDate, endDate, vacationTypes, applications))
                .thenReturn(Map.of(anyPerson, Optional.of(new ApplicationForLeaveStatistics(anyPerson, vacationTypes))));

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

            when(departmentService.getManagedMembersOfPerson(eq(departmentManagement), any(PersonPageRequest.PersonPageRequestUnpaged.class), eq("")))
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

            // actual applications are not of interest. returned list just has to be passed into applicationForLeaveStatisticsBuilder
            final List<Application> applications = List.of(new Application());

            when(applicationService.getApplicationsForACertainPeriodAndStatus(startDate, endDate, ApplicationStatus.activeStatuses(), vacationTypes, ""))
                .thenReturn(applications);

            when(applicationForLeaveStatisticsBuilder.build(List.of(departmentMember, departmentMemberTwo), startDate, endDate, vacationTypes, applications))
                .thenReturn(Map.of(
                    departmentMember, Optional.of(statistics1),
                    departmentMemberTwo, Optional.of(statistics2)
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
