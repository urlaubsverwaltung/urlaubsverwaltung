package org.synyx.urlaubsverwaltung.absence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.synyx.urlaubsverwaltung.SingleTenantTestContainersBase;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeWriteService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static java.util.Collections.emptyList;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;

@SpringBootTest
class AbsenceApiControllerSecurityIT extends SingleTenantTestContainersBase {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private PersonService personService;
    @MockitoBean
    private DepartmentService departmentService;
    @MockitoBean
    private AbsenceService absenceService;
    @MockitoBean
    private WorkingTimeService workingTimeService;
    @MockitoBean
    private WorkingTimeWriteService workingTimeWriteService;

    @Test
    void getAbsencesWithoutOIDCAuthIsUnauthorized() throws Exception {
        perform(
            get("/api/absences")
        )
            .andExpect(status().is4xxClientError());

    }

    @Test
    void getAbsencesAsAuthenticatedUserForOtherUserIsForbidden() throws Exception {
        perform(
            get("/api/persons/1/absences")
                .param("from", "2016-01-01")
                .param("to", "2016-12-31")
                .with(oidcLogin().idToken(builder -> builder.subject("user")).authorities(new SimpleGrantedAuthority("USER")))
        )
            .andExpect(status().isForbidden());
    }

    @Test
    void getAbsencesAsDepartmentHeadUserForOtherUserIsForbidden() throws Exception {
        final Person person = new Person();
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        final Person departmentHead = new Person();
        departmentHead.setPermissions(List.of(DEPARTMENT_HEAD));
        when(personService.getPersonByUsername("departmentHead")).thenReturn(Optional.of(departmentHead));

        final Department department = new Department();
        department.setMembers(List.of());

        final List<Department> departments = List.of(department);
        when(departmentService.getManagedDepartmentsOfDepartmentHead(departmentHead)).thenReturn(departments);

        perform(
            get("/api/persons/1/absences")
                .param("from", "2016-01-01")
                .param("to", "2016-12-31")
                .with(oidcLogin().idToken(builder -> builder.subject("departmentHead")).authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority("DEPARTMENT_HEAD")))
        )
            .andExpect(status().isForbidden());
    }

    @Test
    void getAbsencesAsDepartmentHeadUserForOtherUserInSameDepartmentIsOk() throws Exception {
        final Person person = new Person();
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        final Person departmentHead = new Person();
        departmentHead.setPermissions(List.of(DEPARTMENT_HEAD));
        when(personService.getPersonByUsername("departmentHead")).thenReturn(Optional.of(departmentHead));
        when(departmentService.isDepartmentHeadAllowedToManagePerson(departmentHead, person)).thenReturn(true);

        when(absenceService.getOpenAbsences(person, LocalDate.of(2016, JANUARY, 1), LocalDate.of(2016, DECEMBER, 31)))
            .thenReturn(emptyList());

        perform(
            get("/api/persons/1/absences")
                .param("from", "2016-01-01")
                .param("to", "2016-12-31")
                .with(oidcLogin().idToken(builder -> builder.subject("departmentHead")).authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority("DEPARTMENT_HEAD")))
        )
            .andExpect(status().isOk());
    }

    @Test
    void getAbsencesAsSSAUserForOtherUserIsForbidden() throws Exception {
        final Person person = new Person();
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        final Person ssa = new Person();
        ssa.setPermissions(List.of(SECOND_STAGE_AUTHORITY));
        when(personService.getPersonByUsername("ssa")).thenReturn(Optional.of(ssa));

        final Department department = new Department();
        department.setMembers(List.of());

        final List<Department> departments = List.of(department);
        when(departmentService.getManagedDepartmentsOfSecondStageAuthority(ssa)).thenReturn(departments);

        perform(
            get("/api/persons/1/absences")
                .param("from", "2016-01-01")
                .param("to", "2016-12-31")
                .with(oidcLogin().idToken(builder -> builder.subject("ssa")).authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority("SECOND_STAGE_AUTHORITY")))
        )
            .andExpect(status().isForbidden());
    }

    @Test
    void getAbsencesAsSSAUserForOtherUserInSameDepartmentIsOk() throws Exception {
        final Person person = new Person();
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        final Person ssa = new Person();
        ssa.setPermissions(List.of(SECOND_STAGE_AUTHORITY));
        when(personService.getPersonByUsername("ssa")).thenReturn(Optional.of(ssa));
        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(ssa, person)).thenReturn(true);

        when(absenceService.getOpenAbsences(person, LocalDate.of(2016, JANUARY, 1), LocalDate.of(2016, DECEMBER, 31)))
            .thenReturn(emptyList());

        perform(
            get("/api/persons/1/absences")
                .param("from", "2016-01-01")
                .param("to", "2016-12-31")
                .with(oidcLogin().idToken(builder -> builder.subject("ssa")).authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority("SECOND_STAGE_AUTHORITY")))
        )
            .andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(strings = {"INACTIVE"})
    void getAbsencesForOtherUserIsForbidden(final String role) throws Exception {
        perform(get("/api/persons/1/absences")
            .param("from", "2016-01-01")
            .param("to", "2016-12-31")
            .with(oidcLogin().idToken(builder -> builder.subject("user")).authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority(role)))
        )
            .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @ValueSource(strings = {"OFFICE", "BOSS"})
    void getAbsencesAsOfficeUserForOtherUserIsOk(final String role) throws Exception {

        final Person person = new Person();
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        final LocalDate startDate = LocalDate.of(2016, JANUARY, 1);
        final LocalDate endDate = LocalDate.of(2016, DECEMBER, 31);
        when(workingTimeService.getFederalStatesByPersonAndDateRange(person, new DateRange(startDate, endDate))).thenReturn(Map.of());
        when(absenceService.getOpenAbsences(person, startDate, endDate)).thenReturn(emptyList());

        perform(get("/api/persons/1/absences")
            .param("from", "2016-01-01")
            .param("to", "2016-12-31")
            .with(oidcLogin().idToken(builder -> builder.subject("user")).authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority(role)))
        )
            .andExpect(status().isOk());
    }

    @Test
    void getAbsencesAsOfficeUserForSameUserIsOk() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        when(absenceService.getOpenAbsences(person, LocalDate.of(2016, JANUARY, 1), LocalDate.of(2016, DECEMBER, 31)))
            .thenReturn(emptyList());

        perform(
            get("/api/persons/1/absences")
                .param("from", "2016-01-01")
                .param("to", "2016-12-31")
                .with(oidcLogin().idToken(builder -> builder.subject("user")).authorities(new SimpleGrantedAuthority("USER")))
        )
            .andExpect(status().isOk());
    }

    @Test
    void getAbsencesAsOfficeUserForDifferentUserIsForbidden() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        perform(
            get("/api/persons/1/absences")
                .param("from", "2016-01-01")
                .param("to", "2016-12-31")
                .with(oidcLogin().idToken(builder -> builder.subject("differentUser")).authorities(new SimpleGrantedAuthority("USER")))
        )
            .andExpect(status().isForbidden());
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build().perform(builder);
    }
}
