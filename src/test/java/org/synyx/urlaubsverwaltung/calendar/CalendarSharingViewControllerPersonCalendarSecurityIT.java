package org.synyx.urlaubsverwaltung.calendar;

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
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
class CalendarSharingViewControllerPersonCalendarSecurityIT extends SingleTenantTestContainersBase {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private PersonService personService;
    @MockitoBean
    private PersonCalendarService personCalendarService;
    @MockitoBean
    private CompanyCalendarService companyCalendarService;
    @MockitoBean
    private DepartmentService departmentService;
    @MockitoBean
    private CalendarAccessibleService calendarAccessibleService;

    @Test
    void indexUnauthorized() throws Exception {
        perform(
            get("/web/calendars/share/persons/1")
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("http://localhost/oauth2/authorization/default"));
    }

    @Test
    void indexForDifferentUserIsForbidden() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        when(personCalendarService.getPersonCalendar(1L)).thenReturn(Optional.empty());

        perform(
            get("/web/calendars/share/persons/1")
                .with(oidcLogin().idToken(builder -> builder.subject("differentUser")))
        )
            .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @ValueSource(strings = {"USER", "DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY", "INACTIVE"})
    void ensureIndexAsWithIncorrectRoleIsForbidden(final String role) throws Exception {
        perform(get("/web/calendars/share/persons/1")
            .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority(role))))
            .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @ValueSource(strings = {"USER", "BOSS", "OFFICE"})
    void indexAsOfficeUserForOtherUserIsOk(final String role) throws Exception {

        final Person person = new Person();
        person.setId(1L);
        person.setUsername("user");

        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(person);

        when(personCalendarService.getPersonCalendar(1L)).thenReturn(Optional.empty());

        perform(
            get("/web/calendars/share/persons/1")
                .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority(role)))
        )
            .andExpect(status().isOk());
    }

    @Test
    void linkPrivateCalendarUnauthorized() throws Exception {
        perform(
            post("/web/calendars/share/persons/1/me")
                .with(csrf())
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("http://localhost/oauth2/authorization/default"));
    }

    @Test
    void linkPrivateCalendarAsOfficeUserForDifferentUserIsForbidden() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        perform(
            post("/web/calendars/share/persons/1/me")
                .with(csrf())
                .with(oidcLogin().idToken(builder -> builder.subject("differentUser")).authorities(new SimpleGrantedAuthority("USER")))
        )
            .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @ValueSource(strings = {"USER", "DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY", "INACTIVE"})
    void linkPrivateCalendarIsForbidden(final String role) throws Exception {

        perform(
            post("/web/calendars/share/persons/1/me")
                .with(csrf())
                .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority(role)))
        )
            .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @ValueSource(strings = {"USER", "OFFICE", "BOSS", "INACTIVE"})
    void linkPrivateCalendarAsOfficeUserForOtherUserIsOk(final String role) throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        perform(
            post("/web/calendars/share/persons/1/me")
                .param("calendarPeriod", "YEAR")
                .with(csrf())
                .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority(role)))
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/calendars/share/persons/1"));
    }

    @Test
    void unlinkPrivateCalendarUnauthorized() throws Exception {
        perform(
            post("/web/calendars/share/persons/1/me")
                .param("unlink", "")
                .with(csrf())
                .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER")))
        )
            .andExpect(status().isForbidden());
    }

    @Test
    void unlinkPrivateCalendarAsOfficeUserForDifferentUserIsForbidden() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        perform(
            post("/web/calendars/share/persons/1/me")
                .param("unlink", "")
                .with(csrf())
                .with(oidcLogin().idToken(builder -> builder.subject("differentUser")).authorities(new SimpleGrantedAuthority("USER")))
        )
            .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @ValueSource(strings = {"USER", "DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY", "INACTIVE"})
    void unlinkPrivateCalendarAsDepartmentHeadIsForbidden(final String role) throws Exception {

        perform(
            post("/web/calendars/share/persons/1/me")
                .param("unlink", "")
                .with(csrf())
                .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority(role)))
        )
            .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @ValueSource(strings = {"OFFICE", "BOSS"})
    void unlinkPrivateCalendarAsOfficeUserForOtherUserIsOk(final String role) throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        perform(
            post("/web/calendars/share/persons/1/me")
                .param("unlink", "")
                .with(csrf())
                .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority(role)))
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/calendars/share/persons/1"));
    }

    @Test
    void unlinkPrivateCalendarAsOfficeUserForSameUserIsForbidden() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        perform(
            post("/web/calendars/share/persons/1/me")
                .param("unlink", "")
                .with(csrf())
                .with(oidcLogin().idToken(builder -> builder.subject("user")).authorities(new SimpleGrantedAuthority("USER")))
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/calendars/share/persons/1"));
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return webAppContextSetup(context).apply(springSecurity()).build().perform(builder);
    }
}
