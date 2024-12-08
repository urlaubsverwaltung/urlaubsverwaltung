package org.synyx.urlaubsverwaltung.person.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.context.WebApplicationContext;
import org.synyx.urlaubsverwaltung.SingleTenantTestContainersBase;
import org.synyx.urlaubsverwaltung.notification.UserNotificationSettings;
import org.synyx.urlaubsverwaltung.notification.UserNotificationSettingsService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_APPLICATION_ALLOWED;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@SpringBootTest
class PersonNotificationsViewControllerSecurityIT extends SingleTenantTestContainersBase {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private PersonService personService;

    @MockitoBean
    private UserNotificationSettingsService userNotificationSettingsService;

    @Test
    void personNotificationAsSameUserIsOk() throws Exception {

        final Person person = new Person();
        person.setId(1L);
        person.setUsername("user");
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_ALLOWED));
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(person);

        when(userNotificationSettingsService.findNotificationSettings(new PersonId(1L)))
            .thenReturn(new UserNotificationSettings(new PersonId(1L), false));

        perform(
            get("/web/person/1/notifications")
                .with(oidcLogin().idToken(builder -> builder.subject("user")).authorities(new SimpleGrantedAuthority("USER")))
        )
            .andExpect(status().isOk());
    }

    @Test
    void personNotificationAsOfficeUserForOtherUserIsOk() throws Exception {

        final Person person = new Person();
        person.setId(1L);
        person.setUsername("user");
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_ALLOWED));
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(person);

        when(userNotificationSettingsService.findNotificationSettings(new PersonId(1L)))
            .thenReturn(new UserNotificationSettings(new PersonId(1L), false));

        perform(
            get("/web/person/1/notifications")
                .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority("OFFICE")))
        )
            .andExpect(status().isOk());
    }

    @Test
    void personNotificationAndNotLoggedInWillBeRedirectedToLoginPage() throws Exception {
        perform(
            get("/web/person/1/notifications")
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("http://localhost/oauth2/authorization/default"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY", "INACTIVE"})
    void personNotificationIsForbidden(final String role) throws Exception {
        perform(
            get("/web/person/1/notifications")
                .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority(role)))
        )
            .andExpect(status().isForbidden());
    }

    @Test
    void personChangeNotificationAsSameUserIsOk() throws Exception {

        final Person person = new Person();
        person.setId(1L);
        person.setUsername("user");
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_ALLOWED));
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(person);

        when(personService.update(any())).thenReturn(person);

        perform(post("/web/person/1/notifications")
            .with(oidcLogin().idToken(builder -> builder.subject("user")).authorities(new SimpleGrantedAuthority("USER")))
            .with(csrf())
            .param("id", "1")
            .param("name", "user")
            .param("restrictToDepartments.active", "false")
            .param("applicationAppliedForManagement.active", "false")
            .param("applicationTemporaryAllowedForManagement.active", "false")
            .param("applicationAllowedForManagement.active", "false")
            .param("applicationCancellationForManagement.active", "false")
            .param("applicationAdaptedForManagement.active", "false")
            .param("applicationWaitingReminderForManagement.active", "false")
            .param("applicationCancellationRequestedForManagement.active", "false")
            .param("applicationAppliedAndChanges.active", "false")
            .param("applicationUpcoming.active", "false")
            .param("holidayReplacement.active", "false")
            .param("holidayReplacementUpcoming.active", "false")
            .param("personNewManagementAll.active", "false")
            .param("overtimeAppliedForManagement.active", "false")
            .param("overtimeAppliedByManagement.active", "false")
            .param("overtimeApplied.active", "false")
            .param("absenceForColleagues.active", "false")
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/web/person/1/notifications"));
    }

    @Test
    void personChangeNotificationAsOfficeUserForOtherUserIsOk() throws Exception {

        final Person person = new Person();
        person.setId(1L);
        person.setUsername("user");
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_ALLOWED));
        person.setPermissions(List.of(USER));
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        final Person office = new Person();
        office.setId(2L);
        office.setUsername("office");
        office.setPermissions(List.of(USER, OFFICE));
        when(personService.getSignedInUser()).thenReturn(office);

        perform(post("/web/person/1/notifications")
            .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority("OFFICE")))
            .with(csrf())
            .param("id", "1")
            .param("name", "user")
            .param("restrictToDepartments.active", "false")
            .param("applicationAppliedForManagement.active", "false")
            .param("applicationTemporaryAllowedForManagement.active", "false")
            .param("applicationAllowedForManagement.active", "false")
            .param("applicationCancellationForManagement.active", "false")
            .param("applicationAdaptedForManagement.active", "false")
            .param("applicationWaitingReminderForManagement.active", "false")
            .param("applicationCancellationRequestedForManagement.active", "false")
            .param("applicationAppliedAndChanges.active", "false")
            .param("applicationUpcoming.active", "false")
            .param("holidayReplacement.active", "false")
            .param("holidayReplacementUpcoming.active", "false")
            .param("personNewManagementAll.active", "false")
            .param("overtimeAppliedForManagement.active", "false")
            .param("overtimeAppliedByManagement.active", "false")
            .param("overtimeApplied.active", "false")
            .param("absenceForColleagues.active", "false")
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/web/person/1/notifications"));
    }

    @Test
    void personChangeNotificationAndNotLoggedInWillBeRedirectedToLoginPage() throws Exception {
        perform(
            post("/web/person/1/notifications")
        )
            .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @ValueSource(strings = {"DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY", "INACTIVE"})
    void personChangeNotificationIsForbidden(final String role) throws Exception {
        perform(
            post("/web/person/1/notifications")
                .with(csrf())
                .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority(role)))
        )
            .andExpect(status().isForbidden());
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return webAppContextSetup(context).apply(springSecurity()).build().perform(builder);
    }
}
