package org.synyx.urlaubsverwaltung.statistics.vacationoverview.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.api.ApiExceptionHandlerControllerAdvice;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@RunWith(MockitoJUnitRunner.class)
public class VacationOverviewApiControllerTest {

    private VacationOverviewApiController sut;

    @Mock
    private VacationOverviewService vacationOverviewService;

    @Before
    public void setUp() {
        sut = new VacationOverviewApiController(vacationOverviewService);
    }

    @Test
    public void getHolidayOverview() throws Exception {

        when(vacationOverviewService.getVacationOverviews("niceDepartment", 2015, 1))
            .thenReturn(List.of(new VacationOverview()));

        perform(get("/api/vacationoverview")
            .param("selectedDepartment", "niceDepartment")
            .param("selectedYear", "2015")
            .param("selectedMonth", "1"))
            .andExpect(status().isOk());
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).setControllerAdvice(new ApiExceptionHandlerControllerAdvice()).build().perform(builder);
    }
}
