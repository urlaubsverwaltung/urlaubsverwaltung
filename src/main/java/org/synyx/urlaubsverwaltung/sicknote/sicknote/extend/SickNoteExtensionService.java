package org.synyx.urlaubsverwaltung.sicknote.sicknote.extend;

import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;

import java.time.LocalDate;

interface SickNoteExtensionService {

    /**
     * Creates a new {@linkplain SickNoteExtension} linked to the given {@linkplain SickNote}.
     * If there is a {@linkplain SickNoteExtensionStatus#SUBMITTED submitted} SickNoteExtension already,
     * it will be set to status {@linkplain SickNoteExtensionStatus#SUPERSEDED SUPERSEDED}.
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
}
