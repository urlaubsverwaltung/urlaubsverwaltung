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

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$.[0].id", is(1)))
            .andExpect(jsonPath("$.[0].firstName", is("shane")))
            .andExpect(jsonPath("$.[0].links..rel", hasItem("self")))
            .andExpect(jsonPath("$.[0].links..href", hasItem(endsWith("/api/persons/1"))))
            .andExpect(jsonPath("$.[0].links..rel", hasItem("absences")))
            .andExpect(jsonPath("$.[0].links..href", hasItem(endsWith("/api/persons/1/absences?from={from}&to={to}&noWorkdaysInclusive=false{&type}"))))
            .andExpect(jsonPath("$.[0].links..rel", hasItem("availabilities")))
            .andExpect(jsonPath("$.[0].links..href", hasItem(endsWith("/api/persons/1/availabilities?from={from}&to={to}"))))
            .andExpect(jsonPath("$.[0].links..rel", hasItem("sicknotes")))
            .andExpect(jsonPath("$.[0].links..href", hasItem(endsWith("/api/persons/1/sicknotes?from={from}&to={to}"))))
            .andExpect(jsonPath("$.[0].links..rel", hasItem("vacations")))
            .andExpect(jsonPath("$.[0].links..href", hasItem(endsWith("/api/persons/1/vacations?from={from}&to={to}"))))
            .andExpect(jsonPath("$.[0].links..rel", hasItem("workdays")))
            .andExpect(jsonPath("$.[0].links..href", hasItem(endsWith("/api/persons/1/workdays?from={from}&to={to}{&length}"))))
            .andExpect(jsonPath("$.[1].id", is(2)))
            .andExpect(jsonPath("$.[1].firstName", is("carl")))
            .andExpect(jsonPath("$.[1].links..rel", hasItem("self")))
            .andExpect(jsonPath("$.[1].links..href", hasItem(endsWith("/api/persons/2"))))
            .andExpect(jsonPath("$.[1].links..rel", hasItem("absences")))
            .andExpect(jsonPath("$.[1].links..href", hasItem(endsWith("/api/persons/2/absences?from={from}&to={to}&noWorkdaysInclusive=false{&type}"))))
            .andExpect(jsonPath("$.[1].links..rel", hasItem("availabilities")))
            .andExpect(jsonPath("$.[1].links..href", hasItem(endsWith("/api/persons/2/availabilities?from={from}&to={to}"))))
            .andExpect(jsonPath("$.[1].links..rel", hasItem("sicknotes")))
            .andExpect(jsonPath("$.[1].links..href", hasItem(endsWith("/api/persons/2/sicknotes?from={from}&to={to}"))))
            .andExpect(jsonPath("$.[1].links..rel", hasItem("vacations")))
            .andExpect(jsonPath("$.[1].links..href", hasItem(endsWith("/api/persons/2/vacations?from={from}&to={to}"))))
            .andExpect(jsonPath("$.[1].links..rel", hasItem("workdays")))
            .andExpect(jsonPath("$.[1].links..href", hasItem(endsWith("/api/persons/2/workdays?from={from}&to={to}{&length}"))))
            .andReturn();
    }

    @Test
    void ensureReturnSpecificPerson() throws Exception {

        final Person shane = new Person("shane", "shane", "shane", "shane@example.org");
        shane.setId(1L);

        when(personService.getPersonByID(shane.getId())).thenReturn(Optional.of(shane));

        perform(get("/api/persons/1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.firstName", is("shane")))
            .andExpect(jsonPath("$.lastName", is("shane")))
            .andExpect(jsonPath("$.email", is("shane@example.org")))
            .andExpect(jsonPath("$.links..rel", hasItem("self")))
            .andExpect(jsonPath("$.links..href", hasItem(endsWith("/api/persons/1"))))
            .andExpect(jsonPath("$.links..rel", hasItem("absences")))
            .andExpect(jsonPath("$.links..href", hasItem(endsWith("/api/persons/1/absences?from={from}&to={to}&noWorkdaysInclusive=false{&type}"))))
            .andExpect(jsonPath("$.links..rel", hasItem("availabilities")))
            .andExpect(jsonPath("$.links..href", hasItem(endsWith("/api/persons/1/availabilities?from={from}&to={to}"))))
            .andExpect(jsonPath("$.links..rel", hasItem("sicknotes")))
            .andExpect(jsonPath("$.links..href", hasItem(endsWith("/api/persons/1/sicknotes?from={from}&to={to}"))))
            .andExpect(jsonPath("$.links..rel", hasItem("vacations")))
            .andExpect(jsonPath("$.links..href", hasItem(endsWith("/api/persons/1/vacations?from={from}&to={to}"))))
            .andExpect(jsonPath("$.links..rel", hasItem("workdays")))
            .andExpect(jsonPath("$.links..href", hasItem(endsWith("/api/persons/1/workdays?from={from}&to={to}{&length}"))));
    }

    @Test
    void ensureToCreateNewPerson() throws Exception {

        when(personService.getPersonByUsername("shane@example.org")).thenReturn(Optional.empty());

        final Person createdPerson = new Person("shane@example.org", "last", "shane", "shane@example.org");
        createdPerson.setId(1L);
        when(personService.create("shane@example.org", "shane", "last", "shane@example.org")).thenReturn(createdPerson);

        perform(post("/api/persons")
            .content(asJsonString(new PersonProvisionDto("shane", "last", "shane@example.org")))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
        )
            .andExpect(status().isCreated())
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.firstName", is("shane")))
            .andExpect(jsonPath("$.lastName", is("last")))
            .andExpect(jsonPath("$.email", is("shane@example.org")))
            .andExpect(jsonPath("$.links..rel", hasItem("self")))
            .andExpect(jsonPath("$.links..href", hasItem(endsWith("/api/persons/1"))))
            .andExpect(jsonPath("$.links..rel", hasItem("absences")))
            .andExpect(jsonPath("$.links..href", hasItem(endsWith("/api/persons/1/absences?from={from}&to={to}&noWorkdaysInclusive=false{&type}"))))
            .andExpect(jsonPath("$.links..rel", hasItem("availabilities")))
            .andExpect(jsonPath("$.links..href", hasItem(endsWith("/api/persons/1/availabilities?from={from}&to={to}"))))
            .andExpect(jsonPath("$.links..rel", hasItem("sicknotes")))
            .andExpect(jsonPath("$.links..href", hasItem(endsWith("/api/persons/1/sicknotes?from={from}&to={to}"))))
            .andExpect(jsonPath("$.links..rel", hasItem("vacations")))
            .andExpect(jsonPath("$.links..href", hasItem(endsWith("/api/persons/1/vacations?from={from}&to={to}"))))
            .andExpect(jsonPath("$.links..rel", hasItem("workdays")))
            .andExpect(jsonPath("$.links..href", hasItem(endsWith("/api/persons/1/workdays?from={from}&to={to}{&length}"))));
    }

    @Test
    void ensureToReturnConflictIfPersonDoesAlreadyExists() throws Exception {

        when(personService.getPersonByUsername("shane@example.org")).thenReturn(Optional.of(new Person()));

        perform(post("/api/persons")
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
