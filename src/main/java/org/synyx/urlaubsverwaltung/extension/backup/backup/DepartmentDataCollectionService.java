package org.synyx.urlaubsverwaltung.extension.backup.backup;

import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.department.DepartmentService;
import org.synyx.urlaubsverwaltung.extension.backup.model.DepartmentDTO;

import java.util.List;

@Component
@ConditionalOnBackupCreateEnabled
class DepartmentDataCollectionService {

    private final DepartmentService departmentService;

    DepartmentDataCollectionService(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    List<DepartmentDTO> collectDepartments() {
        return departmentService.getAllDepartments().stream()
            .map(DepartmentDataCollectionService::toDepartmentDTO)
            .toList();
    }

    private static DepartmentDTO toDepartmentDTO(Department department) {
        return new DepartmentDTO(
            department.getId(),
            department.getName(),
            department.getDescription(),
            department.getCreatedAt(),
            department.getLastModification(),
            department.isTwoStageApproval()
        );
    }
}
