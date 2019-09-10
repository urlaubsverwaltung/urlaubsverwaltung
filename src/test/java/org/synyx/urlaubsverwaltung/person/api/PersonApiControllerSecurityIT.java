package org.synyx.urlaubsverwaltung.person.api;

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
import org.synyx.urlaubsverwaltung.WithMockCustomUser;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PersonApiControllerSecurityIT {

    @Autowired
    private WebApplicationContext context;

    @Test
    public void getPersonsWithoutBasicAuthIsUnauthorized() throws Exception {
        final ResultActions resultActions = perform(get("/api/persons"));
        resultActions.andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    public void getPersonsAuthenticatedIsNotOk() throws Exception {
        final ResultActions resultActions = perform(get("/api/persons"));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "OFFICE")
    public void getPersonsWithOfficeRoleIsOk() throws Exception {
        final ResultActions resultActions = perform(get("/api/persons"));
        resultActions.andExpect(status().isOk());
    }

    @Test
    public void getPersonWithoutBasicAuthIsUnauthorized() throws Exception {
        final ResultActions resultActions = perform(get("/api/persons/1"));
        resultActions.andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockCustomUser
    public void getPersonForMyIdIsOk() throws Exception {
        final ResultActions resultActions = perform(get("/api/persons/1"));
        resultActions.andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser(id = 3)
    public void getPersonForAnotherIdIsNotOk() throws Exception {
        final ResultActions resultActions = perform(get("/api/persons/1"));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "OFFICE")
    public void getPersonAuthenticatedWithOfficeRoleIsOk() throws Exception {
        final ResultActions resultActions = perform(get("/api/persons/1"));
        resultActions.andExpect(status().isOk());
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build().perform(builder);
    }
}
