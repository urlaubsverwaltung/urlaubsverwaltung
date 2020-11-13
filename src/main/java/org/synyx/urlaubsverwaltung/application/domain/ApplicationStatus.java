package org.synyx.urlaubsverwaltung.application.domain;

/**
 * Enum describing which states an {@link Application} may have.
 */
public enum ApplicationStatus {

    /**
     * After applying for the leave, the saved application for leave gets this status.
     */
    WAITING,

    /**
     * After the HeadOf has allowed the application in a two stage approval process.
     *
     * @since 2.15.0
     */
    TEMPORARY_ALLOWED,

    /**
     * Status after a boss has allowed the application for leave or after HeadOf has allowed the application in a one
     * stage approval process or after a SECOND_STAGE_AUTHORITY (Role) has released a TEMPORARY_ALLOWED application.
     */
    ALLOWED,

    /**
     * Status after a boss has rejected application for leave.
     */
    REJECTED,

    /**
     * If an application for leave has been allowed and is cancelled afterwards, it gets this status.
     */
    CANCELLED,

    /**
     * If an application for leave has **not** been allowed yet and is cancelled, it gets this status.
     *
     * @since 2.5.2
     */
    REVOKED
}
