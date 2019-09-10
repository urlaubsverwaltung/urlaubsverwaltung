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
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysService;

import java.math.BigDecimal;

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

    @Test
    public void getWorkdaysWithoutAuthIsUnauthorized() throws Exception {
        final ResultActions resultActions = perform(get("/api/workdays"));
        resultActions.andExpect(status().isUnauthorized());
    }

    //@Test TODO
    //@WithMockUser
    //public void getWorkdaysWithAuthenticatedIsNotOk() throws Exception {
    //   final ResultActions resultActions = perform(get("/api/workdays")
    //       .param("from", "2016-01-04")
    //       .param("to", "2016-01-04")
    //       .param("length", "FULL")
    //       .param("person", "23"));
    //   resultActions.andExpect(status().isForbidden());
    //}

    @Test
    @WithMockUser(authorities = "OFFICE")
    public void getWorkdaysWithOfficeRoleIsOk() throws Exception {

        when(workDaysService.getWorkDays(any(), any(), any(), any())).thenReturn(BigDecimal.ONE);

        final ResultActions resultActions = perform(get("/api/workdays")
            .param("from", "2016-01-04")
            .param("to", "2016-01-04")
            .param("length", "FULL")
            .param("person", "1"));
        resultActions.andExpect(status().isOk());
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build().perform(builder);
    }
}
