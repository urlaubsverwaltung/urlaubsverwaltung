package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.department.DepartmentEntity;
import org.synyx.urlaubsverwaltung.department.DepartmentMemberEmbeddable;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DepartmentDTOTest {

    @Test
    void happyPathDTOToDepartmentEntity() {
        DepartmentDTO dto = new DepartmentDTO(1L, "Research", "Research Department", LocalDate.now(), LocalDate.now(), true, List.of("head1"), List.of("authority1"), List.of("member1"));
        List<Person> departmentHeads = List.of(new Person());
        List<Person> secondStageAuthorities = List.of(new Person());
        List<Person> members = List.of(new Person());

        DepartmentEntity entity = dto.toDepartmentEntity(departmentHeads, secondStageAuthorities, members);

        assertThat(entity).isNotNull();
        assertThat(entity.getName()).isEqualTo(dto.name());
        assertThat(entity.getDescription()).isEqualTo(dto.description());
        assertThat(entity.getCreatedAt()).isEqualTo(dto.createdAt());
        assertThat(entity.getLastModification()).isEqualTo(dto.lastModification());
        assertThat(entity.isTwoStageApproval()).isEqualTo(dto.twoStageApproval());
        assertThat(entity.getDepartmentHeads()).isEqualTo(departmentHeads);
        assertThat(entity.getSecondStageAuthorities()).isEqualTo(secondStageAuthorities);
        assertThat(entity.getMembers()).hasSize(1);
    }

    @Test
    void happyPathPersonToDepartmentMemberEmbeddables() {
        Person person = new Person();
        List<DepartmentMemberEmbeddable> departmentMemberEmbeddables = DepartmentDTO.toDepartmentMembers(List.of(person));

        assertThat(departmentMemberEmbeddables).hasSize(1);
        assertThat(departmentMemberEmbeddables.getFirst().getPerson()).isEqualTo(person);
        assertThat(departmentMemberEmbeddables.getFirst().getAccessionDate()).isBeforeOrEqualTo(Instant.now());
    }

}
