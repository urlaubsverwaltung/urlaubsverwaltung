package org.synyx.urlaubsverwaltung.workingtime.api;

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
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysService;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
public class WorkDayApiControllerSecurityIT {

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private WorkDaysService workDaysService;
    @MockBean
    private PersonService personService;

    @Test
    public void getWorkdaysWithoutAuthIsUnauthorized() throws Exception {
        final ResultActions resultActions = perform(get("/api/workdays"));
        resultActions.andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    public void getWorkdaysAsAuthenticatedUserForOtherUserIsForbidden() throws Exception {
        when(workDaysService.getWorkDays(any(), any(), any(), any())).thenReturn(BigDecimal.ONE);

        final ResultActions resultActions = perform(get("/api/workdays")
            .param("from", "2016-01-04")
            .param("to", "2016-01-04")
            .param("length", "FULL")
            .param("person", "1"));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "DEPARTMENT_HEAD")
    public void getWorkdaysAsDepartmentHeadUserForOtherUserIsForbidden() throws Exception {
        when(workDaysService.getWorkDays(any(), any(), any(), any())).thenReturn(BigDecimal.ONE);

        final ResultActions resultActions = perform(get("/api/workdays")
            .param("from", "2016-01-04")
            .param("to", "2016-01-04")
            .param("length", "FULL")
            .param("person", "1"));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "SECOND_STAGE_AUTHORITY")
    public void getWorkdaysAsSecondStageAuthorityUserForOtherUserIsForbidden() throws Exception {
        when(workDaysService.getWorkDays(any(), any(), any(), any())).thenReturn(BigDecimal.ONE);

        final ResultActions resultActions = perform(get("/api/workdays")
            .param("from", "2016-01-04")
            .param("to", "2016-01-04")
            .param("length", "FULL")
            .param("person", "1"));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "BOSS")
    public void getWorkdaysAsBossUserForOtherUserIsForbidden() throws Exception {
        when(workDaysService.getWorkDays(any(), any(), any(), any())).thenReturn(BigDecimal.ONE);

        final ResultActions resultActions = perform(get("/api/workdays")
            .param("from", "2016-01-04")
            .param("to", "2016-01-04")
            .param("length", "FULL")
            .param("person", "1"));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    public void getWorkdaysAsAdminUserForOtherUserIsForbidden() throws Exception {
        when(workDaysService.getWorkDays(any(), any(), any(), any())).thenReturn(BigDecimal.ONE);

        final ResultActions resultActions = perform(get("/api/workdays")
            .param("from", "2016-01-04")
            .param("to", "2016-01-04")
            .param("length", "FULL")
            .param("person", "1"));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "INACTIVE")
    public void getWorkdaysAsInactiveUserForOtherUserIsForbidden() throws Exception {
        when(workDaysService.getWorkDays(any(), any(), any(), any())).thenReturn(BigDecimal.ONE);

        final ResultActions resultActions = perform(get("/api/workdays")
            .param("from", "2016-01-04")
            .param("to", "2016-01-04")
            .param("length", "FULL")
            .param("person", "1"));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "OFFICE")
    public void getWorkdaysWithOfficeRoleIsOk() throws Exception {

        when(personService.getPersonByID(1)).thenReturn(Optional.of(new Person()));
        when(workDaysService.getWorkDays(any(), any(), any(), any())).thenReturn(BigDecimal.ONE);

        final ResultActions resultActions = perform(get("/api/workdays")
            .param("from", "2016-01-04")
            .param("to", "2016-01-04")
            .param("length", "FULL")
            .param("person", "1"));
        resultActions.andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user")
    public void getWorkdaysWithSameUserIsOk() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));
        when(workDaysService.getWorkDays(any(), any(), any(), any())).thenReturn(BigDecimal.ONE);

        final ResultActions resultActions = perform(get("/api/workdays")
            .param("from", "2016-01-04")
            .param("to", "2016-01-04")
            .param("length", "FULL")
            .param("person", "1"));
        resultActions.andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "differentUser")
    public void getWorkdaysWithDifferentUserIsForbidden() throws Exception {

        final Person person = new Person();
        person.setUsername("user");
        when(personService.getPersonByID(1)).thenReturn(Optional.of(person));

        final ResultActions resultActions = perform(get("/api/workdays")
            .param("from", "2016-01-04")
            .param("to", "2016-01-04")
            .param("length", "FULL")
            .param("person", "1"));
        resultActions.andExpect(status().isForbidden());
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build().perform(builder);
    }
}
