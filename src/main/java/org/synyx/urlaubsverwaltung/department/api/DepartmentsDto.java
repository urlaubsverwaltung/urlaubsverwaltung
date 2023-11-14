package org.synyx.urlaubsverwaltung.department.api;

import org.springframework.hateoas.RepresentationModel;

import java.util.List;

public class DepartmentsDto extends RepresentationModel<DepartmentsDto> {

    private final List<DepartmentDto> departments;

    DepartmentsDto(List<DepartmentDto> departments) {
        this.departments = departments;
    }

    public List<DepartmentDto> getDepartments() {
        return departments;
    }
}
