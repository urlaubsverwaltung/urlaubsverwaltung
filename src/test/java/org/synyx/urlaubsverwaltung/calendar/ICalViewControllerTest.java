package org.synyx.urlaubsverwaltung.calendar;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.api.ApiExceptionHandlerControllerAdvice;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;


@RunWith(MockitoJUnitRunner.class)
public class ICalViewControllerTest {

    private ICalViewController sut;

    @Mock
    private PersonCalendarService personCalendarService;
    @Mock
    private DepartmentCalendarService departmentCalendarService;
    @Mock
    private CompanyCalendarService companyCalendarService;

    @Before
    public void setUp() {
        sut = new ICalViewController(personCalendarService, departmentCalendarService, companyCalendarService);
    }

    @Test
    public void getCalendarForPerson() throws Exception {

        when(personCalendarService.getCalendarForPerson(1, "secret")).thenReturn("iCal string");

        perform(get("/web/persons/1/calendar")
            .param("secret", "secret"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type", "text/calendar;charset=UTF-8"))
            .andExpect(header().string("Content-Disposition", "attachment; filename=calendar.ics"))
            .andExpect(content().string("iCal string"));
    }

    @Test
    public void getCalendarForPersonWithBadRequest() throws Exception {

        when(personCalendarService.getCalendarForPerson(1, "secret")).thenThrow(new IllegalArgumentException());

        perform(get("/web/persons/1/calendar").param("secret", "secret"))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void getCalendarForPersonWithNoContent() throws Exception {

        when(personCalendarService.getCalendarForPerson(1, "secret")).thenThrow(CalendarException.class);

        perform(get("/web/persons/1/calendar")
            .param("secret", "secret"))
            .andExpect(status().isNoContent());
    }

    @Test
    public void getCalendarForDepartment() throws Exception {

        when(departmentCalendarService.getCalendarForDepartment(1, 2, "secret")).thenReturn("calendar department");

        perform(get("/web/departments/1/persons/2/calendar")
            .param("secret", "secret"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type", "text/calendar;charset=UTF-8"))
            .andExpect(header().string("Content-Disposition", "attachment; filename=calendar.ics"))
            .andExpect(content().string("calendar department"));
    }

    @Test
    public void getCalendarForDepartmentWithBadRequest() throws Exception {

        when(departmentCalendarService.getCalendarForDepartment(1, 2, "secret")).thenThrow(new IllegalArgumentException());

        perform(get("/web/departments/1/persons/2/calendar").param("secret", "secret"))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void getCalendarForDepartmentWithNoContent() throws Exception {

        when(departmentCalendarService.getCalendarForDepartment(1, 2, "secret")).thenThrow(CalendarException.class);

        perform(get("/web/departments/1/persons/2/calendar")
            .param("secret", "secret"))
            .andExpect(status().isNoContent());
    }


    @Test
    public void getCalendarForAll() throws Exception {

        when(companyCalendarService.getCalendarForAll("secret")).thenReturn("calendar all");

        perform(get("/web/company/calendar")
            .param("secret", "secret"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type", "text/calendar;charset=UTF-8"))
            .andExpect(header().string("Content-Disposition", "attachment; filename=calendar.ics"))
            .andExpect(content().string("calendar all"));
    }

    @Test
    public void getCalendarForAllWithNoContent() throws Exception {

        when(companyCalendarService.getCalendarForAll("secret")).thenThrow(CalendarException.class);

        perform(get("/web/company/calendar")
            .param("secret", "secret"))
            .andExpect(status().isNoContent());
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).setControllerAdvice(new ApiExceptionHandlerControllerAdvice()).build().perform(builder);
    }
}
