package org.synyx.urlaubsverwaltung.extension.backup.model;

import jakarta.annotation.Nullable;
import org.synyx.urlaubsverwaltung.department.DepartmentMembershipEntity;
import org.synyx.urlaubsverwaltung.department.DepartmentMembershipKind;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Instant;

public record DepartmentMembershipDTO(
    String personExternalId,
    Long departmentId,
    DepartmentMembershipKind membershipKind,
    Instant validFrom,
    @Nullable Instant validTo
) {

    public DepartmentMembershipEntity toEntity(Person owner) {
        DepartmentMembershipEntity entity = new DepartmentMembershipEntity();
        entity.setPersonId(owner.getId());
        entity.setDepartmentId(this.departmentId());
        entity.setMembershipKind(this.membershipKind());
        entity.setValidFrom(this.validFrom());
        entity.setValidTo(this.validTo());
        return entity;
    }
}
