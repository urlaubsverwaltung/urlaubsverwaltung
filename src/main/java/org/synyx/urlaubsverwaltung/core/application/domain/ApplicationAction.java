package org.synyx.urlaubsverwaltung.core.application.domain;

/**
 * Executed action on an application for leave.
 *
 * @author  Aljona Murygina
 * @since  2.11.0
 */
public enum ApplicationAction {

    /*
     * Some notes:
     *
     * - maximum length for literals is 20 characters (restricted by the underlying db column).
     * - each added action will need an entry in messages.properties for application.progress.ACTION_NAME. Otherwise
     *   app_progress.jsp will fail to render.
     */
    APPLIED,
    CONVERTED,
    ALLOWED,
    REJECTED,
    CANCELLED,
    CANCEL_REQUESTED,
    REVOKED
}
