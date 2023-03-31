package org.synyx.urlaubsverwaltung.person.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.UnknownPersonException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasProperty;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_APPLIED;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALL;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@ExtendWith(MockitoExtension.class)
class PersonNotificationsViewControllerTest {

    private PersonNotificationsViewController sut;

    private static final int UNKNOWN_PERSON_ID = 675;

    @Mock
    private PersonService personService;
    @Mock
    private PersonNotificationsDtoValidator validator;

    @BeforeEach
    void setUp() {
        sut = new PersonNotificationsViewController(personService, validator);
    }

    @Test
    void ensuresThatOnlyVisibleAndActive() throws Exception {

        final Person person = personWithId(1);
        person.setFirstName("Hans");
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_MANAGEMENT_ALL));
        person.setPermissions(List.of(USER, OFFICE));
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(person);

        perform(get("/web/person/{personId}/notifications", 1))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("personId", is(1))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationManagementAll", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationManagementAll", hasProperty("active", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationManagementDepartment", hasProperty("visible", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationManagementDepartment", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("application", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("application", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationUpcoming", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("applicationUpcoming", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("holidayReplacement", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("holidayReplacement", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("holidayReplacementUpcoming", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("holidayReplacementUpcoming", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("personNewManagementAll", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("personNewManagementAll", hasProperty("active", is(false)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("overtimeManagementAll", hasProperty("visible", is(true)))))
            .andExpect(model().attribute("personNotificationsDto", hasProperty("overtimeManagementAll", hasProperty("active", is(false)))));
    }

    @Test
    void showPersonNotificationsForUnknownIdThrowsUnknownPersonException() {
        assertThatThrownBy(() ->
            perform(get("/web/person/{personId}/notifications", UNKNOWN_PERSON_ID)))
            .hasCauseInstanceOf(UnknownPersonException.class);
    }

    @Test
    void editPersonNotificationsForUnknownIdThrowsUnknownPersonException() {
        assertThatThrownBy(() ->
            perform(post("/web/person/{personId}/notifications", UNKNOWN_PERSON_ID)))
            .hasCauseInstanceOf(UnknownPersonException.class);
    }

    @Test
    void ensuresWhenEditingPersonNotificationsThatItWillBeSavedAndRedirected() throws Exception {

        final Person personWithoutNotifications = new Person();
        personWithoutNotifications.setId(1);
        personWithoutNotifications.setFirstName("Hans");
        personWithoutNotifications.setNotifications(List.of());
        when(personService.getPersonByID(1)).thenReturn(Optional.of(personWithoutNotifications));

        perform(
            post("/web/person/{personId}/notifications", 1)
                .param("personId", "1")
                .param("application.visible", "true")
                .param("application.active", "true")
        )
            .andExpect(redirectedUrl("/web/person/1/notifications"))
            .andExpect(flash().attribute("success", true));

        final ArgumentCaptor<Person> personArgumentCaptor = ArgumentCaptor.forClass(Person.class);
        verify(personService).update(personArgumentCaptor.capture());
        final Person savedPerson = personArgumentCaptor.getValue();
        assertThat(savedPerson.getNotifications()).contains(NOTIFICATION_EMAIL_APPLICATION_APPLIED);
    }

    @Test
    void ensuresWhenEditingPersonNotificationsWithMismatchingIdReturnsNotFoundStatus() throws Exception {

        final Person person = new Person();
        person.setId(1);
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        perform(post("/web/person/{personId}/notifications", 1)
            .param("personId", "2"))
            .andExpect(status().isNotFound());
    }

    @Test
    void ensuresWhenEditingPersonNotificationsHasValidationError() throws Exception {

        final Person personWithoutNotifications = new Person();
        personWithoutNotifications.setId(1);
        personWithoutNotifications.setFirstName("Hans");
        personWithoutNotifications.setNotifications(List.of());
        when(personService.getPersonByID(1)).thenReturn(Optional.of(personWithoutNotifications));

        doAnswer(invocation -> {
            final Errors errors = invocation.getArgument(1);
            errors.reject("errors");
            return null;
        }).when(validator).validate(any(), any());

        perform(post("/web/person/{personId}/notifications", 1)
            .param("personId", "1")
            .param("application.visible", "true")
            .param("application.active", "true")
        ).andExpect(view().name("person/person_notifications"));

        verify(personService, never()).update(personWithoutNotifications);
    }

    private static Person personWithId(int personId) {
        final Person person = new Person();
        person.setId(personId);
        return person;
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
