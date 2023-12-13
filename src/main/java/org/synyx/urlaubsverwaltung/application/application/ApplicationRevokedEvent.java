package org.synyx.urlaubsverwaltung.application.application;

import java.time.Instant;
import java.util.UUID;

public record ApplicationRevokedEvent(UUID id, Instant createdAt, Application application) {

    public static ApplicationRevokedEvent of(Application application) {
        return new ApplicationRevokedEvent(UUID.randomUUID(), Instant.now(), application);
    }
}
