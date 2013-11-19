package org.synyx.urlaubsverwaltung.sicknote;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.transaction.annotation.Transactional;

import org.synyx.urlaubsverwaltung.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.calendar.OwnCalendarService;

import java.math.BigDecimal;

import java.util.List;


/**
 * Service for handling {@link SickNote}s.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Transactional
public class SickNoteService {

    private SickNoteDAO sickNoteDAO;
    private OwnCalendarService calendarService;

    @Autowired
    public SickNoteService(SickNoteDAO sickNoteDAO, OwnCalendarService calendarService) {

        this.sickNoteDAO = sickNoteDAO;
        this.calendarService = calendarService;
    }


    public SickNoteService() {
    }

    public void save(SickNote sickNote) {

        sickNote.setLastEdited(DateMidnight.now());

        BigDecimal workDays = calendarService.getWorkDays(DayLength.FULL, sickNote.getStartDate(),
                sickNote.getEndDate());
        sickNote.setWorkDays(workDays);

        sickNoteDAO.save(sickNote);
    }


    public List<SickNote> getAll() {

        return sickNoteDAO.findAll();
    }


    public SickNote getById(Integer id) {

        return sickNoteDAO.findOne(id);
    }
}
