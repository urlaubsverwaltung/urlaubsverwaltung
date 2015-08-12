package org.synyx.urlaubsverwaltung.core.department;

import org.synyx.urlaubsverwaltung.core.person.Person;

import java.util.List;
import java.util.Optional;


/**
 * Service for handling {@link Department}s.
 *
 * <p>Daniel Hammann - <hammann@synyx.de>.</p>
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
     * @param  departmenId
     */
    void delete(Integer departmenId);


    /**
     * @return  all departments of the application.
     */
    List<Department> getAllDepartments();


    /**
     * Finds all departments with membership of given person.
     *
     * @param  person
     *
     * @return  list of departments which given person is a member.
     */
    List<Department> getAllDepartmentsWithMembership(Person person);
}
