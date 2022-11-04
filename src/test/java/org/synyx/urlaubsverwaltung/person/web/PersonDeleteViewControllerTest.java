package org.synyx.urlaubsverwaltung.person.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;

@ExtendWith(MockitoExtension.class)
class PersonDeleteViewControllerTest {

    private PersonDeleteViewController sut;

    @Mock
    private PersonService personService;

    @BeforeEach
    void setUp() {
        sut = new PersonDeleteViewController(personService);
    }

    @Test
    void deletePerson() throws Exception {

        final Person person = new Person("username", "Muster", "Marlene", "muster@example.org");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        perform(post("/web/person/1/delete").param("niceNameConfirmation", "Marlene Muster"))
            .andExpect(redirectedUrl("/web/person/"))
            .andExpect(flash().attribute("personDeletionSuccess", "Marlene Muster"));

        verify(personService).delete(person);
    }

    @Test
    void deletePersonWithoutConfirmationDoesNotDeletePerson() throws Exception {

        final Person person = new Person("username", "Muster", "Marlene", "muster@example.org");
        person.setId(1);
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        perform(post("/web/person/1/delete").param("niceNameConfirmation", ""))
            .andExpect(redirectedUrl("/web/person/1#person-delete-form"))
            .andExpect(flash().attribute("personDeletionConfirmationValidationError", "person.account.dangerzone.delete.confirmation.validation.error.mismatch"));

        verify(personService).getPersonByID(1);
        verifyNoMoreInteractions(personService);
    }

    @Test
    void deletePersonIsLastOfficeIsNotAllowed() throws Exception {

        final Person person = new Person("username", "Muster", "Marlene", "muster@example.org");
        person.setId(1);
        person.setPermissions(List.of(OFFICE));
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        perform(post("/web/person/1/delete").param("niceNameConfirmation", "Marlene Muster"))
            .andExpect(redirectedUrl("/web/person/1#person-delete-form"))
            .andExpect(flash().attribute("personDeletionConfirmationValidationError", "person.account.dangerzone.delete.confirmation.validation.error.office"));

        verify(personService).getPersonByID(1);
        verify(personService).numberOfPersonsWithOfficeRoleExcludingPerson(1);
        verifyNoMoreInteractions(personService);
    }



    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
