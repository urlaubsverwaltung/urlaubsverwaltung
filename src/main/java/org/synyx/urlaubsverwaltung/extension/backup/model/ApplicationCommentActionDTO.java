package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.synyx.urlaubsverwaltung.application.comment.ApplicationCommentAction;

public enum ApplicationCommentActionDTO {
    APPLIED,
    CONVERTED,
    TEMPORARY_ALLOWED,
    ALLOWED,
    ALLOWED_DIRECTLY,
    REJECTED,
    CANCELLED,
    CANCELLED_DIRECTLY,
    CANCEL_REQUESTED,
    CANCEL_REQUESTED_DECLINED,
    REVOKED,
    REFERRED,
    EDITED;

    public ApplicationCommentAction toApplicationCommentAction() {
        return ApplicationCommentAction.valueOf(this.name());
    }
}
