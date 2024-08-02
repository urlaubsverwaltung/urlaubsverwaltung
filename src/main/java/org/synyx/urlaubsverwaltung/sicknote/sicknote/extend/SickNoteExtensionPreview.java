package org.synyx.urlaubsverwaltung.sicknote.sicknote.extend;

import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Preview of a {@linkplain SickNote} when a {@linkplain SickNoteExtension} would be accepted.
 *
 * @param id id of {@linkplain SickNoteExtension}
 * @param startDate start date of the extended sickNote
 * @param endDate end date of th extended sickNote
 * @param isAub whether AUB exists or not
 * @param workingDays amount of working days
 */
public record SickNoteExtensionPreview(Long id, LocalDate startDate, LocalDate endDate, boolean isAub, BigDecimal workingDays) {
}
