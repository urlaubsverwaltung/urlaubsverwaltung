package org.synyx.urlaubsverwaltung.department.web;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.department.Department;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.Role;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DepartmentDepartmentOverviewDtoMapperTest {

    @Test
    void ensureMapping() {

        final Department department = new Department();
        department.setId(42L);
        final Person activePerson = new Person();
        activePerson.setPermissions(Set.of(Role.USER));
        final Person inactivePerson = new Person();
        inactivePerson.setPermissions(Set.of(Role.INACTIVE));
        department.setMembers(List.of(activePerson, inactivePerson, inactivePerson));
        department.setDescription("Some department info");
        department.setName("Department");
        department.setTwoStageApproval(true);

        final LocalDate now = LocalDate.now();
        department.setLastModification(now);

        final DepartmentOverviewDto departmentOverviewDto = DepartmentDepartmentOverviewDtoMapper.mapToDepartmentOverviewDto(department);

        assertThat(departmentOverviewDto.getId()).isEqualTo(42);
        assertThat(departmentOverviewDto.getDescription()).isEqualTo("Some department info");
        assertThat(departmentOverviewDto.getName()).isEqualTo("Department");
        assertThat(departmentOverviewDto.getLastModification()).isEqualTo(now);
        assertThat(departmentOverviewDto.getActiveMembersCount()).isEqualTo(1);
        assertThat(departmentOverviewDto.getInactiveMembersCount()).isEqualTo(2);
    }
}
