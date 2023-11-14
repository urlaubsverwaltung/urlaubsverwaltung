package org.synyx.urlaubsverwaltung.person.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.api.RestControllerAdviceExceptionHandler;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class PersonApiControllerTest {

    @Mock
    private PersonService personService;

    @Test
    void ensureReturnsAllActivePersons() throws Exception {

        final Person shane = new Person("shane", "shane", "shane", "shane@example.org");
        shane.setId(1L);
        final Person carl = new Person("carl", "carl", "carl", "carl@example.org");
        carl.setId(2L);

        when(personService.getActivePersons()).thenReturn(List.of(shane, carl));

        perform(get("/api/persons"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(content().json("""
            [
              {
                "id": 1,
                "email": "shane@example.org",
                "firstName": "shane",
                "lastName": "shane",
                "niceName": "shane shane",
                "links": [
                  {
                    "rel": "self",
                    "href": "http://localhost/api/persons/1"
                  },
                  {
                    "rel": "absences",
                    "href": "http://localhost/api/persons/1/absences?from={from}&to={to}&absence-types=vacation%2C%20sick_note%2C%20public_holiday%2C%20no_workday"
                  },
                  {
                    "rel": "sicknotes",
                    "href": "http://localhost/api/persons/1/sicknotes?from={from}&to={to}"
                  },
                  {
                    "rel": "vacations",
                    "href": "http://localhost/api/persons/1/vacations?from={from}&to={to}"
                  },
                  {
                    "rel": "workdays",
                    "href": "http://localhost/api/persons/1/workdays?from={from}&to={to}{&length}"
                  }
                ]
              },
              {
                "id": 2,
                "email": "carl@example.org",
                "firstName": "carl",
                "lastName": "carl",
                "niceName": "carl carl",
                "links": [
                  {
                    "rel": "self",
                    "href": "http://localhost/api/persons/2"
                  },
                  {
                    "rel": "absences",
                    "href": "http://localhost/api/persons/2/absences?from={from}&to={to}&absence-types=vacation%2C%20sick_note%2C%20public_holiday%2C%20no_workday"
                  },
                  {
                    "rel": "sicknotes",
                    "href": "http://localhost/api/persons/2/sicknotes?from={from}&to={to}"
                  },
                  {
                    "rel": "vacations",
                    "href": "http://localhost/api/persons/2/vacations?from={from}&to={to}"
                  },
                  {
                    "rel": "workdays",
                    "href": "http://localhost/api/persons/2/workdays?from={from}&to={to}{&length}"
                  }
                ]
              }
            ]
            """, true))
        ;
    }

    @Test
    void ensureReturnSpecificPerson() throws Exception {

        final Person shane = new Person("shane", "shane", "shane", "shane@example.org");
        shane.setId(1L);

        when(personService.getPersonByID(shane.getId())).thenReturn(Optional.of(shane));

        perform(get("/api/persons/1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(content().json("""
            {
              "id": 1,
              "email": "shane@example.org",
              "firstName": "shane",
              "lastName": "shane",
              "niceName": "shane shane",
              "links": [
                {
                  "rel": "self",
                  "href": "http://localhost/api/persons/1"
                },
                {
                  "rel": "absences",
                  "href": "http://localhost/api/persons/1/absences?from={from}&to={to}&absence-types=vacation%2C%20sick_note%2C%20public_holiday%2C%20no_workday"
                },
                {
                  "rel": "sicknotes",
                  "href": "http://localhost/api/persons/1/sicknotes?from={from}&to={to}"
                },
                {
                  "rel": "vacations",
                  "href": "http://localhost/api/persons/1/vacations?from={from}&to={to}"
                },
                {
                  "rel": "workdays",
                  "href": "http://localhost/api/persons/1/workdays?from={from}&to={to}{&length}"
                }
              ]
            }
            """, true));
    }

    @Test
    void ensureToCreateNewPerson() throws Exception {

        when(personService.getPersonByUsername("shane@example.org")).thenReturn(Optional.empty());

        final Person createdPerson = new Person("shane@example.org", "last", "shane", "shane@example.org");
        createdPerson.setId(1L);
        when(personService.create("shane@example.org", "shane", "last", "shane@example.org")).thenReturn(createdPerson);

        perform(
            post("/api/persons")
                .content(asJsonString(new PersonProvisionDto("shane", "last", "shane@example.org")))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
        )
            .andExpect(status().isCreated())
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(content().json("""
            {
              "id": 1,
              "email": "shane@example.org",
              "firstName": "shane",
              "lastName": "last",
              "niceName": "shane last",
              "links": [
                {
                  "rel": "self",
                  "href": "http://localhost/api/persons/1"
                },
                {
                  "rel": "absences",
                  "href": "http://localhost/api/persons/1/absences?from={from}&to={to}&absence-types=vacation%2C%20sick_note%2C%20public_holiday%2C%20no_workday"
                },
                {
                  "rel": "sicknotes",
                  "href": "http://localhost/api/persons/1/sicknotes?from={from}&to={to}"
                },
                {
                  "rel": "vacations",
                  "href": "http://localhost/api/persons/1/vacations?from={from}&to={to}"
                },
                {
                  "rel": "workdays",
                  "href": "http://localhost/api/persons/1/workdays?from={from}&to={to}{&length}"
                }
              ]
            }
            """, true));
    }

    @Test
    void ensureToReturnConflictIfPersonDoesAlreadyExists() throws Exception {

        when(personService.getPersonByUsername("shane@example.org")).thenReturn(Optional.of(new Person()));

        perform(
            post("/api/persons")
                .content(asJsonString(new PersonProvisionDto("shane", "last", "shane@example.org")))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
        )
            .andExpect(status().isConflict());
    }

    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(new PersonApiController(personService))
            .setControllerAdvice(new RestControllerAdviceExceptionHandler())
            .build().perform(builder);
    }
}
