package org.synyx.urlaubsverwaltung.core.application.domain;

/**
 * Executed action on an application for leave.
 *
 * @author  Aljona Murygina
 * @since  2.11.0
 */
public enum ApplicationAction {

    APPLIED,
    CONVERTED,
    ALLOWED,
    REJECTED,
    CANCELLED,
    CANCELLATION_REQUESTED,
    REVOKED
}
