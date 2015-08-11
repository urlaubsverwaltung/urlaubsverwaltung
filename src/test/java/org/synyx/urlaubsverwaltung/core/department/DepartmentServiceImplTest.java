package org.synyx.urlaubsverwaltung.core.department;

import org.joda.time.DateTime;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

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
}
