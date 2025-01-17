package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.synyx.urlaubsverwaltung.application.application.ApplicationStatus;

public enum ApplicationStatusDTO {
    WAITING,
    TEMPORARY_ALLOWED,
    ALLOWED,
    ALLOWED_CANCELLATION_REQUESTED,
    REVOKED,
    REJECTED,
    CANCELLED;

    public ApplicationStatus toApplicationStatus() {
        return ApplicationStatus.valueOf(this.name());
    }
}
