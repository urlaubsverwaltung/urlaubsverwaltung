package org.synyx.urlaubsverwaltung.calendar;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
    private PersonService personService;

    @Mock
    private DepartmentService departmentService;

    @Before
    public void setUp() {
        sut = new CalendarSharingViewController(personCalendarService, departmentCalendarService, personService, departmentService);
    }

    @Test
    public void indexWithoutDepartments() throws Exception {

        final Person person = createPerson();
        person.setId(1);

        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(Collections.emptyList());
        when(personCalendarService.getPersonCalendar(1)).thenReturn(Optional.of(new PersonCalendar()));

        perform(get("/web/persons/1/calendar/share"))
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

        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(List.of(sockentraeger, barfuslaeufer));
        when(personCalendarService.getPersonCalendar(1)).thenReturn(Optional.of(new PersonCalendar()));

        perform(get("/web/persons/1/calendar/share"))
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

        perform(get("/web/persons/1/calendar/share/departments/1337"))
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

        perform(get("/web/persons/1/calendar/share/departments/1337"))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void indexNoPersonCalendar() throws Exception {

        final Person person = createPerson();
        person.setId(1);

        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));
        when(departmentService.getAssignedDepartmentsOfMember(person)).thenReturn(Collections.emptyList());

        when(personCalendarService.getPersonCalendar(1)).thenReturn(Optional.empty());

        perform(get("/web/persons/1/calendar/share"))
            .andExpect(view().name("calendarsharing/index"))
            .andExpect(model().attributeExists("privateCalendarShare"))
            .andExpect(status().isOk());
    }

    @Test
    public void linkPrivateCalendar() throws Exception {

        perform(post("/web/persons/1/calendar/share/me"))
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/persons/1/calendar/share"));

        verify(personCalendarService).createCalendarForPerson(1);
    }

    @Test
    public void unlinkPrivateCalendar() throws Exception {

        perform(post("/web/persons/1/calendar/share/me").param("unlink", ""))
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/persons/1/calendar/share"));

        verify(personCalendarService).deletePersonalCalendarForPerson(1);
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
