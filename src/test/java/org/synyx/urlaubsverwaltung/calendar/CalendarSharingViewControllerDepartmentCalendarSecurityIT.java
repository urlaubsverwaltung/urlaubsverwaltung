package org.synyx.urlaubsverwaltung.calendar;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.context.WebApplicationContext;
import org.synyx.urlaubsverwaltung.TestContainersBase;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createPerson;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;

@SpringBootTest
class CalendarSharingViewControllerDepartmentCalendarSecurityIT extends TestContainersBase {

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private PersonService personService;
    @MockBean
    private PersonCalendarService personCalendarService;
    @MockBean
    private DepartmentCalendarService departmentCalendarService;
    @MockBean
    private CompanyCalendarService companyCalendarService;
    @MockBean
    private DepartmentService departmentService;
    @MockBean
    private CalendarAccessibleService calendarAccessibleService;

    // =========================================================================================================
    // department calendar => index

    @Test
    @WithMockUser(authorities = "USER")
    void indexUnauthorized() throws Exception {
        perform(get("/web/calendars/share/persons/1/departments/2"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "differentUser")
    void indexForDifferentUserIsForbidden() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        perform(get("/web/calendars/share/persons/1/departments/2"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "DEPARTMENT_HEAD")
    void indexAsDepartmentHeadIsForbidden() throws Exception {

        perform(get("/web/calendars/share/persons/1/departments/2"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "SECOND_STAGE_AUTHORITY")
    void indexAsSecondStageAuthorityIsForbidden() throws Exception {

        perform(get("/web/calendars/share/persons/1/departments/2"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void indexAsAdminIsForbidden() throws Exception {

        perform(get("/web/calendars/share/persons/1/departments/2"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "INACTIVE")
    void indexAsInactiveIsForbidden() throws Exception {

        perform(get("/web/calendars/share/persons/1/departments/2"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "OFFICE")
    void indexAsOfficeUserForOtherUserIsOk() throws Exception {

        final Person person = new Person();
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        final Department department = new Department();
        department.setId(2);
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(List.of(department));

        final Person boss = createPerson("boss", BOSS);
        boss.setId(1337);
        when(personService.getSignedInUser()).thenReturn(boss);
        when(companyCalendarService.getCompanyCalendar(1337)).thenReturn(Optional.empty());

        perform(get("/web/calendars/share/persons/1/departments/2"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "BOSS")
    void indexAsBossUserForOtherUserIsOk() throws Exception {

        final Person person = new Person();
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        final Department department = new Department();
        department.setId(2);
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(List.of(department));

        final Person boss = createPerson("boss", BOSS);
        boss.setId(1337);
        when(personService.getSignedInUser()).thenReturn(boss);
        when(companyCalendarService.getCompanyCalendar(1337)).thenReturn(Optional.empty());

        perform(get("/web/calendars/share/persons/1/departments/2"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user")
    void indexForSameUserIsForbidden() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(person);

        final Department department = new Department();
        department.setId(2);
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(List.of(department));

        perform(get("/web/calendars/share/persons/1/departments/2"))
            .andExpect(status().isOk());
    }

    // =========================================================================================================
    // department calendar => link

    @Test
    @WithMockUser(username = "user")
    void linkDepartmentCalendarForUserIsOk() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        when(departmentService.getDepartmentById(2)).thenReturn(Optional.of(new Department()));

        final MockHttpServletRequestBuilder request = post("/web/calendars/share/persons/1/departments/2")
            .with(csrf())
            .param("calendarPeriod", "YEAR");

        perform(request)
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/calendars/share/persons/1/departments/2"));
    }

    @Test
    @WithMockUser(authorities = "BOSS")
    void linkDepartmentCalendarAsBossUserForOtherUserIsOk() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        when(departmentService.getDepartmentById(2)).thenReturn(Optional.of(new Department()));

        final MockHttpServletRequestBuilder request = post("/web/calendars/share/persons/1/departments/2")
            .with(csrf())
            .param("calendarPeriod", "YEAR");

        perform(request)
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/calendars/share/persons/1/departments/2"));
    }

    @Test
    @WithMockUser(authorities = "OFFICE")
    void linkDepartmentCalendarAsOfficeUserForOtherUserIsOk() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        when(departmentService.getDepartmentById(2)).thenReturn(Optional.of(new Department()));

        final MockHttpServletRequestBuilder request = post("/web/calendars/share/persons/1/departments/2")
            .with(csrf())
            .param("calendarPeriod", "YEAR");

        perform(request)
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/calendars/share/persons/1/departments/2"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void linkDepartmentCalendarAsAdminIsForbidden() throws Exception {

        perform(post("/web/calendars/share/persons/1/departments/2").with(csrf()))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "INACTIVE")
    void linkDepartmentCalendarAsInactiveIsForbidden() throws Exception {

        perform(post("/web/calendars/share/persons/1/departments/2").with(csrf()))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "DEPARTMENT_HEAD")
    void linkDepartmentCalendarAsDepartmentHeadIsForbidden() throws Exception {

        perform(post("/web/calendars/share/persons/1/departments/2").with(csrf()))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "SECOND_STAGE_AUTHORITY")
    void linkDepartmentCalendarAsSecondStageAuthorityIsForbidden() throws Exception {

        perform(post("/web/calendars/share/persons/1/departments/2").with(csrf()))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "otheruser")
    void linkDepartmentCalendarForOtherUserIsForbidden() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        perform(post("/web/calendars/share/persons/1/departments/2").with(csrf()))
            .andExpect(status().isForbidden());
    }

    // =========================================================================================================
    // department calendar => unlink

    @Test
    @WithMockUser(username = "user")
    void unlinkDepartmentCalendarForUserIsOk() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        when(departmentService.getDepartmentById(2)).thenReturn(Optional.of(new Department()));

        perform(post("/web/calendars/share/persons/1/departments/2").param("unlink", "").with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/calendars/share/persons/1/departments/2"));
    }

    @Test
    @WithMockUser(authorities = "BOSS")
    void unlinkDepartmentCalendarAsBossUserForOtherUserIsOk() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        when(departmentService.getDepartmentById(2)).thenReturn(Optional.of(new Department()));

        perform(post("/web/calendars/share/persons/1/departments/2").param("unlink", "").with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/calendars/share/persons/1/departments/2"));
    }

    @Test
    @WithMockUser(authorities = "OFFICE")
    void unlinkDepartmentCalendarAsOfficeUserForOtherUserIsOk() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        when(departmentService.getDepartmentById(2)).thenReturn(Optional.of(new Department()));

        perform(post("/web/calendars/share/persons/1/departments/2").param("unlink", "").with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/calendars/share/persons/1/departments/2"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void unlinkDepartmentCalendarAsAdminIsForbidden() throws Exception {

        perform(post("/web/calendars/share/persons/1/departments/2").param("unlink", "").with(csrf()))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "INACTIVE")
    void unlinkDepartmentCalendarAsInactiveIsForbidden() throws Exception {

        perform(post("/web/calendars/share/persons/1/departments/2").param("unlink", "").with(csrf()))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "DEPARTMENT_HEAD")
    void unlinkDepartmentCalendarAsDepartmentHeadIsForbidden() throws Exception {

        perform(post("/web/calendars/share/persons/1/departments/2").param("unlink", "").with(csrf()))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "SECOND_STAGE_AUTHORITY")
    void unlinkDepartmentCalendarAsSecondStageAuthorityIsForbidden() throws Exception {

        perform(post("/web/calendars/share/persons/1/departments/2").param("unlink", "").with(csrf()))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "otheruser")
    void unlinkDepartmentCalendarForOtherUserIsForbidden() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        perform(post("/web/calendars/share/persons/1/departments/2").param("unlink", "").with(csrf()))
            .andExpect(status().isForbidden());
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return webAppContextSetup(context).apply(springSecurity()).build().perform(builder);
    }
}
