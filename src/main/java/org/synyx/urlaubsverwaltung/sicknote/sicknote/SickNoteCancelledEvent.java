package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import java.time.Instant;
import java.util.UUID;

public record SickNoteCancelledEvent(UUID id, Instant createdAt, SickNote sickNote) {

    public static SickNoteCancelledEvent of(SickNote sickNote) {
        return new SickNoteCancelledEvent(UUID.randomUUID(), Instant.now(), sickNote);
    }
}
