package org.synyx.urlaubsverwaltung.core.department;

import org.joda.time.DateTime;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.security.Role;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import static org.mockito.Matchers.eq;


/**
 * @author  Daniel Hammann - hammann@synyx.de
 * @author  Aljona Murygina - murygina@synyx.de
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


    @Test
    public void ensureGetManagedDepartmentsOfDepartmentHeadCallCorrectDAOMethod() throws Exception {

        Person person = Mockito.mock(Person.class);

        sut.getManagedDepartmentsOfDepartmentHead(person);

        Mockito.verify(departmentDAO).getManagedDepartments(person);
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

        Person departmentHead = Mockito.mock(Person.class);

        Person admin1 = new Person();
        Person admin2 = new Person();

        Person marketing1 = new Person();
        Person marketing2 = new Person();
        Person marketing3 = new Person();

        Department admins = new Department();
        admins.setMembers(Arrays.asList(admin1, admin2, departmentHead));

        Department marketing = new Department();
        marketing.setMembers(Arrays.asList(marketing1, marketing2, marketing3, departmentHead));

        Mockito.when(departmentDAO.getManagedDepartments(departmentHead)).thenReturn(Arrays.asList(admins, marketing));

        List<Person> members = sut.getManagedMembersOfDepartmentHead(departmentHead);

        Assert.assertNotNull("Should not be null", members);
        Assert.assertEquals("Wrong number of members", 6, members.size());
    }


    @Test
    public void ensureReturnsEmptyListIfPersonHasNoDepartmentAssigned() {

        Person departmentHead = Mockito.mock(Person.class);

        Mockito.when(departmentDAO.getManagedDepartments(departmentHead)).thenReturn(Collections.emptyList());

        List<Person> members = sut.getManagedMembersOfDepartmentHead(departmentHead);

        Assert.assertNotNull("Should not be null", members);
        Assert.assertTrue("Should be empty", members.isEmpty());
    }


    @Test
    public void ensureReturnsTrueIfIsDepartmentHeadOfTheGivenPerson() {

        Person departmentHead = Mockito.mock(Person.class);
        Mockito.when(departmentHead.hasRole(Role.DEPARTMENT_HEAD)).thenReturn(true);

        Person admin1 = new Person();
        Person admin2 = new Person();

        Department admins = new Department();
        admins.setMembers(Arrays.asList(admin1, admin2, departmentHead));

        Mockito.when(departmentDAO.getManagedDepartments(departmentHead)).thenReturn(Collections.singletonList(admins));

        boolean isDepartmentHead = sut.isDepartmentHeadOfPerson(departmentHead, admin1);

        Assert.assertTrue("Should be the department head of the given person", isDepartmentHead);
    }


    @Test
    public void ensureReturnsFalseIfIsNotDepartmentHeadOfTheGivenPerson() {

        Person departmentHead = Mockito.mock(Person.class);
        Mockito.when(departmentHead.hasRole(Role.DEPARTMENT_HEAD)).thenReturn(true);

        Person admin1 = new Person();
        Person admin2 = new Person();

        Department admins = new Department();
        admins.setMembers(Arrays.asList(admin1, admin2, departmentHead));

        Person marketing1 = new Person();

        Mockito.when(departmentDAO.getManagedDepartments(departmentHead)).thenReturn(Collections.singletonList(admins));

        boolean isDepartmentHead = sut.isDepartmentHeadOfPerson(departmentHead, marketing1);

        Assert.assertFalse("Should not be the department head of the given person", isDepartmentHead);
    }


    @Test
    public void ensureReturnsFalseIfIsInTheSameDepartmentButHasNotDepartmentHeadRole() {

        Person noDepartmentHead = Mockito.mock(Person.class);
        Mockito.when(noDepartmentHead.hasRole(Role.DEPARTMENT_HEAD)).thenReturn(false);

        Person admin1 = new Person();
        Person admin2 = new Person();

        Department admins = new Department();
        admins.setMembers(Arrays.asList(admin1, admin2, noDepartmentHead));

        Mockito.when(departmentDAO.getManagedDepartments(noDepartmentHead))
            .thenReturn(Collections.singletonList(admins));

        boolean isDepartmentHead = sut.isDepartmentHeadOfPerson(noDepartmentHead, admin1);

        Assert.assertFalse("Should not be the department head of the given person", isDepartmentHead);
    }
}
