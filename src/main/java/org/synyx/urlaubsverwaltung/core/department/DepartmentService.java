package org.synyx.urlaubsverwaltung.core.department;

import org.synyx.urlaubsverwaltung.core.person.Person;

import java.util.List;
import java.util.Optional;


/**
 * Service for handling {@link Department}s.
 *
 * @author  Daniel Hammann - <hammann@synyx.de>
 * @author  Aljona Murygina - <murygina@synyx.de>
 */
public interface DepartmentService {

    /**
     * @param  departmentId
     *
     * @return  department to given id
     */
    Optional<Department> getDepartmentById(Integer departmentId);


    /**
     * adds the given department to repository.
     *
     * @param  department
     */
    void create(Department department);


    /**
     * Updates a given department in repository.
     *
     * @param  department
     */
    void update(Department department);


    /**
     * Deletes department with given id.
     *
     * @param  departmentId
     */
    void delete(Integer departmentId);


    /**
     * @return  all departments of the application.
     */
    List<Department> getAllDepartments();


    /**
     * Finds all departments the given person is set as department head.
     *
     * @param  departmentHead  to get the departments of
     *
     * @return  list of departments the department head manages
     */
    List<Department> getManagedDepartmentsOfDepartmentHead(Person departmentHead);


    /**
     * Get all members of the departments where the given person is the department head.
     *
     * @param  departmentHead  manages the members to be fetched
     *
     * @return  all unique members of the departments where the given person is the department head
     */
    List<Person> getManagedMembersOfDepartmentHead(Person departmentHead);


    /**
     * Check if the given department head manages a department that the given person is assigned to.
     *
     * @param  departmentHead  to be checked if he is the department head of a department that the given person is
     *                         assigned to
     * @param  person  to be checked if he is assigned to a department that has the given department head
     *
     * @return  {@code true} if the given department head manages a department that the given person is assigned to,
     *          else {@code false}
     */
    boolean isDepartmentHeadOfPerson(Person departmentHead, Person person);
}
