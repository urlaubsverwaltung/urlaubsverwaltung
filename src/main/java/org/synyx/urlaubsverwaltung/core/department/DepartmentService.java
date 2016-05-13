package org.synyx.urlaubsverwaltung.core.department;

import org.joda.time.DateMidnight;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
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
     * Finds all departments the given person is member of.
     *
     * @param  member  to get the departments of
     *
     * @return  list of departments the given person is assigned to
     */
    List<Department> getAssignedDepartmentsOfMember(Person member);


    /**
     * Finds all departments the given person is set as department head.
     *
     * @param  departmentHead  to get the departments of
     *
     * @return  list of departments the department head manages
     */
    List<Department> getManagedDepartmentsOfDepartmentHead(Person departmentHead);


    /**
     * Finds all departments the given person is set as second stage authority.
     *
     * @param  secondStageAuthority  to get the departments of
     *
     * @return  list of departments the second stage authority manages
     */
    List<Department> getManagedDepartmentsOfSecondStageAuthority(Person secondStageAuthority);


    /**
     * Get all active (waiting or allowed) applications for leave of the members of the departments of the given person
     * for the provided period.
     *
     * @param  member  to get the departments of
     * @param  startDate  of the period
     * @param  endDate  of the period
     *
     * @return  list of waiting or allowed applications for leave of departments members
     */
    List<Application> getApplicationsForLeaveOfMembersInDepartmentsOfPerson(Person member, DateMidnight startDate,
        DateMidnight endDate);


    /**
     * Get all members (including the given person) of the departments where the given person is the department head.
     *
     * @param  departmentHead  manages the members to be fetched
     *
     * @return  all unique members of the departments where the given person is the department head
     */
    List<Person> getManagedMembersOfDepartmentHead(Person departmentHead);


    /**
     * Get all members of the departments where the given person is the secondStageAuthority.
     *
     * @param  secondStageAuthority  responsible for releases of the members to be fetched
     *
     * @return  all unique members of the departments where the given person is the secondStageAuthority
     */
    List<Person> getMembersForSecondStageAuthority(Person secondStageAuthority);


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


    /**
     * Check if the given secondStageAuthority is responsible for the department that the given person is assigned to.
     *
     * @param  secondStageAuthority  to be checked if he is responsible for a department that the given person is
     *                               assigned to
     * @param  person  to be checked if he is assigned to a department that has the given secondStageAuthority
     *
     * @return  {@code true} if the given secondStageAuthority is responsible for a department that the given person is
     *          assigned to, else {@code false}
     */
    boolean isSecondStageAuthorityOfPerson(Person secondStageAuthority, Person person);

    /**
     * Get all the "relevant" departments for a given Person.
     *
     * This includes memberships, managed departments, etc.
     * It is used to populate the department selector pulldown.
     */
    public List<Department> getRelevantDepartments(Person person);
}
