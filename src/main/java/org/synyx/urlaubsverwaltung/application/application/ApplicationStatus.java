package org.synyx.urlaubsverwaltung.application.application;

import java.util.List;

/**
 * Enum describing which states an {@link Application} may have.
 */
public enum ApplicationStatus {

    /**
     * After applying for the leave, the saved application for leave gets this status.
     */
    WAITING,

    /**
     * After the department head has allowed the application in a two stage approval process and before the
     * second stage authority has rejected or allowed the application.
     * This status is a special case of WAITING and needs to be handled as a waiting, not yet allowed, application for leave.
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
     * Status after the application for leave was allowed but the applicant wants to cancel the own application.
     * The application for leave is at this point not cancelled and can only be approved or the request of
     * cancellation can be declined.
     */
    ALLOWED_CANCELLATION_REQUESTED,

    /**
     * If an application for leave has **not** been allowed yet and is revoked by the applicant, it gets this status.
     */
    REVOKED,

    /**
     * If an application for leave has **not** been allowed yet and the management has rejected the application, it gets this status.
     */
    REJECTED,

    /**
     * If an application for leave has been allowed and is cancelled afterward, it gets this status.
     */
    CANCELLED;

    /**
     * Returns all active statuses of an application.
     * Hint: Applications with one of these statuses will be used for calculations and shown as active on the ui.
     *
     * @return a list of all active statuses
     */
    public static List<ApplicationStatus> activeStatuses() {
        return List.of(WAITING, TEMPORARY_ALLOWED, ALLOWED, ALLOWED_CANCELLATION_REQUESTED);
    }

    /**
     * Returns all inactive statuses of an application.
     * Hint: Applications with one of these statuses will not be used for calculations and shown as inactive on the ui.
     *
     * @return a list of all inactive statuses
     */
    public static List<ApplicationStatus> inactiveStatuses() {
        return List.of(REVOKED, REJECTED, CANCELLED);
    }
}
