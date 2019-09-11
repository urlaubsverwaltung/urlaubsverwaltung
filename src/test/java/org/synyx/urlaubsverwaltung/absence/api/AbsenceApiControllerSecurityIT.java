package org.synyx.urlaubsverwaltung.absence.api;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AbsenceApiControllerSecurityIT {

    @Autowired
    private WebApplicationContext context;

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
    @WithMockUser(authorities = "DEPARTMENT_HEAD")
    public void getAbsencesAsDepartmentHeadUserForOtherUserIsForbidden() throws Exception {
        perform(get("/api/absences")
            .param("year", String.valueOf(LocalDate.now().getYear()))
            .param("person", "1"))
            .andExpect(status().isForbidden());
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
    @WithMockUser(authorities = "BOSS")
    public void getAbsencesAsBossUserForOtherUserIsForbidden() throws Exception {
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
        perform(get("/api/absences")
            .param("year", String.valueOf(LocalDate.now().getYear()))
            .param("person", "1"))
            .andExpect(status().isOk());
    }


    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build().perform(builder);
    }
}
