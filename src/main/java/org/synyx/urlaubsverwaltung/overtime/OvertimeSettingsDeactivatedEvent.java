package org.synyx.urlaubsverwaltung.overtime;

import java.time.Instant;
import java.util.UUID;

public record OvertimeSettingsDeactivatedEvent(UUID id, Instant createdAt) {

    public static OvertimeSettingsDeactivatedEvent of() {
        return new OvertimeSettingsDeactivatedEvent(UUID.randomUUID(), Instant.now());
    }
}
