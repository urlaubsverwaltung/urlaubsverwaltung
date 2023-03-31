package org.synyx.urlaubsverwaltung.overview;

import org.synyx.urlaubsverwaltung.sicknote.sickdays.SickDays;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.synyx.urlaubsverwaltung.sicknote.sickdays.SickDays.SickDayType.TOTAL;
import static org.synyx.urlaubsverwaltung.sicknote.sickdays.SickDays.SickDayType.WITH_AUB;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory.SICK_NOTE_CHILD;

/**
 * Build a sick days statistic for a certain year and person.
 */
public class SickDaysOverview {

    private final SickDays sickDays;
    private final SickDays childSickDays;

    SickDaysOverview(List<SickNote> sickNotes, WorkDaysCountService workDaysCountService, LocalDate from, LocalDate to) {

        this.sickDays = new SickDays();
        this.childSickDays = new SickDays();

        for (SickNote sickNote : sickNotes) {
            if (!sickNote.isActive()) {
                continue;
            }

            if (sickNote.getSickNoteType().isOfCategory(SICK_NOTE_CHILD)) {
                this.childSickDays.addDays(TOTAL, getTotalDays(sickNote, workDaysCountService, from, to));

                if (sickNote.isAubPresent()) {
                    this.childSickDays.addDays(WITH_AUB, getDaysWithAUB(sickNote, workDaysCountService, from, to));
                }
            } else {
                this.sickDays.addDays(TOTAL, getTotalDays(sickNote, workDaysCountService, from, to));

                if (sickNote.isAubPresent()) {
                    this.sickDays.addDays(WITH_AUB, getDaysWithAUB(sickNote, workDaysCountService, from, to));
                }
            }
        }
    }

    private BigDecimal getTotalDays(SickNote sickNote, WorkDaysCountService workDaysCountService, LocalDate from, LocalDate to) {

        final LocalDate start = maxDate(sickNote.getStartDate(), from);
        final LocalDate end = minDate(sickNote.getEndDate(), to);

        return workDaysCountService.getWorkDaysCount(sickNote.getDayLength(), start, end, sickNote.getPerson());
    }

    private BigDecimal getDaysWithAUB(SickNote sickNote, WorkDaysCountService workDaysCountService, LocalDate from, LocalDate to) {

        final LocalDate start = maxDate(sickNote.getAubStartDate(), from);
        final LocalDate end = minDate(sickNote.getAubEndDate(), to);

        return workDaysCountService.getWorkDaysCount(sickNote.getDayLength(), start, end, sickNote.getPerson());
    }

    public SickDays getSickDays() {
        return sickDays;
    }

    public SickDays getChildSickDays() {
        return childSickDays;
    }

    private static LocalDate maxDate(LocalDate date, LocalDate date2) {
        return date.isAfter(date2) ? date : date2;
    }

    private static LocalDate minDate(LocalDate date, LocalDate date2) {
        return date.isBefore(date2) ? date : date2;
    }
}
