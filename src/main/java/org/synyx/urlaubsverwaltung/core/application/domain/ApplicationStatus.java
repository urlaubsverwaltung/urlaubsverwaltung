package org.synyx.urlaubsverwaltung.core.application.domain;

/**
 * Enum describing which states an {@link Application} may have.
 *
 * @author  Johannes Reuter
 * @author  Aljona Murygina
 */
public enum ApplicationStatus {

    /**
     * After applying for the leave, the saved application for leave gets this status.
     */
    WAITING,

    /**
     * Status after a boss has allowed the application for leave.
     */
    ALLOWED,

    /**
     * Status after a boss has rejected the application for leave.
     */
    REJECTED,

    /**
     * If an application for leave has been allowed and is cancelled afterwards, it gets this status.
     */
    CANCELLED,

    /**
     * If an application for leave has not been allowed yer and is cancelled, it gets this status.
     *
     * @since  2.5.2
     */
    REVOKED;
}
