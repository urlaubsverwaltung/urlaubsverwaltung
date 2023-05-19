package org.synyx.urlaubsverwaltung.application.application;

import java.time.Instant;
import java.util.UUID;

public class ApplicationUpdatedEvent {

    private final UUID id;
    private final Instant createdAt;
    private Application application;

    public ApplicationUpdatedEvent(UUID id, Instant createdAt, Application application) {
        this.id = id;
        this.createdAt = createdAt;
        this.application = application;
    }

    public static ApplicationUpdatedEvent of(Application application) {
        return new ApplicationUpdatedEvent(UUID.randomUUID(), Instant.now(), application);
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
