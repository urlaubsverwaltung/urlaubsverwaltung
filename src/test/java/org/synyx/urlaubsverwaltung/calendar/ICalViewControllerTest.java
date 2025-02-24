package org.synyx.urlaubsverwaltung.calendar;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static java.util.Locale.GERMAN;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;


@ExtendWith(MockitoExtension.class)
class ICalViewControllerTest {

    private ICalViewController sut;

    @Mock
    private PersonCalendarService personCalendarService;
    @Mock
    private DepartmentCalendarService departmentCalendarService;
    @Mock
    private CompanyCalendarService companyCalendarService;

    @BeforeEach
    void setUp() {
        sut = new ICalViewController(personCalendarService, departmentCalendarService, companyCalendarService);
    }

    @Test
    void getCalendarForPerson() throws Exception {

        when(personCalendarService.getCalendarForPerson(1L, "secret", GERMAN)).thenReturn(new ByteArrayResource("iCal string".getBytes()));

        perform(get("/web/persons/1/calendar")
            .locale(GERMAN)
            .param("secret", "secret"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type", "text/calendar;charset=UTF-8"))
            .andExpect(header().string("Content-Disposition", "attachment; filename=calendar.ics"))
            .andExpect(content().string(containsString("iCal string")));
    }

    @Test
    void getCalendarForPersonWithBadRequest() throws Exception {

        when(personCalendarService.getCalendarForPerson(1L, "secret", GERMAN)).thenThrow(new IllegalArgumentException());

        perform(get("/web/persons/1/calendar")
            .locale(GERMAN)
            .param("secret", "secret"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getCalendarForPersonWithNoContent() throws Exception {

        when(personCalendarService.getCalendarForPerson(1L, "secret", GERMAN)).thenThrow(CalendarException.class);

        perform(get("/web/persons/1/calendar")
            .locale(GERMAN)
            .param("secret", "secret"))
            .andExpect(status().isNoContent());
    }

    @Test
    void getCalendarForDepartment() throws Exception {

        when(departmentCalendarService.getCalendarForDepartment(1L, 2L, "secret", GERMAN)).thenReturn(new ByteArrayResource("calendar department".getBytes()));

        perform(get("/web/departments/1/persons/2/calendar")
            .locale(GERMAN)
            .param("secret", "secret"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type", "text/calendar;charset=UTF-8"))
            .andExpect(header().string("Content-Disposition", "attachment; filename=calendar.ics"))
            .andExpect(content().string(containsString("calendar department")));
    }

    @Test
    void getCalendarForDepartmentWithBadRequest() throws Exception {

        when(departmentCalendarService.getCalendarForDepartment(1L, 2L, "secret", GERMAN)).thenThrow(new IllegalArgumentException());

        perform(get("/web/departments/1/persons/2/calendar")
            .locale(GERMAN)
            .param("secret", "secret"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getCalendarForDepartmentWithNoContent() throws Exception {

        when(departmentCalendarService.getCalendarForDepartment(1L, 2L, "secret", GERMAN)).thenThrow(CalendarException.class);

        perform(get("/web/departments/1/persons/2/calendar")
            .locale(GERMAN)
            .param("secret", "secret"))
            .andExpect(status().isNoContent());
    }

    @Test
    void getCalendarForAll() throws Exception {

        when(companyCalendarService.getCalendarForAll(2L, "secret", GERMAN)).thenReturn(new ByteArrayResource("calendar all".getBytes()));

        perform(get("/web/company/persons/2/calendar")
            .locale(GERMAN)
            .param("secret", "secret"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type", "text/calendar;charset=UTF-8"))
            .andExpect(header().string("Content-Disposition", "attachment; filename=calendar.ics"))
            .andExpect(content().string(containsString("calendar all")));
    }

    @Test
    void getCalendarForAllWithNoContent() throws Exception {

        when(companyCalendarService.getCalendarForAll(2L, "secret", GERMAN)).thenThrow(CalendarException.class);

        perform(get("/web/company/persons/2/calendar")
            .locale(GERMAN)
            .param("secret", "secret"))
            .andExpect(status().isNoContent());
    }

    @Test
    void getCalendarForAllWithIllegalArgumentException() throws Exception {

        when(companyCalendarService.getCalendarForAll(2L, "secret", GERMAN)).thenThrow(IllegalArgumentException.class);

        perform(get("/web/company/persons/2/calendar")
            .locale(GERMAN)
            .param("secret", "secret"))
            .andExpect(status().isBadRequest());
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
