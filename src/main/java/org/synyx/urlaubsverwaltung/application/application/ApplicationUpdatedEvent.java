package org.synyx.urlaubsverwaltung.application.application;

import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Instant;
import java.util.UUID;

public record ApplicationUpdatedEvent(UUID id, Instant createdAt, Application application, Person editor) {

    public static ApplicationUpdatedEvent of(Application application, Person editor) {
        return new ApplicationUpdatedEvent(UUID.randomUUID(), Instant.now(), application, editor);
    }
}
