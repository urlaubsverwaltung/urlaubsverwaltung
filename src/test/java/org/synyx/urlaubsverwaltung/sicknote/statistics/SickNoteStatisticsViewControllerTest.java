package org.synyx.urlaubsverwaltung.sicknote.statistics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.time.Clock;
import java.time.Year;

import static org.mockito.ArgumentMatchers.any;
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
    private SickNoteService sickNoteService;
    @Mock
    private WorkDaysCountService workDaysCountService;

    private final Clock clock = Clock.systemUTC();

    @BeforeEach
    void setUp() {
        sut = new SickNoteStatisticsViewController(statisticsService, clock);
    }

    @Test
    void sickNoteStatistics() throws Exception {

        final SickNoteStatistics sickNoteStatistics = new SickNoteStatistics(clock, sickNoteService, workDaysCountService);
        when(statisticsService.createStatistics(any(Clock.class))).thenReturn(sickNoteStatistics);

        final ResultActions resultActions = perform(get("/web/sicknote/statistics")
            .param("year", String.valueOf(Year.now(clock).getValue())));
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(model().attribute("statistics", sickNoteStatistics));
        resultActions.andExpect(view().name("sicknote/sick_notes_statistics"));
    }

    @Test
    void sickNoteStatisticsWithoutYear() throws Exception {

        final SickNoteStatistics sickNoteStatistics = new SickNoteStatistics(clock, sickNoteService, workDaysCountService);
        when(statisticsService.createStatistics(any(Clock.class))).thenReturn(sickNoteStatistics);

        final ResultActions resultActions = perform(get("/web/sicknote/statistics"));
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(model().attribute("statistics", sickNoteStatistics));
        resultActions.andExpect(view().name("sicknote/sick_notes_statistics"));
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return standaloneSetup(sut).build().perform(builder);
    }
}
