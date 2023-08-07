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

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;
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
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        perform(post("/web/person/1/delete").param("delete", "true"))
            .andExpect(redirectedUrl("/web/person/1#person-delete-form"))
            .andExpect(flash().attribute("firstDeleteActionConfirmed", true));

        verifyNoMoreInteractions(personService);
    }

    @Test
    void deletePersonAjax() throws Exception {

        final Person person = new Person("username", "Muster", "Marlene", "muster@example.org");
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        perform(
            post("/web/person/1/delete")
                .param("delete", "true")
                .header("Turbo-Frame", "frame-delete-person")
        )
            .andExpect(model().attribute("person", person))
            .andExpect(model().attribute("lastOfficeUserCannotBeDeleted", false))
            .andExpect(model().attribute("firstDeleteActionConfirmed", true))
            .andExpect(view().name("person/detail-section/action-delete-person :: #frame-delete-person"));

        verifyNoMoreInteractions(personService);
    }

    @Test
    void deletePersonLastOfficeUser() throws Exception {

        final Person person = new Person("username", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);
        person.setPermissions(List.of(OFFICE));
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        perform(post("/web/person/1/delete").param("delete", "true"))
            .andExpect(redirectedUrl("/web/person/1#person-delete-form"))
            .andExpect(flash().attribute("lastOfficeUserCannotBeDeleted", true));

        verify(personService).numberOfPersonsWithOfficeRoleExcludingPerson(1);
        verifyNoMoreInteractions(personService);
    }

    @Test
    void deletePersonLastOfficeUserAjax() throws Exception {

        final Person person = new Person("username", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);
        person.setPermissions(List.of(OFFICE));
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        perform(
            post("/web/person/1/delete")
                .param("delete", "true")
                .header("Turbo-Frame", "frame-delete-person")
        )
            .andExpect(model().attribute("person", person))
            .andExpect(model().attribute("lastOfficeUserCannotBeDeleted", true))
            .andExpect(model().attribute("firstDeleteActionConfirmed", false))
            .andExpect(view().name("person/detail-section/action-delete-person :: #frame-delete-person"));

        verify(personService).numberOfPersonsWithOfficeRoleExcludingPerson(1);
        verifyNoMoreInteractions(personService);
    }

    @Test
    void deletePersonConfirmed() throws Exception {

        final Person signedInUser = new Person("signedInUser", "signed", "in", "user@example.org");
        when(personService.getSignedInUser()).thenReturn(signedInUser);
        final Person person = new Person("username", "Muster", "Marlene", "muster@example.org");
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        perform(post("/web/person/1/delete").param("niceNameConfirmation", "Marlene Muster"))
            .andExpect(redirectedUrl("/web/person?active=true"))
            .andExpect(flash().attribute("personDeletionSuccess", "Marlene Muster"));

        verify(personService).delete(person, signedInUser);
    }

    @Test
    void deletePersonConfirmedAjax() throws Exception {

        final Person signedInUser = new Person("signedInUser", "signed", "in", "user@example.org");
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        final Person person = new Person("username", "Muster", "Marlene", "muster@example.org");
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        perform(
            post("/web/person/1/delete")
                .param("niceNameConfirmation", "Marlene Muster")
                .header("Turbo-Frame", "frame-delete-person")
        )
            .andExpect(redirectedUrl("/web/person?active=true"))
            .andExpect(flash().attribute("personDeletionSuccess", "Marlene Muster"));

        verify(personService).delete(person, signedInUser);
    }

    @Test
    void ensureRedirectToInactivePersons() throws Exception {

        final Person signedInUser = new Person("signedInUser", "signed", "in", "user@example.org");
        when(personService.getSignedInUser()).thenReturn(signedInUser);
        final Person person = new Person("username", "Muster", "Marlene", "muster@example.org");
        person.setPermissions(List.of(INACTIVE));
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        perform(post("/web/person/1/delete").param("niceNameConfirmation", "Marlene Muster"))
            .andExpect(redirectedUrl("/web/person?active=false"))
            .andExpect(flash().attribute("personDeletionSuccess", "Marlene Muster"));

        verify(personService).delete(person, signedInUser);
    }

    @Test
    void deletePersonConfirmedWithoutConfirmationDoesNotDeletePerson() throws Exception {

        final Person person = new Person("username", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        perform(post("/web/person/1/delete").param("niceNameConfirmation", ""))
            .andExpect(redirectedUrl("/web/person/1#person-delete-form"))
            .andExpect(flash().attribute("personDeletionConfirmationValidationError", "person.account.dangerzone.delete.confirmation.validation.error.mismatch"));

        verify(personService).getPersonByID(1L);
        verifyNoMoreInteractions(personService);
    }

    @Test
    void deletePersonConfirmedWithoutConfirmationDoesNotDeletePersonAjax() throws Exception {

        final Person person = new Person("username", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        perform(
            post("/web/person/1/delete")
                .param("niceNameConfirmation", "")
                .header("Turbo-Frame", "frame-delete-person")
        )
            .andExpect(model().attribute("person", person))
            .andExpect(model().attribute("firstDeleteActionConfirmed", true))
            .andExpect(model().attribute("personDeletionConfirmationValidationError", "person.account.dangerzone.delete.confirmation.validation.error.mismatch"))
            .andExpect(view().name("person/detail-section/action-delete-person :: #frame-delete-person"));

        verify(personService).getPersonByID(1L);
        verifyNoMoreInteractions(personService);
    }

    @Test
    void deletePersonConfirmedIsLastOfficeIsNotAllowed() throws Exception {

        final Person person = new Person("username", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);
        person.setPermissions(List.of(OFFICE));
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        perform(post("/web/person/1/delete").param("niceNameConfirmation", "Marlene Muster"))
            .andExpect(redirectedUrl("/web/person/1#person-delete-form"))
            .andExpect(flash().attribute("personDeletionConfirmationValidationError", "person.account.dangerzone.delete.confirmation.validation.error.office"));

        verify(personService).getPersonByID(1L);
        verify(personService).numberOfPersonsWithOfficeRoleExcludingPerson(1);
        verifyNoMoreInteractions(personService);
    }

    @Test
    void deletePersonConfirmedIsLastOfficeIsNotAllowedAjax() throws Exception {

        final Person person = new Person("username", "Muster", "Marlene", "muster@example.org");
        person.setId(1L);
        person.setPermissions(List.of(OFFICE));
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        perform(
            post("/web/person/1/delete")
                .param("niceNameConfirmation", "Marlene Muster")
                .header("Turbo-Frame", "frame-delete-person")
        )
            .andExpect(model().attribute("person", person))
            .andExpect(model().attribute("firstDeleteActionConfirmed", true))
            .andExpect(model().attribute("personDeletionConfirmationValidationError", "person.account.dangerzone.delete.confirmation.validation.error.office"))
            .andExpect(view().name("person/detail-section/action-delete-person :: #frame-delete-person"));

        verify(personService).getPersonByID(1L);
        verify(personService).numberOfPersonsWithOfficeRoleExcludingPerson(1);
        verifyNoMoreInteractions(personService);
    }


    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
