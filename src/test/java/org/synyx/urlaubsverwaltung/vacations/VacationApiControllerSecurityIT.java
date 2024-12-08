package org.synyx.urlaubsverwaltung.vacations;

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
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

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
class VacationApiControllerSecurityIT extends SingleTenantTestContainersBase {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private PersonService personService;
    @MockitoBean
    private ApplicationService applicationService;
    @MockitoBean
    private DepartmentService departmentService;

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Test
    void getVacationsWithoutBasicAuthIsUnauthorized() throws Exception {
        perform(
            get("/api/persons/1/vacations")
        )
            .andExpect(status().is4xxClientError());
    }

    @Test
    void getVacationsAsAuthenticatedUserForOtherUserIsForbidden() throws Exception {
        final LocalDateTime now = LocalDateTime.now();

        perform(
            get("/api/persons/1/vacations")
                .param("from", dtf.format(now))
                .param("to", dtf.format(now.plusDays(5)))
                .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER")))
        )
            .andExpect(status().isForbidden());
    }

    @Test
    void getVacationsAsDepartmentHeadUserForOthersNotFromOwnDepartmentIsForbidden() throws Exception {

        final Person requester = new Person();
        requester.setPermissions(List.of(USER, DEPARTMENT_HEAD));
        when(personService.getPersonByUsername("department head")).thenReturn(Optional.of(requester));

        final Person requestedPerson = new Person();
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(requestedPerson));

        when(departmentService.isDepartmentHeadAllowedToManagePerson(requester, requestedPerson)).thenReturn(false);

        final LocalDateTime now = LocalDateTime.now();

