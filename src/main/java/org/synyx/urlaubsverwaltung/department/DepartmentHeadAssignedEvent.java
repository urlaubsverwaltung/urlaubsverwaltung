package org.synyx.urlaubsverwaltung.department;

import java.time.Instant;
import java.util.UUID;

public record DepartmentHeadAssignedEvent(UUID id, Instant createdAt, Long departmentId, String departmentHeadUsername) {

    public static DepartmentHeadAssignedEvent of(Long departmentId, String username) {
        return new DepartmentHeadAssignedEvent(UUID.randomUUID(), Instant.now(), departmentId, username);
    }
}
