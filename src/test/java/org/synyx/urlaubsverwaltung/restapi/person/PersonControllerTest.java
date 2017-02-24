package org.synyx.urlaubsverwaltung.restapi.person;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.restapi.ApiExceptionHandlerControllerAdvice;
import org.synyx.urlaubsverwaltung.restapi.person.PersonController;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class PersonControllerTest {

    private MockMvc mockMvc;

    private PersonService personServiceMock;

    @Before
    public void setUp() {

        personServiceMock = Mockito.mock(PersonService.class);

        mockMvc = MockMvcBuilders.standaloneSetup(new PersonController(personServiceMock))
            .setControllerAdvice(new ApiExceptionHandlerControllerAdvice())
                .build();
    }


    @Test
    public void ensureReturnsAllActivePersons() throws Exception {

        Person person1 = TestDataCreator.createPerson("foo");
        Person person2 = TestDataCreator.createPerson("bar");

        Mockito.when(personServiceMock.getActivePersons()).thenReturn(Arrays.asList(person1, person2));

        mockMvc.perform(get("/api/persons"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json;charset=UTF-8"))
            .andExpect(jsonPath("$.response").exists())
            .andExpect(jsonPath("$.response.persons").exists())
            .andExpect(jsonPath("$.response.persons", hasSize(2)));

        Mockito.verify(personServiceMock).getActivePersons();
    }


    @Test
    public void ensureReturnsListWithOneElementIfLoginNameSpecified() throws Exception {

        Person person = TestDataCreator.createPerson("muster");
        Mockito.when(personServiceMock.getPersonByLogin(Mockito.anyString())).thenReturn(Optional.of(person));

        mockMvc.perform(get("/api/persons").param("ldap", "muster"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json;charset=UTF-8"))
            .andExpect(jsonPath("$.response").exists())
            .andExpect(jsonPath("$.response.persons").exists())
            .andExpect(jsonPath("$.response.persons", hasSize(1)))
            .andExpect(jsonPath("$.response.persons[0].ldapName", is("muster")));

        Mockito.verify(personServiceMock).getPersonByLogin("muster");
    }


    @Test
    public void ensureReturnsEmptyListForUnknownLoginName() throws Exception {

        Mockito.when(personServiceMock.getPersonByLogin(Mockito.anyString())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/persons").param("ldap", "muster"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json;charset=UTF-8"))
            .andExpect(jsonPath("$.response").exists())
            .andExpect(jsonPath("$.response.persons").exists())
            .andExpect(jsonPath("$.response.persons", hasSize(0)));

        Mockito.verify(personServiceMock).getPersonByLogin("muster");
    }
}
