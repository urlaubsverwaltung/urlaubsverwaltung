package org.synyx.urlaubsverwaltung.restapi.vacationoverview;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import org.synyx.urlaubsverwaltung.restapi.ApiExceptionHandlerControllerAdvice;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * @author  Peter Bolch - peter@bolch.space
 */
public class VacationOverviewControllerTest {

    private MockMvc mockMvc;

    private VacationOverviewService vacationOverviewServiceMock;

    @Before
    public void setUp() {

        vacationOverviewServiceMock = Mockito.mock(VacationOverviewService.class);

        mockMvc = MockMvcBuilders.standaloneSetup(new VacationOverviewController(vacationOverviewServiceMock))
                .setControllerAdvice(new ApiExceptionHandlerControllerAdvice())
                .build();
    }


    @Test
    public void ensureBadRequestForMissingSelectedDepartmentParameter() throws Exception {

        mockMvc.perform(get("/api/vacationoverview").param("selectedYear", "2016").param("selectedMonth", "8"))
            .andExpect(status().isBadRequest());
    }


    @Test
    public void ensureBadRequestForMissingSelectedYearParameter() throws Exception {

        mockMvc.perform(get("/api/vacationoverview").param("selectedDepartment", "Entwicklung")
                .param("selectedMonth", "8"))
            .andExpect(status().isBadRequest());
    }


    @Test
    public void ensureBadRequestForMissingSelectedMonthParameter() throws Exception {

        mockMvc.perform(get("/api/vacationoverview").param("selectedYear", "2016")
                .param("selectedDepartment", "Entwicklung"))
            .andExpect(status().isBadRequest());
    }
}
