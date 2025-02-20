package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import java.time.Instant;
import java.util.UUID;

public record SickNoteAcceptedEvent(UUID id, Instant createdAt, SickNote sickNote) {

    public static SickNoteAcceptedEvent of(SickNote sickNote) {
        return new SickNoteAcceptedEvent(UUID.randomUUID(), Instant.now(), sickNote);
    }
}
