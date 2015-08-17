package org.synyx.urlaubsverwaltung.core.department;

import org.joda.time.DateTime;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.security.Role;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;


/**
 * Implementation for {@link DepartmentService}.
 *
 * @author  Daniel Hammann - <hammann@synyx.de>
 * @author  Aljona Murygina - <murygina@synyx.de>
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
    public List<Department> getManagedDepartmentsOfDepartmentHead(Person departmentHead) {

        return departmentDAO.getManagedDepartments(departmentHead);
    }


    @Override
    public List<Person> getManagedMembersOfDepartmentHead(Person departmentHead) {

        Set<Person> relevantPersons = new HashSet<>();
        List<Department> departments = getManagedDepartmentsOfDepartmentHead(departmentHead);

        for (Department department : departments) {
            List<Person> members = department.getMembers();
            relevantPersons.addAll(members);
        }

        return new ArrayList<>(relevantPersons);
    }


    @Override
    public boolean isDepartmentHeadOfPerson(Person departmentHead, Person person) {

        if (departmentHead.hasRole(Role.DEPARTMENT_HEAD)) {
            List<Person> members = getManagedMembersOfDepartmentHead(departmentHead);

            if (members.contains(person)) {
                return true;
            }
        }

        return false;
    }
}
