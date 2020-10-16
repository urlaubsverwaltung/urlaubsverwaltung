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
import org.synyx.urlaubsverwaltung.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    @WithMockUser(authorities = "DEPARTMENT_HEAD")
    void getVacationsAsDepartmentHeadUserForOtherUserIsForbidden() throws Exception {
        final LocalDateTime now = LocalDateTime.now();
        final ResultActions resultActions = perform(get("/api/persons/1/vacations")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5))));

        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "SECOND_STAGE_AUTHORITY")
    void getVacationsAsSecondStageAuthorityUserForOtherUserIsForbidden() throws Exception {
        final LocalDateTime now = LocalDateTime.now();
        final ResultActions resultActions = perform(get("/api/persons/1/vacations")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5))));

        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "BOSS")
    void getVacationsAsBossUserForOtherUserIsForbidden() throws Exception {
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
    @WithMockUser(authorities = "OFFICE")
    void getVacationsWithBasicAuthIsOk() throws Exception {

        when(personService.getPersonByID(1)).thenReturn(Optional.of(new Person()));

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
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

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
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

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
    @WithMockUser(authorities = "DEPARTMENT_HEAD")
    void getVacationsOfDepartmentMembersAsDepartmentHeadUserForOtherUserIsForbidden() throws Exception {
        final LocalDateTime now = LocalDateTime.now();
        final ResultActions resultActions = perform(get("/api/persons/1/vacations")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5)))
            .param("ofDepartmentMembers", "true"));

        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "SECOND_STAGE_AUTHORITY")
    void getVacationsOfDepartmentMembersAsSecondStageAuthorityUserForOtherUserIsForbidden() throws Exception {
        final LocalDateTime now = LocalDateTime.now();
        final ResultActions resultActions = perform(get("/api/persons/1/vacations")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5)))
            .param("ofDepartmentMembers", "true"));

        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "BOSS")
    void getVacationsOfDepartmentMembersAsBossUserForOtherUserIsForbidden() throws Exception {
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
    @WithMockUser(authorities = "OFFICE")
    void getVacationsOfDepartmentMembersWithOfficeUserIsOk() throws Exception {

        when(personService.getPersonByID(1)).thenReturn(Optional.of(new Person()));

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
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

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
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

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
