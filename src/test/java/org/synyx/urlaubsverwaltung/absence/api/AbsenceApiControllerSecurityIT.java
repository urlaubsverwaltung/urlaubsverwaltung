package org.synyx.urlaubsverwaltung.absence.api;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator.createApplication;
import static org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator.createSickNote;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AbsenceApiControllerSecurityIT {

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private PersonService personService;
    @MockBean
    private SickNoteService sickNoteService;
    @MockBean
    private ApplicationService applicationService;
    @MockBean
    private DepartmentService departmentService;

    @Test
    public void getAbsencesWithoutBasicAuthIsUnauthorized() throws Exception {
        final ResultActions resultActions = perform(get("/api/absences"));
        resultActions.andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    public void getAbsencesAsAuthenticatedUserForOtherUserIsForbidden() throws Exception {
        perform(get("/api/absences")
            .param("year", String.valueOf(LocalDate.now().getYear()))
            .param("person", "1"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "DEPARTMENT_HEAD", username = "departmentHead")
    public void getAbsencesAsDepartmentHeadUserForOtherUserIsForbidden() throws Exception {
        final Person person = new Person();
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        final Person departmentHead = new Person();
        departmentHead.setPermissions(List.of(DEPARTMENT_HEAD));
        when(personService.getPersonByUsername("departmentHead")).thenReturn(Optional.of(departmentHead));

        final Department department = new Department();
        department.setMembers(List.of());

        final List<Department> departments = List.of(department);
        when(departmentService.getManagedDepartmentsOfDepartmentHead(departmentHead)).thenReturn(departments);

        perform(get("/api/absences")
            .param("year", String.valueOf(LocalDate.now().getYear()))
            .param("person", "1"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "DEPARTMENT_HEAD", username = "departmentHead")
    public void getAbsencesAsDepartmentHeadUserForOtherUserInSameDepartmentIsOk() throws Exception {
        final Person person = new Person();
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        final Person departmentHead = new Person();
        departmentHead.setPermissions(List.of(DEPARTMENT_HEAD));
        when(personService.getPersonByUsername("departmentHead")).thenReturn(Optional.of(departmentHead));

        final Department department = new Department();
        department.setMembers(List.of(person));
        final List<Department> departments = List.of(department);
        when(departmentService.getManagedDepartmentsOfDepartmentHead(departmentHead)).thenReturn(departments);

        final SickNote sickNote = createSickNote(person, LocalDate.of(2016, 5, 19),
            LocalDate.of(2016, 5, 20), DayLength.FULL);
        sickNote.setId(1);
        when(sickNoteService.getByPersonAndPeriod(any(Person.class), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(singletonList(sickNote));

        final Application vacation = createApplication(person, LocalDate.of(2016, 4, 6),
            LocalDate.of(2016, 4, 6), DayLength.FULL);
        when(applicationService.getApplicationsForACertainPeriodAndPerson(any(LocalDate.class), any(LocalDate.class), any(Person.class)))
            .thenReturn(singletonList(vacation));

        perform(get("/api/absences")
            .param("year", String.valueOf(LocalDate.now().getYear()))
            .param("person", "1"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "SECOND_STAGE_AUTHORITY")
    public void getAbsencesAsSecondStageAuthorityUserForOtherUserIsForbidden() throws Exception {
        perform(get("/api/absences")
            .param("year", String.valueOf(LocalDate.now().getYear()))
            .param("person", "1"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    public void getAbsencesAsAdminUserForOtherUserIsForbidden() throws Exception {
        perform(get("/api/absences")
            .param("year", String.valueOf(LocalDate.now().getYear()))
            .param("person", "1"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "INACTIVE")
    public void getAbsencesAsInactiveUserForOtherUserIsForbidden() throws Exception {
        perform(get("/api/absences")
            .param("year", String.valueOf(LocalDate.now().getYear()))
            .param("person", "1"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "OFFICE")
    public void getAbsencesAsOfficeUserForOtherUserIsOk() throws Exception {

        testWithUserWithCorrectPermissions();
    }

    @Test
    @WithMockUser(authorities = "BOSS")
    public void getAbsencesAsBossUserForOtherUserIsOk() throws Exception {

        testWithUserWithCorrectPermissions();
    }

    @Test
    @WithMockUser(username = "user")
    public void getAbsencesAsOfficeUserForSameUserIsOk() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        final SickNote sickNote = createSickNote(person, LocalDate.of(2016, 5, 19),
            LocalDate.of(2016, 5, 20), DayLength.FULL);
        sickNote.setId(1);
        when(sickNoteService.getByPersonAndPeriod(any(Person.class), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(singletonList(sickNote));

        final Application vacation = createApplication(person, LocalDate.of(2016, 4, 6),
            LocalDate.of(2016, 4, 6), DayLength.FULL);
        when(applicationService.getApplicationsForACertainPeriodAndPerson(any(LocalDate.class), any(LocalDate.class), any(Person.class)))
            .thenReturn(singletonList(vacation));

        perform(get("/api/absences")
            .param("year", String.valueOf(LocalDate.now().getYear()))
            .param("person", "1"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "differentUser")
    public void getAbsencesAsOfficeUserForDifferentUserIsForbidden() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        perform(get("/api/absences")
            .param("year", String.valueOf(LocalDate.now().getYear()))
            .param("person", "1"))
            .andExpect(status().isForbidden());
    }

    private void testWithUserWithCorrectPermissions() throws Exception {
        final Person person = new Person();
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        final SickNote sickNote = createSickNote(person, LocalDate.of(2016, 5, 19),
            LocalDate.of(2016, 5, 20), DayLength.FULL);
        sickNote.setId(1);
        when(sickNoteService.getByPersonAndPeriod(any(Person.class), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(singletonList(sickNote));

        final Application vacation = createApplication(person, LocalDate.of(2016, 4, 6),
            LocalDate.of(2016, 4, 6), DayLength.FULL);
        when(applicationService.getApplicationsForACertainPeriodAndPerson(any(LocalDate.class), any(LocalDate.class), any(Person.class)))
            .thenReturn(singletonList(vacation));

        perform(get("/api/absences")
            .param("year", String.valueOf(LocalDate.now().getYear()))
            .param("person", "1"))
            .andExpect(status().isOk());
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build().perform(builder);
    }
}
