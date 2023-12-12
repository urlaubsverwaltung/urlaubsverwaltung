package org.synyx.urlaubsverwaltung.application.application;

import java.time.Instant;
import java.util.UUID;

public record ApplicationDeletedEvent(UUID id, Instant createdAt, Application application) {

    public static ApplicationDeletedEvent of(Application application) {
        return new ApplicationDeletedEvent(UUID.randomUUID(), Instant.now(), application);
    }
}
