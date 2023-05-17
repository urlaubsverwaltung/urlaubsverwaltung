package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import java.time.Instant;
import java.util.UUID;

public class SickNoteCancelledEvent {

    private final UUID id;
    private final Instant createdAt;
    private final SickNote sickNote;

    public SickNoteCancelledEvent(UUID id, Instant createdAt, SickNote sickNote) {
        this.id = id;
        this.createdAt = createdAt;
        this.sickNote = sickNote;
    }

    public static SickNoteCancelledEvent of(SickNote sickNote) {
        return new SickNoteCancelledEvent(UUID.randomUUID(), Instant.now(), sickNote);
    }

    public UUID getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public SickNote getSickNote() {
        return sickNote;
    }
}
