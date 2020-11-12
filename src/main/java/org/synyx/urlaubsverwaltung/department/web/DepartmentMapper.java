package org.synyx.urlaubsverwaltung.department.web;

import org.synyx.urlaubsverwaltung.department.Department;

import java.util.List;

import static java.util.stream.Collectors.toList;

final class DepartmentMapper {

    private DepartmentMapper() {
        // prevents init
    }

    static List<DepartmentForm> mapToDepartmentForm(List<Department> departments) {

        return departments.stream().map(DepartmentMapper::mapToDepartmentForm).collect(toList());
    }

    static DepartmentForm mapToDepartmentForm(Department department) {

        final DepartmentForm departmentForm = new DepartmentForm();
        departmentForm.setId(department.getId());
        departmentForm.setName(department.getName());
        departmentForm.setDescription(department.getDescription());
        departmentForm.setMembers(department.getMembers());
        departmentForm.setDepartmentHeads(department.getDepartmentHeads());
        departmentForm.setLastModification(department.getLastModification());
        departmentForm.setTwoStageApproval(department.isTwoStageApproval());
        departmentForm.setSecondStageAuthorities(department.getSecondStageAuthorities());

        return departmentForm;
    }

    static Department mapToDepartment(DepartmentForm departmentForm) {

        final Department department = new Department();
        department.setId(departmentForm.getId());
        department.setName(departmentForm.getName());
        department.setDescription(departmentForm.getDescription());
        department.setMembers(departmentForm.getMembers());
        department.setDepartmentHeads(departmentForm.getDepartmentHeads());
        department.setLastModification(departmentForm.getLastModification());
        department.setTwoStageApproval(departmentForm.isTwoStageApproval());
        department.setSecondStageAuthorities(departmentForm.getSecondStageAuthorities());

        return department;
    }
}
