package org.synyx.urlaubsverwaltung.department;

import org.springframework.stereotype.Service;

@Service
public class DepartmentImportService {

    private final DepartmentRepository departmentRepository;

    DepartmentImportService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    public void deleteAll() {
        departmentRepository.deleteAll();
    }

    public DepartmentEntity importDepartment(DepartmentEntity departmentEntity) {
        return departmentRepository.save(departmentEntity);
    }
}
