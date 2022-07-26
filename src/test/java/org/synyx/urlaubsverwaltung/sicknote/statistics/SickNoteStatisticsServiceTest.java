package org.synyx.urlaubsverwaltung.sicknote.statistics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class SickNoteStatisticsServiceTest {

    @Mock
    private WorkDaysCountService workDaysCountService;
    @Mock
    private SickNoteService sickNoteService;

    @Captor
    private ArgumentCaptor<SickNoteStatistics> sickNoteStatisticsArgumentCaptor;

    private SickNoteStatisticsService sickNoteStatisticsService;

    @BeforeEach
    void setUp() {
        sickNoteStatisticsService = new SickNoteStatisticsService(sickNoteService, workDaysCountService);
    }

    @Test
    void createStatistics() {

        Clock clock = Clock.systemUTC();

        final SickNoteStatistics statistics = sickNoteStatisticsService.createStatistics(clock);

        assertThat(statistics).isNotNull();
    }
}
