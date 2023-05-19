package org.synyx.urlaubsverwaltung.application.application;

import java.time.Instant;
import java.util.UUID;

public class ApplicationDeclinedCancellationRequestEvent {

    private final UUID id;
    private final Instant createdAt;
    private final Application application;

    public ApplicationDeclinedCancellationRequestEvent(UUID id, Instant createdAt, Application application) {
        this.id = id;
        this.createdAt = createdAt;
        this.application = application;
    }

    public static ApplicationDeclinedCancellationRequestEvent of(Application application) {
        return new ApplicationDeclinedCancellationRequestEvent(UUID.randomUUID(), Instant.now(), application);
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
