package org.synyx.urlaubsverwaltung.department;

import java.time.Instant;
import java.util.UUID;

public record DepartmentCreatedEvent(
    UUID id, Instant createdAt, Long departmentId, String departmentName,
    int memberCount
) {

    public static DepartmentCreatedEvent of(Department department) {
        return new DepartmentCreatedEvent(
            UUID.randomUUID(), Instant.now(),
            department.getId(), department.getName(),
            department.getMembers().size()
        );
    }
}
