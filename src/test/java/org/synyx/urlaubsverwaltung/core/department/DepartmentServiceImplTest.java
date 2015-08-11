package org.synyx.urlaubsverwaltung.core.department;

import org.joda.time.DateTime;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

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
}
