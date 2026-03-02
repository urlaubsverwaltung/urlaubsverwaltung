package org.synyx.urlaubsverwaltung.calendar;

import java.time.Instant;
import java.util.UUID;

public record CompanyCalendarEnabledEvent(UUID id, Instant createdAt) {

    public static CompanyCalendarEnabledEvent of() {
        return new CompanyCalendarEnabledEvent(UUID.randomUUID(), Instant.now());
    }
}
