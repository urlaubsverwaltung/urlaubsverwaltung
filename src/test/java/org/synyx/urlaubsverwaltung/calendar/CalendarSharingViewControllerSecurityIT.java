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
public class CalendarSharingViewControllerSecurityIT {

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private PersonService personService;
    @MockBean
    private PersonCalendarService personCalendarService;

    @Test
    @WithMockUser(authorities = "USER")
    public void indexUnauthorized() throws Exception {
        perform(get("/web/persons/1/calendar/share"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "differentUser")
    public void getAbsencesAsOfficeUserForDifferentUserIsForbidden() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        when(personCalendarService.getPersonCalendar(1)).thenReturn(Optional.empty());

        perform(get("/web/persons/1/calendar/share"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "DEPARTMENT_HEAD")
    public void getAbsencesAsDepartmentHeadIsForbidden() throws Exception {

        perform(get("/web/persons/1/calendar/share"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "SECOND_STAGE_AUTHORITY")
    public void getAbsencesAsSecondStageAuthorityIsForbidden() throws Exception {

        perform(get("/web/persons/1/calendar/share"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    public void getAbsencesAsAdminIsForbidden() throws Exception {

        perform(get("/web/persons/1/calendar/share"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "INACTIVE")
    public void indexAsInactiveIsForbidden() throws Exception {

        perform(get("/web/persons/1/calendar/share"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "OFFICE")
    public void indexAsOfficeUserForOtherUserIsOk() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        when(personCalendarService.getPersonCalendar(1)).thenReturn(Optional.empty());

        perform(get("/web/persons/1/calendar/share"))
            .andExpect(status().isOk());
    }


    @Test
    @WithMockUser(authorities = "BOSS")
    public void indexAsBossUserForOtherUserIsOk() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));


        when(personCalendarService.getPersonCalendar(1)).thenReturn(Optional.empty());

        perform(get("/web/persons/1/calendar/share"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user")
    public void getAbsencesAsOfficeUserForSameUserIsForbidden() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        when(personCalendarService.getPersonCalendar(1)).thenReturn(Optional.empty());

        perform(get("/web/persons/1/calendar/share"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "USER")
    public void linkPrivateCalendarUnauthorized() throws Exception {
        perform(post("/web/persons/1/calendar/share/me"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "differentUser")
    public void linkPrivateCalendarAsOfficeUserForDifferentUserIsForbidden() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        perform(post("/web/persons/1/calendar/share/me"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "DEPARTMENT_HEAD")
    public void linkPrivateCalendarAsDepartmentHeadIsForbidden() throws Exception {

        perform(post("/web/persons/1/calendar/share/me"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "SECOND_STAGE_AUTHORITY")
    public void linkPrivateCalendarAsSecondStageAuthorityIsForbidden() throws Exception {

        perform(post("/web/persons/1/calendar/share/me"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    public void linkPrivateCalendarAsAdminIsForbidden() throws Exception {

        perform(post("/web/persons/1/calendar/share/me"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "INACTIVE")
    public void linkPrivateCalendarAsInactiveIsForbidden() throws Exception {

        perform(post("/web/persons/1/calendar/share/me"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "OFFICE")
    public void linkPrivateCalendarAsOfficeUserForOtherUserIsOk() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        perform(post("/web/persons/1/calendar/share/me"))
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/persons/1/calendar/share"));
    }

    @Test
    @WithMockUser(authorities = "BOSS")
    public void linkPrivateCalendarAsBossUserForOtherUserIsOk() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        perform(post("/web/persons/1/calendar/share/me"))
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/persons/1/calendar/share"));
    }

    @Test
    @WithMockUser(username = "user")
    public void linkPrivateCalendarAsOfficeUserForSameUserIsForbidden() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        perform(post("/web/persons/1/calendar/share/me"))
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/persons/1/calendar/share"));
    }

    @Test
    @WithMockUser(authorities = "USER")
    public void unlinkPrivateCalendarUnauthorized() throws Exception {
        perform(post("/web/persons/1/calendar/share/me").param("unlink", ""))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "differentUser")
    public void unlinkPrivateCalendarAsOfficeUserForDifferentUserIsForbidden() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        perform(post("/web/persons/1/calendar/share/me"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "DEPARTMENT_HEAD")
    public void unlinkPrivateCalendarAsDepartmentHeadIsForbidden() throws Exception {

        perform(post("/web/persons/1/calendar/share/me").param("unlink", ""))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "SECOND_STAGE_AUTHORITY")
    public void unlinkPrivateCalendarAsSecondStageAuthorityIsForbidden() throws Exception {

        perform(post("/web/persons/1/calendar/share/me").param("unlink", ""))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    public void unlinkPrivateCalendarAsAdminIsForbidden() throws Exception {

        perform(post("/web/persons/1/calendar/share/me").param("unlink", ""))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "INACTIVE")
    public void unlinkPrivateCalendarAsInactiveIsForbidden() throws Exception {

        perform(post("/web/persons/1/calendar/share/me").param("unlink", ""))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "OFFICE")
    public void unlinkPrivateCalendarAsOfficeUserForOtherUserIsOk() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        perform(post("/web/persons/1/calendar/share/me").param("unlink", ""))
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/persons/1/calendar/share"));
    }

    @Test
    @WithMockUser(authorities = "BOSS")
    public void unlinkPrivateCalendarAsBossUserForOtherUserIsOk() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        perform(post("/web/persons/1/calendar/share/me").param("unlink", ""))
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/persons/1/calendar/share"));
    }

    @Test
    @WithMockUser(username = "user")
    public void unlinkPrivateCalendarAsOfficeUserForSameUserIsForbidden() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        perform(post("/web/persons/1/calendar/share/me").param("unlink",  ""))
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/persons/1/calendar/share"));
    }


    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return webAppContextSetup(context).apply(springSecurity()).build().perform(builder);
    }
}
