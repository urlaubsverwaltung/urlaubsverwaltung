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
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class WorkDaysCountApiControllerSecurityIT extends TestContainersBase {

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private WorkDaysCountService workDaysCountService;
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
    @WithMockUser(authorities = "DEPARTMENT_HEAD")
    void getWorkdaysAsDepartmentHeadUserForOtherUserIsForbidden() throws Exception {
        when(workDaysCountService.getWorkDaysCount(any(), any(), any(), any())).thenReturn(BigDecimal.ONE);

        final ResultActions resultActions = perform(get("/api/persons/1/workdays")
            .param("from", "2016-01-04")
            .param("to", "2016-01-04")
            .param("length", "FULL"));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "SECOND_STAGE_AUTHORITY")
    void getWorkdaysAsSecondStageAuthorityUserForOtherUserIsForbidden() throws Exception {
        when(workDaysCountService.getWorkDaysCount(any(), any(), any(), any())).thenReturn(BigDecimal.ONE);

        final ResultActions resultActions = perform(get("/api/persons/1/workdays")
            .param("from", "2016-01-04")
            .param("to", "2016-01-04")
            .param("length", "FULL"));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "BOSS")
    void getWorkdaysAsBossUserForOtherUserIsForbidden() throws Exception {
        when(workDaysCountService.getWorkDaysCount(any(), any(), any(), any())).thenReturn(BigDecimal.ONE);

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
