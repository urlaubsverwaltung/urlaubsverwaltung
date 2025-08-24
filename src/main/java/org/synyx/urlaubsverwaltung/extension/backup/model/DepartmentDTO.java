package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.synyx.urlaubsverwaltung.department.DepartmentEntity;

import java.time.LocalDate;

public record DepartmentDTO(
    Long id,
    String name,
    String description,
    LocalDate createdAt,
    LocalDate lastModification,
    boolean twoStageApproval
) {

    public DepartmentEntity toDepartmentEntity() {
        final DepartmentEntity entity = new DepartmentEntity();
        entity.setName(this.name());
        entity.setDescription(this.description());
        entity.setCreatedAt(this.createdAt());
        entity.setLastModification(this.lastModification());
        entity.setTwoStageApproval(this.twoStageApproval());

        return entity;
    }
}
