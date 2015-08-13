package org.synyx.urlaubsverwaltung.core.department;

import org.joda.time.DateTime;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import org.synyx.urlaubsverwaltung.core.person.Person;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;


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
    public Optional<Department> getDepartmentById(Integer departmentId) {

        return Optional.ofNullable(departmentDAO.findOne(departmentId));
    }


    @Override
    public void create(Department department) {

        department.setLastModification(DateTime.now());

        departmentDAO.save(department);
    }


    @Override
    public void update(Department department) {

        department.setLastModification(DateTime.now());

        departmentDAO.save(department);
    }


    @Override
    public void delete(Integer departmentId) {

        if (departmentDAO.getOne(departmentId) != null) {
            departmentDAO.delete(departmentId);
        } else {
            throw new IllegalStateException("Repository does not contain a department with given id");
        }
    }


    @Override
    public List<Department> getAllDepartments() {

        return departmentDAO.findAll();
    }


    @Override
    public List<Department> getAllDepartmentsWithMembership(Person person) {

        return departmentDAO.getDepartmentsWithMembership(person.getId());
    }


    @Override
    public List<Person> getAllMembersOfDepartmentsOfPerson(Person person) {

        Set<Person> relevantPersons = new HashSet<>();
        List<Department> departments = getAllDepartmentsWithMembership(person);

        for (Department department : departments) {
            List<Person> members = department.getMembers();
            relevantPersons.addAll(members);
        }

        return new ArrayList<>(relevantPersons);
    }
}
