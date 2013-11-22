package org.synyx.urlaubsverwaltung.sicknote.statistics;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.transaction.annotation.Transactional;

import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteDAO;

import java.util.ArrayList;
import java.util.List;


/**
 * Service for creating {@link SickNoteStatistics}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Transactional
public class SickNoteStatisticsService {

    private SickNoteDAO sickNoteDAO;

    @Autowired
    public SickNoteStatisticsService(SickNoteDAO sickNoteDAO) {

        this.sickNoteDAO = sickNoteDAO;
    }


    public SickNoteStatisticsService() {
    }

    public SickNoteStatistics createStatistics(int year) {

        List<MonthStatistic> monthStatistics = new ArrayList<MonthStatistic>();

        for (Month month : Month.values()) {
            List<SickNote> sickNotes = sickNoteDAO.findByMonth(year, month.ordinal() + 1);
            MonthStatistic monthStatistic = new MonthStatistic(month, sickNotes);
            monthStatistics.add(monthStatistic);
        }

        SickNoteStatistics statistics = new SickNoteStatistics(year, monthStatistics);

        return statistics;
    }
}
