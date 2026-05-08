package org.synyx.urlaubsverwaltung.overview;

import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;

import java.util.List;

final class SickNotesOverviewDTO {

    private final List<SickNote> sickNotes;
    private final SickDaysSummaryDto sickDaysSummary;
    private final boolean canAddSickNoteAnotherUser;
    private final boolean canViewSickNoteOfMyselfAndAnotherUser;

    SickNotesOverviewDTO(List<SickNote> sickNotes, SickDaysSummaryDto sickDaysSummary, boolean canAddSickNoteAnotherUser, boolean canViewSickNoteOfMyselfAndAnotherUser) {
        this.sickNotes = sickNotes;
        this.sickDaysSummary = sickDaysSummary;
        this.canAddSickNoteAnotherUser = canAddSickNoteAnotherUser;
        this.canViewSickNoteOfMyselfAndAnotherUser = canViewSickNoteOfMyselfAndAnotherUser;
    }

    public SickDaysSummaryDto getSickDaysSummary() {
        return sickDaysSummary;
    }

    public List<SickNote> getSickNotes() {
        return sickNotes;
    }

    public boolean isCanAddSickNoteAnotherUser() {
        return canAddSickNoteAnotherUser;
    }

    public boolean isCanViewSickNoteOfMyselfAndAnotherUser() {
        return canViewSickNoteOfMyselfAndAnotherUser;
    }
}
