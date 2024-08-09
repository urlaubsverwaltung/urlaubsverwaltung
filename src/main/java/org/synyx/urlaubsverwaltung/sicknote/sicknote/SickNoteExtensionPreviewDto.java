package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import java.time.LocalDate;

// TODO half working day
record SickNoteExtensionPreviewDto(LocalDate startDate, LocalDate endDate, long workingDays) {
}
