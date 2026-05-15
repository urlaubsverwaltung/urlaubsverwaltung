package org.synyx.urlaubsverwaltung.overview;

import java.util.List;

final class SickNotesOverviewDTO {

    private final List<SickNoteDto> sickNotes;
    private final SickDaysSummaryDto sickDaysSummary;
    private final boolean canAddSickNoteAnotherUser;
    private final boolean canViewSickNoteOfMyselfAndAnotherUser;
    private final int numberOfShownSickNotes;
    private final int numberOfTotalSickNotes;

    SickNotesOverviewDTO(
        List<SickNoteDto> sickNotes,
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

    public List<SickNoteDto> getSickNotes() {
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
