package org.synyx.urlaubsverwaltung.restapi.department;

import java.util.List;


/**
 * @author  David Schilling - schilling@synyx.de
 */
class DepartmentsListWrapper {

    private final List<DepartmentResponse> departments;

    DepartmentsListWrapper(List<DepartmentResponse> departments) {

        this.departments = departments;
    }

    public List<DepartmentResponse> getDepartments() {

        return departments;
    }
}
