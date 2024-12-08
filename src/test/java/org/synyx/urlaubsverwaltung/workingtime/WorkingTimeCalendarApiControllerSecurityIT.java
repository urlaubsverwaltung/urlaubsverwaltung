package org.synyx.urlaubsverwaltung.workingtime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.context.WebApplicationContext;
import org.synyx.urlaubsverwaltung.SingleTenantTestContainersBase;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@SpringBootTest
class WorkingTimeCalendarApiControllerSecurityIT extends SingleTenantTestContainersBase {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private WorkDaysCountService workDaysCountService;
    @MockitoBean
    private DepartmentService departmentService;
    @MockitoBean
    private PersonService personService;

    @Test
    void getWorkdaysWithoutAuthIsUnauthorized() throws Exception {
        perform(
            get("/api/workdays")
        )
            .andExpect(status().is4xxClientError());
    }

    @Test
    void getWorkdaysAsDepartmentHeadUserForOtherUserIsForbidden() throws Exception {
        when(workDaysCountService.getWorkDaysCount(any(), any(), any(), any())).thenReturn(BigDecimal.ONE);

        final Person requester = new Person();
        requester.setPermissions(List.of(USER, DEPARTMENT_HEAD));
        when(personService.getPersonByUsername("department head")).thenReturn(Optional.of(requester));

        final Person requestedPerson = new Person();
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(requestedPerson));

        when(departmentService.isDepartmentHeadAllowedToManagePerson(requester, requestedPerson)).thenReturn(false);

        perform(
            get("/api/persons/1/workdays")
                .with(oidcLogin().idToken(builder -> builder.subject("department head")).authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority("DEPARTMENT_HEAD")))
                .param("from", "2016-01-04")
                .param("to", "2016-01-04")
                .param("length", "FULL")
        )
            .andExpect(status().isForbidden());
    }

    @Test
    void getWorkdaysAsSecondStageAuthorityUserForOtherUserIsForbidden() throws Exception {
        when(workDaysCountService.getWorkDaysCount(any(), any(), any(), any())).thenReturn(BigDecimal.ONE);

        final Person requester = new Person();
        requester.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY));
        when(personService.getPersonByUsername("second stage authority")).thenReturn(Optional.of(requester));

        final Person requestedPerson = new Person();
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(requestedPerson));

        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(requester, requestedPerson)).thenReturn(false);

        perform(
            get("/api/persons/1/workdays")
                .with(oidcLogin().idToken(builder -> builder.subject("second stage authority")).authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority("SECOND_STAGE_AUTHORITY")))
                .param("from", "2016-01-04")
                .param("to", "2016-01-04")
                .param("length", "FULL")
        )
            .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @ValueSource(strings = {"USER", "INACTIVE"})
    void getWorkdaysIsForbiddenFor(final String role) throws Exception {
        when(workDaysCountService.getWorkDaysCount(any(), any(), any(), any())).thenReturn(BigDecimal.ONE);

        perform(
            get("/api/persons/1/workdays")
                .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority(role)))
                .param("from", "2016-01-04")
                .param("to", "2016-01-04")
                .param("length", "FULL")
        )
            .andExpect(status().isForbidden());
    }

    @Test
    void getWorkdaysAsDepartmentHeadUserForOtherUserIsOk() throws Exception {
        when(workDaysCountService.getWorkDaysCount(any(), any(), any(), any())).thenReturn(BigDecimal.ONE);

        final Person requester = new Person();
        requester.setPermissions(List.of(USER, DEPARTMENT_HEAD));
        when(personService.getPersonByUsername("department head")).thenReturn(Optional.of(requester));

        final Person requestedPerson = new Person();
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(requestedPerson));

        when(departmentService.isDepartmentHeadAllowedToManagePerson(requester, requestedPerson)).thenReturn(true);

        perform(
            get("/api/persons/1/workdays")
                .with(oidcLogin().idToken(builder -> builder.subject("department head")).authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority("DEPARTMENT_HEAD")))
                .param("from", "2016-01-04")
                .param("to", "2016-01-04")
                .param("length", "FULL")
        )
            .andExpect(status().isOk());
    }

    @Test
    void getWorkdaysAsSecondStageAuthorityUserForOtherUserIsOk() throws Exception {
        when(workDaysCountService.getWorkDaysCount(any(), any(), any(), any())).thenReturn(BigDecimal.ONE);

        final Person requester = new Person();
        requester.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY));
        when(personService.getPersonByUsername("second stage authority")).thenReturn(Optional.of(requester));

        final Person requestedPerson = new Person();
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(requestedPerson));

        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(requester, requestedPerson)).thenReturn(true);

        perform(
            get("/api/persons/1/workdays")
                .with(oidcLogin().idToken(builder -> builder.subject("second stage authority")).authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority("SECOND_STAGE_AUTHORITY")))
                .param("from", "2016-01-04")
                .param("to", "2016-01-04")
                .param("length", "FULL")
        )
            .andExpect(status().isOk());
    }

    @Test
    void getWorkdaysAsBossUserForOtherUserIsForbidden() throws Exception {
        when(workDaysCountService.getWorkDaysCount(any(), any(), any(), any())).thenReturn(BigDecimal.ONE);
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(new Person()));

        perform(
            get("/api/persons/1/workdays")
                .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority("BOSS")))
                .param("from", "2016-01-04")
                .param("to", "2016-01-04")
                .param("length", "FULL")
        )
            .andExpect(status().isOk());
    }

    @Test
    void getWorkdaysWithOfficeRoleIsOk() throws Exception {

        when(personService.getPersonByID(1L)).thenReturn(Optional.of(new Person()));
        when(workDaysCountService.getWorkDaysCount(any(), any(), any(), any())).thenReturn(BigDecimal.ONE);

        perform(
            get("/api/persons/1/workdays")
                .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority("OFFICE")))
                .param("from", "2016-01-04")
                .param("to", "2016-01-04")
                .param("length", "FULL")
        )
            .andExpect(status().isOk());
    }

    @Test
    void getWorkdaysWithSameUserIsOk() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        when(workDaysCountService.getWorkDaysCount(any(), any(), any(), any())).thenReturn(BigDecimal.ONE);

        perform(
            get("/api/persons/1/workdays")
                .with(oidcLogin().idToken(builder -> builder.subject("user")).authorities(new SimpleGrantedAuthority("USER")))
                .param("from", "2016-01-04")
                .param("to", "2016-01-04")
                .param("length", "FULL")
        )
            .andExpect(status().isOk());
    }

    @Test
    void getWorkdaysWithDifferentUserIsForbidden() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        perform(
            get("/api/persons/1/workdays")
                .with(oidcLogin().idToken(builder -> builder.subject("differentUser")).authorities(new SimpleGrantedAuthority("USER")))
                .param("from", "2016-01-04")
                .param("to", "2016-01-04")
                .param("length", "FULL")
        )
            .andExpect(status().isForbidden());
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return webAppContextSetup(context).apply(springSecurity()).build().perform(builder);
    }
}
