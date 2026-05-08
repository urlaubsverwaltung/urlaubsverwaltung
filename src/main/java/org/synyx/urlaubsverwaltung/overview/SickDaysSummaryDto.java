package org.synyx.urlaubsverwaltung.overview;

import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static java.util.Collections.max;
import static java.util.Collections.min;
import static java.util.List.of;
import static org.synyx.urlaubsverwaltung.overview.SickDaysDto.SickDayType.TOTAL;
import static org.synyx.urlaubsverwaltung.overview.SickDaysDto.SickDayType.WITH_AUB;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory.SICK_NOTE_CHILD;

/**
 * Build a sick days statistic for a certain year and person.
 */
public class SickDaysSummaryDto {

    private final SickDaysDto sickDays;
    private final SickDaysDto childSickDays;

    SickDaysSummaryDto(List<SickNote> sickNotes, WorkDaysCountService workDaysCountService, LocalDate from, LocalDate to) {

        this.sickDays = new SickDaysDto();
        this.childSickDays = new SickDaysDto();

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

        final LocalDate start = max(of(sickNote.getStartDate(), from));
        final LocalDate end = min(of(sickNote.getEndDate(), to));

        return workDaysCountService.getWorkDaysCount(sickNote.getDayLength(), start, end, sickNote.getPerson());
    }

    private BigDecimal getDaysWithAUB(SickNote sickNote, WorkDaysCountService workDaysCountService, LocalDate from, LocalDate to) {

        final LocalDate startAUB = max(of(sickNote.getAubStartDate(), from));
        final LocalDate endAUB = min(of(sickNote.getAubEndDate(), to));

        // requested period from user is e.g. 1.1.2026 to 31.12.2026
        // start if sick note is in 2025 and end in 2026
        // AUB start and AUB end is both in 2025 and is not part of the user given period
        final boolean aubBeforeRequestedInterval = endAUB.isBefore(startAUB);
        if (aubBeforeRequestedInterval) {
            return BigDecimal.ZERO;
        }

        return workDaysCountService.getWorkDaysCount(sickNote.getDayLength(), startAUB, endAUB, sickNote.getPerson());
    }

    public SickDaysDto getSickDays() {
        return sickDays;
    }

    public SickDaysDto getChildSickDays() {
        return childSickDays;
    }
}
