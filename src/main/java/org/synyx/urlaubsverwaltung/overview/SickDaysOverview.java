package org.synyx.urlaubsverwaltung.overview;

import org.synyx.urlaubsverwaltung.sickdays.web.SickDays;
import org.synyx.urlaubsverwaltung.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.math.BigDecimal;
import java.util.List;

import static org.synyx.urlaubsverwaltung.sickdays.web.SickDays.SickDayType.TOTAL;
import static org.synyx.urlaubsverwaltung.sickdays.web.SickDays.SickDayType.WITH_AUB;
import static org.synyx.urlaubsverwaltung.sicknote.SickNoteCategory.SICK_NOTE_CHILD;

/**
 * Build a sick days statistic for a certain year and person.
 */
public class SickDaysOverview {

    private final SickDays sickDays;
    private final SickDays childSickDays;

    SickDaysOverview(List<SickNote> sickNotes, WorkDaysCountService workDaysCountService) {

        this.sickDays = new SickDays();
        this.childSickDays = new SickDays();

        for (SickNote sickNote : sickNotes) {
            if (!sickNote.isActive()) {
                continue;
            }

            if (sickNote.getSickNoteType().isOfCategory(SICK_NOTE_CHILD)) {
                this.childSickDays.addDays(TOTAL, getTotalDays(sickNote, workDaysCountService));

                if (sickNote.isAubPresent()) {
                    this.childSickDays.addDays(WITH_AUB, getDaysWithAUB(sickNote, workDaysCountService));
                }
            } else {
                this.sickDays.addDays(TOTAL, getTotalDays(sickNote, workDaysCountService));

                if (sickNote.isAubPresent()) {
                    this.sickDays.addDays(WITH_AUB, getDaysWithAUB(sickNote, workDaysCountService));
                }
            }
        }
    }

    private BigDecimal getTotalDays(SickNote sickNote, WorkDaysCountService workDaysCountService) {
        return workDaysCountService.getWorkDaysCount(sickNote.getDayLength(), sickNote.getStartDate(), sickNote.getEndDate(),
            sickNote.getPerson());
    }

    private BigDecimal getDaysWithAUB(SickNote sickNote, WorkDaysCountService workDaysCountService) {
        return workDaysCountService.getWorkDaysCount(sickNote.getDayLength(), sickNote.getAubStartDate(),
            sickNote.getAubEndDate(), sickNote.getPerson());
    }

    public SickDays getSickDays() {
        return sickDays;
    }

    public SickDays getChildSickDays() {
        return childSickDays;
    }
}
