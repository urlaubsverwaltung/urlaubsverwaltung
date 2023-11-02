package org.synyx.urlaubsverwaltung.person.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.synyx.urlaubsverwaltung.TestContainersBase;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class PersonApiControllerSecurityIT extends TestContainersBase {

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private PersonService personService;

    @Test
    void ensureAccessIsUnAuthorizedIfNoAuthenticationIsAvailableOnPersonsUsers() throws Exception {
        perform(
            get("/api/persons")
        )
            .andExpect(status().is4xxClientError());
    }

    @ParameterizedTest
    @ValueSource(strings = {"USER", "DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY", "BOSS", "ADMIN", "INACTIVE"})
    void ensureAccessIsForbiddenForOtherOnPersonsUsers(final String role) throws Exception {
        perform(get("/api/persons")
            .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority(role)))
        ).andExpect(status().isForbidden());
    }

    @Test
    void ensureAccessIsOkAsOfficeForOtherOnPersonsUsers() throws Exception {
        perform(get("/api/persons")
            .with(user("office").authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority("OFFICE")))
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
    @ValueSource(strings = {"USER", "DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY", "BOSS", "ADMIN", "INACTIVE"})
    void ensureAccessIsForbiddenForOtherUsersOnSpecificPerson(final String role) throws Exception {
        perform(get("/api/persons/1")
            .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority(role)))
        ).andExpect(status().isForbidden());
    }

    @Test
    void ensureAccessIsOkAsOfficeForOtherOnSpecificPerson() throws Exception {

        when(personService.getPersonByID(1L)).thenReturn(Optional.of(new Person()));

        perform(get("/api/persons/1")
            .with(user("office").authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority("OFFICE")))
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
    @ValueSource(strings = {"USER", "DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY", "BOSS", "ADMIN", "INACTIVE"})
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
