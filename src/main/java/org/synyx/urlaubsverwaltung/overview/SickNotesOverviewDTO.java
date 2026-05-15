package org.synyx.urlaubsverwaltung.overview;

import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;

import java.util.List;

final class SickNotesOverviewDTO {

    private final List<SickNote> sickNotes;
    private final SickDaysSummaryDto sickDaysSummary;
    private final boolean canAddSickNoteAnotherUser;
    private final boolean canViewSickNoteOfMyselfAndAnotherUser;
    private final int numberOfShownSickNotes;
    private final int numberOfTotalSickNotes;

    SickNotesOverviewDTO(
        List<SickNote> sickNotes,
        SickDaysSummaryDto sickDaysSummary,
        boolean canAddSickNoteAnotherUser,
        boolean canViewSickNoteOfMyselfAndAnotherUser,
        int numberOfShownSickNotes,
        int numberOfTotalSickNotes
    ) {
        this.sickNotes = sickNotes;
        this.sickDaysSummary = sickDaysSummary;
        this.canAddSickNoteAnotherUser = canAddSickNoteAnotherUser;
        this.canViewSickNoteOfMyselfAndAnotherUser = canViewSickNoteOfMyselfAndAnotherUser;
        this.numberOfShownSickNotes = numberOfShownSickNotes;
        this.numberOfTotalSickNotes = numberOfTotalSickNotes;
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

    public int getNumberOfShownSickNotes() {
        return numberOfShownSickNotes;
    }

    public int getNumberOfTotalSickNotes() {
        return numberOfTotalSickNotes;
    }
}
