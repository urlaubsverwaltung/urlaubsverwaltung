package org.synyx.urlaubsverwaltung.restapi.wrapper;

import org.synyx.urlaubsverwaltung.restapi.DepartmentResponse;

import java.util.List;


/**
 * @author  David Schilling - schilling@synyx.de
 */
public class DepartmentsListWrapper {

    private final List<DepartmentResponse> departments;

    public DepartmentsListWrapper(List<DepartmentResponse> departments) {

        this.departments = departments;
    }

    public List<DepartmentResponse> getDepartments() {

        return departments;
    }
}
