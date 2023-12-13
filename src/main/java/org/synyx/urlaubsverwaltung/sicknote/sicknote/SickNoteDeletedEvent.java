package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import java.time.Instant;
import java.util.UUID;

public record SickNoteDeletedEvent(UUID id, Instant createdAt, SickNote sickNote) {

    public static SickNoteDeletedEvent of(SickNote sickNote) {
        return new SickNoteDeletedEvent(UUID.randomUUID(), Instant.now(), sickNote);
    }
}
