package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import java.time.Instant;
import java.util.UUID;

public class SickNoteDeletedEvent {

    private final UUID id;
    private final Instant createdAt;
    private final SickNote sickNote;

    public SickNoteDeletedEvent(UUID id, Instant createdAt, SickNote sickNote) {
        this.id = id;
        this.createdAt = createdAt;
        this.sickNote = sickNote;
    }

    public static SickNoteDeletedEvent of(SickNote sickNote) {
        return new SickNoteDeletedEvent(UUID.randomUUID(), Instant.now(), sickNote);
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
