package org.synyx.urlaubsverwaltung.publicholiday;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.context.WebApplicationContext;
import org.synyx.urlaubsverwaltung.TestContainersBase;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeWriteService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static org.synyx.urlaubsverwaltung.person.Role.DEPARTMENT_HEAD;
import static org.synyx.urlaubsverwaltung.person.Role.SECOND_STAGE_AUTHORITY;
import static org.synyx.urlaubsverwaltung.workingtime.FederalState.GERMANY_BAYERN;

@SpringBootTest
class PublicHolidayApiControllerSecurityIT extends TestContainersBase {

    @Autowired
    private WebApplicationContext context;
    @MockBean
    private PersonService personService;
    @MockBean
    private WorkingTimeService workingTimeService;
    @MockBean
    private DepartmentService departmentService;

    // WorkingTimeWriteService is required for personService
    @MockBean
    private WorkingTimeWriteService workingTimeWriteService;

    @Test
    void getHolidaysWithoutAuthIsUnauthorized() throws Exception {
        final ResultActions resultActions = perform(get("/api/public-holidays"));
        resultActions.andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void getHolidaysAsAuthenticatedUserForOtherUserIsForbidden() throws Exception {
        final ResultActions resultActions = perform(get("/api/public-holidays")
            .param("from", "2016-01-01")
            .param("to", "2016-01-31"));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "DEPARTMENT_HEAD")
    void getHolidaysAsDepartmentHeadUserForOtherUserIsForbidden() throws Exception {
        final ResultActions resultActions = perform(get("/api/public-holidays")
            .param("from", "2016-01-01")
            .param("to", "2016-01-31"));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "SECOND_STAGE_AUTHORITY")
    void getHolidaysAsSecondStageAuthorityUserForOtherUserIsForbidden() throws Exception {
        final ResultActions resultActions = perform(get("/api/public-holidays")
            .param("from", "2016-01-01")
            .param("to", "2016-01-31"));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "BOSS")
    void getHolidaysAsBossUserForOtherUserIsForbidden() throws Exception {
        final ResultActions resultActions = perform(get("/api/public-holidays")
            .param("from", "2016-01-01")
            .param("to", "2016-01-31"));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void getHolidaysAsAdminUserForOtherUserIsForbidden() throws Exception {
        final ResultActions resultActions = perform(get("/api/public-holidays")
            .param("from", "2016-01-01")
            .param("to", "2016-01-31"));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "INACTIVE")
    void getHolidaysAsInactiveUserForOtherUserIsForbidden() throws Exception {
        final ResultActions resultActions = perform(get("/api/public-holidays")
            .param("from", "2016-01-01")
            .param("to", "2016-01-31"));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "OFFICE")
    void getHolidaysWithOfficeRoleIsOk() throws Exception {

        when(workingTimeService.getFederalStateForPerson(any(Person.class), any(LocalDate.class))).thenReturn(GERMANY_BAYERN);

        final ResultActions resultActions = perform(get("/api/public-holidays")
            .param("from", "2016-01-01")
            .param("to", "2016-01-31"));
        resultActions.andExpect(status().isOk());
    }

    @Test
    void personsPublicHolidaysWithoutAuthIsUnauthorized() throws Exception {
        final ResultActions resultActions = perform(get("/api/persons/1/public-holidays"));
        resultActions.andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void personsPublicHolidaysAsAuthenticatedUserForOtherUserIsForbidden() throws Exception {
        final ResultActions resultActions = perform(get("/api/persons/1/public-holidays")
            .param("from", "2016-01-01")
            .param("to", "2016-01-31"));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void personsPublicHolidaysAsAdminUserForOtherUserIsForbidden() throws Exception {
        final ResultActions resultActions = perform(get("/api/persons/1/public-holidays")
            .param("from", "2016-01-01")
            .param("to", "2016-01-31"));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "INACTIVE")
    void personsPublicHolidaysAsInactiveUserForOtherUserIsForbidden() throws Exception {
        final ResultActions resultActions = perform(get("/api/persons/1/public-holidays")
            .param("from", "2016-01-01")
            .param("to", "2016-01-31"));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "DEPARTMENT_HEAD", username = "departmentHead")
    void personsPublicHolidaysAsDepartmentHeadUserForOtherUserIsOk() throws Exception {

        final Person person = new Person();
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        final Person departmentHead = new Person();
        departmentHead.setPermissions(List.of(DEPARTMENT_HEAD));
        when(personService.getPersonByUsername("departmentHead")).thenReturn(Optional.of(departmentHead));
        when(departmentService.isDepartmentHeadAllowedToManagePerson(departmentHead, person)).thenReturn(true);

        when(workingTimeService.getFederalStateForPerson(eq(person), any(LocalDate.class))).thenReturn(GERMANY_BAYERN);

        final ResultActions resultActions = perform(get("/api/persons/1/public-holidays")
            .param("from", "2016-01-01")
            .param("to", "2016-01-31"));
        resultActions.andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "SECOND_STAGE_AUTHORITY", username = "ssa")
    void personsPublicHolidaysAsSSAUserForOtherUserIsOk() throws Exception {

        final Person person = new Person();
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        final Person ssa = new Person();
        ssa.setPermissions(List.of(SECOND_STAGE_AUTHORITY));
        when(personService.getPersonByUsername("ssa")).thenReturn(Optional.of(ssa));
        when(departmentService.isSecondStageAuthorityAllowedToManagePerson(ssa, person)).thenReturn(true);
        when(workingTimeService.getFederalStateForPerson(eq(person), any(LocalDate.class))).thenReturn(GERMANY_BAYERN);

        final ResultActions resultActions = perform(get("/api/persons/1/public-holidays")
            .param("from", "2016-01-01")
            .param("to", "2016-01-31"));
        resultActions.andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "BOSS")
    void personsPublicHolidaysWithBossRoleIsOk() throws Exception {

        when(personService.getPersonByID(1)).thenReturn(Optional.of(new Person()));
        when(workingTimeService.getFederalStateForPerson(any(Person.class), any(LocalDate.class))).thenReturn(GERMANY_BAYERN);

        final ResultActions resultActions = perform(get("/api/persons/1/public-holidays")
            .param("from", "2016-01-01")
            .param("to", "2016-01-31"));
        resultActions.andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "OFFICE")
    void personsPublicHolidaysWithOfficeRoleIsOk() throws Exception {

        when(personService.getPersonByID(1)).thenReturn(Optional.of(new Person()));
        when(workingTimeService.getFederalStateForPerson(any(Person.class), any(LocalDate.class))).thenReturn(GERMANY_BAYERN);

        final ResultActions resultActions = perform(get("/api/persons/1/public-holidays")
            .param("from", "2016-01-01")
            .param("to", "2016-01-31"));
        resultActions.andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user")
    void personsPublicHolidaysWithSameUserIsOk() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));
        when(workingTimeService.getFederalStateForPerson(any(Person.class), any(LocalDate.class))).thenReturn(GERMANY_BAYERN);

        final ResultActions resultActions = perform(get("/api/persons/1/public-holidays")
            .param("from", "2016-01-01")
            .param("to", "2016-01-31"));
        resultActions.andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "differentUser")
    void personsPublicHolidaysWithDifferentUserIsForbidden() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        final ResultActions resultActions = perform(get("/api/persons/1/public-holidays")
            .param("from", "2016-01-01")
            .param("to", "2016-01-31"));
        resultActions.andExpect(status().isForbidden());
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return webAppContextSetup(context).apply(springSecurity()).build().perform(builder);
    }
}
