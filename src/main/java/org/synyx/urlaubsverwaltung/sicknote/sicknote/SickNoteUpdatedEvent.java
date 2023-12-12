package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import java.time.Instant;
import java.util.UUID;

public record SickNoteUpdatedEvent(UUID id, Instant createdAt, SickNote sickNote) {

    public static SickNoteUpdatedEvent of(SickNote sickNote) {
        return new SickNoteUpdatedEvent(UUID.randomUUID(), Instant.now(), sickNote);
    }
}
