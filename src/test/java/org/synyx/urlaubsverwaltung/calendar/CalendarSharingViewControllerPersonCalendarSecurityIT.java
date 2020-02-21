package org.synyx.urlaubsverwaltung.calendar;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
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

@RunWith(SpringRunner.class)
@SpringBootTest
public class CalendarSharingViewControllerPersonCalendarSecurityIT {

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
    public void indexUnauthorized() throws Exception {
        perform(get("/web/calendars/share/persons/1"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "differentUser")
    public void indexForDifferentUserIsForbidden() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        when(personCalendarService.getPersonCalendar(1)).thenReturn(Optional.empty());

        perform(get("/web/calendars/share/persons/1"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "DEPARTMENT_HEAD")
    public void indexAsDepartmentHeadIsForbidden() throws Exception {

        perform(get("/web/calendars/share/persons/1"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "SECOND_STAGE_AUTHORITY")
    public void indexAsSecondStageAuthorityIsForbidden() throws Exception {

        perform(get("/web/calendars/share/persons/1"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    public void indexAsAdminIsForbidden() throws Exception {

        perform(get("/web/calendars/share/persons/1"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "INACTIVE")
    public void indexAsInactiveIsForbidden() throws Exception {

        perform(get("/web/calendars/share/persons/1"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "OFFICE")
    public void indexAsOfficeUserForOtherUserIsOk() throws Exception {

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
    public void indexAsBossUserForOtherUserIsOk() throws Exception {

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
    public void indexForSameUserIsForbidden() throws Exception {

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
    public void linkPrivateCalendarUnauthorized() throws Exception {
        perform(post("/web/calendars/share/persons/1/me"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "differentUser")
    public void linkPrivateCalendarAsOfficeUserForDifferentUserIsForbidden() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        perform(post("/web/calendars/share/persons/1/me"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "DEPARTMENT_HEAD")
    public void linkPrivateCalendarAsDepartmentHeadIsForbidden() throws Exception {

        perform(post("/web/calendars/share/persons/1/me"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "SECOND_STAGE_AUTHORITY")
    public void linkPrivateCalendarAsSecondStageAuthorityIsForbidden() throws Exception {

        perform(post("/web/calendars/share/persons/1/me"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    public void linkPrivateCalendarAsAdminIsForbidden() throws Exception {

        perform(post("/web/calendars/share/persons/1/me"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "INACTIVE")
    public void linkPrivateCalendarAsInactiveIsForbidden() throws Exception {

        perform(post("/web/calendars/share/persons/1/me"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "OFFICE")
    public void linkPrivateCalendarAsOfficeUserForOtherUserIsOk() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        perform(post("/web/calendars/share/persons/1/me"))
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/calendars/share/persons/1"));
    }

    @Test
    @WithMockUser(authorities = "BOSS")
    public void linkPrivateCalendarAsBossUserForOtherUserIsOk() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        perform(post("/web/calendars/share/persons/1/me"))
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/calendars/share/persons/1"));
    }

    @Test
    @WithMockUser(username = "user")
    public void linkPrivateCalendarAsOfficeUserForSameUserIsForbidden() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        perform(post("/web/calendars/share/persons/1/me"))
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/calendars/share/persons/1"));
    }

    @Test
    @WithMockUser(authorities = "USER")
    public void unlinkPrivateCalendarUnauthorized() throws Exception {
        perform(post("/web/calendars/share/persons/1/me").param("unlink", ""))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "differentUser")
    public void unlinkPrivateCalendarAsOfficeUserForDifferentUserIsForbidden() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        perform(post("/web/calendars/share/persons/1/me").param("unlink", ""))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "DEPARTMENT_HEAD")
    public void unlinkPrivateCalendarAsDepartmentHeadIsForbidden() throws Exception {

        perform(post("/web/calendars/share/persons/1/me").param("unlink", ""))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "SECOND_STAGE_AUTHORITY")
    public void unlinkPrivateCalendarAsSecondStageAuthorityIsForbidden() throws Exception {

        perform(post("/web/calendars/share/persons/1/me").param("unlink", ""))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    public void unlinkPrivateCalendarAsAdminIsForbidden() throws Exception {

        perform(post("/web/calendars/share/persons/1/me").param("unlink", ""))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "INACTIVE")
    public void unlinkPrivateCalendarAsInactiveIsForbidden() throws Exception {

        perform(post("/web/calendars/share/persons/1/me").param("unlink", ""))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "OFFICE")
    public void unlinkPrivateCalendarAsOfficeUserForOtherUserIsOk() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        perform(post("/web/calendars/share/persons/1/me").param("unlink", ""))
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/calendars/share/persons/1"));
    }

    @Test
    @WithMockUser(authorities = "BOSS")
    public void unlinkPrivateCalendarAsBossUserForOtherUserIsOk() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        perform(post("/web/calendars/share/persons/1/me").param("unlink", ""))
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/calendars/share/persons/1"));
    }

    @Test
    @WithMockUser(username = "user")
    public void unlinkPrivateCalendarAsOfficeUserForSameUserIsForbidden() throws Exception {

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
