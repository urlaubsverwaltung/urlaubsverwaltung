package org.synyx.urlaubsverwaltung.department.web;

import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.person.Role;

import java.util.List;

final class DepartmentDepartmentOverviewDtoMapper {

    private DepartmentDepartmentOverviewDtoMapper() {
        // prevents init
    }

    static List<DepartmentOverviewDto> mapToDepartmentOverviewDtos(List<Department> departments) {
        return departments.stream().map(DepartmentDepartmentOverviewDtoMapper::mapToDepartmentOverviewDto).toList();
    }

    static DepartmentOverviewDto mapToDepartmentOverviewDto(Department department) {

        final DepartmentOverviewDto departmentOverviewDto = new DepartmentOverviewDto();
        departmentOverviewDto.setId(department.getId());
        departmentOverviewDto.setName(department.getName());
        departmentOverviewDto.setDescription(department.getDescription());
        departmentOverviewDto.setActiveMembersCount((int) department.getMembers().stream().filter(member -> !member.hasRole(Role.INACTIVE)).count());
        departmentOverviewDto.setInactiveMembersCount((int) department.getMembers().stream().filter(member -> member.hasRole(Role.INACTIVE)).count());
        departmentOverviewDto.setLastModification(department.getLastModification());
        departmentOverviewDto.setTwoStageApproval(department.isTwoStageApproval());

        return departmentOverviewDto;
    }
}
