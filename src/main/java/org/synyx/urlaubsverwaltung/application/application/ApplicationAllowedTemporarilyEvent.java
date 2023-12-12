package org.synyx.urlaubsverwaltung.application.application;

import java.time.Instant;
import java.util.UUID;

public record ApplicationAllowedTemporarilyEvent(UUID id, Instant createdAt, Application application) {

    public static ApplicationAllowedTemporarilyEvent of(Application application) {
        return new ApplicationAllowedTemporarilyEvent(UUID.randomUUID(), Instant.now(), application);
    }
}
