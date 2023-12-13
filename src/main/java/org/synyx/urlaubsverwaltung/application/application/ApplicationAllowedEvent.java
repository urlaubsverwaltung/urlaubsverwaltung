package org.synyx.urlaubsverwaltung.application.application;

import java.time.Instant;
import java.util.UUID;

public record ApplicationAllowedEvent(UUID id, Instant createdAt, Application application) {

    public static ApplicationAllowedEvent of(Application application) {
        return new ApplicationAllowedEvent(UUID.randomUUID(), Instant.now(), application);
    }
}
