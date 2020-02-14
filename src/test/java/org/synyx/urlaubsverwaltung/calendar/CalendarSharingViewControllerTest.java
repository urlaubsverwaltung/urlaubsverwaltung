package org.synyx.urlaubsverwaltung.calendar;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator.createDepartment;
import static org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator.createPerson;


@RunWith(MockitoJUnitRunner.class)
public class CalendarSharingViewControllerTest {

    private CalendarSharingViewController sut;

    @Mock
    private PersonCalendarService personCalendarService;

    @Mock
    private DepartmentCalendarService departmentCalendarService;

    @Mock
    private CompanyCalendarService companyCalendarService;

    @Mock
    private PersonService personService;

    @Mock
    private DepartmentService departmentService;

    @Mock
    private CalendarAccessibleService calendarAccessibleService;

    @Before
    public void setUp() {
        sut = new CalendarSharingViewController(personCalendarService, departmentCalendarService, companyCalendarService, personService, departmentService, calendarAccessibleService);
    }

    @Test
    public void indexWithoutCompanyCalendarForUserDueToDisabledFeature() throws Exception {

        final Person person = createPerson();
        person.setId(1);

        when(personService.getSignedInUser()).thenReturn(person);
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(Collections.emptyList());
        when(personCalendarService.getPersonCalendar(1)).thenReturn(Optional.of(new PersonCalendar()));

        when(calendarAccessibleService.isCompanyCalendarAccessible()).thenReturn(false);

        perform(get("/web/calendars/share/persons/1"))
            .andExpect(view().name("calendarsharing/index"))
            .andExpect(model().attributeDoesNotExist("companyCalendarShare"))
            .andExpect(status().isOk());
    }

    @Test
    public void indexWithCompanyCalendarForUserDueToDisabledFeatureButRoleBoss() throws Exception {

        final Person bossPerson = mock(Person.class);
        when(bossPerson.hasRole(eq(Role.BOSS))).thenReturn(true);

        final Person person = createPerson();
        person.setId(1);

        when(personService.getSignedInUser()).thenReturn(bossPerson);
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(Collections.emptyList());
        when(personCalendarService.getPersonCalendar(1)).thenReturn(Optional.of(new PersonCalendar()));

        when(calendarAccessibleService.isCompanyCalendarAccessible()).thenReturn(false);

        perform(get("/web/calendars/share/persons/1"))
            .andExpect(view().name("calendarsharing/index"))
            .andExpect(model().attributeExists("companyCalendarShare"))
            .andExpect(status().isOk());
    }

    @Test
    public void indexWithCompanyCalendarForUserDueToDisabledFeatureButRoleOffice() throws Exception {

        final Person officeUser = mock(Person.class);
        when(officeUser.hasRole(eq(Role.OFFICE))).thenReturn(true);

        final Person person = createPerson();
        person.setId(1);

        when(personService.getSignedInUser()).thenReturn(officeUser);
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(Collections.emptyList());
        when(personCalendarService.getPersonCalendar(1)).thenReturn(Optional.of(new PersonCalendar()));

        when(calendarAccessibleService.isCompanyCalendarAccessible()).thenReturn(false);

        perform(get("/web/calendars/share/persons/1"))
            .andExpect(view().name("calendarsharing/index"))
            .andExpect(model().attributeExists("companyCalendarShare"))
            .andExpect(status().isOk());
    }

    @Test
    public void indexWithCompanyCalendarForUser() throws Exception {

        final Person person = mock(Person.class);
        when(person.hasRole(Role.BOSS)).thenReturn(false);
        when(person.hasRole(Role.OFFICE)).thenReturn(false);

        when(personService.getSignedInUser()).thenReturn(person);
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(Collections.emptyList());
        when(personCalendarService.getPersonCalendar(1)).thenReturn(Optional.of(new PersonCalendar()));

        when(calendarAccessibleService.isCompanyCalendarAccessible()).thenReturn(true);

        perform(get("/web/calendars/share/persons/1"))
            .andExpect(view().name("calendarsharing/index"))
            .andExpect(model().attributeExists("companyCalendarShare"))
            .andExpect(status().isOk());
    }

    @Test
    public void indexWithoutCompanyCalendarAccessibleForUser() throws Exception {

        final Person person = mock(Person.class);
        when(person.hasRole(eq(Role.BOSS))).thenReturn(false);
        when(person.hasRole(eq(Role.OFFICE))).thenReturn(false);

        when(personService.getSignedInUser()).thenReturn(person);
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(Collections.emptyList());
        when(personCalendarService.getPersonCalendar(1)).thenReturn(Optional.of(new PersonCalendar()));

        perform(get("/web/calendars/share/persons/1"))
            .andExpect(view().name("calendarsharing/index"))
            .andExpect(model().attributeDoesNotExist("companyCalendarAccessible"))
            .andExpect(status().isOk());
    }

    @Test
    public void indexWithCompanyCalendarAccessibleForBoss() throws Exception {

        final Person bossPerson = mock(Person.class);
        when(bossPerson.hasRole(eq(Role.BOSS))).thenReturn(true);

        when(personService.getSignedInUser()).thenReturn(bossPerson);
        when(personService.getPersonByID(1)).thenReturn(Optional.of(bossPerson));
        when(departmentService.getAssignedDepartmentsOfMember(bossPerson)).thenReturn(Collections.emptyList());
        when(personCalendarService.getPersonCalendar(1)).thenReturn(Optional.of(new PersonCalendar()));

        perform(get("/web/calendars/share/persons/1"))
            .andExpect(view().name("calendarsharing/index"))
            .andExpect(model().attributeExists("companyCalendarAccessible"))
            .andExpect(status().isOk());
    }

