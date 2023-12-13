package org.synyx.urlaubsverwaltung.application.application;

import java.time.Instant;
import java.util.UUID;

public record ApplicationCancelledEvent(UUID id, Instant createdAt, Application application) {

    public static ApplicationCancelledEvent of(Application application) {
        return new ApplicationCancelledEvent(UUID.randomUUID(), Instant.now(), application);
    }
}
