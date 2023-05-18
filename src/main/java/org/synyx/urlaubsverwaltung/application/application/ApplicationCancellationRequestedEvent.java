package org.synyx.urlaubsverwaltung.application.application;

import java.time.Instant;
import java.util.UUID;

public class ApplicationCancellationRequestedEvent {

    private final UUID id;
    private final Instant createdAt;
    private final Application application;

    public ApplicationCancellationRequestedEvent(UUID id, Instant createdAt, Application application) {
        this.id = id;
        this.createdAt = createdAt;
        this.application = application;
    }

    public static ApplicationCancellationRequestedEvent of(Application application) {
        return new ApplicationCancellationRequestedEvent(UUID.randomUUID(), Instant.now(), application);
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
