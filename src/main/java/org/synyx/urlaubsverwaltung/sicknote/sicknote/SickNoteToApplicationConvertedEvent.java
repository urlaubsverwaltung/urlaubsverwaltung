package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Instant;
import java.util.UUID;

public class SickNoteToApplicationConvertedEvent {

    private final UUID id;
    private final Instant createdAt;
    private final SickNote sickNote;
    private final Application application;

    public SickNoteToApplicationConvertedEvent(UUID id, Instant createdAt, SickNote sickNote, Application application) {
        this.id = id;
        this.createdAt = createdAt;
        this.sickNote = sickNote;
        this.application = application;
    }

    public static SickNoteToApplicationConvertedEvent of(SickNote sickNote, Application application) {
        return new SickNoteToApplicationConvertedEvent(UUID.randomUUID(), Instant.now(), sickNote, application);
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

    public Application getApplication() {
        return application;
    }
}
