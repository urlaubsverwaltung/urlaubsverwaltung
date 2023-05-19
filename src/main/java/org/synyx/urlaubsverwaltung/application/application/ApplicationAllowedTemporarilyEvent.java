package org.synyx.urlaubsverwaltung.application.application;

import java.time.Instant;
import java.util.UUID;

public class ApplicationAllowedTemporarilyEvent {

    private final UUID id;
    private final Instant createdAt;
    private final Application application;

    public ApplicationAllowedTemporarilyEvent(UUID id, Instant createdAt, Application application) {
        this.id = id;
        this.createdAt = createdAt;
        this.application = application;
    }

    public static ApplicationAllowedTemporarilyEvent of(Application application) {
        return new ApplicationAllowedTemporarilyEvent(UUID.randomUUID(), Instant.now(), application);
    }

    public UUID getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Application getApplication() {
        return application;
    }
}
