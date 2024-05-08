package org.synyx.urlaubsverwaltung.application.vacationtype;

import java.time.Instant;
import java.util.UUID;

public record VacationTypeCreatedEvent(UUID id, Instant createdAt, VacationType<?> vacationType) {

    public static VacationTypeCreatedEvent of(VacationType<?> vacationType) {
        return new VacationTypeCreatedEvent(UUID.randomUUID(), Instant.now(), vacationType);
    }
}
