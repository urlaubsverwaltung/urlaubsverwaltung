package org.synyx.urlaubsverwaltung.department.api;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.synyx.urlaubsverwaltung.TestContainersBase;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class DepartmentApiControllerSecurityIT extends TestContainersBase {

    @Autowired
    private WebApplicationContext context;

    @Test
    void getDepartmentsWithoutBasicAuthIsUnauthorized() throws Exception {
        final ResultActions resultActions = perform(get("/api/departments"));
        resultActions.andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void getDepartmentsWithBasicAuthIsNotOk() throws Exception {

        final ResultActions resultActions = perform(get("/api/departments")
            .with(httpBasic("user", "password")));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "DEPARTMENT_HEAD")
    void getDepartmentsAsDepartmentHeadUserForOtherUserIsForbidden() throws Exception {

        final ResultActions resultActions = perform(get("/api/departments")
            .with(httpBasic("user", "password")));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "SECOND_STAGE_AUTHORITY")
    void getDepartmentsAsSecondStageAuthorityUserForOtherUserIsForbidden() throws Exception {

        final ResultActions resultActions = perform(get("/api/departments")
            .with(httpBasic("user", "password")));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "BOSS")
    void getDepartmentsAsBossUserForOtherUserIsForbidden() throws Exception {

        final ResultActions resultActions = perform(get("/api/departments")
            .with(httpBasic("user", "password")));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void getDepartmentsAsAdminUserForOtherUserIsForbidden() throws Exception {

        final ResultActions resultActions = perform(get("/api/departments")
            .with(httpBasic("user", "password")));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "INACTIVE")
    void getDepartmentsAsInactiveUserForOtherUserIsForbidden() throws Exception {

        final ResultActions resultActions = perform(get("/api/departments")
            .with(httpBasic("user", "password")));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "OFFICE")
    void getDepartmentsWithOfficeRoleIsOk() throws Exception {

        final ResultActions resultActions = perform(get("/api/departments")
            .with(httpBasic("user", "password")));
        resultActions.andExpect(status().isOk());
    }


    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build().perform(builder);
    }
}
