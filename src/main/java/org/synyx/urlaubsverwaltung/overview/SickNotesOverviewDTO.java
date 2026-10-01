package org.synyx.urlaubsverwaltung.overview;

import org.synyx.urlaubsverwaltung.web.TodayMarker;

import java.util.List;

record SickNotesOverviewDTO(
    List<SickNoteDto> sickNotes,
    SickDaysSummaryDto sickDaysSummary,
    boolean canAddSickNoteAnotherUser,
    boolean canViewSickNoteOfMyselfAndAnotherUser,
    int numberOfShownSickNotes,
    int numberOfTotalSickNotes,
    TodayMarker todayMarker
) {
}
