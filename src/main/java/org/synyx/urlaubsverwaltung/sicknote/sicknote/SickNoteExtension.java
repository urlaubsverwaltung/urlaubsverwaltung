package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import java.time.LocalDate;

/**
 * Extension of a {@linkplain SickNote}
 *
 * <p>
 * e.g. person has a {@linkplain SickNote} that ends yesterday and the person wants to extend this {@linkplain SickNote}
 * about 1 day because she is still ill today.
 *
 * <p>
 * A privileged person can accept this extension afterward, which then creates a new SickNote.
 * (TODO or edits it?)
 *
 * @param id id of the this {@linkplain SickNoteExtension}
 * @param sickNoteId id of the {@linkplain SickNote} to extend
 * @param nextEndDate
 * @param isAub
 * @param status
 */
record SickNoteExtension(Long id, Long sickNoteId, LocalDate nextEndDate, boolean isAub, SickNoteExtensionStatus status) {
}
