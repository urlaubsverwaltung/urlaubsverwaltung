package org.synyx.urlaubsverwaltung.application.vacationtype;

import java.time.Instant;
import java.util.UUID;

public record VacationTypeUpdatedEvent(UUID id, Instant createdAt, VacationType<?> updatedVacationType) {

    public static VacationTypeUpdatedEvent of(VacationType<?> updatedVacationType) {
        return new VacationTypeUpdatedEvent(UUID.randomUUID(), Instant.now(), updatedVacationType);
    }
}
