package org.synyx.urlaubsverwaltung.sicknote.api;


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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SickNoteApiControllerSecurityIT {

    @Autowired
    private WebApplicationContext context;

    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Test
    public void getSickNotesWithoutBasicAuthIsUnauthorized() throws Exception {
        final ResultActions resultActions = perform(get("/api/sicknotes"));
        resultActions.andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "OFFICE")
    public void getSickNotesWithBasicAuthIsOk() throws Exception {
        final LocalDateTime now = LocalDateTime.now();
        final ResultActions resultActions = perform(get("/api/sicknotes")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5))));

        resultActions.andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void getSicknotesWithNotPrivilegedUserIsForbidden() throws Exception {
        final LocalDateTime now = LocalDateTime.now();
        perform(get("/api/sicknotes")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5))))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "BOSS")
    public void getSicknotesWithBossUserIsForbidden() throws Exception {
        final LocalDateTime now = LocalDateTime.now();
        perform(get("/api/sicknotes")
            .param("from", dtf.format(now)))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "DEPARTMENT_HEAD")
    public void getSicknotesWithDepartmentHeadUserIsForbidden() throws Exception {

        final LocalDateTime now = LocalDateTime.now();
        perform(get("/api/sicknotes")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5))))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "SECOND_STAGE_AUTHORITY")
    public void getSicknotesWithSecondStageUserIsForbidden() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        perform(get("/api/sicknotes")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5))))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "USER")
    public void getSicknotesWithUserIsForbidden() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        perform(get("/api/sicknotes")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5))))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "INACTIVE")
    public void getSicknotesWithInactiveIsForbidden() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        perform(get("/api/sicknotes")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5))))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    public void getSicknotesWithAdminIsForbidden() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        perform(get("/api/sicknotes")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5))))
            .andExpect(status().isForbidden());
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build().perform(builder);
    }
}
