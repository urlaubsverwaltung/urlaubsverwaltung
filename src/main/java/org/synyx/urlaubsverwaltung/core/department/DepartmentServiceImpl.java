package org.synyx.urlaubsverwaltung.core.department;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import java.util.Optional;


/**
 * Implementation for {@link DepartmentService}.
 *
 * @author  Daniel Hammann - <hammann@synyx.de>
 */
@Service
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentDAO departmentDAO;

    @Autowired
    public DepartmentServiceImpl(DepartmentDAO departmentDAO) {

        this.departmentDAO = departmentDAO;
    }

    @Override
    public Optional<Department> getDepartmentByName(String name) {

        return departmentDAO.findAbsenceMappingByName(name);
    }


    @Override
    public void save(Department department) {

        departmentDAO.save(department);
    }


    @Override
    public void update(Department department) {

        departmentDAO.save(department);
    }
}
