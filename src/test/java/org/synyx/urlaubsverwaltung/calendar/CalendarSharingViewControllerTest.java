package org.synyx.urlaubsverwaltung.calendar;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Optional;

import static org.hamcrest.Matchers.hasProperty;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;


@RunWith(MockitoJUnitRunner.class)
public class CalendarSharingViewControllerTest {

    private CalendarSharingViewController sut;

    @Mock
    private PersonCalendarService personCalendarService;

    @Before
    public void setUp() {
        sut = new CalendarSharingViewController(personCalendarService);
    }

    @Test
    public void index() throws Exception {

        when(personCalendarService.getPersonCalendar(1)).thenReturn(Optional.of(new PersonCalendar()));

        perform(get("/web/persons/1/calendar/share"))
            .andExpect(view().name("calendarsharing/index"))
            .andExpect(status().isOk());
    }

    @Test
    public void indexNoPersonCalendar() throws Exception {

        when(personCalendarService.getPersonCalendar(1)).thenReturn(Optional.empty());

        perform(get("/web/persons/1/calendar/share"))
            .andExpect(view().name("calendarsharing/index"))
            .andExpect(model().attributeExists("privateCalendarShare"))
            .andExpect(status().isOk());
    }

    @Test
    public void linkPrivateCalendar() throws Exception {

        perform(post("/web/persons/1/calendar/share/me"))
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/persons/1/calendar/share"));

        verify(personCalendarService).createCalendarForPerson(1);
    }

    @Test
    public void unlinkPrivateCalendar() throws Exception {

        perform(post("/web/persons/1/calendar/share/me").param("unlink", ""))
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/web/persons/1/calendar/share"));

        verify(personCalendarService).deletePersonalCalendarForPerson(1);
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
