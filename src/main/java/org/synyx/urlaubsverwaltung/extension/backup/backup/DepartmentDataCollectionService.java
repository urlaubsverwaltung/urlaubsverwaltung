package org.synyx.urlaubsverwaltung.extension.backup.backup;

import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.extension.backup.model.DepartmentDTO;
import org.synyx.urlaubsverwaltung.person.Person;

import java.util.List;

@Component
@ConditionalOnBackupCreateEnabled
class DepartmentDataCollectionService {

    private final DepartmentService departmentService;

    DepartmentDataCollectionService(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    List<DepartmentDTO> collectDepartments() {
        return departmentService.getAllDepartments().stream().map(department -> {
            final List<String> externalIdsOfDepartmentHeads = department.getDepartmentHeads().stream().map(Person::getUsername).toList();
            final List<String> externalIdsOfSecondStageAuthorities = department.getSecondStageAuthorities().stream().map(Person::getUsername).toList();
            final List<String> externalIdsOfMembers = department.getMembers().stream().map(Person::getUsername).toList();
            return new DepartmentDTO(department.getId(), department.getName(), department.getDescription(), department.getCreatedAt(), department.getLastModification(), department.isTwoStageApproval(), externalIdsOfDepartmentHeads, externalIdsOfSecondStageAuthorities, externalIdsOfMembers);
        }).toList();
    }
}
