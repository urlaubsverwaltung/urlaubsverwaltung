package org.synyx.urlaubsverwaltung.person.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.api.ApiExceptionHandlerControllerAdvice;
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
import static org.synyx.urlaubsverwaltung.demodatacreator.DemoDataCreator.createPerson;

@ExtendWith(MockitoExtension.class)
class PersonApiControllerTest {

    @Mock
    private PersonService personService;

    @Test
    void ensureReturnsAllActivePersons() throws Exception {

        final Person person1 = createPerson("foo");
        person1.setId(1);
        final Person person2 = createPerson("bar");
        person2.setId(2);

        when(personService.getActivePersons()).thenReturn(List.of(person1, person2));

        perform(get("/api/persons"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$.[0].firstName", is("Foo")))
            .andExpect(jsonPath("$.[0].links..rel", hasItem("self")))
            .andExpect(jsonPath("$.[0].links..href", hasItem(endsWith("/api/persons/1"))))
            .andExpect(jsonPath("$.[0].links..rel", hasItem("availabilities")))
            .andExpect(jsonPath("$.[0].links..href", hasItem(endsWith("/api/persons/1/availabilities?from={from}&to={to}"))))
            .andExpect(jsonPath("$.[0].email", is("foo@test.de")))
            .andExpect(jsonPath("$.[1].lastName", is("Bar")))
            .andReturn();
    }

    @Test
    void ensureReturnSpecificPerson() throws Exception {

        final Person person1 = createPerson("foo");
        person1.setId(42);

        when(personService.getPersonByID(person1.getId())).thenReturn(Optional.of(person1));

        perform(get("/api/persons/42"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.firstName", is("Foo")))
            .andExpect(jsonPath("$.lastName", is("Foo")))
            .andExpect(jsonPath("$.email", is("foo@test.de")))
            .andExpect(jsonPath("$.links..rel", hasItem("self")))
            .andExpect(jsonPath("$.links..href", hasItem(endsWith("/api/persons/42"))))
            .andExpect(jsonPath("$.links..rel", hasItem("availabilities")))
            .andExpect(jsonPath("$.links..href", hasItem(endsWith("/api/persons/42/availabilities?from={from}&to={to}"))));
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(new PersonApiController(personService))
            .setControllerAdvice(new ApiExceptionHandlerControllerAdvice())
            .build().perform(builder);
    }
}
