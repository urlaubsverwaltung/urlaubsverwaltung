package org.synyx.urlaubsverwaltung.overtime;

import java.time.Instant;
import java.util.UUID;

public record OvertimeSettingsActivatedEvent(UUID id, Instant createdAt) {

    public static OvertimeSettingsActivatedEvent of() {
        return new OvertimeSettingsActivatedEvent(UUID.randomUUID(), Instant.now());
    }
}
