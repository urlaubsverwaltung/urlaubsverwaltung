package org.synyx.urlaubsverwaltung.sicknote.sicknote.extend;

import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;

import java.time.LocalDate;
import java.util.Optional;

public interface SickNoteExtensionService {

    /**
     * Find the most recent {@link SickNoteExtension} with status {@linkplain SickNoteExtensionStatus#SUBMITTED}
     * of a {@link org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote}.
     *
     * @param sickNote referenced {@link org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote}
     * @return optional resolving to last created {@link SickNoteExtension} of sickNote, empty otherwise
     */
    Optional<SickNoteExtension> findSubmittedExtensionOfSickNote(SickNote sickNote);

    /**
     * Creates a new {@linkplain SickNoteExtension} linked to the given {@linkplain SickNote}.
     * If there is a {@linkplain SickNoteExtensionStatus#SUBMITTED submitted} SickNoteExtension already,
     * it will be updated to the next newEndDate.
     *
     * <p>
     * This method does not handle authorization. You have to check this upfront!
     * Whether the currently logged-in user is allowed to create an extension for the sickNote or not, for instance.
     *
     * @param sickNote the referenced {@linkplain SickNote}
     * @param newEndDate new endDate of the {@linkplain SickNote} when extension is accepted
     * @return the created {@linkplain SickNoteExtension} with status {@linkplain SickNoteExtensionStatus#SUBMITTED submitted}.
     */
    SickNoteExtension createSickNoteExtension(SickNote sickNote, LocalDate newEndDate);

    /**
     * Updates the referenced {@linkplain SickNote} to match the desired extension and set the status of the
     * {@linkplain SickNoteExtension} to {@linkplain SickNoteExtensionStatus#SUBMITTED submitted}.
     *
     * <p>
     * This method does not handle authorization. You have to check this upfront!
     * Whether the currently logged-in user is allowed to accept the extension for the sickNote or not, for instance.
     *
     * @param sickNoteId id of the {@linkplain SickNote} to update
     * @return the updated {@linkplain SickNote}
     * @throws IllegalStateException when sickNote or extension does not exist
     */
    SickNote acceptSubmittedExtension(Long sickNoteId);

    /**
     * Update the status of all referenced {@link SickNoteExtension} to match the new {@link SickNote#getStatus() status}.
     *
     * <p>
     * e.g. submitted extensions will be set to superseded if sickNote has been converted to vacation or has been cancelled.
     *
     * @param sickNote {@link SickNote} to update referenced extensions
     */
    void updateExtensionsForConvertedSickNote(SickNote sickNote);
}
