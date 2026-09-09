package org.synyx.urlaubsverwaltung.application.export;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationForLeave;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.PersonPageRequest;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedata;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedataService;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static java.time.Month.JANUARY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@ExtendWith(MockitoExtension.class)
class ApplicationForLeaveExportServiceTest {

    private static final LocalDate FROM = LocalDate.of(2023, JANUARY, 1);
    private static final LocalDate TO = LocalDate.of(2023, JANUARY, 31);

    @Mock
    private ApplicationService applicationService;
    @Mock
    private DepartmentService departmentService;
    @Mock
    private PersonBasedataService personBasedataService;
    @Mock
    private PersonService personService;
    @Mock
    private WorkDaysCountService workDaysCountService;

    private ApplicationForLeaveExportService sut;

    @BeforeEach
    void setUp() {
        sut = new ApplicationForLeaveExportService(applicationService, departmentService, personBasedataService, personService, workDaysCountService);
    }

    @Nested
    class GetAll {

        @ParameterizedTest
        @EnumSource(value = Role.class, names = {"OFFICE", "BOSS"})
        void ensuresEveryActivePersonIsExportedForOfficeOrBoss(Role role) {

            final Person person = person(1L, role);
            final Person user = person(2L, "Marlene", "Muster");

            when(personService.getActivePersons(PersonPageRequest.unpaged(), "")).thenReturn(new PageImpl<>(List.of(user)));

            final ApplicationForLeave app = application(user);
            when(applicationService.getForStatesAndPerson(List.of(ALLOWED, TEMPORARY_ALLOWED, ALLOWED_CANCELLATION_REQUESTED), List.of(user), FROM, TO)).thenReturn(List.of(app));
            when(workDaysCountService.getWorkDaysCountByYearForApplications(any())).thenReturn(Map.of(app, new TreeMap<>()));
            when(personBasedataService.getBasedataByPersonId(List.of(2L))).thenReturn(Map.of(new PersonId(2L), new PersonBasedata(new PersonId(2L), "personnelNumber", "")));
            when(departmentService.getDepartmentNamesByMembers(List.of(user))).thenReturn(Map.of(new PersonId(2L), List.of("department")));

            final List<ApplicationForLeaveExport> export = sut.getAll(person, FROM, TO);

            assertThat(export).hasSize(1);
            assertThat(export.getFirst().getFirstName()).isEqualTo("Marlene");
            assertThat(export.getFirst().getLastName()).isEqualTo("Muster");
            assertThat(export.getFirst().getPersonalNumber()).isEqualTo("personnelNumber");
            assertThat(export.getFirst().getDepartments()).containsExactly("department");
            assertThat(export.getFirst().getApplicationForLeaves()).containsExactly(app);
        }

        @Test
        void ensuresExportIsOrderedByFirstAndLastName() {

            final Person office = person(1L, OFFICE);
            final Person zoe = person(2L, "Zoe", "Zimmermann");
            final Person anna = person(3L, "anna", "Bauer");
            final Person annaAlbers = person(4L, "Anna", "Albers");

            when(personService.getActivePersons(PersonPageRequest.unpaged(), "")).thenReturn(new PageImpl<>(List.of(zoe, anna, annaAlbers)));
            when(applicationService.getForStatesAndPerson(any(), any(), any(), any())).thenReturn(List.of());
            when(workDaysCountService.getWorkDaysCountByYearForApplications(any())).thenReturn(Map.of());
            when(personBasedataService.getBasedataByPersonId(any())).thenReturn(Map.of());
            when(departmentService.getDepartmentNamesByMembers(any())).thenReturn(Map.of());

            final List<ApplicationForLeaveExport> export = sut.getAll(office, FROM, TO);

            assertThat(export)
                .extracting(ApplicationForLeaveExport::getFirstName, ApplicationForLeaveExport::getLastName)
                .containsExactly(
                    tuple("Anna", "Albers"),
                    tuple("anna", "Bauer"),
                    tuple("Zoe", "Zimmermann")
                );
        }

        @Test
        void ensuresEmptyExportWhenPersonIsNotAllowedToSeeAnybody() {

            final Person person = person(1L, USER);

            when(departmentService.getManagedActiveMembersOfPerson(person, PersonPageRequest.unpaged(), ""))
                .thenReturn(new PageImpl<>(List.of()));

            final List<ApplicationForLeaveExport> export = sut.getAll(person, FROM, TO);

            verifyNoMoreInteractions(departmentService);
            verifyNoInteractions(applicationService, personBasedataService, workDaysCountService);

            assertThat(export).isEmpty();
        }
    }

    @Nested
    class GetAllForPersons {

