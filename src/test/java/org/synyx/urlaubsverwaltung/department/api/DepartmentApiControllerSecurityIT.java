package org.synyx.urlaubsverwaltung.department.api;


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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DepartmentApiControllerSecurityIT {

    @Autowired
    private WebApplicationContext context;

    @Test
    public void getDepartmentsWithoutBasicAuthIsUnauthorized() throws Exception {
        final ResultActions resultActions = perform(get("/api/departments"));
        resultActions.andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    public void getDepartmentsWithBasicAuthIsNotOk() throws Exception {

        final ResultActions resultActions = perform(get("/api/departments")
            .with(httpBasic("user", "password")));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "DEPARTMENT_HEAD")
    public void getDepartmentsAsDepartmentHeadUserForOtherUserIsForbidden() throws Exception {

        final ResultActions resultActions = perform(get("/api/departments")
            .with(httpBasic("user", "password")));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "SECOND_STAGE_AUTHORITY")
    public void getDepartmentsAsSecondStageAuthorityUserForOtherUserIsForbidden() throws Exception {

        final ResultActions resultActions = perform(get("/api/departments")
            .with(httpBasic("user", "password")));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "BOSS")
    public void getDepartmentsAsBossUserForOtherUserIsForbidden() throws Exception {

        final ResultActions resultActions = perform(get("/api/departments")
            .with(httpBasic("user", "password")));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    public void getDepartmentsAsAdminUserForOtherUserIsForbidden() throws Exception {

        final ResultActions resultActions = perform(get("/api/departments")
            .with(httpBasic("user", "password")));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "INACTIVE")
    public void getDepartmentsAsInactiveUserForOtherUserIsForbidden() throws Exception {

        final ResultActions resultActions = perform(get("/api/departments")
            .with(httpBasic("user", "password")));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "OFFICE")
    public void getDepartmentsWithOfficeRoleIsOk() throws Exception {

        final ResultActions resultActions = perform(get("/api/departments")
            .with(httpBasic("user", "password")));
        resultActions.andExpect(status().isOk());
    }


    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build().perform(builder);
    }
}
