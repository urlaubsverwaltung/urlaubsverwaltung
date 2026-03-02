package org.synyx.urlaubsverwaltung.department;

import java.time.Instant;
import java.util.UUID;

public record DepartmentDeletedEvent(UUID id, Instant createdAt, Long departmentId) {

    public static DepartmentDeletedEvent of(Long departmentId) {
        return new DepartmentDeletedEvent(UUID.randomUUID(), Instant.now(), departmentId);
    }
}
