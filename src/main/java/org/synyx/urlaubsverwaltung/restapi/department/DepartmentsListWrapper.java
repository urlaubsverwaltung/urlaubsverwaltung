package org.synyx.urlaubsverwaltung.restapi.department;

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
