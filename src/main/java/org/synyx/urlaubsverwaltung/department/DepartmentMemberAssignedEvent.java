package org.synyx.urlaubsverwaltung.department;

import java.time.Instant;
import java.util.UUID;

public record DepartmentMemberAssignedEvent(UUID id, Instant createdAt, Long departmentId, String username) {

    public static DepartmentMemberAssignedEvent of(Long departmentId, String username) {
        return new DepartmentMemberAssignedEvent(UUID.randomUUID(), Instant.now(), departmentId, username);
    }
}
