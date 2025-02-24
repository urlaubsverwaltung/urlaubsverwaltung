package org.synyx.urlaubsverwaltung.sicknote.sicknote.extend;

import org.springframework.security.access.AccessDeniedException;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;

import java.time.LocalDate;

public interface SickNoteExtensionInteractionService {

    /**
     * Submits a {@linkplain SickNoteExtension} that have to be accepted by a privileged person.
     *
     * <p>
     * This method also handles cases when a {@linkplain SickNote} can directly be edited instead of submitting a
     * {@linkplain SickNoteExtension}.
     *
     * <p>
     * This sick note extension has to be {@linkplain SickNoteExtensionInteractionService.acceptSubmittedExtension(Person, Long, String) accepted}
     * by a privileged person afterward.
     *
     * @param submitter {@linkplain Person} who submits the sick note extension
     * @param sickNoteId id of a {@linkplain SickNote} that should be extended
     * @param newEndDate new end date of the {@linkplain SickNote}
     * @throws AccessDeniedException when submitter is not allowed to extend the sick note
     * @throws IllegalStateException when sick note does not exist
     */
    void submitSickNoteExtension(Person submitter, Long sickNoteId, LocalDate newEndDate);

    /**
     * A maintainer (OFFICE or SICK_NOTE_VIEW_ADD_EDIT) accepts the submitted {@linkplain SickNoteExtension extension} of a {@linkplain SickNote sick note}.
     *
     * @param maintainer with {@linkplain org.synyx.urlaubsverwaltung.person.Role role} OFFICE or SICK_NOTE_VIEW_ADD_EDIT
     * @param sickNoteId id of the {@linkplain SickNote} to extend
     * @param comment optional comment, can be {@code null}
     * @return the saved sick note in ACTIVE state
     * @throws AccessDeniedException when maintainer is not authorized to accept the submitted extension
     * @throws IllegalStateException when neither {@linkplain SickNoteExtension} nor {@linkplain SickNote} exists
     */
    SickNote acceptSubmittedExtension(Person maintainer, Long sickNoteId, String comment);
}
