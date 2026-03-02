package org.synyx.urlaubsverwaltung.calendar;

import java.time.Instant;
import java.util.UUID;

public record CompanyCalendarDisabledEvent(UUID id, Instant createdAt) {

    public static CompanyCalendarDisabledEvent of() {
        return new CompanyCalendarDisabledEvent(UUID.randomUUID(), Instant.now());
    }
}
