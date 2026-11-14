package org.synyx.urlaubsverwaltung.sicknote.me;

import org.synyx.urlaubsverwaltung.sicknote.sickdays.SickDays;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;

import java.time.LocalDate;
import java.util.List;

import static org.synyx.urlaubsverwaltung.sicknote.sickdays.SickDays.SickDayType.TOTAL;
import static org.synyx.urlaubsverwaltung.sicknote.sickdays.SickDays.SickDayType.WITH_AUB;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory.SICK_NOTE_CHILD;

/**
 * Build a sick days statistic for a certain year and person.
 */
public class YearlySickDaysSummary {

    private final SickDays sickDays;
    private final SickDays childSickDays;

    YearlySickDaysSummary(List<SickNote> sickNotes, LocalDate from, LocalDate to) {

        this.sickDays = new SickDays();
        this.childSickDays = new SickDays();

        for (SickNote sickNote : sickNotes) {
            if (!sickNote.isActive()) {
                continue;
            }

            if (sickNote.getSickNoteType().isOfCategory(SICK_NOTE_CHILD)) {
                this.childSickDays.addDays(TOTAL, sickNote.getWorkDays(from, to));
                this.childSickDays.addDays(WITH_AUB, sickNote.getWorkDaysWithAub(from, to));
            } else {
                this.sickDays.addDays(TOTAL, sickNote.getWorkDays(from, to));
                this.sickDays.addDays(WITH_AUB, sickNote.getWorkDaysWithAub(from, to));
            }
        }
    }

    public SickDays getSickDays() {
        return sickDays;
    }

    public SickDays getChildSickDays() {
        return childSickDays;
    }
}
