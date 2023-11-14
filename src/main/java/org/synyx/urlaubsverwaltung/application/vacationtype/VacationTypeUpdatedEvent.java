package org.synyx.urlaubsverwaltung.application.vacationtype;

import java.util.UUID;

public record VacationTypeUpdatedEvent(UUID id, VacationType<?> updatedVacationType) {

    public VacationTypeUpdatedEvent(VacationType<?> updatedVacationType) {
        this(UUID.randomUUID(), updatedVacationType);
    }
}
