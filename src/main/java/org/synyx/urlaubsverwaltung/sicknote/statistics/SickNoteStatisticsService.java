package org.synyx.urlaubsverwaltung.sicknote.statistics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.time.Clock;

/**
 * Service for creating {@link SickNoteStatistics}.
 */
@Service
@Transactional
public class SickNoteStatisticsService {

    private final SickNoteService sickNoteService;
    private final WorkDaysCountService workDaysCountService;

    @Autowired
    SickNoteStatisticsService(SickNoteService sickNoteService, WorkDaysCountService workDaysCountService) {
        this.sickNoteService = sickNoteService;
        this.workDaysCountService = workDaysCountService;
    }

    SickNoteStatistics createStatistics(Clock clock) {
        return new SickNoteStatistics(clock, sickNoteService, workDaysCountService);
    }
}
