package org.synyx.urlaubsverwaltung.calendar;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.context.WebApplicationContext;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class CalendarSharingViewControllerPersonCalendarSecurityIT {

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private PersonService personService;
    @MockBean
    private PersonCalendarService personCalendarService;
    @MockBean
    private CompanyCalendarService companyCalendarService;
    @MockBean
    private DepartmentService departmentService;
    @MockBean
    private CalendarAccessibleService calendarAccessibleService;

    @Test
    @WithMockUser(authorities = "USER")
    void indexUnauthorized() throws Exception {
        perform(get("/web/calendars/share/persons/1"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "differentUser")
    void indexForDifferentUserIsForbidden() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        when(personCalendarService.getPersonCalendar(1)).thenReturn(Optional.empty());

        perform(get("/web/calendars/share/persons/1"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "DEPARTMENT_HEAD")
    void indexAsDepartmentHeadIsForbidden() throws Exception {

        perform(get("/web/calendars/share/persons/1"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "SECOND_STAGE_AUTHORITY")
    void indexAsSecondStageAuthorityIsForbidden() throws Exception {

        perform(get("/web/calendars/share/persons/1"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void indexAsAdminIsForbidden() throws Exception {

        perform(get("/web/calendars/share/persons/1"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "INACTIVE")
    void indexAsInactiveIsForbidden() throws Exception {

        perform(get("/web/calendars/share/persons/1"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "OFFICE")
    void indexAsOfficeUserForOtherUserIsOk() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(person);

        when(personCalendarService.getPersonCalendar(1)).thenReturn(Optional.empty());

        perform(get("/web/calendars/share/persons/1"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "BOSS")
    void indexAsBossUserForOtherUserIsOk() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(person);

        when(personCalendarService.getPersonCalendar(1)).thenReturn(Optional.empty());

        perform(get("/web/calendars/share/persons/1"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user")
    void indexForSameUserIsForbidden() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(person);

        when(personCalendarService.getPersonCalendar(1)).thenReturn(Optional.empty());

        perform(get("/web/calendars/share/persons/1"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "USER")
    void linkPrivateCalendarUnauthorized() throws Exception {
        perform(post("/web/calendars/share/persons/1/me"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "differentUser")
    void linkPrivateCalendarAsOfficeUserForDifferentUserIsForbidden() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        perform(post("/web/calendars/share/persons/1/me"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "DEPARTMENT_HEAD")
    void linkPrivateCalendarAsDepartmentHeadIsForbidden() throws Exception {

        perform(post("/web/calendars/share/persons/1/me"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "SECOND_STAGE_AUTHORITY")
    void linkPrivateCalendarAsSecondStageAuthorityIsForbidden() throws Exception {

        perform(post("/web/calendars/share/persons/1/me"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void linkPrivateCalendarAsAdminIsForbidden() throws Exception {

        perform(post("/web/calendars/share/persons/1/me"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "INACTIVE")
    void linkPrivateCalendarAsInactiveIsForbidden() throws Exception {

        perform(post("/web/calendars/share/persons/1/me"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "OFFICE")
    void linkPrivateCalendarAsOfficeUserForOtherUserIsOk() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        perform(post("/web/calendars/share/persons/1/me"))
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/calendars/share/persons/1"));
    }

    @Test
    @WithMockUser(authorities = "BOSS")
    void linkPrivateCalendarAsBossUserForOtherUserIsOk() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        perform(post("/web/calendars/share/persons/1/me"))
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/calendars/share/persons/1"));
    }

    @Test
    @WithMockUser(username = "user")
    void linkPrivateCalendarAsOfficeUserForSameUserIsForbidden() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        perform(post("/web/calendars/share/persons/1/me"))
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/calendars/share/persons/1"));
    }

    @Test
    @WithMockUser(authorities = "USER")
    void unlinkPrivateCalendarUnauthorized() throws Exception {
        perform(post("/web/calendars/share/persons/1/me").param("unlink", ""))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "differentUser")
    void unlinkPrivateCalendarAsOfficeUserForDifferentUserIsForbidden() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        perform(post("/web/calendars/share/persons/1/me").param("unlink", ""))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "DEPARTMENT_HEAD")
    void unlinkPrivateCalendarAsDepartmentHeadIsForbidden() throws Exception {

        perform(post("/web/calendars/share/persons/1/me").param("unlink", ""))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "SECOND_STAGE_AUTHORITY")
    void unlinkPrivateCalendarAsSecondStageAuthorityIsForbidden() throws Exception {

        perform(post("/web/calendars/share/persons/1/me").param("unlink", ""))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void unlinkPrivateCalendarAsAdminIsForbidden() throws Exception {

        perform(post("/web/calendars/share/persons/1/me").param("unlink", ""))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "INACTIVE")
    void unlinkPrivateCalendarAsInactiveIsForbidden() throws Exception {

        perform(post("/web/calendars/share/persons/1/me").param("unlink", ""))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "OFFICE")
    void unlinkPrivateCalendarAsOfficeUserForOtherUserIsOk() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        perform(post("/web/calendars/share/persons/1/me").param("unlink", ""))
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/calendars/share/persons/1"));
    }

    @Test
    @WithMockUser(authorities = "BOSS")
    void unlinkPrivateCalendarAsBossUserForOtherUserIsOk() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        perform(post("/web/calendars/share/persons/1/me").param("unlink", ""))
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/calendars/share/persons/1"));
    }

    @Test
    @WithMockUser(username = "user")
    void unlinkPrivateCalendarAsOfficeUserForSameUserIsForbidden() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        perform(post("/web/calendars/share/persons/1/me").param("unlink", ""))
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/calendars/share/persons/1"));
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return webAppContextSetup(context).apply(springSecurity()).build().perform(builder);
    }
}
