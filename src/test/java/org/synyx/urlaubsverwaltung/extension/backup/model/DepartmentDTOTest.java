package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.department.DepartmentEntity;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class DepartmentDTOTest {

    @Test
    void happyPathDTOToDepartmentEntity() {

        final LocalDate createdAt = LocalDate.now();
        final LocalDate lastModification = LocalDate.now();

        final DepartmentDTO dto = new DepartmentDTO(1L, "Research", "Research Department", createdAt, lastModification, true);
        final DepartmentEntity entity = dto.toDepartmentEntity();

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isNull();
        assertThat(entity.getName()).isEqualTo(dto.name());
        assertThat(entity.getDescription()).isEqualTo(dto.description());
        assertThat(entity.getCreatedAt()).isSameAs(createdAt);
        assertThat(entity.getLastModification()).isSameAs(lastModification);
        assertThat(entity.isTwoStageApproval()).isEqualTo(dto.twoStageApproval());
    }
}
