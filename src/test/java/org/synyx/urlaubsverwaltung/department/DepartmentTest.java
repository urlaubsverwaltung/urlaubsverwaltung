package org.synyx.urlaubsverwaltung.department;

import org.junit.Assert;
import org.junit.Test;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.time.ZoneOffset.UTC;


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
        modifiableList.add(TestDataCreator.createPerson());

        Department department = new Department();
        department.setMembers(modifiableList);

        try {
            department.getMembers().add(TestDataCreator.createPerson());
            Assert.fail("Members list should be unmodifiable!");
        } catch (UnsupportedOperationException ex) {
            // Expected
        }
    }


    @Test
    public void ensureDepartmentHeadsListIsUnmodifiable() {

        List<Person> modifiableList = new ArrayList<>();
        modifiableList.add(TestDataCreator.createPerson());

        Department department = new Department();
        department.setDepartmentHeads(modifiableList);

        try {
            department.getDepartmentHeads().add(TestDataCreator.createPerson());
            Assert.fail("Department head list should be unmodifiable!");
        } catch (UnsupportedOperationException ex) {
            // Expected
        }
    }
}
