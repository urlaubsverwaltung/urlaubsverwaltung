package org.synyx.urlaubsverwaltung.core.application.domain;

/**
 * Enum describing which states an {@link Application} may have.
 *
 * @author  Johannes Reuter
 * @author  Aljona Murygina
 */
public enum ApplicationStatus {

    WAITING,
    ALLOWED,
    REJECTED,
    CANCELLED;
}
