package org.synyx.urlaubsverwaltung.person.api;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.api.ApiExceptionHandlerControllerAdvice;
import org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class PersonControllerTest {

    private MockMvc mockMvc;

    private PersonService personServiceMock;

    @Before
    public void setUp() {

        personServiceMock = mock(PersonService.class);

        mockMvc = MockMvcBuilders.standaloneSetup(new PersonController(personServiceMock))
            .setControllerAdvice(new ApiExceptionHandlerControllerAdvice())
                .build();
    }


    @Test
    public void ensureReturnsAllActivePersons() throws Exception {

        Person person1 = TestDataCreator.createPerson("foo");
        Person person2 = TestDataCreator.createPerson("bar");

        when(personServiceMock.getActivePersons()).thenReturn(Arrays.asList(person1, person2));

        mockMvc.perform(get("/api/persons"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json;charset=UTF-8"))
            .andExpect(jsonPath("$.response").exists())
            .andExpect(jsonPath("$.response.persons").exists())
            .andExpect(jsonPath("$.response.persons", hasSize(2)));

        verify(personServiceMock).getActivePersons();
    }


    @Test
    public void ensureReturnsListWithOneElementIfLoginNameSpecified() throws Exception {

        Person person = TestDataCreator.createPerson("muster");
        when(personServiceMock.getPersonByLogin(anyString())).thenReturn(Optional.of(person));

        mockMvc.perform(get("/api/persons").param("ldap", "muster"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json;charset=UTF-8"))
            .andExpect(jsonPath("$.response").exists())
            .andExpect(jsonPath("$.response.persons").exists())
            .andExpect(jsonPath("$.response.persons", hasSize(1)))
            .andExpect(jsonPath("$.response.persons[0].ldapName", is("muster")));

        verify(personServiceMock).getPersonByLogin("muster");
    }


    @Test
    public void ensureReturnsEmptyListForUnknownLoginName() throws Exception {

        when(personServiceMock.getPersonByLogin(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/persons").param("ldap", "muster"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json;charset=UTF-8"))
            .andExpect(jsonPath("$.response").exists())
            .andExpect(jsonPath("$.response.persons").exists())
            .andExpect(jsonPath("$.response.persons", hasSize(0)));

        verify(personServiceMock).getPersonByLogin("muster");
    }
}
