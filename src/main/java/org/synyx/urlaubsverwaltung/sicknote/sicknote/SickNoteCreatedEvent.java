package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import java.time.Instant;
import java.util.UUID;

public record SickNoteCreatedEvent(UUID id, Instant createdAt, SickNote sickNote) {

    public static SickNoteCreatedEvent of(SickNote sickNote) {
        return new SickNoteCreatedEvent(UUID.randomUUID(), Instant.now(), sickNote);
    }
}
