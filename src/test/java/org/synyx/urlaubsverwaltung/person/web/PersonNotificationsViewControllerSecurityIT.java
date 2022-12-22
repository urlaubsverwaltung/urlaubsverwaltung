package org.synyx.urlaubsverwaltung.person.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.context.WebApplicationContext;
import org.synyx.urlaubsverwaltung.TestContainersBase;
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
class PersonNotificationsViewControllerSecurityIT extends TestContainersBase {

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private PersonService personService;

    @MockBean
    private UserNotificationSettingsService userNotificationSettingsService;

    @Test
    @WithMockUser(authorities = "USER", username = "user")
    void personNotificationAsSameUserIsOk() throws Exception {

        final Person person = new Person();
        person.setId(1L);
        person.setUsername("user");
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_ALLOWED));
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(person);

        when(userNotificationSettingsService.findNotificationSettings(new PersonId(1L)))
            .thenReturn(new UserNotificationSettings(new PersonId(1L), false));

        perform(get("/web/person/1/notifications"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = {"USER", "OFFICE"})
    void personNotificationAsOfficeUserForOtherUserIsOk() throws Exception {

        final Person person = new Person();
        person.setId(1L);
        person.setUsername("user");
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_ALLOWED));
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(person);

        when(userNotificationSettingsService.findNotificationSettings(new PersonId(1L)))
            .thenReturn(new UserNotificationSettings(new PersonId(1L), false));

        perform(get("/web/person/1/notifications"))
            .andExpect(status().isOk());
    }

    @Test
    void personNotificationAndNotLoggedInWillBeRedirectedToLoginPage() throws Exception {
        perform(get("/web/person/1/notifications"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(authorities = {"USER", "DEPARTMENT_HEAD"})
    void personNotificationAsDepartmentHeadIsForbidden() throws Exception {
        perform(get("/web/person/1/notifications"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"USER", "SECOND_STAGE_AUTHORITY"})
    void personNotificationAsSecondStageAuthorityIsForbidden() throws Exception {
        perform(get("/web/person/1/notifications"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"USER", "ADMIN"})
    void personNotificationAsAdminIsForbidden() throws Exception {
        perform(get("/web/person/1/notifications"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "INACTIVE")
    void personNotificationAsInactiveIsForbidden() throws Exception {
        perform(get("/web/person/1/notifications"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "USER", username = "user")
    void personChangeNotificationAsSameUserIsOk() throws Exception {

        final Person person = new Person();
        person.setId(1L);
        person.setUsername("user");
        person.setNotifications(List.of(NOTIFICATION_EMAIL_APPLICATION_ALLOWED));
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(person);

        when(personService.update(any())).thenReturn(person);

        perform(post("/web/person/1/notifications")
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
    @WithMockUser(authorities = {"USER", "OFFICE"})
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
        perform(post("/web/person/1/notifications"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"USER", "DEPARTMENT_HEAD"})
    void personChangeNotificationAsDepartmentHeadIsForbidden() throws Exception {
        perform(post("/web/person/1/notifications")
            .with(csrf()))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"USER", "SECOND_STAGE_AUTHORITY"})
    void personChangeNotificationAsSecondStageAuthorityIsForbidden() throws Exception {
        perform(post("/web/person/1/notifications")
            .with(csrf()))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"USER", "ADMIN"})
    void personChangeNotificationAsAdminIsForbidden() throws Exception {
        perform(post("/web/person/1/notifications")
            .with(csrf()))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "INACTIVE")
    void personChangeNotificationAsInactiveIsForbidden() throws Exception {
        perform(post("/web/person/1/notifications")
            .with(csrf()))
            .andExpect(status().isForbidden());
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return webAppContextSetup(context).apply(springSecurity()).build().perform(builder);
    }
}
