package org.synyx.urlaubsverwaltung.application.application;

import java.time.Instant;
import java.util.UUID;

public record ApplicationRejectedEvent(UUID id, Instant createdAt, Application application) {

    public static ApplicationRejectedEvent of(Application application) {
        return new ApplicationRejectedEvent(UUID.randomUUID(), Instant.now(), application);
    }
}
