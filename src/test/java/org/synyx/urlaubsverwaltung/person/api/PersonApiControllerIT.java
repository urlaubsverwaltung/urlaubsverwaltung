package org.synyx.urlaubsverwaltung.person.api;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.synyx.urlaubsverwaltung.SingleTenantTestContainersBase;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.user.UserSettingsService;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.json.JsonCompareMode.STRICT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class PersonApiControllerIT extends SingleTenantTestContainersBase {

    private static final SimpleGrantedAuthority AUTH_USER = new SimpleGrantedAuthority("USER");

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private PersonService personService;
    @MockitoBean
    private UserSettingsService userSettingsService;

    @Test
    void ensureToReturnCurrentLoggedInPerson() throws Exception {

        final Person person = new Person("shane@example.org", "last", "shane", "shane@example.org");
        person.setId(1L);

        when(personService.getPersonByUsername("shane@example.org")).thenReturn(Optional.of(person));

        perform(
            get("/api/persons/me")
                .with(oidcLogin().idToken(builder -> builder.subject("shane@example.org")).authorities(AUTH_USER))
                .accept(HAL_JSON_VALUE)
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(HAL_JSON_VALUE))
            .andExpect(content().json("""
                {
                  "id": 1,
                  "email": "shane@example.org",
                  "firstName": "shane",
                  "lastName": "last",
                  "niceName": "shane last",
                  "active": true,
                  "_links":{
                    "self":{
                      "href":"http://localhost/api/persons/1"
                    },
                    "absences":{
                      "href":"http://localhost/api/persons/1/absences?from={from}&to={to}&absence-types=vacation&absence-types=sick_note&absence-types=public_holiday&absence-types=no_workday",
                      "templated":true
                      },
                    "sicknotes":{
                      "href":"http://localhost/api/persons/1/sicknotes?from={from}&to={to}",
                      "templated":true
                    },
                    "vacations":{
                      "href":"http://localhost/api/persons/1/vacations?from={from}&to={to}&status=waiting&status=temporary_allowed&status=allowed&status=allowed_cancellation_requested",
                      "templated":true
                    },
                    "workdays":{
                      "href":"http://localhost/api/persons/1/workdays?from={from}&to={to}{&length}",
                      "templated":true
                    }
                  }
                }
                """, STRICT));
    }

    @Test
    void ensureToReturn404IfOidcIsNull() throws Exception {
        perform(
            get("/api/persons/me")
                .with(oauth2Login().authorities(AUTH_USER))
                .accept(HAL_JSON_VALUE)
        )
            .andExpect(status().isNotFound());
    }

    @Test
    void ensureReturnSpecificPerson() throws Exception {

        final Person shane = new Person("shane", "shane", "shane", "shane@example.org");
        shane.setId(1L);

        when(personService.getPersonByID(shane.getId())).thenReturn(Optional.of(shane));

        perform(get("/api/persons/1")
            .with(oidcLogin().idToken(builder -> builder.subject("shane@example.org")).authorities(AUTH_USER, new SimpleGrantedAuthority("OFFICE")))
            .accept(HAL_JSON_VALUE)
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(HAL_JSON_VALUE))
            .andExpect(content().json("""
                {
                  "id": 1,
                  "email": "shane@example.org",
                  "firstName": "shane",
                  "lastName": "shane",
                  "niceName": "shane shane",
                  "active": true,
                  "_links":{
                    "self":{
                      "href":"http://localhost/api/persons/1"
                    },
                    "absences":{
                      "href":"http://localhost/api/persons/1/absences?from={from}&to={to}&absence-types=vacation&absence-types=sick_note&absence-types=public_holiday&absence-types=no_workday",
                      "templated":true
                      },
                    "sicknotes":{
                      "href":"http://localhost/api/persons/1/sicknotes?from={from}&to={to}",
                      "templated":true
                    },
                    "vacations":{
                      "href":"http://localhost/api/persons/1/vacations?from={from}&to={to}&status=waiting&status=temporary_allowed&status=allowed&status=allowed_cancellation_requested",
                      "templated":true
                    },
                    "workdays":{
                      "href":"http://localhost/api/persons/1/workdays?from={from}&to={to}{&length}",
                      "templated":true
                    }
                  }
                }
                """, STRICT));
    }

    @Test
    void ensureReturnsAllActivePersons() throws Exception {

        final Person shane = new Person("shane@example.org", "shane", "shane", "shane@example.org");
        shane.setId(1L);
        final Person carl = new Person("carl@example.org", "carl", "carl", "carl@example.org");
        carl.setId(2L);

        when(personService.getActivePersons()).thenReturn(List.of(shane, carl));

        perform(get("/api/persons")
            .with(oidcLogin().idToken(builder -> builder.subject("shane@example.org")).authorities(AUTH_USER, new SimpleGrantedAuthority("OFFICE")))
            .accept(HAL_JSON_VALUE)
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(HAL_JSON_VALUE))
            .andExpect(content().json("""
                {
                   "persons": [
                     {
                       "id": 1,
                       "email": "shane@example.org",
                       "firstName": "shane",
                       "lastName": "shane",
                       "niceName": "shane shane",
                       "active": true,
                       "_links": {
                         "self": {
                           "href": "http://localhost/api/persons/1"
                         },
                         "absences": {
                           "href": "http://localhost/api/persons/1/absences?from={from}&to={to}&absence-types=vacation&absence-types=sick_note&absence-types=public_holiday&absence-types=no_workday",
                           "templated": true
                         },
                         "sicknotes": {
                           "href": "http://localhost/api/persons/1/sicknotes?from={from}&to={to}",
                           "templated": true
                         },
                         "vacations": {
                           "href": "http://localhost/api/persons/1/vacations?from={from}&to={to}&status=waiting&status=temporary_allowed&status=allowed&status=allowed_cancellation_requested",
                           "templated": true
                         },
                         "workdays": {
                           "href": "http://localhost/api/persons/1/workdays?from={from}&to={to}{&length}",
                           "templated": true
                         }
                       }
                     },
                     {
                       "id": 2,
                       "email": "carl@example.org",
                       "firstName": "carl",
                       "lastName": "carl",
                       "niceName": "carl carl",
                       "active": true,
                       "_links": {
                         "self": {
                           "href": "http://localhost/api/persons/2"
                         },
                         "absences": {
                           "href": "http://localhost/api/persons/2/absences?from={from}&to={to}&absence-types=vacation&absence-types=sick_note&absence-types=public_holiday&absence-types=no_workday",
                           "templated": true
                         },
                         "sicknotes": {
                           "href": "http://localhost/api/persons/2/sicknotes?from={from}&to={to}",
                           "templated": true
                         },
                         "vacations": {
                           "href": "http://localhost/api/persons/2/vacations?from={from}&to={to}&status=waiting&status=temporary_allowed&status=allowed&status=allowed_cancellation_requested",
                           "templated": true
                         },
                         "workdays": {
                           "href": "http://localhost/api/persons/2/workdays?from={from}&to={to}{&length}",
                           "templated": true
                         }
                       }
                     }
                   ]
                 }
                """, STRICT));
    }

    @Test
    void ensureReturnsInactivePersons() throws Exception {

        final Person carl = new Person("carl@example.org", "carl", "carl", "carl@example.org");
        carl.setPermissions(List.of(Role.INACTIVE));
        carl.setId(2L);

        when(personService.getInactivePersons()).thenReturn(List.of(carl));

        perform(get("/api/persons").queryParam("active", "false")
            .with(oidcLogin().idToken(builder -> builder.subject("shane@example.org")).authorities(AUTH_USER, new SimpleGrantedAuthority("OFFICE")))
            .accept(HAL_JSON_VALUE)
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(HAL_JSON_VALUE))
            .andExpect(content().json("""
                {
                   "persons": [
                     {
                       "id": 2,
                       "email": "carl@example.org",
                       "firstName": "carl",
                       "lastName": "carl",
                       "niceName": "carl carl",
                       "active": false,
                       "_links": {
                         "self": {
                           "href": "http://localhost/api/persons/2"
                         },
                         "absences": {
                           "href": "http://localhost/api/persons/2/absences?from={from}&to={to}&absence-types=vacation&absence-types=sick_note&absence-types=public_holiday&absence-types=no_workday",
                           "templated": true
                         },
                         "sicknotes": {
                           "href": "http://localhost/api/persons/2/sicknotes?from={from}&to={to}",
                           "templated": true
                         },
                         "vacations": {
                           "href": "http://localhost/api/persons/2/vacations?from={from}&to={to}&status=waiting&status=temporary_allowed&status=allowed&status=allowed_cancellation_requested",
                           "templated": true
                         },
                         "workdays": {
                           "href": "http://localhost/api/persons/2/workdays?from={from}&to={to}{&length}",
                           "templated": true
                         }
                       }
                     }
                   ]
                 }
                """, STRICT));
    }

    @Test
    void ensureToCreateNewPerson() throws Exception {

        when(personService.getPersonByUsername("shane@example.org")).thenReturn(Optional.empty());

        final Person createdPerson = new Person("shane@example.org", "last", "shane", "shane@example.org");
        createdPerson.setId(1L);
        when(personService.create("shane@example.org", "shane", "last", "shane@example.org")).thenReturn(createdPerson);

        perform(
            post("/api/persons")
                .with(oidcLogin().idToken(builder -> builder.subject("shane@example.org")).authorities(AUTH_USER, new SimpleGrantedAuthority("PERSON_ADD")))
                .content(asJsonString(new PersonProvisionDto("shane", "last", "shane@example.org")))
                .contentType(APPLICATION_JSON)
                .accept(HAL_JSON_VALUE)
        )
            .andExpect(status().isCreated())
            .andExpect(content().contentType(HAL_JSON_VALUE))
            .andExpect(content().json("""
                {
                  "id": 1,
                  "email": "shane@example.org",
                  "firstName": "shane",
                  "lastName": "last",
                  "niceName": "shane last",
                  "active": true,
                  "_links":{
                    "self":{
                      "href":"http://localhost/api/persons/1"
                    },
                    "absences":{
                      "href":"http://localhost/api/persons/1/absences?from={from}&to={to}&absence-types=vacation&absence-types=sick_note&absence-types=public_holiday&absence-types=no_workday",
                      "templated":true
                      },
                    "sicknotes":{
                      "href":"http://localhost/api/persons/1/sicknotes?from={from}&to={to}",
                      "templated":true
                    },
                    "vacations":{
                      "href":"http://localhost/api/persons/1/vacations?from={from}&to={to}&status=waiting&status=temporary_allowed&status=allowed&status=allowed_cancellation_requested",
                      "templated":true
                    },
                    "workdays":{
                      "href":"http://localhost/api/persons/1/workdays?from={from}&to={to}{&length}",
                      "templated":true
                    }
                  }
                }
                """, STRICT));
    }

    @Test
    void ensureToReturnConflictIfPersonDoesAlreadyExists() throws Exception {

        when(personService.getPersonByUsername("shane@example.org")).thenReturn(Optional.of(new Person()));

        perform(
            post("/api/persons")
                .with(oidcLogin().idToken(builder -> builder.subject("shane@example.org")).authorities(AUTH_USER, new SimpleGrantedAuthority("PERSON_ADD")))
                .content(asJsonString(new PersonProvisionDto("shane", "last", "shane@example.org")))
                .contentType(APPLICATION_JSON)
                .accept(HAL_JSON_VALUE)
        )
            .andExpect(status().isConflict());
    }

    @Nested
    class UpdateSettings {

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void ensureToUpdateSettings(boolean navigationCollapsed) throws Exception {

            final String username = "user@example.org";
            final Person user = new Person(username, "last", "shane", username);
            user.setId(100L);

            when(personService.getPersonByUsername(username)).thenReturn(Optional.of(user));

            perform(
                patch("/api/persons/me/settings")
                    .with(oidcLogin().idToken(builder -> builder.subject(username)).authorities(AUTH_USER))
                    .content(asJsonString(new PersonSettingsDto(navigationCollapsed)))
                    .contentType(APPLICATION_JSON)
            )
                .andExpect(status().isNoContent());

            verify(userSettingsService).updateNavigationState(user, navigationCollapsed);
        }

        @Test
        void ensureToUpdateSettingsWithoutNavigationCollapsed() throws Exception {

            final String username = "user@example.org";
            final Person user = new Person(username, "last", "shane", username);
            user.setId(100L);

            when(personService.getPersonByUsername(username)).thenReturn(Optional.of(user));

            perform(
                patch("/api/persons/me/settings")
                    .with(oidcLogin().idToken(builder -> builder.subject(username)).authorities(AUTH_USER))
                    .content(asJsonString(new PersonSettingsDto(null)))
                    .contentType(APPLICATION_JSON)
            )
                .andExpect(status().isNoContent());

            verifyNoInteractions(userSettingsService);
        }

        @Test
        void ensureToUpdateSettingsReturnsNotFoundWhenOidcUserIsNull() throws Exception {

            perform(
                patch("/api/persons/me/settings")
                    .with(oauth2Login().authorities(AUTH_USER))
                    .content(asJsonString(new PersonSettingsDto(true)))
                    .contentType(APPLICATION_JSON)
            )
                .andExpect(status().isNotFound());

            verifyNoInteractions(userSettingsService);
        }

        @Test
        void ensureToUpdateSettingsReturnsNotFoundWhenPersonNotFound() throws Exception {

            final String username = "user@example.org";
            final Person unknown = new Person(username, "last", "unknown", username);
            unknown.setId(101L);

            perform(
                patch("/api/persons/me/settings")
                    .with(oidcLogin().idToken(builder -> builder.subject(username)).authorities(AUTH_USER))
                    .content(asJsonString(new PersonSettingsDto(true)))
                    .contentType(APPLICATION_JSON)
            )
                .andExpect(status().isNotFound());

            verifyNoInteractions(userSettingsService);
        }
    }

    public static String asJsonString(final Object obj) {
        try {
            return new JsonMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build().perform(builder);
    }
}
