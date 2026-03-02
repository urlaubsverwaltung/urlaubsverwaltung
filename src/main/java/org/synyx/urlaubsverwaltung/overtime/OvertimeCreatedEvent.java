package org.synyx.urlaubsverwaltung.overtime;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record OvertimeCreatedEvent(
    UUID id, Instant createdAt, Long overtimeId, String username,
    LocalDate startDate, LocalDate endDate, Duration duration
) {

    public static OvertimeCreatedEvent of(Long overtimeId, String username, LocalDate startDate, LocalDate endDate, Duration duration) {
        return new OvertimeCreatedEvent(UUID.randomUUID(), Instant.now(), overtimeId, username, startDate, endDate, duration);
    }
}
