package org.synyx.urlaubsverwaltung.application.application;

import java.time.Instant;
import java.util.UUID;

public record ApplicationDeclinedCancellationRequestEvent(UUID id, Instant createdAt, Application application) {

    public static ApplicationDeclinedCancellationRequestEvent of(Application application) {
        return new ApplicationDeclinedCancellationRequestEvent(UUID.randomUUID(), Instant.now(), application);
    }
}
