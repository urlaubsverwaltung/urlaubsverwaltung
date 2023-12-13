package org.synyx.urlaubsverwaltung.application.application;

import java.time.Instant;
import java.util.UUID;

public record ApplicationAppliedEvent(UUID id, Instant createdAt, Application application) {

    public static ApplicationAppliedEvent of(Application application) {
        return new ApplicationAppliedEvent(UUID.randomUUID(), Instant.now(), application);
    }
}
