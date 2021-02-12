package org.synyx.urlaubsverwaltung.department;

import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


/**
 * Service for handling {@link Department}s.
 */
public interface DepartmentService {

    /**
     * Check if a {@link Department} with the given departmentId exists or not.
     *
     * @param departmentId id of a {@link Department} to check
     * @return <code>true</code> if the departmentId exists, <code>false</code> otherwise
     */
    boolean departmentExists(Integer departmentId);

    /**
     * Returns a department by its unique identifier
     *
     * @param departmentId the unique identifier to of a department
     * @return department to given id
     */
    Optional<Department> getDepartmentById(Integer departmentId);

    /**
     * adds the given department to repository.
     *
     * @param department the {@link Department} to create
     */
    Department create(Department department);

    /**
     * Updates a given department in repository.
     *
     * @param department the {@link Department} to update
     */
    Department update(Department department);

    /**
     * Deletes department with given id.
     *
     * @param departmentId the unique identifier to delete a department
     */
    void delete(Integer departmentId);

    /**
     * @return all departments of the application.
     */
    List<Department> getAllDepartments();

    /**
     * Finds all departments the given person is member of.
     *
     * @param member to get the departments of
     * @return list of departments the given person is assigned to
     */
    List<Department> getAssignedDepartmentsOfMember(Person member);

    /**
     * Finds all departments the given person is set as department head.
     *
     * @param departmentHead to get the departments of
     * @return list of departments the department head manages
     */
    List<Department> getManagedDepartmentsOfDepartmentHead(Person departmentHead);

    /**
     * Finds all departments the given person is set as second stage authority.
     *
     * @param secondStageAuthority to get the departments of
     * @return list of departments the second stage authority manages
     */
    List<Department> getManagedDepartmentsOfSecondStageAuthority(Person secondStageAuthority);

    /**
     * Get all active (waiting or allowed) applications for leave of the members of the departments of the given person
     * for the provided period.
     *
     * @param member    to get the departments of
     * @param startDate of the period
     * @param endDate   of the period
     * @return list of waiting or allowed applications for leave of departments members
     */
    List<Application> getApplicationsForLeaveOfMembersInDepartmentsOfPerson(Person member, LocalDate startDate, LocalDate endDate);

    /**
     * Get all members (including the given person) of the departments where the given person is the department head.
     *
     * @param departmentHead manages the members to be fetched
     * @return all unique members of the departments where the given person is the department head
     */
    List<Person> getManagedMembersOfDepartmentHead(Person departmentHead);

    /**
     * Get all members of the departments where the given person is the secondStageAuthority.
     *
     * @param secondStageAuthority responsible for releases of the members to be fetched
     * @return all unique members of the departments where the given person is the secondStageAuthority
     */
    List<Person> getManagedMembersForSecondStageAuthority(Person secondStageAuthority);

    /**
     * Check if the given department head manages a department that the given person is assigned to.
     *
     * @param departmentHead to be checked if he is the department head of a department that the given person is
     *                       assigned to
     * @param person         to be checked if he is assigned to a department that has the given department head
     * @return {@code true} if the given department head manages a department that the given person is assigned to,
     * else {@code false}
     */
    boolean isDepartmentHeadOfPerson(Person departmentHead, Person person);

    /**
     * Check if the given secondStageAuthority is responsible for the department that the given person is assigned to.
     *
     * @param secondStageAuthority to be checked if he is responsible for a department that the given person is
     *                             assigned to
     * @param person               to be checked if he is assigned to a department that has the given secondStageAuthority
     * @return {@code true} if the given secondStageAuthority is responsible for a department that the given person is
     * assigned to, else {@code false}
     */
    boolean isSecondStageAuthorityOfPerson(Person secondStageAuthority, Person person);

    /**
     * Check if the given signed in user is allowed to access the data of the given person.
     *
     * @param signedInUser to check the permissions
     * @param person       which data should be accessed
     * @return {@code true} if the given user may access the data of the given person, else {@code false}
     */
    boolean isSignedInUserAllowedToAccessPersonData(Person signedInUser, Person person);

    /**
     * Get all departments which the given user is allowed to access the data
     *
     * @param person to check the permissions
     * @return List of departments which are accessible by the given person
     */
    List<Department> getAllowedDepartmentsOfPerson(Person person);
}
