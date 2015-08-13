package org.synyx.urlaubsverwaltung.core.department;

import org.joda.time.DateTime;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.synyx.urlaubsverwaltung.core.person.Person;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import static org.mockito.Matchers.eq;


/**
 * Daniel Hammann - <hammann@synyx.de>.
 */
public class DepartmentServiceImplTest {

    private DepartmentServiceImpl sut;
    private DepartmentDAO departmentDAO;

    @Before
    public void setUp() throws Exception {

        departmentDAO = Mockito.mock(DepartmentDAO.class);
        sut = new DepartmentServiceImpl(departmentDAO);
    }


    @Test
    public void ensureCallDepartmentDAOSave() throws Exception {

        Department dummyDepartment = createDummyDepartment();

        sut.create(dummyDepartment);

        Mockito.verify(departmentDAO).save(eq(dummyDepartment));
    }


    private Department createDummyDepartment() {

        Department department = new Department();
        department.setName("FooDepartment");
        department.setDescription("This is the foo department.");
        department.setLastModification(DateTime.now());

        return department;
    }


    @Test
    public void ensureCallDepartmentDAOFindOne() throws Exception {

        sut.getDepartmentById(42);
        Mockito.verify(departmentDAO).findOne(eq(42));
    }


    @Test
    public void ensureUpdateCallDepartmentDAOUpdate() throws Exception {

        Department dummyDepartment = createDummyDepartment();

        sut.update(dummyDepartment);

        Mockito.verify(departmentDAO).save(eq(dummyDepartment));
    }


    @Test
    public void ensureGetAllCallDepartmentDAOFindAll() throws Exception {

        sut.getAllDepartments();

        Mockito.verify(departmentDAO).findAll();
    }


    @Test(expected = IllegalStateException.class)
    public void ensureExceptionOnDeletionOfNonPersistedId() throws Exception {

        int id = 0;
        Mockito.when(departmentDAO.findOne(id)).thenReturn(null);

        sut.delete(id);
    }


    @Test
    public void ensureDeleteCallFindOneAndDelete() throws Exception {

        int id = 0;
        Mockito.when(departmentDAO.getOne(id)).thenReturn(new Department());

        sut.delete(id);

        Mockito.verify(departmentDAO).getOne(eq(id));
        Mockito.verify(departmentDAO).delete(eq(id));
    }


    @Test
    public void ensureSetLastModificationOnCreate() throws Exception {

        Department department = new Department();
        department.setName("Test Department");
        department.setDescription("Test Description");

        assertNull(department.getLastModification());

        sut.create(department);

        assertNotNull(department.getLastModification());
    }


    @Test
    public void ensureSetLastModificationOnUpdate() throws Exception {

        Department department = new Department();
        department.setName("Test Department");
        department.setDescription("Test Description");

        assertNull(department.getLastModification());

        sut.update(department);

        assertNotNull(department.getLastModification());
    }


    @Test
    public void ensureReturnsAllMembersOfTheDepartmentsOfThePerson() {

        Person person = Mockito.mock(Person.class);
        Mockito.when(person.getId()).thenReturn(42);

        Person admin1 = new Person("admin1", "", "", "");
        Person admin2 = new Person("admin2", "", "", "");

        Person marketing1 = new Person("marketing1", "", "", "");
        Person marketing2 = new Person("marketing2", "", "", "");
        Person marketing3 = new Person("marketing3", "", "", "");

        Department admins = new Department();
        admins.setMembers(Arrays.asList(admin1, admin2));

        Department marketing = new Department();
        marketing.setMembers(Arrays.asList(marketing1, marketing2, marketing3));

        Mockito.when(departmentDAO.getDepartmentsWithMembership(Mockito.anyInt()))
            .thenReturn(Arrays.asList(admins, marketing));

        List<Person> members = sut.getAllMembersOfDepartmentsOfPerson(person);

        Assert.assertNotNull("Should not be null", members);
        Assert.assertEquals("Wrong number of members", 5, members.size());
    }


    @Test
    public void ensureReturnsEmptyListIfPersonHasNoDepartmentAssigned() {

        Person person = Mockito.mock(Person.class);
        Mockito.when(person.getId()).thenReturn(42);

        Mockito.when(departmentDAO.getDepartmentsWithMembership(Mockito.anyInt())).thenReturn(Collections.emptyList());

        List<Person> members = sut.getAllMembersOfDepartmentsOfPerson(person);

        Assert.assertNotNull("Should not be null", members);
        Assert.assertTrue("Should be empty", members.isEmpty());
    }


    @Test
    public void ensureReturnsEmptyListIfAssignedDepartmentsHaveNoMembers() {

        Person person = Mockito.mock(Person.class);
        Mockito.when(person.getId()).thenReturn(42);

        Department admins = new Department();
        Department marketing = new Department();

        Mockito.when(departmentDAO.getDepartmentsWithMembership(Mockito.anyInt()))
            .thenReturn(Arrays.asList(admins, marketing));

        List<Person> members = sut.getAllMembersOfDepartmentsOfPerson(person);

        Assert.assertNotNull("Should not be null", members);
        Assert.assertTrue("Should be empty", members.isEmpty());
    }


    @Test
    public void ensureReturnedMembersOfTheDepartmentsOfThePersonAreUnique() {

        Person person = Mockito.mock(Person.class);
        Mockito.when(person.getId()).thenReturn(42);

        Person admin1 = new Person();

        Person marketing1 = new Person();
        Person marketing2 = new Person();

        Person adminAndMarketing = new Person();

        Department admins = new Department();
        admins.setMembers(Arrays.asList(admin1, adminAndMarketing));

        Department marketing = new Department();
        marketing.setMembers(Arrays.asList(marketing1, marketing2, adminAndMarketing));

        Mockito.when(departmentDAO.getDepartmentsWithMembership(Mockito.anyInt()))
            .thenReturn(Arrays.asList(admins, marketing));

        List<Person> members = sut.getAllMembersOfDepartmentsOfPerson(person);

        Assert.assertNotNull("Should not be null", members);
        Assert.assertEquals("Wrong number of members", 4, members.size());
    }
}
