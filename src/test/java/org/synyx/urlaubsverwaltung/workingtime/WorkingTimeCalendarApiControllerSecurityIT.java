package org.synyx.urlaubsverwaltung.workingtime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.synyx.urlaubsverwaltung.TestContainersBase;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@SpringBootTest
class WorkingTimeCalendarApiControllerSecurityIT extends TestContainersBase {

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private WorkDaysCountService workDaysCountService;
    @MockBean
    private DepartmentService departmentService;
    @MockBean
    private PersonService personService;

    @Test
    void getWorkdaysWithoutAuthIsUnauthorized() throws Exception {
        final ResultActions resultActions = perform(get("/api/workdays"));
        resultActions.andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void getWorkdaysAsAuthenticatedUserForOtherUserIsForbidden() throws Exception {
        when(workDaysCountService.getWorkDaysCount(any(), any(), any(), any())).thenReturn(BigDecimal.ONE);

        final ResultActions resultActions = perform(get("/api/persons/1/workdays")
            .param("from", "2016-01-04")
            .param("to", "2016-01-04")
            .param("length", "FULL"));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "department head", authorities = "DEPARTMENT_HEAD")
    void getWorkdaysAsDepartmentHeadUserForOtherUserIsForbidden() throws Exception {
        when(workDaysCountService.getWorkDaysCount(any(), any(), any(), any())).thenReturn(BigDecimal.ONE);

        final Person requester = new Person();
        requester.setPermissions(List.of(USER, DEPARTMENT_HEAD));
        when(personService.getPersonByUsername("department head")).thenReturn(Optional.of(requester));

        final Person requestedPerson = new Person();
        when(personService.getPersonByID(1)).thenReturn(Optional.of(requestedPerson));

        when(departmentService.isDepartmentHeadAllowedToManagePerson(requester, requestedPerson)).thenReturn(false);

        final ResultActions resultActions = perform(get("/api/persons/1/workdays")
            .param("from", "2016-01-04")
            .param("to", "2016-01-04")
            .param("length", "FULL"));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "second stage authority", authorities = "SECOND_STAGE_AUTHORITY")
    void getWorkdaysAsSecondStageAuthorityUserForOtherUserIsForbidden() throws Exception {
        when(workDaysCountService.getWorkDaysCount(any(), any(), any(), any())).thenReturn(BigDecimal.ONE);

        final Person requester = new Person();
        requester.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY));
        when(personService.getPersonByUsername("second stage authority")).thenReturn(Optional.of(requester));

        final Person requestedPerson = new Person();
        when(personService.getPersonByID(1)).thenReturn(Optional.of(requestedPerson));

        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(requester, requestedPerson)).thenReturn(false);

        final ResultActions resultActions = perform(get("/api/persons/1/workdays")
            .param("from", "2016-01-04")
            .param("to", "2016-01-04")
            .param("length", "FULL"));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void getWorkdaysAsAdminUserForOtherUserIsForbidden() throws Exception {
        when(workDaysCountService.getWorkDaysCount(any(), any(), any(), any())).thenReturn(BigDecimal.ONE);

        final ResultActions resultActions = perform(get("/api/persons/1/workdays")
            .param("from", "2016-01-04")
            .param("to", "2016-01-04")
            .param("length", "FULL"));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "INACTIVE")
    void getWorkdaysAsInactiveUserForOtherUserIsForbidden() throws Exception {
        when(workDaysCountService.getWorkDaysCount(any(), any(), any(), any())).thenReturn(BigDecimal.ONE);

        final ResultActions resultActions = perform(get("/api/persons/1/workdays")
            .param("from", "2016-01-04")
            .param("to", "2016-01-04")
            .param("length", "FULL"));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "department head", authorities = "DEPARTMENT_HEAD")
    void getWorkdaysAsDepartmentHeadUserForOtherUserIsOk() throws Exception {
        when(workDaysCountService.getWorkDaysCount(any(), any(), any(), any())).thenReturn(BigDecimal.ONE);

        final Person requester = new Person();
        requester.setPermissions(List.of(USER, DEPARTMENT_HEAD));
        when(personService.getPersonByUsername("department head")).thenReturn(Optional.of(requester));

        final Person requestedPerson = new Person();
        when(personService.getPersonByID(1)).thenReturn(Optional.of(requestedPerson));

        when(departmentService.isDepartmentHeadAllowedToManagePerson(requester, requestedPerson)).thenReturn(true);

        final ResultActions resultActions = perform(get("/api/persons/1/workdays")
            .param("from", "2016-01-04")
            .param("to", "2016-01-04")
            .param("length", "FULL"));
        resultActions.andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "second stage authority", authorities = "SECOND_STAGE_AUTHORITY")
    void getWorkdaysAsSecondStageAuthorityUserForOtherUserIsOk() throws Exception {
        when(workDaysCountService.getWorkDaysCount(any(), any(), any(), any())).thenReturn(BigDecimal.ONE);

        final Person requester = new Person();
        requester.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY));
        when(personService.getPersonByUsername("second stage authority")).thenReturn(Optional.of(requester));

        final Person requestedPerson = new Person();
        when(personService.getPersonByID(1)).thenReturn(Optional.of(requestedPerson));

        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(requester, requestedPerson)).thenReturn(true);

        final ResultActions resultActions = perform(get("/api/persons/1/workdays")
            .param("from", "2016-01-04")
            .param("to", "2016-01-04")
            .param("length", "FULL"));
        resultActions.andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "BOSS")
    void getWorkdaysAsBossUserForOtherUserIsForbidden() throws Exception {
        when(workDaysCountService.getWorkDaysCount(any(), any(), any(), any())).thenReturn(BigDecimal.ONE);
        when(personService.getPersonByID(1)).thenReturn(Optional.of(new Person()));

        final ResultActions resultActions = perform(get("/api/persons/1/workdays")
            .param("from", "2016-01-04")
            .param("to", "2016-01-04")
            .param("length", "FULL"));
        resultActions.andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "OFFICE")
    void getWorkdaysWithOfficeRoleIsOk() throws Exception {

        when(personService.getPersonByID(1)).thenReturn(Optional.of(new Person()));
        when(workDaysCountService.getWorkDaysCount(any(), any(), any(), any())).thenReturn(BigDecimal.ONE);

        final ResultActions resultActions = perform(get("/api/persons/1/workdays")
            .param("from", "2016-01-04")
            .param("to", "2016-01-04")
            .param("length", "FULL"));
        resultActions.andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user")
    void getWorkdaysWithSameUserIsOk() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));
        when(workDaysCountService.getWorkDaysCount(any(), any(), any(), any())).thenReturn(BigDecimal.ONE);

        final ResultActions resultActions = perform(get("/api/persons/1/workdays")
            .param("from", "2016-01-04")
            .param("to", "2016-01-04")
            .param("length", "FULL"));
        resultActions.andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "differentUser")
    void getWorkdaysWithDifferentUserIsForbidden() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        final ResultActions resultActions = perform(get("/api/persons/1/workdays")
            .param("from", "2016-01-04")
            .param("to", "2016-01-04")
            .param("length", "FULL"));
        resultActions.andExpect(status().isForbidden());
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build().perform(builder);
    }
}