        perform(
            get("/api/persons/1/vacations")
                .param("from", dtf.format(now))
                .param("to", dtf.format(now.plusDays(5)))
                .with(oidcLogin().idToken(builder -> builder.subject("department head")).authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority("DEPARTMENT_HEAD")))
        )
            .andExpect(status().isForbidden());
    }

    @Test
    void getVacationsAsSSAUserForOthersNotFromOwnDepartmentIsForbidden() throws Exception {

        final Person requester = new Person();
        requester.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY));
        when(personService.getPersonByUsername("second stage authority")).thenReturn(Optional.of(requester));

        final Person requestedPerson = new Person();
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(requestedPerson));

        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(requester, requestedPerson)).thenReturn(false);

        final LocalDateTime now = LocalDateTime.now();

        perform(
            get("/api/persons/1/vacations")
                .param("from", dtf.format(now))
                .param("to", dtf.format(now.plusDays(5)))
                .with(oidcLogin().idToken(builder -> builder.subject("second stage authority")).authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority("SECOND_STAGE_AUTHORITY")))
        )
            .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @ValueSource(strings = {"USER", "INACTIVE"})
    void getVacationsForOtherUserIsForbidden(final String role) throws Exception {
        final LocalDateTime now = LocalDateTime.now();

        perform(
            get("/api/persons/1/vacations")
                .param("from", dtf.format(now))
                .param("to", dtf.format(now.plusDays(5)))
                .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority(role)))
        )
            .andExpect(status().isForbidden());
    }

    @Test
    void getVacationsAsDepartmentHeadUserForOtherUserIsOk() throws Exception {

        final Person requester = new Person();
        requester.setPermissions(List.of(USER, DEPARTMENT_HEAD));
        when(personService.getPersonByUsername("department head")).thenReturn(Optional.of(requester));

        final Person requestedPerson = new Person();
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(requestedPerson));

        when(departmentService.isDepartmentHeadAllowedToManagePerson(requester, requestedPerson)).thenReturn(true);

        final LocalDateTime now = LocalDateTime.now();

        perform(
            get("/api/persons/1/vacations")
                .param("from", dtf.format(now))
                .param("to", dtf.format(now.plusDays(5)))
                .with(oidcLogin().idToken(builder -> builder.subject("department head")).authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority("DEPARTMENT_HEAD")))
        )
            .andExpect(status().isOk());
    }

    @Test
    void getVacationsAsSecondStageAuthorityUserForOtherUserIsOk() throws Exception {

        final Person requester = new Person();
        requester.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY));
        when(personService.getPersonByUsername("second stage authority")).thenReturn(Optional.of(requester));

        final Person requestedPerson = new Person();
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(requestedPerson));

        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(requester, requestedPerson)).thenReturn(true);

        final LocalDateTime now = LocalDateTime.now();

        perform(
            get("/api/persons/1/vacations")
                .param("from", dtf.format(now))
                .param("to", dtf.format(now.plusDays(5)))
                .with(oidcLogin().idToken(builder -> builder.subject("second stage authority")).authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority("SECOND_STAGE_AUTHORITY")))
        )
            .andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(strings = {"BOSS", "OFFICE"})
    void getVacationsAsBossUserForOtherUserIsOk(final String role) throws Exception {

        when(personService.getPersonByID(1L)).thenReturn(Optional.of(new Person()));

        final LocalDateTime now = LocalDateTime.now();

        perform(
            get("/api/persons/1/vacations")
                .param("from", dtf.format(now))
                .param("to", dtf.format(now.plusDays(5)))
                .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority(role)))
        )
            .andExpect(status().isOk());
    }

    @Test
    void getVacationsForSameUserIsOk() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        final LocalDateTime now = LocalDateTime.now();

        perform(
            get("/api/persons/1/vacations")
                .param("from", dtf.format(now))
                .param("to", dtf.format(now.plusDays(5)))
                .with(oidcLogin().idToken(builder -> builder.subject("user")).authorities(new SimpleGrantedAuthority("USER")))
        )
            .andExpect(status().isOk());
    }

    @Test
    void getVacationsForDifferentUserIsForbidden() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        final LocalDateTime now = LocalDateTime.now();

        perform(
            get("/api/persons/1/vacations")
                .param("from", dtf.format(now))
                .param("to", dtf.format(now.plusDays(5)))
                .with(oidcLogin().idToken(builder -> builder.subject("differentUser")).authorities(new SimpleGrantedAuthority("USER")))
        )
            .andExpect(status().isForbidden());
    }


    @Test
    void getVacationsOfDepartmentMembersWithoutBasicAuthIsUnauthorized() throws Exception {
        perform(
            get("/api/persons/1/vacations")
                .param("ofDepartmentMembers", "true")
        )
            .andExpect(status().is4xxClientError());
    }

    @Test
    void getVacationsOfDepartmentMembersAsAuthenticatedUserForOtherUserIsForbidden() throws Exception {
        final LocalDateTime now = LocalDateTime.now();

        perform(
            get("/api/persons/1/vacations")
                .param("from", dtf.format(now))
                .param("to", dtf.format(now.plusDays(5)))
                .param("ofDepartmentMembers", "true")
                .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER")))
        )
            .andExpect(status().isForbidden());
    }

    @Test
    void getVacationsOfDepartmentMembersAsDepartmentHeadUserForOtherUserIsForbidden() throws Exception {

        final Person requester = new Person();
        requester.setPermissions(List.of(USER, DEPARTMENT_HEAD));
        when(personService.getPersonByUsername("department head")).thenReturn(Optional.of(requester));

        final Person requestedPerson = new Person();
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(requestedPerson));

        when(departmentService.isDepartmentHeadAllowedToManagePerson(requester, requestedPerson)).thenReturn(false);

        final LocalDateTime now = LocalDateTime.now();

        perform(
            get("/api/persons/1/vacations")
                .param("from", dtf.format(now))
                .param("to", dtf.format(now.plusDays(5)))
                .param("ofDepartmentMembers", "true")
                .with(oidcLogin().idToken(builder -> builder.subject("department head")).authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority("DEPARTMENT_HEAD")))
        )
            .andExpect(status().isForbidden());
    }

    @Test
    void getVacationsOfDepartmentMembersAsSecondStageAuthorityUserForOtherUserIsForbidden() throws Exception {

        final Person requester = new Person();
        requester.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY));
        when(personService.getPersonByUsername("second stage authority")).thenReturn(Optional.of(requester));

        final Person requestedPerson = new Person();
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(requestedPerson));

        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(requester, requestedPerson)).thenReturn(false);

        final LocalDateTime now = LocalDateTime.now();

        perform(
            get("/api/persons/1/vacations")
                .param("from", dtf.format(now))
                .param("to", dtf.format(now.plusDays(5)))
                .param("ofDepartmentMembers", "true")
                .with(oidcLogin().idToken(builder -> builder.subject("second stage authority")).authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority("SECOND_STAGE_AUTHORITY")))
        )
            .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @ValueSource(strings = {"INACTIVE"})
    void getVacationsOfDepartmentMembersAsAdminUserForOtherUserIsForbidden(final String role) throws Exception {
        final LocalDateTime now = LocalDateTime.now();

        perform(
            get("/api/persons/1/vacations")
                .param("from", dtf.format(now))
                .param("to", dtf.format(now.plusDays(5)))
                .param("ofDepartmentMembers", "true")
                .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority(role)))
        )
            .andExpect(status().isForbidden());
    }

    @Test
    void getVacationsOfDepartmentMembersAsDepartmentHeadUserForOtherUserIsOk() throws Exception {

        final Person requester = new Person();
        requester.setPermissions(List.of(USER, DEPARTMENT_HEAD));
        when(personService.getPersonByUsername("department head")).thenReturn(Optional.of(requester));

        final Person requestedPerson = new Person();
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(requestedPerson));

        when(departmentService.isDepartmentHeadAllowedToManagePerson(requester, requestedPerson)).thenReturn(true);

        final LocalDateTime now = LocalDateTime.now();

        perform(
            get("/api/persons/1/vacations")
                .param("from", dtf.format(now))
                .param("to", dtf.format(now.plusDays(5)))
                .param("ofDepartmentMembers", "true")
                .with(oidcLogin().idToken(builder -> builder.subject("department head")).authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority("DEPARTMENT_HEAD")))
        )
            .andExpect(status().isOk());
    }

    @Test
    void getVacationsOfDepartmentMembersAsSecondStageAuthorityUserForOtherUserIsOk() throws Exception {

        final Person requester = new Person();
        requester.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY));
        when(personService.getPersonByUsername("second stage authority")).thenReturn(Optional.of(requester));

        final Person requestedPerson = new Person();
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(requestedPerson));

        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(requester, requestedPerson)).thenReturn(true);

        final LocalDateTime now = LocalDateTime.now();

        perform(
            get("/api/persons/1/vacations")
                .param("from", dtf.format(now))
                .param("to", dtf.format(now.plusDays(5)))
                .param("ofDepartmentMembers", "true")
                .with(oidcLogin().idToken(builder -> builder.subject("second stage authority")).authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority("SECOND_STAGE_AUTHORITY")))
        )
            .andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(strings = {"OFFICE", "BOSS"})
    void getVacationsOfDepartmentMembersWithOfficeUserIsOk(final String role) throws Exception {

        when(personService.getPersonByID(1L)).thenReturn(Optional.of(new Person()));

        final LocalDateTime now = LocalDateTime.now();

        perform(
            get("/api/persons/1/vacations")
                .param("from", dtf.format(now))
                .param("to", dtf.format(now.plusDays(5)))
                .param("ofDepartmentMembers", "true")
                .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority(role)))
        )
            .andExpect(status().isOk());
    }

    @Test
    void getVacationsOfDepartmentMembersForSameUserIsOk() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        final LocalDateTime now = LocalDateTime.now();

        perform(
            get("/api/persons/1/vacations")
                .param("from", dtf.format(now))
                .param("to", dtf.format(now.plusDays(5)))
                .param("ofDepartmentMembers", "true")
                .with(oidcLogin().idToken(builder -> builder.subject("user")).authorities(new SimpleGrantedAuthority("USER")))
        )
            .andExpect(status().isOk());
    }

    @Test
    void getVacationsOfDepartmentMembersForDifferentUserIsForbidden() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        final LocalDateTime now = LocalDateTime.now();

        perform(
            get("/api/persons/1/vacations")
                .param("from", dtf.format(now))
                .param("to", dtf.format(now.plusDays(5)))
                .param("ofDepartmentMembers", "true")
                .with(oidcLogin().idToken(builder -> builder.subject("differentUser")).authorities(new SimpleGrantedAuthority("USER")))
        )
            .andExpect(status().isForbidden());
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return webAppContextSetup(context).apply(springSecurity()).build().perform(builder);
    }
}
