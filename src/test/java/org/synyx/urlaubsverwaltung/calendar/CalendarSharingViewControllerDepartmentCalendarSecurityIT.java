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
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static org.synyx.urlaubsverwaltung.TestDataCreator.createPerson;
import static org.synyx.urlaubsverwaltung.person.Role.BOSS;

@SpringBootTest
class CalendarSharingViewControllerDepartmentCalendarSecurityIT extends SingleTenantTestContainersBase {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private PersonService personService;
    @MockitoBean
    private PersonCalendarService personCalendarService;
    @MockitoBean
    private DepartmentCalendarService departmentCalendarService;
    @MockitoBean
    private CompanyCalendarService companyCalendarService;
    @MockitoBean
    private DepartmentService departmentService;
    @MockitoBean
    private CalendarAccessibleService calendarAccessibleService;

    // =========================================================================================================
    // department calendar => index

    @Test
    void indexUnauthorized() throws Exception {
        perform(
            get("/web/calendars/share/persons/1/departments/2")
                .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER")))
        )
            .andExpect(status().isForbidden());
    }

    @Test
    void indexForDifferentUserIsForbidden() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        perform(
            get("/web/calendars/share/persons/1/departments/2")
                .with(oidcLogin().idToken(builder -> builder.subject("differentUser")).authorities(new SimpleGrantedAuthority("USER")))
        )
            .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @ValueSource(strings = {"USER", "DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY", "INACTIVE"})
    void indexIsForbidden(final String role) throws Exception {

        perform(
            get("/web/calendars/share/persons/1/departments/2")
                .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority(role)))
        )
            .andExpect(status().isForbidden());
    }

    @Test
    void indexAsOfficeUserForOtherUserIsOk() throws Exception {

        final Person person = new Person();
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        final Department department = new Department();
        department.setId(2L);
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(List.of(department));

        final Person boss = createPerson("boss", BOSS);
        boss.setId(1337L);
        when(personService.getSignedInUser()).thenReturn(boss);
        when(companyCalendarService.getCompanyCalendar(1337)).thenReturn(Optional.empty());

        perform(
            get("/web/calendars/share/persons/1/departments/2")
                .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority("OFFICE")))
        )
            .andExpect(status().isOk());
    }

    @Test
    void indexAsBossUserForOtherUserIsOk() throws Exception {

        final Person person = new Person();
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        final Department department = new Department();
        department.setId(2L);
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(List.of(department));

        final Person boss = createPerson("boss", BOSS);
        boss.setId(1337L);
        when(personService.getSignedInUser()).thenReturn(boss);
        when(companyCalendarService.getCompanyCalendar(1337)).thenReturn(Optional.empty());

        perform(
            get("/web/calendars/share/persons/1/departments/2")
                .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority("BOSS")))
        )
            .andExpect(status().isOk());
    }

    @Test
    void indexForSameUserIsOk() throws Exception {

        final Person person = new Person();
        person.setId(1L);
        person.setUsername("user");
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));
        when(personService.getSignedInUser()).thenReturn(person);

        final Department department = new Department();
        department.setId(2L);
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(List.of(department));

        perform(
            get("/web/calendars/share/persons/1/departments/2")
                .with(oidcLogin().idToken(builder -> builder.subject("user")).authorities(new SimpleGrantedAuthority("USER")))
        )
            .andExpect(status().isOk());
    }

    // =========================================================================================================
    // department calendar => link

    @Test
    void linkDepartmentCalendarForUserIsOk() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        when(departmentService.getDepartmentById(2L)).thenReturn(Optional.of(new Department()));

        perform(
            post("/web/calendars/share/persons/1/departments/2")
                .with(csrf())
                .param("calendarPeriod", "YEAR")
                .with(oidcLogin().idToken(builder -> builder.subject("user")).authorities(new SimpleGrantedAuthority("USER")))
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/calendars/share/persons/1/departments/2"));
    }

    @Test
    void linkDepartmentCalendarAsBossUserForOtherUserIsOk() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        when(departmentService.getDepartmentById(2L)).thenReturn(Optional.of(new Department()));

        perform(
            post("/web/calendars/share/persons/1/departments/2")
                .with(csrf())
                .param("calendarPeriod", "YEAR")
                .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority("BOSS")))
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/calendars/share/persons/1/departments/2"));
    }

    @Test
    void linkDepartmentCalendarAsOfficeUserForOtherUserIsOk() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        when(departmentService.getDepartmentById(2L)).thenReturn(Optional.of(new Department()));

        perform(
            post("/web/calendars/share/persons/1/departments/2")
                .with(csrf())
                .param("calendarPeriod", "YEAR")
                .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority("OFFICE")))
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/calendars/share/persons/1/departments/2"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"USER", "DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY", "INACTIVE"})
    void linkDepartmentCalendarIsForbidden(final String role) throws Exception {

        perform(
            post("/web/calendars/share/persons/1/departments/2")
                .with(csrf())
                .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority(role)))
        )
            .andExpect(status().isForbidden());
    }

    @Test
    void linkDepartmentCalendarForOtherUserIsForbidden() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        perform(
            post("/web/calendars/share/persons/1/departments/2")
                .with(csrf())
                .with(oidcLogin().idToken(builder -> builder.subject("differentUser")).authorities(new SimpleGrantedAuthority("USER")))
        )
            .andExpect(status().isForbidden());
    }

    // =========================================================================================================
    // department calendar => unlink

    @Test
    void unlinkDepartmentCalendarForUserIsOk() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        when(departmentService.getDepartmentById(2L)).thenReturn(Optional.of(new Department()));

        perform(
            post("/web/calendars/share/persons/1/departments/2")
                .param("unlink", "")
                .with(csrf())
                .with(oidcLogin().idToken(builder -> builder.subject("user")).authorities(new SimpleGrantedAuthority("USER")))
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/calendars/share/persons/1/departments/2"));
    }

    @Test
    void unlinkDepartmentCalendarAsBossUserForOtherUserIsOk() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        when(departmentService.getDepartmentById(2L)).thenReturn(Optional.of(new Department()));

        perform(
            post("/web/calendars/share/persons/1/departments/2")
                .param("unlink", "")
                .with(csrf())
                .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority("BOSS")))
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/calendars/share/persons/1/departments/2"));
    }

    @Test
    void unlinkDepartmentCalendarAsOfficeUserForOtherUserIsOk() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        when(departmentService.getDepartmentById(2L)).thenReturn(Optional.of(new Department()));

        perform(
            post("/web/calendars/share/persons/1/departments/2")
                .param("unlink", "")
                .with(csrf())
                .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority("OFFICE")))
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/calendars/share/persons/1/departments/2"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"USER", "DEPARTMENT_HEAD", "SECOND_STAGE_AUTHORITY", "INACTIVE"})
    void unlinkDepartmentCalendarIsForbidden(final String role) throws Exception {

        perform(post("/web/calendars/share/persons/1/departments/2")
            .param("unlink", "")
            .with(csrf())
            .with(oidcLogin().authorities(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority(role)))
        )
            .andExpect(status().isForbidden());
    }

    @Test
    void unlinkDepartmentCalendarForOtherUserIsForbidden() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        perform(
            post("/web/calendars/share/persons/1/departments/2")
                .param("unlink", "")
                .with(csrf())
                .with(oidcLogin().idToken(builder -> builder.subject("otheruser")).authorities(new SimpleGrantedAuthority("USER")))
        )
            .andExpect(status().isForbidden());
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return webAppContextSetup(context).apply(springSecurity()).build().perform(builder);
    }
}
