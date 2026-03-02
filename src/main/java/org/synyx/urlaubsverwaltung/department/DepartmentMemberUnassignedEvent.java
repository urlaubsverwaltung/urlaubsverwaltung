package org.synyx.urlaubsverwaltung.department;

import java.time.Instant;
import java.util.UUID;

public record DepartmentMemberUnassignedEvent(UUID id, Instant createdAt, Long departmentId, String username) {

    public static DepartmentMemberUnassignedEvent of(Long departmentId, String username) {
        return new DepartmentMemberUnassignedEvent(UUID.randomUUID(), Instant.now(), departmentId, username);
    }
}