        @Test
        void ensuresOnlyTheGivenPersonsAreExported() {

            final Person office = person(1L, OFFICE);
            final Person marlene = person(2L, "Marlene", "Muster");
            final Person klaus = person(3L, "Klaus", "Müller");
            final Person notRequested = person(4L, "Juliane", "Huber");

            when(personService.getActivePersons(PersonPageRequest.unpaged(), "")).thenReturn(new PageImpl<>(List.of(marlene, klaus, notRequested)));

            final ApplicationForLeave app = application(klaus);
            when(applicationService.getForStatesAndPerson(List.of(ALLOWED, TEMPORARY_ALLOWED, ALLOWED_CANCELLATION_REQUESTED), List.of(klaus, marlene), FROM, TO)).thenReturn(List.of(app));
            when(workDaysCountService.getWorkDaysCountByYearForApplications(any())).thenReturn(Map.of(app, new TreeMap<>()));
            when(personBasedataService.getBasedataByPersonId(List.of(3L, 2L))).thenReturn(Map.of());
            when(departmentService.getDepartmentNamesByMembers(List.of(klaus, marlene))).thenReturn(Map.of());

            final List<ApplicationForLeaveExport> export = sut.getAllForPersons(office, FROM, TO, List.of(new PersonId(3L), new PersonId(2L)));

            assertThat(export)
                .extracting(ApplicationForLeaveExport::getFirstName)
                .containsExactly("Klaus", "Marlene");
        }

        @Test
        void ensuresTheGivenOrderIsKeptSinceItIsTheOrderOfTheVisiblePersons() {

            final Person office = person(1L, OFFICE);
            final Person anna = person(2L, "Anna", "Albers");
            final Person zoe = person(3L, "Zoe", "Zimmermann");

            when(personService.getActivePersons(PersonPageRequest.unpaged(), "")).thenReturn(new PageImpl<>(List.of(anna, zoe)));
            when(applicationService.getForStatesAndPerson(any(), any(), any(), any())).thenReturn(List.of());
            when(workDaysCountService.getWorkDaysCountByYearForApplications(any())).thenReturn(Map.of());
            when(personBasedataService.getBasedataByPersonId(any())).thenReturn(Map.of());
            when(departmentService.getDepartmentNamesByMembers(any())).thenReturn(Map.of());

            final List<ApplicationForLeaveExport> export = sut.getAllForPersons(office, FROM, TO, List.of(new PersonId(3L), new PersonId(2L)));

            assertThat(export)
                .extracting(ApplicationForLeaveExport::getFirstName)
                .containsExactly("Zoe", "Anna");
        }

        @Test
        void ensuresPersonIdsTheSignedInUserMustNotAccessAreIgnored() {

            final Person departmentHead = person(1L, Role.DEPARTMENT_HEAD);
            final Person managedMember = person(2L, "Marlene", "Muster");

            when(departmentService.getManagedActiveMembersOfPerson(departmentHead, PersonPageRequest.unpaged(), ""))
                .thenReturn(new PageImpl<>(List.of(managedMember)));

            when(applicationService.getForStatesAndPerson(any(), any(), any(), any())).thenReturn(List.of());
            when(workDaysCountService.getWorkDaysCountByYearForApplications(any())).thenReturn(Map.of());
            when(personBasedataService.getBasedataByPersonId(List.of(2L))).thenReturn(Map.of());
            when(departmentService.getDepartmentNamesByMembers(List.of(managedMember))).thenReturn(Map.of());

            // 42 is not managed by the department head
            final List<ApplicationForLeaveExport> export = sut.getAllForPersons(departmentHead, FROM, TO, List.of(new PersonId(2L), new PersonId(42L)));

            assertThat(export)
                .extracting(ApplicationForLeaveExport::getFirstName)
                .containsExactly("Marlene");
        }

        @Test
        void ensuresEmptyExportWithoutPersonIds() {

            final Person office = person(1L, OFFICE);

            final List<ApplicationForLeaveExport> export = sut.getAllForPersons(office, FROM, TO, List.of());

            verifyNoInteractions(personService, departmentService, applicationService, personBasedataService, workDaysCountService);

            assertThat(export).isEmpty();
        }

        @Test
        void ensuresEmptyExportWhenNoneOfThePersonIdsIsAccessible() {

            final Person office = person(1L, OFFICE);
            final Person other = person(2L, "Marlene", "Muster");

            when(personService.getActivePersons(PersonPageRequest.unpaged(), "")).thenReturn(new PageImpl<>(List.of(other)));

            final List<ApplicationForLeaveExport> export = sut.getAllForPersons(office, FROM, TO, List.of(new PersonId(42L)));

            verifyNoInteractions(applicationService, personBasedataService, workDaysCountService);

            assertThat(export).isEmpty();
        }
    }

    private static Person person(Long id, Role... roles) {
        final Person person = new Person();
        person.setId(id);
        person.setPermissions(List.of(roles));
        return person;
    }

    private static Person person(Long id, String firstName, String lastName) {
        final Person person = person(id, USER);
        person.setFirstName(firstName);
        person.setLastName(lastName);
        return person;
    }

    private static ApplicationForLeave application(Person person) {
        final ApplicationForLeave app = new ApplicationForLeave(new Application(), new TreeMap<>());
        app.setId(1L);
        app.setPerson(person);
        return app;
    }
}
