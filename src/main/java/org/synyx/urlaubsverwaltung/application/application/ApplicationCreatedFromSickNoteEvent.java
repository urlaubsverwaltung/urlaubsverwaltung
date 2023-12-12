package org.synyx.urlaubsverwaltung.application.application;

import java.time.Instant;
import java.util.UUID;

public record ApplicationCreatedFromSickNoteEvent(UUID id, Instant createdAt, Application application) {

    public static ApplicationCreatedFromSickNoteEvent of(Application application) {
        return new ApplicationCreatedFromSickNoteEvent(UUID.randomUUID(), Instant.now(), application);
    }
}
