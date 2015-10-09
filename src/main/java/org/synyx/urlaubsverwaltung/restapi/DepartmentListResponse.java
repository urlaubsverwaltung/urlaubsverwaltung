package org.synyx.urlaubsverwaltung.restapi;

import java.util.List;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
class DepartmentListResponse {

    private final List<DepartmentResponse> departments;

    DepartmentListResponse(List<DepartmentResponse> departments) {

        this.departments = departments;
    }

    public List<DepartmentResponse> getDepartments() {

        return departments;
    }
}
