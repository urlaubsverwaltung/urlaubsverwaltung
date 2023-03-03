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
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.time.Clock;
import java.time.Year;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
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
    private WorkDaysCountService workDaysCountService;
    @Mock
    private PersonService personService;

    private final Clock clock = Clock.systemUTC();

    @BeforeEach
    void setUp() {
        sut = new SickNoteStatisticsViewController(statisticsService, personService, clock);
    }

    @Test
    void sickNoteStatistics() throws Exception {

        final Person person = new Person();
        when(personService.getSignedInUser()).thenReturn(person);

        final SickNoteStatistics sickNoteStatistics = new SickNoteStatistics(clock, List.of(), workDaysCountService);
        when(statisticsService.createStatisticsForPerson(eq(person), any(Clock.class))).thenReturn(sickNoteStatistics);

        final int currentYear = Year.now(clock).getValue();
        final ResultActions resultActions = perform(get("/web/sicknote/statistics")
            .param("year", String.valueOf(currentYear)));
        resultActions
            .andExpect(status().isOk())
            .andExpect(model().attribute("statistics", sickNoteStatistics))
            .andExpect(model().attribute("currentYear", currentYear))
            .andExpect(view().name("sicknote/sick_notes_statistics"));
    }

    @Test
    void sickNoteStatisticsWithoutYear() throws Exception {

        final Person person = new Person();
        when(personService.getSignedInUser()).thenReturn(person);

        final SickNoteStatistics sickNoteStatistics = new SickNoteStatistics(clock, List.of(), workDaysCountService);
        when(statisticsService.createStatisticsForPerson(eq(person), any(Clock.class))).thenReturn(sickNoteStatistics);

        final int currentYear = Year.now(clock).getValue();

        final ResultActions resultActions = perform(get("/web/sicknote/statistics"));
        resultActions
            .andExpect(status().isOk())
            .andExpect(model().attribute("statistics", sickNoteStatistics))
            .andExpect(model().attribute("currentYear", currentYear))
            .andExpect(view().name("sicknote/sick_notes_statistics"));
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
