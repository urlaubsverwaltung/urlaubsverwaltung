package org.synyx.urlaubsverwaltung.department.api;

import java.util.List;

class DepartmentsDto {

    private final List<DepartmentDto> departments;

    DepartmentsDto(List<DepartmentDto> departments) {
        this.departments = departments;
    }

    public List<DepartmentDto> getDepartments() {
        return departments;
    }
}
