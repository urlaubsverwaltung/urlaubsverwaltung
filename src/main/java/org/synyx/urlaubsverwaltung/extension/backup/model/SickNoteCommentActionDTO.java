package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.synyx.urlaubsverwaltung.sicknote.comment.SickNoteCommentAction;

public enum SickNoteCommentActionDTO {
    SUBMITTED,
    ACCEPTED,
    CREATED,
    EDITED,
    CONVERTED_TO_VACATION,
    CANCELLED,
    COMMENTED,
    EXTENSION_ACCEPTED;

    public SickNoteCommentAction toSickNoteCommentAction() {
        return SickNoteCommentAction.valueOf(this.name());
    }
}