    @Test
    public void indexWithCompanyCalendarAccessibleForOffice() throws Exception {

        final Person officePerson = mock(Person.class);
        when(officePerson.hasRole(eq(Role.OFFICE))).thenReturn(true);

        when(personService.getSignedInUser()).thenReturn(officePerson);
        when(personService.getPersonByID(1)).thenReturn(Optional.of(officePerson));
        when(departmentService.getAssignedDepartmentsOfMember(officePerson)).thenReturn(Collections.emptyList());
        when(personCalendarService.getPersonCalendar(1)).thenReturn(Optional.of(new PersonCalendar()));

        perform(get("/web/calendars/share/persons/1"))
            .andExpect(view().name("calendarsharing/index"))
            .andExpect(model().attributeExists("companyCalendarAccessible"))
            .andExpect(status().isOk());
    }

    @Test
    public void indexWithoutDepartments() throws Exception {

        final Person person = createPerson();
        person.setId(1);

        when(personService.getSignedInUser()).thenReturn(person);
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(Collections.emptyList());
        when(personCalendarService.getPersonCalendar(1)).thenReturn(Optional.of(new PersonCalendar()));

        perform(get("/web/calendars/share/persons/1"))
            .andExpect(view().name("calendarsharing/index"))
            .andExpect(model().attributeExists("departmentCalendars"))
            .andExpect(status().isOk());
    }

    @Test
    public void indexWithDepartments() throws Exception {

        final Person person = createPerson();
        person.setId(1);

        final Department sockentraeger = createDepartment("sockenträger");
        sockentraeger.setId(42);

        final Department barfuslaeufer = createDepartment("barfußläufer");
        barfuslaeufer.setId(1337);

        when(personService.getSignedInUser()).thenReturn(person);
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(List.of(sockentraeger, barfuslaeufer));
        when(personCalendarService.getPersonCalendar(1)).thenReturn(Optional.of(new PersonCalendar()));

        perform(get("/web/calendars/share/persons/1"))
            .andExpect(view().name("calendarsharing/index"))
            .andExpect(status().isOk());
    }

    @Test
    public void indexWithActiveDepartment() throws Exception {

        final Person person = createPerson();
        person.setId(1);

        final Department sockentraeger = createDepartment("sockenträger");
        sockentraeger.setId(42);

        final Department barfuslaeufer = createDepartment("barfußläufer");
        barfuslaeufer.setId(1337);

        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(List.of(sockentraeger, barfuslaeufer));
        when(personCalendarService.getPersonCalendar(1)).thenReturn(Optional.of(new PersonCalendar()));

        perform(get("/web/calendars/share/persons/1/departments/1337"))
            .andExpect(view().name("calendarsharing/index"))
            .andExpect(status().isOk());
    }

    @Test
    public void indexWithActiveDepartmentThrowsWhenPersonIsNotAMemberOfTheDepartment() throws Exception {

        final Person person = createPerson();
        person.setId(1);

        final Department sockentraeger = createDepartment("sockenträger");
        sockentraeger.setId(42);

        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(List.of(sockentraeger));
        when(personCalendarService.getPersonCalendar(1)).thenReturn(Optional.of(new PersonCalendar()));

        perform(get("/web/calendars/share/persons/1/departments/1337"))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void indexNoPersonCalendar() throws Exception {

        final Person person = createPerson();
        person.setId(1);

        when(personService.getSignedInUser()).thenReturn(person);
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(Collections.emptyList());

        when(personCalendarService.getPersonCalendar(1)).thenReturn(Optional.empty());

        perform(get("/web/calendars/share/persons/1"))
            .andExpect(view().name("calendarsharing/index"))
            .andExpect(model().attributeExists("privateCalendarShare"))
            .andExpect(status().isOk());
    }

    @Test
    public void linkPrivateCalendar() throws Exception {

        perform(post("/web/calendars/share/persons/1/me"))
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/calendars/share/persons/1"));

        verify(personCalendarService).createCalendarForPerson(1);
    }

    @Test
    public void unlinkPrivateCalendar() throws Exception {

        perform(post("/web/calendars/share/persons/1/me").param("unlink", ""))
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/calendars/share/persons/1"));

        verify(personCalendarService).deletePersonalCalendarForPerson(1);
    }

    @Test
    public void linkCompanyCalendar() throws Exception {

        perform(post("/web/calendars/share/persons/1/company"))
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/calendars/share/persons/1"));

        verify(companyCalendarService).createCalendarForPerson(1);
    }

    @Test
    public void unlinkCompanyCalendar() throws Exception {

        perform(post("/web/calendars/share/persons/1/company").param("unlink", ""))
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/calendars/share/persons/1"));

        verify(companyCalendarService).deleteCalendarForPerson(1);
    }

    @Test
    public void ensureCompanyCalendarFeatureEnable() throws Exception {

        perform(
            post("/web/calendars/share/persons/1/company/accessible")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("accessible", "true")
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/calendars/share/persons/1"));

        verify(calendarAccessibleService).enableCompanyCalendar();
    }

    @Test
    public void ensureCompanyCalendarFeatureDisable() throws Exception {

        perform(
            post("/web/calendars/share/persons/1/company/accessible")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("accessible", "false")
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/calendars/share/persons/1"));

        verify(calendarAccessibleService).disableCompanyCalendar();
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
