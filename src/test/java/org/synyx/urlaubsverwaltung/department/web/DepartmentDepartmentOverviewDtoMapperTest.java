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
        department.setId(42);
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

    @Test
    void ensureEquals() {

        DepartmentOverviewDto d1 = new DepartmentOverviewDto();
        d1.setId(1);
        d1.setName("d1");
        DepartmentOverviewDto d2 = new DepartmentOverviewDto();
        d2.setId(1);
        d2.setName("d2");
        DepartmentOverviewDto d3 = new DepartmentOverviewDto();
        d3.setId(2);
        d3.setName("d3");

        assertThat(d1).isEqualTo(d2);
        assertThat(d1).isNotEqualTo(d3);
    }

    @Test
    void ensureHashCode() {

        DepartmentOverviewDto d1 = new DepartmentOverviewDto();
        d1.setId(1);
        d1.setName("d1");
        DepartmentOverviewDto d2 = new DepartmentOverviewDto();
        d2.setId(1);
        d2.setName("d2");
        DepartmentOverviewDto d3 = new DepartmentOverviewDto();
        d3.setId(2);
        d3.setName("d3");

        assertThat(d1.hashCode()).isEqualTo(d2.hashCode());
        assertThat(d1.hashCode()).isNotEqualTo(d3.hashCode());
    }
}
