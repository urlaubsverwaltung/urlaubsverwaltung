package org.synyx.urlaubsverwaltung.sicknote.sicknote.extend;

import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;

import java.util.Optional;

public interface SubmittedSickNoteExtensionService {

    /**
     * Find the most recent {@link SickNoteExtension} with status {@linkplain SickNoteExtensionStatus#SUBMITTED}
     * of a {@link org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote}.
     *
     * @param sickNote referenced {@link org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote}
     * @return optional resolving to last created {@link SickNoteExtension} of sickNote, empty otherwise
     */
    Optional<SickNoteExtension> findSubmittedExtensionOfSickNote(SickNote sickNote);
}
