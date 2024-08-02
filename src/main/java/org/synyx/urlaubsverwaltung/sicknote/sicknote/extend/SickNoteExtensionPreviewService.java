package org.synyx.urlaubsverwaltung.sicknote.sicknote.extend;

import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;

import java.util.Optional;

public interface SickNoteExtensionPreviewService {

    /**
     * Find the newest {@linkplain SickNoteExtensionPreview} of the given {@linkplain SickNote} id.
     *
     * @param sickNoteId {@linkplain SickNote} id
     * @return Optional resolving to the newest {@linkplain SickNoteExtensionPreview}
     */
    Optional<SickNoteExtensionPreview> findExtensionPreviewOfSickNote(Long sickNoteId);
}
