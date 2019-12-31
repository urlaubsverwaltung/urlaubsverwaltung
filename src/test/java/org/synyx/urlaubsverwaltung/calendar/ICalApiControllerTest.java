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
public class ICalApiControllerTest {

    private ICalApiController sut;

    @Mock
    private ICalService iCalService;

    @Before
    public void setUp() {
        sut = new ICalApiController(iCalService);
    }

    @Test
    public void getCalendarForPerson() throws Exception {

        when(iCalService.getCalendarForPerson(1)).thenReturn("iCal string");

        perform(get("/api/persons/1/calendar"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type", "text/calendar;charset=UTF-8"))
            .andExpect(header().string("Content-Disposition", "attachment; filename=calendar.ics"))
            .andExpect(content().string("iCal string"));
    }

    @Test
    public void getCalendarForPersonWithBadRequest() throws Exception {

        when(iCalService.getCalendarForPerson(1)).thenThrow(new IllegalArgumentException());

        perform(get("/api/persons/1/calendar"))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void getCalendarForDepartment() throws Exception {

        when(iCalService.getCalendarForDepartment(1)).thenReturn("calendar department");

        perform(get("/api/departments/1/calendar"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type", "text/calendar;charset=UTF-8"))
            .andExpect(header().string("Content-Disposition", "attachment; filename=calendar.ics"))
            .andExpect(content().string("calendar department"));
    }

    @Test
    public void getCalendarForDepartmentWithBadRequest() throws Exception {

        when(iCalService.getCalendarForDepartment(1)).thenThrow(new IllegalArgumentException());

        perform(get("/api/departments/1/calendar"))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void getCalendarForAll() throws Exception {

        when(iCalService.getCalendarForAll()).thenReturn("calendar all");

        perform(get("/api/company/calendar"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type", "text/calendar;charset=UTF-8"))
            .andExpect(header().string("Content-Disposition", "attachment; filename=calendar.ics"))
            .andExpect(content().string("calendar all"));
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).setControllerAdvice(new ApiExceptionHandlerControllerAdvice()).build().perform(builder);
    }
}
