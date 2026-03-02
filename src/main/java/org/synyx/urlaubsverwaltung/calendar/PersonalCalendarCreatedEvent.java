package org.synyx.urlaubsverwaltung.calendar;

import java.time.Instant;
import java.util.UUID;

public record PersonalCalendarCreatedEvent(UUID id, Instant createdAt, String username) {

    public static PersonalCalendarCreatedEvent of(String username) {
        return new PersonalCalendarCreatedEvent(UUID.randomUUID(), Instant.now(), username);
    }
}
