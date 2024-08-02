package org.synyx.urlaubsverwaltung.sicknote.sicknote.extend;

import org.springframework.security.access.AccessDeniedException;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteInteractionService;

import java.time.LocalDate;
import java.util.Optional;

public interface SickNoteExtensionService {

    /**
     * Find the newest {@linkplain SickNoteExtensionPreview} of the given {@linkplain SickNote} id.
     *
     * @param sickNoteId {@linkplain SickNote} id
     * @return Optional resolving to the newest {@linkplain SickNoteExtensionPreview}
     */
    Optional<SickNoteExtensionPreview> findExtensionPreviewOfSickNote(Long sickNoteId);

    /**
     * Submits a {@linkplain SickNoteExtension} that have to be accepted by a privileged person.
     *
     * <p>
     * This sick note extension has to be {@linkplain SickNoteInteractionService#acceptSubmittedExtension(Long, Person) accepted}
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
     * Updates the status of the last known submitted {@linkplain SickNoteExtension} to {@linkplain SickNoteExtensionStatus#ACCEPTED accepted}.
     *
     * <p>
     * Please note that you have to handle authorization of this action yourself.
     * Authorization is not considered here.
     *
     * @param sickNoteId if of the {@linkplain SickNote} to handle
     */
    void acceptSubmittedExtension(Long sickNoteId);
}
