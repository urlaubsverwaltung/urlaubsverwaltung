package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.synyx.urlaubsverwaltung.application.application.Application;

import java.time.Instant;
import java.util.UUID;

public record SickNoteToApplicationConvertedEvent(UUID id, Instant createdAt, SickNote sickNote, Application application) {

    public static SickNoteToApplicationConvertedEvent of(SickNote sickNote, Application application) {
        return new SickNoteToApplicationConvertedEvent(UUID.randomUUID(), Instant.now(), sickNote, application);
    }
}
