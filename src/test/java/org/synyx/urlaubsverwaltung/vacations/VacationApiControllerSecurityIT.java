package org.synyx.urlaubsverwaltung.vacations;

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
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@SpringBootTest
class VacationApiControllerSecurityIT extends TestContainersBase {

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private PersonService personService;
    @MockBean
    private ApplicationService applicationService;
    @MockBean
    private DepartmentService departmentService;

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Test
    void getVacationsWithoutBasicAuthIsUnauthorized() throws Exception {
        final ResultActions resultActions = perform(get("/api/persons/1/vacations"));
        resultActions.andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void getVacationsAsAuthenticatedUserForOtherUserIsForbidden() throws Exception {
        final LocalDateTime now = LocalDateTime.now();
        final ResultActions resultActions = perform(get("/api/persons/1/vacations")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5))));

        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "department head", authorities = "DEPARTMENT_HEAD")
    void getVacationsAsDepartmentHeadUserForOthersNotFromOwnDepartmentIsForbidden() throws Exception {

        final Person requester = new Person();
        requester.setPermissions(List.of(USER, DEPARTMENT_HEAD));
        when(personService.getPersonByUsername("department head")).thenReturn(Optional.of(requester));

        final Person requestedPerson = new Person();
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(requestedPerson));

        when(departmentService.isDepartmentHeadAllowedToManagePerson(requester, requestedPerson)).thenReturn(false);

        final LocalDateTime now = LocalDateTime.now();
        final ResultActions resultActions = perform(get("/api/persons/1/vacations")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5))));

        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "second stage authority", authorities = "SECOND_STAGE_AUTHORITY")
    void getVacationsAsSSAUserForOthersNotFromOwnDepartmentIsForbidden() throws Exception {

        final Person requester = new Person();
        requester.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY));
        when(personService.getPersonByUsername("second stage authority")).thenReturn(Optional.of(requester));

        final Person requestedPerson = new Person();
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(requestedPerson));

        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(requester, requestedPerson)).thenReturn(false);

        final LocalDateTime now = LocalDateTime.now();
        final ResultActions resultActions = perform(get("/api/persons/1/vacations")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5))));

        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void getVacationsAsAdminUserForOtherUserIsForbidden() throws Exception {
        final LocalDateTime now = LocalDateTime.now();
        final ResultActions resultActions = perform(get("/api/persons/1/vacations")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5))));

        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "INACTIVE")
    void getVacationsAsInactiveUserForOtherUserIsForbidden() throws Exception {
        final LocalDateTime now = LocalDateTime.now();
        final ResultActions resultActions = perform(get("/api/persons/1/vacations")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5))));

        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "department head", authorities = "DEPARTMENT_HEAD")
    void getVacationsAsDepartmentHeadUserForOtherUserIsOk() throws Exception {

        final Person requester = new Person();
        requester.setPermissions(List.of(USER, DEPARTMENT_HEAD));
        when(personService.getPersonByUsername("department head")).thenReturn(Optional.of(requester));

        final Person requestedPerson = new Person();
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(requestedPerson));

        when(departmentService.isDepartmentHeadAllowedToManagePerson(requester, requestedPerson)).thenReturn(true);

        final LocalDateTime now = LocalDateTime.now();
        final ResultActions resultActions = perform(get("/api/persons/1/vacations")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5))));

        resultActions.andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "second stage authority", authorities = "SECOND_STAGE_AUTHORITY")
    void getVacationsAsSecondStageAuthorityUserForOtherUserIsOk() throws Exception {

        final Person requester = new Person();
        requester.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY));
        when(personService.getPersonByUsername("second stage authority")).thenReturn(Optional.of(requester));

        final Person requestedPerson = new Person();
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(requestedPerson));

        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(requester, requestedPerson)).thenReturn(true);

        final LocalDateTime now = LocalDateTime.now();
        final ResultActions resultActions = perform(get("/api/persons/1/vacations")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5))));

        resultActions.andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "BOSS")
    void getVacationsAsBossUserForOtherUserIsOk() throws Exception {

        when(personService.getPersonByID(1L)).thenReturn(Optional.of(new Person()));

        final LocalDateTime now = LocalDateTime.now();
        final ResultActions resultActions = perform(get("/api/persons/1/vacations")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5))));

        resultActions.andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "OFFICE")
    void getVacationsWithBasicAuthIsOk() throws Exception {

        when(personService.getPersonByID(1L)).thenReturn(Optional.of(new Person()));

        final LocalDateTime now = LocalDateTime.now();
        final ResultActions resultActions = perform(get("/api/persons/1/vacations")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5))));

        resultActions.andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user")
    void getVacationsForSameUserIsOk() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        final LocalDateTime now = LocalDateTime.now();
        final ResultActions resultActions = perform(get("/api/persons/1/vacations")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5)))
        );

        resultActions.andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "differentUser")
    void getVacationsForDifferentUserIsForbidden() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        final LocalDateTime now = LocalDateTime.now();
        final ResultActions resultActions = perform(get("/api/persons/1/vacations")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5)))
        );

        resultActions.andExpect(status().isForbidden());
    }


    @Test
    void getVacationsOfDepartmentMembersWithoutBasicAuthIsUnauthorized() throws Exception {
        final ResultActions resultActions = perform(get("/api/persons/1/vacations")
            .param("ofDepartmentMembers", "true"));
        resultActions.andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void getVacationsOfDepartmentMembersAsAuthenticatedUserForOtherUserIsForbidden() throws Exception {
        final LocalDateTime now = LocalDateTime.now();
        final ResultActions resultActions = perform(get("/api/persons/1/vacations")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5)))
            .param("ofDepartmentMembers", "true"));

        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "department head", authorities = "DEPARTMENT_HEAD")
    void getVacationsOfDepartmentMembersAsDepartmentHeadUserForOtherUserIsForbidden() throws Exception {

        final Person requester = new Person();
        requester.setPermissions(List.of(USER, DEPARTMENT_HEAD));
        when(personService.getPersonByUsername("department head")).thenReturn(Optional.of(requester));

        final Person requestedPerson = new Person();
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(requestedPerson));

        when(departmentService.isDepartmentHeadAllowedToManagePerson(requester, requestedPerson)).thenReturn(false);

        final LocalDateTime now = LocalDateTime.now();
        final ResultActions resultActions = perform(get("/api/persons/1/vacations")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5)))
            .param("ofDepartmentMembers", "true"));

        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "second stage authority", authorities = "SECOND_STAGE_AUTHORITY")
    void getVacationsOfDepartmentMembersAsSecondStageAuthorityUserForOtherUserIsForbidden() throws Exception {

        final Person requester = new Person();
        requester.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY));
        when(personService.getPersonByUsername("second stage authority")).thenReturn(Optional.of(requester));

        final Person requestedPerson = new Person();
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(requestedPerson));

        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(requester, requestedPerson)).thenReturn(false);

        final LocalDateTime now = LocalDateTime.now();
        final ResultActions resultActions = perform(get("/api/persons/1/vacations")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5)))
            .param("ofDepartmentMembers", "true"));

        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void getVacationsOfDepartmentMembersAsAdminUserForOtherUserIsForbidden() throws Exception {
        final LocalDateTime now = LocalDateTime.now();
        final ResultActions resultActions = perform(get("/api/persons/1/vacations")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5)))
            .param("ofDepartmentMembers", "true"));

        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "INACTIVE")
    void getVacationsOfDepartmentMembersAsInactiveUserForOtherUserIsForbidden() throws Exception {
        final LocalDateTime now = LocalDateTime.now();
        final ResultActions resultActions = perform(get("/api/persons/1/vacations")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5)))
            .param("ofDepartmentMembers", "true"));

        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "department head", authorities = "DEPARTMENT_HEAD")
    void getVacationsOfDepartmentMembersAsDepartmentHeadUserForOtherUserIsOk() throws Exception {

        final Person requester = new Person();
        requester.setPermissions(List.of(USER, DEPARTMENT_HEAD));
        when(personService.getPersonByUsername("department head")).thenReturn(Optional.of(requester));

        final Person requestedPerson = new Person();
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(requestedPerson));

        when(departmentService.isDepartmentHeadAllowedToManagePerson(requester, requestedPerson)).thenReturn(true);

        final LocalDateTime now = LocalDateTime.now();
        final ResultActions resultActions = perform(get("/api/persons/1/vacations")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5)))
            .param("ofDepartmentMembers", "true"));

        resultActions.andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "second stage authority", authorities = "SECOND_STAGE_AUTHORITY")
    void getVacationsOfDepartmentMembersAsSecondStageAuthorityUserForOtherUserIsOk() throws Exception {

        final Person requester = new Person();
        requester.setPermissions(List.of(USER, SECOND_STAGE_AUTHORITY));
        when(personService.getPersonByUsername("second stage authority")).thenReturn(Optional.of(requester));

        final Person requestedPerson = new Person();
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(requestedPerson));

        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(requester, requestedPerson)).thenReturn(true);

        final LocalDateTime now = LocalDateTime.now();
        final ResultActions resultActions = perform(get("/api/persons/1/vacations")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5)))
            .param("ofDepartmentMembers", "true"));

        resultActions.andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "OFFICE")
    void getVacationsOfDepartmentMembersWithOfficeUserIsOk() throws Exception {

        when(personService.getPersonByID(1L)).thenReturn(Optional.of(new Person()));

        final LocalDateTime now = LocalDateTime.now();
        final ResultActions resultActions = perform(get("/api/persons/1/vacations")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5)))
            .param("ofDepartmentMembers", "true"));

        resultActions.andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "BOSS")
    void getVacationsOfDepartmentMembersAsBossUserForOtherUserIsOk() throws Exception {

        when(personService.getPersonByID(1L)).thenReturn(Optional.of(new Person()));

        final LocalDateTime now = LocalDateTime.now();
        final ResultActions resultActions = perform(get("/api/persons/1/vacations")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5)))
            .param("ofDepartmentMembers", "true"));

        resultActions.andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user")
    void getVacationsOfDepartmentMembersForSameUserIsOk() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        final LocalDateTime now = LocalDateTime.now();
        final ResultActions resultActions = perform(get("/api/persons/1/vacations")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5)))
            .param("ofDepartmentMembers", "true")
        );

        resultActions.andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "differentUser")
    void getVacationsOfDepartmentMembersForDifferentUserIsForbidden() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1L)).thenReturn(Optional.of(person));

        final LocalDateTime now = LocalDateTime.now();
        final ResultActions resultActions = perform(get("/api/persons/1/vacations")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5)))
            .param("ofDepartmentMembers", "true")
        );

        resultActions.andExpect(status().isForbidden());
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build().perform(builder);
    }
}
