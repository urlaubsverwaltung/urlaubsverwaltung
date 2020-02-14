package org.synyx.urlaubsverwaltung.sicknote.statistics.web;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.sicknote.statistics.SickNoteStatistics;
import org.synyx.urlaubsverwaltung.sicknote.statistics.SickNoteStatisticsService;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysService;

import java.time.Clock;
import java.time.Instant;
import java.time.Year;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@RunWith(MockitoJUnitRunner.class)
public class SickNoteStatisticsViewControllerTest {

    private SickNoteStatisticsViewController sut;

    @Mock
    private SickNoteStatisticsService statisticsService;
    @Mock
    private SickNoteService sickNoteService;
    @Mock
    private WorkDaysService calendarService;
    private Clock clock;

    @Before
    public void setUp() {

        clock = Clock.systemUTC();
        sut = new SickNoteStatisticsViewController(statisticsService, clock);
    }

    @Test
    public void sickNoteStatistics() throws Exception {

        final Clock fixedClock = Clock.fixed(Instant.parse("2017-04-02T00:00:00.00Z"), clock.getZone());

        final SickNoteStatistics sickNoteStatistics = new SickNoteStatistics(fixedClock, sickNoteService, calendarService);
        when(statisticsService.createStatistics(any(Clock.class))).thenReturn(sickNoteStatistics);

        final ResultActions resultActions = perform(get("/web/sicknote/statistics")
            .param("year", "2017"));
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(model().attribute("statistics", sickNoteStatistics));
        resultActions.andExpect(view().name("sicknote/sick_notes_statistics"));
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
