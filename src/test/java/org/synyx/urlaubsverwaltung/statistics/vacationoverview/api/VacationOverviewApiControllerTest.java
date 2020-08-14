package org.synyx.urlaubsverwaltung.statistics.vacationoverview.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.api.RestControllerAdviceExceptionHandler;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.api.PersonMapper;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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

        final DayOfMonth dayOfMonth = new DayOfMonth();
        dayOfMonth.setDayNumber(1);
        dayOfMonth.setDayText("Monday");
        dayOfMonth.setTypeOfDay(DayOfMonth.TypeOfDay.WORKDAY);

        final Person person = new Person("shane", "shane", "shane", "shane@example.org");
        final VacationOverviewDto vacationOverviewDto = new VacationOverviewDto();
        vacationOverviewDto.setPerson(PersonMapper.mapToDto(person));
        vacationOverviewDto.setPersonID(2);
        vacationOverviewDto.setDays(List.of(dayOfMonth));

        when(vacationOverviewService.getVacationOverviews("niceDepartment", 2015, 1)).thenReturn(List.of(vacationOverviewDto));

        perform(get("/api/vacationoverview")
            .param("selectedDepartment", "niceDepartment")
            .param("selectedYear", "2015")
            .param("selectedMonth", "1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.overviews[0].person.firstName", is("shane")))
            .andExpect(jsonPath("$.overviews[0].personID", is(2)))
            .andExpect(jsonPath("$.overviews[0].days[0].dayNumber", is(1)))
            .andExpect(jsonPath("$.overviews[0].days[0].dayText", is("Monday")))
            .andExpect(jsonPath("$.overviews[0].days[0].typeOfDay", is("WORKDAY")));
    }

    @Test
    void getHolidayOverviewWithEmptyResult() throws Exception {

        when(vacationOverviewService.getVacationOverviews("niceDepartment", 2015, 1)).thenReturn(List.of());

        perform(get("/api/vacationoverview")
            .param("selectedDepartment", "niceDepartment")
            .param("selectedYear", "2015")
            .param("selectedMonth", "1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.overviews").isEmpty());
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).setControllerAdvice(new RestControllerAdviceExceptionHandler()).build().perform(builder);
    }
}
