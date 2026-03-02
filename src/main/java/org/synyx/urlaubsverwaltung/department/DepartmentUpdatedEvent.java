package org.synyx.urlaubsverwaltung.department;

import java.time.Instant;
import java.util.UUID;

public record DepartmentUpdatedEvent(
    UUID id, Instant createdAt, Long departmentId, String departmentName,
    int memberCount
) {

    public static DepartmentUpdatedEvent of(Department department) {
        return new DepartmentUpdatedEvent(
            UUID.randomUUID(), Instant.now(),
            department.getId(), department.getName(),
            department.getMembers().size()
        );
    }
}
