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
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeService;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static org.synyx.urlaubsverwaltung.settings.FederalState.BAYERN;

@SpringBootTest
class PublicHolidayApiControllerSecurityIT extends TestContainersBase {

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private PersonService personService;
    @MockBean
    private WorkingTimeService workingTimeService;

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

        when(workingTimeService.getFederalStateForPerson(any(Person.class), any(LocalDate.class))).thenReturn(BAYERN);

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
    @WithMockUser(authorities = "DEPARTMENT_HEAD")
    void personsPublicHolidaysAsDepartmentHeadUserForOtherUserIsForbidden() throws Exception {
        final ResultActions resultActions = perform(get("/api/persons/1/public-holidays")
            .param("from", "2016-01-01")
            .param("to", "2016-01-31"));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "SECOND_STAGE_AUTHORITY")
    void personsPublicHolidaysAsSecondStageAuthorityUserForOtherUserIsForbidden() throws Exception {
        final ResultActions resultActions = perform(get("/api/persons/1/public-holidays")
            .param("from", "2016-01-01")
            .param("to", "2016-01-31"));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "BOSS")
    void personsPublicHolidaysAsBossUserForOtherUserIsForbidden() throws Exception {
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
    @WithMockUser(authorities = "OFFICE")
    void personsPublicHolidaysWithOfficeRoleIsOk() throws Exception {

        when(personService.getPersonByID(1)).thenReturn(Optional.of(new Person()));
        when(workingTimeService.getFederalStateForPerson(any(Person.class), any(LocalDate.class))).thenReturn(BAYERN);

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
        when(workingTimeService.getFederalStateForPerson(any(Person.class), any(LocalDate.class))).thenReturn(BAYERN);

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
