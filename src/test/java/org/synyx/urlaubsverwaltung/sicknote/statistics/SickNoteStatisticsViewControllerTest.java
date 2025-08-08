package org.synyx.urlaubsverwaltung.sicknote.statistics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class SickNoteStatisticsViewControllerTest {

    private SickNoteStatisticsViewController sut;

    @Mock
    private SickNoteStatisticsService statisticsService;
    @Mock
    private PersonService personService;

    private final Clock clock = Clock.systemUTC();

    @BeforeEach
    void setUp() {
        sut = new SickNoteStatisticsViewController(statisticsService, personService, clock);
    }

    @Test
    void sickNoteStatistics() throws Exception {

        final Year year = Year.now(clock);

        final Person person = new Person();
        when(personService.getSignedInUser()).thenReturn(person);

        final SickNoteStatistics sickNoteStatistics = new SickNoteStatistics(year, LocalDate.now(clock), List.of(), List.of());
        when(statisticsService.createStatisticsForPerson(eq(year), eq(person))).thenReturn(sickNoteStatistics);

        perform(get("/web/sicknote/statistics")
            .param("year", String.valueOf(year.getValue()))
        )
            .andExpect(status().isOk())
            .andExpect(model().attribute("statistics", sickNoteStatistics))
            .andExpect(model().attribute("currentYear", year.getValue()))
            .andExpect(view().name("sicknote/sick_notes_statistics"));
    }

    @Test
    void sickNoteStatisticsWithoutYear() throws Exception {

        final Year year = Year.now(clock);

        final Person person = new Person();
        when(personService.getSignedInUser()).thenReturn(person);
        final SickNoteStatistics sickNoteStatistics = new SickNoteStatistics(year, LocalDate.now(clock), List.of(), List.of());
        when(statisticsService.createStatisticsForPerson(eq(year), eq(person))).thenReturn(sickNoteStatistics);

        perform(get("/web/sicknote/statistics"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("statistics", sickNoteStatistics))
            .andExpect(model().attribute("currentYear", year.getValue()))
            .andExpect(view().name("sicknote/sick_notes_statistics"));
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
