package org.synyx.urlaubsverwaltung.department;

import java.time.Instant;
import java.util.UUID;

public record DepartmentHeadUnassignedEvent(UUID id, Instant createdAt, Long departmentId, String departmentHeadUsername) {

    public static DepartmentHeadUnassignedEvent of(Long departmentId, String username) {
        return new DepartmentHeadUnassignedEvent(UUID.randomUUID(), Instant.now(), departmentId, username);
    }
}
