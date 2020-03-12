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
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;
import static org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator.createPerson;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CalendarSharingViewControllerDepartmentCalendarSecurityIT {

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
    public void indexUnauthorized() throws Exception {
        perform(get("/web/calendars/share/persons/1/departments/2"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "differentUser")
    public void indexForDifferentUserIsForbidden() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        perform(get("/web/calendars/share/persons/1/departments/2"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "DEPARTMENT_HEAD")
    public void indexAsDepartmentHeadIsForbidden() throws Exception {

        perform(get("/web/calendars/share/persons/1/departments/2"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "SECOND_STAGE_AUTHORITY")
    public void indexAsSecondStageAuthorityIsForbidden() throws Exception {

        perform(get("/web/calendars/share/persons/1/departments/2"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    public void indexAsAdminIsForbidden() throws Exception {

        perform(get("/web/calendars/share/persons/1/departments/2"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "INACTIVE")
    public void indexAsInactiveIsForbidden() throws Exception {

        perform(get("/web/calendars/share/persons/1/departments/2"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "OFFICE")
    public void indexAsOfficeUserForOtherUserIsOk() throws Exception {

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
    public void indexAsBossUserForOtherUserIsOk() throws Exception {

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
    public void indexForSameUserIsForbidden() throws Exception {

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
    public void linkDepartmentCalendarForUserIsOk() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        when(departmentService.getDepartmentById(2)).thenReturn(Optional.of(new Department()));

        perform(post("/web/calendars/share/persons/1/departments/2"))
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/calendars/share/persons/1/departments/2"));
    }

    @Test
    @WithMockUser(authorities = "BOSS")
    public void linkDepartmentCalendarAsBossUserForOtherUserIsOk() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        when(departmentService.getDepartmentById(2)).thenReturn(Optional.of(new Department()));

        perform(post("/web/calendars/share/persons/1/departments/2"))
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/calendars/share/persons/1/departments/2"));
    }

    @Test
    @WithMockUser(authorities = "OFFICE")
    public void linkDepartmentCalendarAsOfficeUserForOtherUserIsOk() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        when(departmentService.getDepartmentById(2)).thenReturn(Optional.of(new Department()));

        perform(post("/web/calendars/share/persons/1/departments/2"))
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/calendars/share/persons/1/departments/2"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    public void linkDepartmentCalendarAsAdminIsForbidden() throws Exception {

        perform(post("/web/calendars/share/persons/1/departments/2"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "INACTIVE")
    public void linkDepartmentCalendarAsInactiveIsForbidden() throws Exception {

        perform(post("/web/calendars/share/persons/1/departments/2"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "DEPARTMENT_HEAD")
    public void linkDepartmentCalendarAsDepartmentHeadIsForbidden() throws Exception {

        perform(post("/web/calendars/share/persons/1/departments/2"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "SECOND_STAGE_AUTHORITY")
    public void linkDepartmentCalendarAsSecondStageAuthorityIsForbidden() throws Exception {

        perform(post("/web/calendars/share/persons/1/departments/2"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "otheruser")
    public void linkDepartmentCalendarForOtherUserIsForbidden() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        perform(post("/web/calendars/share/persons/1/departments/2"))
            .andExpect(status().isForbidden());
    }

    // =========================================================================================================
    // department calendar => unlink

    @Test
    @WithMockUser(username = "user")
    public void unlinkDepartmentCalendarForUserIsOk() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        when(departmentService.getDepartmentById(2)).thenReturn(Optional.of(new Department()));

        perform(post("/web/calendars/share/persons/1/departments/2").param("unlink", ""))
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/calendars/share/persons/1/departments/2"));
    }

    @Test
    @WithMockUser(authorities = "BOSS")
    public void unlinkDepartmentCalendarAsBossUserForOtherUserIsOk() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        when(departmentService.getDepartmentById(2)).thenReturn(Optional.of(new Department()));

        perform(post("/web/calendars/share/persons/1/departments/2").param("unlink", ""))
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/calendars/share/persons/1/departments/2"));
    }

    @Test
    @WithMockUser(authorities = "OFFICE")
    public void unlinkDepartmentCalendarAsOfficeUserForOtherUserIsOk() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        when(departmentService.getDepartmentById(2)).thenReturn(Optional.of(new Department()));

        perform(post("/web/calendars/share/persons/1/departments/2").param("unlink", ""))
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/calendars/share/persons/1/departments/2"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    public void unlinkDepartmentCalendarAsAdminIsForbidden() throws Exception {

        perform(post("/web/calendars/share/persons/1/departments/2").param("unlink", ""))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "INACTIVE")
    public void unlinkDepartmentCalendarAsInactiveIsForbidden() throws Exception {

        perform(post("/web/calendars/share/persons/1/departments/2").param("unlink", ""))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "DEPARTMENT_HEAD")
    public void unlinkDepartmentCalendarAsDepartmentHeadIsForbidden() throws Exception {

        perform(post("/web/calendars/share/persons/1/departments/2").param("unlink", ""))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "SECOND_STAGE_AUTHORITY")
    public void unlinkDepartmentCalendarAsSecondStageAuthorityIsForbidden() throws Exception {

        perform(post("/web/calendars/share/persons/1/departments/2").param("unlink", ""))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "otheruser")
    public void unlinkDepartmentCalendarForOtherUserIsForbidden() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        perform(post("/web/calendars/share/persons/1/departments/2").param("unlink", ""))
            .andExpect(status().isForbidden());
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return webAppContextSetup(context).apply(springSecurity()).build().perform(builder);
    }
}
