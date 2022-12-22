package org.synyx.urlaubsverwaltung.application.export;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationForLeave;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedata;
import org.synyx.urlaubsverwaltung.person.basedata.PersonBasedataService;
import org.synyx.urlaubsverwaltung.search.PageableSearchQuery;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
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

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"OFFICE", "BOSS"})
    void getAllForOfficeOrBoss(Role role) {

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(role));

        final Person user = new Person();
        user.setId(2L);
        user.setPermissions(List.of(USER));
        user.setFirstName("Marlene");
        user.setLastName("Muster");
        final List<Person> personsForExport = List.of(user);
        final PersonId userId = new PersonId(user.getId());

        final PageRequest personPageRequest = PageRequest.of(0, 10, Sort.unsorted());
        final PageableSearchQuery personSearchQuery = new PageableSearchQuery(personPageRequest, "");

        when(personService.getActivePersons(personSearchQuery)).thenReturn(new PageImpl<>(personsForExport));

        final LocalDate from = LocalDate.of(2023, 1, 1);
        final LocalDate to = LocalDate.of(2023, 1, 31);
        final ApplicationForLeave app = new ApplicationForLeave(new Application(), workDaysCountService);
        app.setId(1L);
        app.setPerson(user);
        when(applicationService.getForStatesAndPerson(List.of(ALLOWED, TEMPORARY_ALLOWED, ALLOWED_CANCELLATION_REQUESTED), personsForExport, from, to)).thenReturn(List.of(app));

        final PersonBasedata personBasedata = new PersonBasedata(userId, "personnelNumber", "");
        when(personBasedataService.getBasedataByPersonId(List.of(user.getId()))).thenReturn(Map.of(userId, personBasedata));

        when(departmentService.getDepartmentNamesByMembers(personsForExport)).thenReturn(Map.of(userId, List.of("department")));

        final Page<ApplicationForLeaveExport> export = sut.getAll(person, from, to, personSearchQuery);

        assertThat(export.getContent()).hasSize(1);

        final ApplicationForLeaveExport applicationForLeaveExport = export.getContent().get(0);
        assertThat(applicationForLeaveExport.getFirstName()).isEqualTo("Marlene");
        assertThat(applicationForLeaveExport.getLastName()).isEqualTo("Muster");
        assertThat(applicationForLeaveExport.getPersonalNumber()).isEqualTo("personnelNumber");
        assertThat(applicationForLeaveExport.getDepartments()).containsExactly("department");
        assertThat(applicationForLeaveExport.getApplicationForLeaves()).containsExactly(app);
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY"})
    void getAllForDepartmentHeadOrSecondStageAuthority(Role role) {

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(role));

        final Person departmentMember = new Person();
        departmentMember.setId(2L);
        departmentMember.setPermissions(List.of(USER));
        departmentMember.setFirstName("Marlene");
        departmentMember.setLastName("Muster");
        final PersonId departmentMemberId = new PersonId(departmentMember.getId());
        final List<Person> personsForExport = List.of(departmentMember);

        final PageRequest personPageRequest = PageRequest.of(0, 10, Sort.unsorted());
        final PageableSearchQuery personSearchQuery = new PageableSearchQuery(personPageRequest, "");

        when(departmentService.getManagedMembersOfPerson(person, personSearchQuery)).thenReturn(new PageImpl<>(List.of(departmentMember)));

        final LocalDate from = LocalDate.of(2023, 1, 1);
        final LocalDate to = LocalDate.of(2023, 1, 31);
        final ApplicationForLeave app = new ApplicationForLeave(new Application(), workDaysCountService);
        app.setId(1L);
        app.setPerson(departmentMember);
        when(applicationService.getForStatesAndPerson(List.of(ALLOWED, TEMPORARY_ALLOWED, ALLOWED_CANCELLATION_REQUESTED), personsForExport, from, to)).thenReturn(List.of(app));

        final PersonBasedata personBasedata = new PersonBasedata(departmentMemberId, "personnelNumber", "");
        when(personBasedataService.getBasedataByPersonId(List.of(departmentMember.getId()))).thenReturn(Map.of(departmentMemberId, personBasedata));

        when(departmentService.getDepartmentNamesByMembers(personsForExport)).thenReturn(Map.of(departmentMemberId, List.of("department")));

        final Page<ApplicationForLeaveExport> export = sut.getAll(person, LocalDate.of(2023, 1, 1), LocalDate.of(2023, 1, 31), personSearchQuery);

        final ApplicationForLeaveExport applicationForLeaveExport = export.getContent().get(0);
        assertThat(applicationForLeaveExport.getFirstName()).isEqualTo("Marlene");
        assertThat(applicationForLeaveExport.getLastName()).isEqualTo("Muster");
        assertThat(applicationForLeaveExport.getPersonalNumber()).isEqualTo("personnelNumber");
        assertThat(applicationForLeaveExport.getDepartments()).containsExactly("department");
        assertThat(applicationForLeaveExport.getApplicationForLeaves()).containsExactly(app);
    }

    @Test
    void getAllNotAllowed() {

        final Person person = new Person();
        person.setId(1L);
        person.setPermissions(List.of(USER));

        final PageRequest personPageRequest = PageRequest.of(0, 10, Sort.unsorted());
        final PageableSearchQuery personSearchQuery = new PageableSearchQuery(personPageRequest, "");

        when(departmentService.getManagedMembersOfPerson(person, personSearchQuery)).thenReturn(new PageImpl<>(List.of()));

        final Page<ApplicationForLeaveExport> export = sut.getAll(person, LocalDate.of(2023, 1, 1), LocalDate.of(2023, 1, 31), personSearchQuery);

        verifyNoMoreInteractions(departmentService);
        verifyNoInteractions(applicationService, personBasedataService);

        assertThat(export.getContent()).isEmpty();
    }

    @Test
    void getAllSortByPerson() {

        final Person office = new Person();
        office.setId(1L);
        office.setPermissions(List.of(OFFICE));

        final Person user = new Person();
        user.setId(2L);
        user.setPermissions(List.of(USER));
        user.setFirstName("Marlene");
        user.setLastName("Muster");
        final List<Person> personsForExport = List.of(user);
        final PersonId userId = new PersonId(user.getId());

        final PageRequest exportPageRequest = PageRequest.of(0, 10, Sort.Direction.ASC, "person.firstName");
        final PageableSearchQuery exportSearchQuery = new PageableSearchQuery(exportPageRequest, "");

        final PageRequest personPageRequest = PageRequest.of(0, 10, Sort.Direction.ASC, "firstName");
        final PageableSearchQuery personSearchQuery = new PageableSearchQuery(personPageRequest, "");

        when(personService.getActivePersons(personSearchQuery)).thenReturn(new PageImpl<>(personsForExport));

        final LocalDate from = LocalDate.of(2023, 1, 1);
        final LocalDate to = LocalDate.of(2023, 1, 31);
        final ApplicationForLeave app = new ApplicationForLeave(new Application(), workDaysCountService);
        app.setId(1L);
        app.setPerson(user);
        when(applicationService.getForStatesAndPerson(List.of(ALLOWED, TEMPORARY_ALLOWED, ALLOWED_CANCELLATION_REQUESTED), personsForExport, from, to)).thenReturn(List.of(app));

        final PersonBasedata personBasedata = new PersonBasedata(userId, "personnelNumber", "");
        when(personBasedataService.getBasedataByPersonId(List.of(user.getId()))).thenReturn(Map.of(userId, personBasedata));

        when(departmentService.getDepartmentNamesByMembers(personsForExport)).thenReturn(Map.of(userId, List.of("department")));

        final Page<ApplicationForLeaveExport> export = sut.getAll(office, from, to, exportSearchQuery);

        assertThat(export.getContent()).hasSize(1);

        final ApplicationForLeaveExport applicationForLeaveExport = export.getContent().get(0);
        assertThat(applicationForLeaveExport.getFirstName()).isEqualTo("Marlene");
        assertThat(applicationForLeaveExport.getLastName()).isEqualTo("Muster");
        assertThat(applicationForLeaveExport.getPersonalNumber()).isEqualTo("personnelNumber");
        assertThat(applicationForLeaveExport.getDepartments()).containsExactly("department");
        assertThat(applicationForLeaveExport.getApplicationForLeaves()).containsExactly(app);
    }

    @Test
    void getAllSortByNonPersonQuery() {

        final Person office = new Person();
        office.setId(1L);
        office.setPermissions(List.of(OFFICE));

        final Person user = new Person();
        user.setId(2L);
        user.setPermissions(List.of(USER));
        user.setFirstName("Marlene");
        user.setLastName("Muster");
        final List<Person> personsForExport = List.of(user);
        final PersonId userId = new PersonId(user.getId());

        final PageRequest exportPageRequest = PageRequest.of(0, 10, Sort.Direction.ASC, "totalAllowedVacationDays");
        final PageableSearchQuery exportSearchQuery = new PageableSearchQuery(exportPageRequest, "");

        final PageRequest personPageRequest = PageRequest.of(0, 10, Sort.unsorted());
        final PageableSearchQuery personSearchQuery = new PageableSearchQuery(personPageRequest, "");

        when(personService.getActivePersons(personSearchQuery)).thenReturn(new PageImpl<>(personsForExport));

        final LocalDate from = LocalDate.of(2023, 1, 1);
        final LocalDate to = LocalDate.of(2023, 1, 31);
        final ApplicationForLeave app = new ApplicationForLeave(new Application(), workDaysCountService);
        app.setId(1L);
        app.setPerson(user);
        when(applicationService.getForStatesAndPerson(List.of(ALLOWED, TEMPORARY_ALLOWED, ALLOWED_CANCELLATION_REQUESTED), personsForExport, from, to)).thenReturn(List.of(app));

        final PersonBasedata personBasedata = new PersonBasedata(userId, "personnelNumber", "");
        when(personBasedataService.getBasedataByPersonId(List.of(user.getId()))).thenReturn(Map.of(userId, personBasedata));

        when(departmentService.getDepartmentNamesByMembers(personsForExport)).thenReturn(Map.of(userId, List.of("department")));

        final Page<ApplicationForLeaveExport> export = sut.getAll(office, from, to, exportSearchQuery);

        assertThat(export.getContent()).hasSize(1);

        final ApplicationForLeaveExport applicationForLeaveExport = export.getContent().get(0);
        assertThat(applicationForLeaveExport.getFirstName()).isEqualTo("Marlene");
        assertThat(applicationForLeaveExport.getLastName()).isEqualTo("Muster");
        assertThat(applicationForLeaveExport.getPersonalNumber()).isEqualTo("personnelNumber");
        assertThat(applicationForLeaveExport.getDepartments()).containsExactly("department");
        assertThat(applicationForLeaveExport.getApplicationForLeaves()).containsExactly(app);
    }
}
