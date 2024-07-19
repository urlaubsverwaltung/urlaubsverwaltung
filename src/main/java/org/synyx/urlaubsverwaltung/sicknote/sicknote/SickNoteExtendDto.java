package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import java.time.LocalDate;

record SickNoteExtendDto(Long id, LocalDate startDate, LocalDate endDate, long workingDays, boolean isAub) {
}
