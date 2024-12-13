package org.synyx.urlaubsverwaltung.sicknote.sicknote.extend;


import java.time.Instant;
import java.time.LocalDate;


public record SickNoteExtensionHistory(Instant createdAt, LocalDate newEndDate, boolean isAub,
                                       SickNoteExtensionStatus status) {
}
