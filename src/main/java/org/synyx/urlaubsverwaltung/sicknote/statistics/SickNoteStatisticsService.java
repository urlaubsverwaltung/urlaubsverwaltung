package org.synyx.urlaubsverwaltung.sicknote.statistics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysService;

import java.time.Clock;


/**
 * Service for creating {@link SickNoteStatistics}.
 */
@Service
@Transactional
public class SickNoteStatisticsService {

    private final SickNoteService sickNoteService;
    private final WorkDaysService calendarService;

    @Autowired
    public SickNoteStatisticsService(SickNoteService sickNoteService, WorkDaysService calendarService) {

        this.sickNoteService = sickNoteService;
        this.calendarService = calendarService;
    }

    public SickNoteStatistics createStatistics(Clock clock) {

        return new SickNoteStatistics(clock, sickNoteService, calendarService);
    }
}
