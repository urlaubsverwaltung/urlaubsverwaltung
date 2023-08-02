package org.synyx.urlaubsverwaltung.department;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;

class DepartmentTest {

    @Test
    void ensureLastModificationDateIsSetAfterInitialization() {

        Department department = new Department();

        assertThat(department.getLastModification()).isNotNull();
        assertThat(department.getLastModification()).isEqualTo(LocalDate.now(UTC));
    }

    @Test
    void ensureReturnsCorrectLastModificationDate() {

        LocalDate lastModification = ZonedDateTime.now(UTC).minusDays(5).toLocalDate();

        Department department = new Department();
        department.setLastModification(lastModification);

        assertThat(department.getLastModification()).isEqualTo(lastModification);
    }

    @Test
    void toStringTest() {
        final Department department = new Department();
        department.setId(1L);
        department.setLastModification(LocalDate.MAX);
        department.setDescription("Description");
        department.setName("DepartmentName");
        department.setTwoStageApproval(true);
        department.setMembers(List.of(new Person("Member", "Theo", "Theo", "Theo")));
        department.setDepartmentHeads(List.of(new Person("Heads", "Theo", "Theo", "Theo")));
        department.setSecondStageAuthorities(List.of(new Person("Second", "Theo", "Theo", "Theo")));

        final String departmentToString = department.toString();
        assertThat(departmentToString).isEqualTo("Department{name='DepartmentName', description='Description', " +
            "lastModification=+999999999-12-31, twoStageApproval=true, members=[Person{id='null'}], " +
            "departmentHeads=[Person{id='null'}], secondStageAuthorities=[Person{id='null'}]}");
    }

    @Test
    void equals() {
        final Department departmentOne = new Department();
        departmentOne.setId(1L);

        final Department departmentOneOne = new Department();
        departmentOneOne.setId(1L);

        final Department departmentTwo = new Department();
        departmentTwo.setId(2L);

        assertThat(departmentOne)
            .isEqualTo(departmentOne)
            .isEqualTo(departmentOneOne)
            .isNotEqualTo(departmentTwo)
            .isNotEqualTo(new Object())
            .isNotEqualTo(null);
    }
}
