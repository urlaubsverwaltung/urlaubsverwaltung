package org.synyx.urlaubsverwaltung.department.api;

import java.util.List;


class DepartmentsListWrapper {

    private final List<DepartmentResponse> departments;

    DepartmentsListWrapper(List<DepartmentResponse> departments) {
        this.departments = departments;
    }

    public List<DepartmentResponse> getDepartments() {
        return departments;
    }
}
