package org.synyx.urlaubsverwaltung.person.api;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.api.ApiExceptionHandlerControllerAdvice;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;


public class PersonApiControllerTest {

    private PersonService personServiceMock;

    @Before
    public void setUp() {

        personServiceMock = mock(PersonService.class);
    }


    @Test
    public void ensureReturnsAllActivePersons() throws Exception {

        Person person1 = TestDataCreator.createPerson("foo");
        person1.setId(1);
        Person person2 = TestDataCreator.createPerson("bar");
        person2.setId(2);

        when(personServiceMock.getActivePersons()).thenReturn(Arrays.asList(person1, person2));

        perform(get("/api/persons"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json;charset=UTF-8"))
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
    public void ensureReturnSpecificPerson() throws Exception {

        Person person1 = TestDataCreator.createPerson("foo");
        person1.setId(42);

        when(personServiceMock.getPersonByID(person1.getId())).thenReturn(Optional.of(person1));

        perform(get("/api/persons/42"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json;charset=UTF-8"))
            .andExpect(jsonPath("$.firstName", is("Foo")))
            .andExpect(jsonPath("$.lastName", is("Foo")))
            .andExpect(jsonPath("$.email", is("foo@test.de")))
            .andExpect(jsonPath("$.links..rel", hasItem("self")))
            .andExpect(jsonPath("$.links..href", hasItem(endsWith("/api/persons/42"))))
            .andExpect(jsonPath("$.links..rel", hasItem("availabilities")))
            .andExpect(jsonPath("$.links..href", hasItem(endsWith("/api/persons/42/availabilities?from={from}&to={to}"))));
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {

        return standaloneSetup(new PersonApiController(personServiceMock))
            .setControllerAdvice(new ApiExceptionHandlerControllerAdvice())
            .build().perform(builder);
    }
}
