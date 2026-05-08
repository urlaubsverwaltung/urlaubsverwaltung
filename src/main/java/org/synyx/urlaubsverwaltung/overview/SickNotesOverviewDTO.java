package org.synyx.urlaubsverwaltung.overview;

import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;

import java.util.List;

final class SickNotesOverviewDTO {

    private final List<SickNote> sickNotes;
    private final SickDaysSummaryDto sickDaysSummary;

    SickNotesOverviewDTO(List<SickNote> sickNotes, SickDaysSummaryDto sickDaysSummary) {
        this.sickNotes = sickNotes;
        this.sickDaysSummary = sickDaysSummary;
    }

    public SickDaysSummaryDto getSickDaysSummary() {
        return sickDaysSummary;
    }

    public List<SickNote> getSickNotes() {
        return sickNotes;
    }
}
