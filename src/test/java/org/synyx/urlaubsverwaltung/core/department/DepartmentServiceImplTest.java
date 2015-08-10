package org.synyx.urlaubsverwaltung.core.department;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import java.util.Date;

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

        sut.save(dummyDepartment);

        Mockito.verify(departmentDAO).save(eq(dummyDepartment));
    }


    private Department createDummyDepartment() {

        Department department = new Department();
        department.setName("FooDepartment");
        department.setDescription("This is the foo department.");
        department.setLastModification(new Date());

        return department;
    }


    @Test
    public void ensureCallDepartmentDAOFind() throws Exception {

        String departmentName = "FooDepartment";
        sut.getDepartmentByName(departmentName);
        Mockito.verify(departmentDAO).findAbsenceMappingByName(eq(departmentName));
    }


    @Test
    public void ensureUpdateCallDepartmentDAOUpdate() throws Exception {

        Department dummyDepartment = createDummyDepartment();

        sut.update(dummyDepartment);

        Mockito.verify(departmentDAO).save(eq(dummyDepartment));
    }
}
