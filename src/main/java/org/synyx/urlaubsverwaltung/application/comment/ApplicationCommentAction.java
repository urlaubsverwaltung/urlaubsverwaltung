package org.synyx.urlaubsverwaltung.application.comment;

/**
 * Executed action on an application for leave.
 *
 * @since 2.11.0
 */
public enum ApplicationCommentAction {

    /*
     * Some notes:
     *
     * - maximum length for literals is 20 characters (restricted by the underlying db column).
     * - each added action will need an entry in messages_*.properties for application.progress.ACTION_NAME. Otherwise
     *   app_progress will fail to render.
     */
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
    EDITED
}
