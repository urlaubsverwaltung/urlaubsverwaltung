package org.synyx.urlaubsverwaltung.statistics.vacationoverview.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.api.RestControllerAdviceExceptionHandler;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class VacationOverviewApiControllerTest {

    private VacationOverviewApiController sut;

    @Mock
    private VacationOverviewService vacationOverviewService;

    @BeforeEach
    void setUp() {
        sut = new VacationOverviewApiController(vacationOverviewService);
    }

    @Test
    void getHolidayOverview() throws Exception {

        when(vacationOverviewService.getVacationOverviews("niceDepartment", 2015, 1))
            .thenReturn(List.of(new VacationOverview()));

        perform(get("/api/vacationoverview")
            .param("selectedDepartment", "niceDepartment")
            .param("selectedYear", "2015")
            .param("selectedMonth", "1"))
            .andExpect(status().isOk());
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).setControllerAdvice(new RestControllerAdviceExceptionHandler()).build().perform(builder);
    }
}
