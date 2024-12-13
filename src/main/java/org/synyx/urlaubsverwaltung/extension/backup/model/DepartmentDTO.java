package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.synyx.urlaubsverwaltung.department.DepartmentEntity;
import org.synyx.urlaubsverwaltung.department.DepartmentMemberEmbeddable;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record DepartmentDTO(Long id, String name, String description, LocalDate createdAt, LocalDate lastModification,
                            boolean twoStageApproval, List<String> externalIdsOfDepartmentHeads,
                            List<String> externalIdsOfSecondStageAuthorities,
                            List<String> externalIdsOfMembers) {

    public static List<DepartmentMemberEmbeddable> toDepartmentMembers(List<Person> members) {
        final Instant accessionDate = Instant.now();
        return members.stream().map(person -> {
            final DepartmentMemberEmbeddable memberEmbeddable = new DepartmentMemberEmbeddable();
            memberEmbeddable.setPerson(person);
            memberEmbeddable.setAccessionDate(accessionDate);
            return memberEmbeddable;
        }).toList();
    }

    public DepartmentEntity toDepartmentEntity(List<Person> departmentHeads, List<Person> secondStageAuthorities, List<Person> members) {
        final DepartmentEntity departmentEntity = new DepartmentEntity();
        departmentEntity.setName(this.name());
        departmentEntity.setDescription(this.description());
        departmentEntity.setCreatedAt(this.createdAt());
        departmentEntity.setLastModification(this.lastModification());
        departmentEntity.setTwoStageApproval(this.twoStageApproval());
        departmentEntity.setDepartmentHeads(departmentHeads);
        departmentEntity.setSecondStageAuthorities(secondStageAuthorities);
        List<DepartmentMemberEmbeddable> departmentMemberEmbeddables = toDepartmentMembers(members);
        departmentEntity.setMembers(departmentMemberEmbeddables);
        return departmentEntity;
    }
}
