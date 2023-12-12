package org.synyx.urlaubsverwaltung.application.application;

import java.time.Instant;
import java.util.UUID;

public record ApplicationCancellationRequestedEvent(UUID id, Instant createdAt, Application application) {

    public static ApplicationCancellationRequestedEvent of(Application application) {
        return new ApplicationCancellationRequestedEvent(UUID.randomUUID(), Instant.now(), application);
    }
}
