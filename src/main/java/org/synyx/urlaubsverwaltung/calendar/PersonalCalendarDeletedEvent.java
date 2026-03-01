package org.synyx.urlaubsverwaltung.calendar;

import java.time.Instant;
import java.util.UUID;

public record PersonalCalendarDeletedEvent(UUID id, Instant createdAt, String username) {

    public static PersonalCalendarDeletedEvent of(String username) {
        return new PersonalCalendarDeletedEvent(UUID.randomUUID(), Instant.now(), username);
    }
}
