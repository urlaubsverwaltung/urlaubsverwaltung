package org.synyx.urlaubsverwaltung.overview.calendar;

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
public class VacationApiControllerSecurityIT {

    @Autowired
    private WebApplicationContext context;

    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Test
    public void getVacationsWithoutBasicAuthIsUnauthorized() throws Exception {
        final ResultActions resultActions = perform(get("/api/vacations"));
        resultActions.andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "OFFICE")
    public void getVacationsWithBasicAuthIsOk() throws Exception {
        final LocalDateTime now = LocalDateTime.now();
        final ResultActions resultActions = perform(get("/api/vacations")
            .param("from", dtf.format(now))
            .param("to", dtf.format(now.plusDays(5))));

        resultActions.andExpect(status().isOk());
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build().perform(builder);
    }
}
