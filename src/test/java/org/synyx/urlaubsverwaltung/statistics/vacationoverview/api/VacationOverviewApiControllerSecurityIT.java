package org.synyx.urlaubsverwaltung.statistics.vacationoverview.api;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
public class VacationOverviewApiControllerSecurityIT {

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private VacationOverviewService vacationOverviewService;

    @Test
    public void getWorkdaysWithoutAuthIsUnauthorized() throws Exception {
        final ResultActions resultActions = perform(get("/api/vacationoverview"));
        resultActions.andExpect(status().isUnauthorized());
    }

    // TODO self test

    @Test
    @WithMockUser(authorities = "OFFICE")
    public void getWorkdaysWithOfficeRoleIsOk() throws Exception {

        when(vacationOverviewService.getVacationOverviews("niceDepartment", 2015, 1))
            .thenReturn(List.of(new VacationOverview()));

        final ResultActions resultActions = perform(get("/api/vacationoverview")
            .param("selectedDepartment", "niceDepartment")
            .param("selectedYear", "2015")
            .param("selectedMonth", "1"));
        resultActions.andExpect(status().isOk());
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build().perform(builder);
    }
}
