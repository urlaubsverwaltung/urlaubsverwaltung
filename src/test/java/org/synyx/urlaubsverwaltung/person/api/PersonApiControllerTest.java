package org.synyx.urlaubsverwaltung.person.api;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
        shane.setId(1);
        final Person carl = new Person("carl", "carl", "carl", "carl@example.org");
        carl.setId(2);

        when(personService.getActivePersons()).thenReturn(List.of(shane, carl));

        perform(get("/api/persons"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$.[0].firstName", is("shane")))
            .andExpect(jsonPath("$.[0].links..rel", hasItem("self")))
            .andExpect(jsonPath("$.[0].links..href", hasItem(endsWith("/api/persons/1"))))
            .andExpect(jsonPath("$.[0].links..rel", hasItem("availabilities")))
            .andExpect(jsonPath("$.[0].links..href", hasItem(endsWith("/api/persons/1/availabilities?from={from}&to={to}"))))
            .andExpect(jsonPath("$.[0].email", is("shane@example.org")))
            .andExpect(jsonPath("$.[1].lastName", is("carl")))
            .andReturn();
    }

    @Test
    void ensureReturnSpecificPerson() throws Exception {

        final Person shane = new Person("shane", "shane", "shane", "shane@example.org");
        shane.setId(1);

        when(personService.getPersonByID(shane.getId())).thenReturn(Optional.of(shane));

        perform(get("/api/persons/1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.firstName", is("shane")))
            .andExpect(jsonPath("$.lastName", is("shane")))
            .andExpect(jsonPath("$.email", is("shane@example.org")))
            .andExpect(jsonPath("$.links..rel", hasItem("self")))
            .andExpect(jsonPath("$.links..href", hasItem(endsWith("/api/persons/1"))))
            .andExpect(jsonPath("$.links..rel", hasItem("availabilities")))
            .andExpect(jsonPath("$.links..href", hasItem(endsWith("/api/persons/1/availabilities?from={from}&to={to}"))));
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(new PersonApiController(personService))
            .setControllerAdvice(new RestControllerAdviceExceptionHandler())
            .build().perform(builder);
    }
}
