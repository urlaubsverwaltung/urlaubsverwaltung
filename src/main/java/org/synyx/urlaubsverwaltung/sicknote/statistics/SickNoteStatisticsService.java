package org.synyx.urlaubsverwaltung.sicknote.statistics;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.transaction.annotation.Transactional;

import org.synyx.urlaubsverwaltung.calendar.OwnCalendarService;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteDAO;


/**
 * Service for creating {@link SickNoteStatistics}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Transactional
public class SickNoteStatisticsService {

    private SickNoteDAO sickNoteDAO;
    private OwnCalendarService calendarService;

    @Autowired
    public SickNoteStatisticsService(SickNoteDAO sickNoteDAO, OwnCalendarService calendarService) {

        this.sickNoteDAO = sickNoteDAO;
        this.calendarService = calendarService;
    }


    public SickNoteStatisticsService() {
    }

    public SickNoteStatistics createStatistics(int year) {

        return new SickNoteStatistics(year, sickNoteDAO, calendarService);
    }
}
