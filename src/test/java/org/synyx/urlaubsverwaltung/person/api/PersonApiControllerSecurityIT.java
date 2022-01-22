package org.synyx.urlaubsverwaltung.person.api;

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
import org.synyx.urlaubsverwaltung.user.UserThemeControllerAdvice;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class PersonApiControllerSecurityIT extends TestContainersBase {

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private PersonService personService;

    @MockBean
    private UserThemeControllerAdvice userThemeControllerAdvice;

    @Test
    void getPersonsWithoutBasicAuthIsUnauthorized() throws Exception {
        final ResultActions resultActions = perform(get("/api/persons"));
        resultActions.andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void getPersonsAuthenticatedIsNotOk() throws Exception {
        final ResultActions resultActions = perform(get("/api/persons"));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "DEPARTMENT_HEAD")
    void getPersonsAsDepartmentHeadUserForOtherUserIsForbidden() throws Exception {
        final ResultActions resultActions = perform(get("/api/persons"));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "SECOND_STAGE_AUTHORITY")
    void getPersonsAsSecondStageAuthorityUserForOtherUserIsForbidden() throws Exception {
        final ResultActions resultActions = perform(get("/api/persons"));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "BOSS")
    void getPersonsAsBossUserForOtherUserIsForbidden() throws Exception {
        final ResultActions resultActions = perform(get("/api/persons"));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void getPersonsAsAdminUserForOtherUserIsForbidden() throws Exception {
        final ResultActions resultActions = perform(get("/api/persons"));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "INACTIVE")
    void getPersonsAsInactiveUserForOtherUserIsForbidden() throws Exception {
        final ResultActions resultActions = perform(get("/api/persons"));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "OFFICE")
    void getPersonsWithOfficeRoleIsOk() throws Exception {
        final ResultActions resultActions = perform(get("/api/persons"));
        resultActions.andExpect(status().isOk());
    }

    @Test
    void getPersonWithoutBasicAuthIsUnauthorized() throws Exception {
        final ResultActions resultActions = perform(get("/api/persons/1"));
        resultActions.andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void getPersonAsAuthenticatedUserForOtherUserIsForbidden() throws Exception {
        final ResultActions resultActions = perform(get("/api/persons/1"));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "DEPARTMENT_HEAD")
    void getPersonAsDepartmentHeadUserForOtherUserIsForbidden() throws Exception {
        final ResultActions resultActions = perform(get("/api/persons/1"));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "SECOND_STAGE_AUTHORITY")
    void getPersonAsSecondStageAuthorityUserForOtherUserIsForbidden() throws Exception {
        final ResultActions resultActions = perform(get("/api/persons/1"));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "BOSS")
    void getPersonAsBossUserForOtherUserIsForbidden() throws Exception {
        final ResultActions resultActions = perform(get("/api/persons/1"));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void getPersonAsAdminUserForOtherUserIsForbidden() throws Exception {
        final ResultActions resultActions = perform(get("/api/persons/1"));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "INACTIVE")
    void getPersonAsInactiveUserForOtherUserIsForbidden() throws Exception {
        final ResultActions resultActions = perform(get("/api/persons/1"));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "OFFICE")
    void getPersonAuthenticatedWithOfficeRoleIsOk() throws Exception {

        when(personService.getPersonByID(1)).thenReturn(Optional.of(new Person()));

        final ResultActions resultActions = perform(get("/api/persons/1"));
        resultActions.andExpect(status().isOk());
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build().perform(builder);
    }
}
