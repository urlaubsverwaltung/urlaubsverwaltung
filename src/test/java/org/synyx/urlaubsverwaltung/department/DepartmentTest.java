package org.synyx.urlaubsverwaltung.department;

import org.junit.Assert;
import org.junit.Test;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.demodatacreator.DemoDataCreator;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;


public class DepartmentTest {

    @Test
    public void ensureLastModificationDateIsSetAfterInitialization() {

        Department department = new Department();

        Assert.assertNotNull("Last modification date should be set", department.getLastModification());
        Assert.assertEquals("Wrong last modification date", LocalDate.now(UTC),
            department.getLastModification());
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureCanNotSetLastModificationDateToNull() {

        new Department().setLastModification(null);
    }


    @Test
    public void ensureReturnsCorrectLastModificationDate() {

        LocalDate lastModification = ZonedDateTime.now(UTC).minusDays(5).toLocalDate();

        Department department = new Department();
        department.setLastModification(lastModification);

        Assert.assertEquals("Wrong last modification date", lastModification, department.getLastModification());
    }


    @Test
    public void ensureMembersListIsUnmodifiable() {

        List<Person> modifiableList = new ArrayList<>();
        modifiableList.add(DemoDataCreator.createPerson());

        Department department = new Department();
        department.setMembers(modifiableList);

        try {
            department.getMembers().add(DemoDataCreator.createPerson());
            Assert.fail("Members list should be unmodifiable!");
        } catch (UnsupportedOperationException ex) {
            // Expected
        }
    }


    @Test
    public void ensureDepartmentHeadsListIsUnmodifiable() {

        List<Person> modifiableList = new ArrayList<>();
        modifiableList.add(DemoDataCreator.createPerson());

        Department department = new Department();
        department.setDepartmentHeads(modifiableList);

        try {
            department.getDepartmentHeads().add(DemoDataCreator.createPerson());
            Assert.fail("Department head list should be unmodifiable!");
        } catch (UnsupportedOperationException ex) {
            // Expected
        }
    }

    @Test
    public void toStringTest() {
        final Department department = new Department();
        department.setId(1);
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
}
