package org.synyx.urlaubsverwaltung.person.api;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@SpringBootTest
class PersonApiControllerSecurityIT extends SingleTenantTestContainersBase {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private PersonService personService;
    @MockitoBean
    private DepartmentService departmentService;

    @Test
    void ensureAccessIsUnAuthorizedIfNoAuthenticationIsAvailableOnCurrentPerson() throws Exception {
        perform(
            get("/api/persons/me")
        )
            .andExpect(status().is4xxClientError());
    }

    @Test
    void ensureAccessIsOkAsOfficeForOtherOnCurrentPerson() throws Exception {

        when(personService.getPersonByUsername("shane@example.org")).thenReturn(Optional.of(new Person()));

        perform(get("/api/persons/me")
            .with(oidcLogin().idToken(builder -> builder.subject("shane@example.org")).authorities(new SimpleGrantedAuthority("USER")))
        ).andExpect(status().isOk());
    }

    @Test
    void ensureAccessIsUnAuthorizedIfNoAuthenticationIsAvailableOnSpecificPerson() throws Exception {
        perform(
            get("/api/persons/1")
        )
            .andExpect(status().is4xxClientError());
    }

    @ParameterizedTest
    @ValueSource(strings = {"USER", "INACTIVE"})
    void ensureAccessIsForbiddenForOtherUsersOnSpecificPerson(final String role) throws Exception {
        perform(get("/api/persons/1")
            .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority(role)))
        ).andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @ValueSource(strings = {"OFFICE", "BOSS"})
    void ensureAccessIsOkAsOfficeForOtherOnSpecificPerson(final String role) throws Exception {

        when(personService.getPersonByID(1L)).thenReturn(Optional.of(new Person()));

        perform(get("/api/persons/1")
            .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority(role)))
        ).andExpect(status().isOk());
    }

    @Test
    void ensureAccessIsOkForOtherUsersOnSpecificPersonAsDepartmentHead() throws Exception {
        final Person person = new Person();
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        final Person departmentHead = new Person();
        departmentHead.setPermissions(List.of(USER, DEPARTMENT_HEAD));
        when(personService.getPersonByUsername("departmentHead")).thenReturn(Optional.of(departmentHead));
        when(departmentService.isDepartmentHeadAllowedToManagePerson(departmentHead, person)).thenReturn(true);

        perform(
            get("/api/persons/1")
                .with(oidcLogin().idToken(builder -> builder.subject("departmentHead")).authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority("DEPARTMENT_HEAD")))
        )
            .andExpect(status().isOk());
    }

    @Test
    void ensureAccessIsOkForOtherUsersOnSpecificPersonAsSecondStageAuthority() throws Exception {
        final Person person = new Person();
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        final Person departmentHead = new Person();
        departmentHead.setPermissions(List.of(USER, DEPARTMENT_HEAD));
        when(personService.getPersonByUsername("ssa")).thenReturn(Optional.of(departmentHead));
        when(departmentService.isDepartmentHeadAllowedToManagePerson(departmentHead, person)).thenReturn(true);

        perform(
            get("/api/persons/1")
                .with(oidcLogin().idToken(builder -> builder.subject("ssa")).authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority("SECOND_STAGE_AUTHORITY")))
        )
            .andExpect(status().isOk());
    }

    @Test
    void ensureAccessIsOkForOtherUsersOnSpecificPersonAsSameUser() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        perform(
            get("/api/persons/1")
                .with(oidcLogin().idToken(builder -> builder.subject("user")).authorities(new SimpleGrantedAuthority("USER")))
        )
            .andExpect(status().isOk());
    }

    @Test
    void ensureAccessIsForbiddenForOtherUsersOnSpecificPersonAsDifferentUser() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        perform(
            get("/api/persons/1")
                .with(oidcLogin().idToken(builder -> builder.subject("differentUser")).authorities(new SimpleGrantedAuthority("USER")))
        )
            .andExpect(status().isForbidden());
    }

    @Test
    void ensureAccessIsUnAuthorizedIfNoAuthenticationIsAvailableOnPersonsUsers() throws Exception {
        perform(
            get("/api/persons")
        )
            .andExpect(status().is4xxClientError());
    }

    @ParameterizedTest
    @ValueSource(strings = {"USER", "DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY", "INACTIVE"})
    void ensureAccessIsForbiddenForOtherOnPersonsUsers(final String role) throws Exception {
        perform(get("/api/persons")
            .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority(role)))
        ).andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @ValueSource(strings = {"OFFICE", "BOSS"})
    void ensureAccessIsOkAsOfficeForOtherOnPersonsUsers(final String role) throws Exception {
        perform(get("/api/persons")
            .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority(role)))
        ).andExpect(status().isOk());
    }

    @Test
    void ensureAccessIsUnAuthorizedIfPersonWantsToCreateNewUserWithoutAuthorization() throws Exception {
        perform(
            post("/api/persons")
        )
            .andExpect(status().is4xxClientError());
    }

    @ParameterizedTest
    @ValueSource(strings = {"USER", "DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY", "BOSS", "INACTIVE"})
    void ensureAccessIsForbiddenForUserWithoutRolePersonAdd(final String role) throws Exception {
        perform(post("/api/persons")
            .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority(role)))
            .content(asJsonString(new PersonProvisionDto("shane", "last", "shane@example.org")))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
        ).andExpect(status().isForbidden());
    }

    @Test
    void ensureAccessIsAllowedForPersonWithRolePersonAdd() throws Exception {

        when(personService.getPersonByUsername("shane@example.org")).thenReturn(Optional.empty());

        final Person createdPerson = new Person("shane@example.org", "last", "shane", "shane@example.org");
        createdPerson.setId(2L);
        when(personService.create("shane@example.org", "shane", "last", "shane@example.org")).thenReturn(createdPerson);

        perform(post("/api/persons")
            .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority("PERSON_ADD")))
            .content(asJsonString(new PersonProvisionDto("shane", "last", "shane@example.org")))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
        ).andExpect(status().isCreated());
    }

    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build().perform(builder);
    }
}
