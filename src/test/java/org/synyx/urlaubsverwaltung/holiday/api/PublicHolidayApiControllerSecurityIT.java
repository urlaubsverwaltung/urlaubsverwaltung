package org.synyx.urlaubsverwaltung.holiday.api;

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

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PublicHolidayApiControllerSecurityIT {

    @Autowired
    private WebApplicationContext context;

    @Test
    public void getHolidaysWithoutAuthIsUnauthorized() throws Exception {
        final ResultActions resultActions = perform(get("/api/holidays"));
        resultActions.andExpect(status().isUnauthorized());
    }

    // TODO self test

    @Test
    @WithMockUser(authorities = "OFFICE")
    public void getHolidaysWithOfficeRoleIsOk() throws Exception {
        final ResultActions resultActions = perform(get("/api/holidays")
            .param("year", "2016")
            .param("month", "11")
            .param("person", "1"));
        resultActions.andExpect(status().isOk());
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build().perform(builder);
    }
}
