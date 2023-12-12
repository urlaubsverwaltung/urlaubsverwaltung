package org.synyx.urlaubsverwaltung.application.application;

import java.time.Instant;
import java.util.UUID;

public record ApplicationUpdatedEvent(UUID id, Instant createdAt, Application application) {

    public static ApplicationUpdatedEvent of(Application application) {
        return new ApplicationUpdatedEvent(UUID.randomUUID(), Instant.now(), application);
    }
}
