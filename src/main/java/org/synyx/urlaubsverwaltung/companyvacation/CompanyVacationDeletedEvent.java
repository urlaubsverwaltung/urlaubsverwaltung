package org.synyx.urlaubsverwaltung.companyvacation;

import java.time.Instant;
import java.util.UUID;

public record CompanyVacationDeletedEvent(String sourceId, UUID id, Instant createdAt) {
}
