package org.synyx.urlaubsverwaltung.workingtime;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record WorkingTimeConfiguredEvent(
    UUID id, Instant createdAt, String username,
    LocalDate validFrom, List<Integer> workingDays, String federalState
) {

    public static WorkingTimeConfiguredEvent of(String username, LocalDate validFrom, List<Integer> workingDays, String federalState) {
        return new WorkingTimeConfiguredEvent(UUID.randomUUID(), Instant.now(), username, validFrom, List.copyOf(workingDays), federalState);
    }
}
