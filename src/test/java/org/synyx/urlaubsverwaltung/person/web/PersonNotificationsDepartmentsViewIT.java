package org.synyx.urlaubsverwaltung.person.web;

import org.junit.jupiter.api.Test;
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
import org.synyx.urlaubsverwaltung.person.MailNotification;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_EMAIL_REMAINING_VACATION_DAYS_EXPIRED_MANAGEMENT_ALL;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

/**
 * The "Abteilungen" tab carries the personal notifications as hidden fields. A notification the person may not have
 * must not be posted back: the validator rejects the whole form otherwise.
 */
@SpringBootTest
class PersonNotificationsDepartmentsViewIT extends SingleTenantTestContainersBase {

    private static final String HIDDEN_FIELD = "name=\"remainingVacationDaysExpiredManagementAll.active\"";

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private PersonService personService;
    @MockitoBean
    private UserNotificationSettingsService userNotificationSettingsService;

    @Test
    void ensureDepartmentsTabDoesNotPostTheOfficeNotificationStoredForANonOfficePerson() throws Exception {

        // e.g. a former office person
        signedIn(List.of(USER), List.of(NOTIFICATION_EMAIL_REMAINING_VACATION_DAYS_EXPIRED_MANAGEMENT_ALL));

        perform(get("/web/person/1/notifications/departments")
            .with(oidcLogin().idToken(builder -> builder.subject("user")).authorities(new SimpleGrantedAuthority("USER"))))
            .andExpect(status().isOk())
            .andExpect(content().string(not(containsString(HIDDEN_FIELD))));
    }

    @Test
    void ensureDepartmentsTabKeepsTheOfficeNotificationOfOffice() throws Exception {

        signedIn(List.of(USER, OFFICE), List.of(NOTIFICATION_EMAIL_REMAINING_VACATION_DAYS_EXPIRED_MANAGEMENT_ALL));

        perform(get("/web/person/1/notifications/departments")
            .with(oidcLogin().idToken(builder -> builder.subject("user")).authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority("OFFICE"))))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString(HIDDEN_FIELD + " value=\"true\"")));
    }

    private void signedIn(List<Role> permissions, List<MailNotification> notifications) {
        final Person person = new Person();
        person.setId(1L);
        person.setUsername("user");
        person.setPermissions(permissions);
        person.setNotifications(notifications);
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(person);
        when(userNotificationSettingsService.findNotificationSettings(new PersonId(1L)))
            .thenReturn(new UserNotificationSettings(new PersonId(1L), false));
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return webAppContextSetup(context).apply(springSecurity()).build().perform(builder);
    }
}
