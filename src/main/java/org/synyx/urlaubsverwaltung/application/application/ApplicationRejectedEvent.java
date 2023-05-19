package org.synyx.urlaubsverwaltung.application.application;

import java.time.Instant;
import java.util.UUID;

public class ApplicationRejectedEvent {

    private final UUID id;
    private final Instant createdAt;
    private final Application application;

    public ApplicationRejectedEvent(UUID id, Instant createdAt, Application application) {
        this.id = id;
        this.createdAt = createdAt;
        this.application = application;
    }

    public static ApplicationRejectedEvent of(Application application) {
        return new ApplicationRejectedEvent(UUID.randomUUID(), Instant.now(), application);
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
