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
     * This sick note extension has to be {@linkplain SickNoteExtensionInteractionService#acceptSubmittedExtension(Person, Long) accepted}
     * by a privileged person afterward.
     *
     * @param submitter {@linkplain Person} who submits the sick note extension
     * @param sickNoteId id of a {@linkplain SickNote} that should be extended
     * @param newEndDate new end date of the {@linkplain SickNote}
     * @param isAub whether AUB exists or not
     * @return the created {@linkplain SickNoteExtension} with status {@linkplain SickNoteExtensionStatus#SUBMITTED SUBMITTED}.
     * @throws AccessDeniedException when submitter is not allowed to extend the sick note
     * @throws IllegalStateException when sick note does not exist
     */
    SickNoteExtension submitSickNoteExtension(Person submitter, Long sickNoteId, LocalDate newEndDate, boolean isAub);

    /**
     * A maintainer (OFFICE or SICK_NOTE_VIEW_ADD_EDIT) accepts the submitted {@linkplain SickNoteExtension extension} of a {@linkplain SickNote sick note}.
     *
     * @param maintainer with {@linkplain org.synyx.urlaubsverwaltung.person.Role role} OFFICE or SICK_NOTE_VIEW_ADD_EDIT
     * @param sickNoteId id of the {@linkplain SickNote} to extend
     * @return the saved sick note in ACTIVE state
     * @throws AccessDeniedException when maintainer is not authorized to accept the submitted extension
     * @throws IllegalStateException when neither {@linkplain SickNoteExtension} nor {@linkplain SickNote} exists
     */
    SickNote acceptSubmittedExtension(Person maintainer, Long sickNoteId);
}
