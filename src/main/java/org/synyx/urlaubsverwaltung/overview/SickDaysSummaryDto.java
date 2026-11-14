package org.synyx.urlaubsverwaltung.overview;

import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;

import java.time.LocalDate;
import java.util.List;

import static org.synyx.urlaubsverwaltung.overview.SickDaysDto.SickDayType.TOTAL;
import static org.synyx.urlaubsverwaltung.overview.SickDaysDto.SickDayType.WITH_AUB;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory.SICK_NOTE_CHILD;

/**
 * Build a sick days statistic for a certain year and person.
 */
final class SickDaysSummaryDto {

    private final SickDaysDto sickDays;
    private final SickDaysDto childSickDays;

    SickDaysSummaryDto(List<SickNote> sickNotes, LocalDate from, LocalDate to) {

        this.sickDays = new SickDaysDto();
        this.childSickDays = new SickDaysDto();

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

    public SickDaysDto getSickDays() {
        return sickDays;
    }

    public SickDaysDto getChildSickDays() {
        return childSickDays;
    }
}
