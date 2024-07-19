package org.synyx.urlaubsverwaltung.department;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonId;
import org.synyx.urlaubsverwaltung.search.PageableSearchQuery;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
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
    boolean departmentExists(Long departmentId);

    /**
     * Returns a department by its unique identifier
     *
     * @param departmentId the unique identifier to of a department
     * @return department to given id
     */
    Optional<Department> getDepartmentById(Long departmentId);

    /**
     * Returns a department by its name
     *
     * @param departmentName the name of a department
     * @return department to given name
     */
    Optional<Department> getDepartmentByName(String departmentName);

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
    void delete(Long departmentId);

    /**
     * @return all departments ordered by the department name
     */
    List<Department> getAllDepartments();

    /**
     * Finds all departments the given person is member of.
     *
     * @param member to get the departments of
     * @return list of departments the given person is assigned to ordered by the department name
     */
    List<Department> getAssignedDepartmentsOfMember(Person member);

    /**
     * Finds all departments the given person is set as department head.
     *
     * @param departmentHead to get the departments of
     * @return list of departments the department head manages ordered by the department name
     */
    List<Department> getManagedDepartmentsOfDepartmentHead(Person departmentHead);

    /**
     * Finds all departments the given person is set as second stage authority.
     *
     * @param secondStageAuthority to get the departments of
     * @return list of departments the second stage authority manages ordered by the department name
     */
    List<Department> getManagedDepartmentsOfSecondStageAuthority(Person secondStageAuthority);

    /**
     * Finds all departments the given person has access to see the data
     * <p>
     * If the person has the permission:
     * <ul>
     * <li>BOSS or OFFICE - return all departments</li>
     * <li>DEPARTMENT_HEAD - return all managed department of the person as department head</li>
     * <li>SECOND_STAGE_AUTHORITY - return all managed department of the person as second stage authority</li>
     * </ul>
     * </p>
     *
     * @param person to check the permissions
     * @return List of departments which are accessible by the given person
     */
    List<Department> getDepartmentsPersonHasAccessTo(Person person);

    /**
     * Get all active (waiting or allowed) applications for leave of the members of the departments of the given person
     * for the provided period. Sorted by the start date of the application.
     *
     * @param member    to get the departments of
     * @param startDate of the period
     * @param endDate   of the period
     * @return list of waiting or allowed applications for leave of departments members
     */
    List<Application> getApplicationsFromColleaguesOf(Person member, LocalDate startDate, LocalDate endDate);

    /**
     * Get all distinct members of the departments where the given person is department head.
     * (including the given person and second stage authorities)
     *
     * @param departmentHead to know all the members of the department
     * @return all unique members of the departments where the given person is department head.
     */
    List<Person> getMembersForDepartmentHead(Person departmentHead);

    /**
     * Get all distinct members of the departments where the given person is second stage authority.
     * (including the given person)
     *
     * @param secondStageAuthority to know all the members of the department
     * @return all unique members of the departments where the given person is second stage authority.
     */
    List<Person> getMembersForSecondStageAuthority(Person secondStageAuthority);

    /**
     * Check if the given department head manages a department that the given person is assigned to.
     *
     * @param departmentHead to be checked if he is the department head of a department that the given person is
     *                       assigned to
     * @param person         to be checked if he is assigned to a department that has the given department head
     * @return {@code true} if the given department head manages a department that the given person is assigned to,
     * else {@code false}
     */
    boolean isDepartmentHeadAllowedToManagePerson(Person departmentHead, Person person);

    /**
     * Check the role of the given person and return a {@link Page} of all managed and active {@link Person}s for the
     * {@link Pageable} request. Managed members are all persons for which a privileged person are responsible
     * for and can perform actions for this person.
     *
     * @param person                    person to get managed members for
     * @param personPageableSearchQuery search query containing pageable and an optional query for firstname/lastname
     * @return all managed and active members for the person
     */
    Page<Person> getManagedMembersOfPerson(Person person, PageableSearchQuery personPageableSearchQuery);

    /**
     * Check the role of the given person and return a {@link List} of all managed and active {@link Person}s.
     * Managed members are all persons for which a privileged person are responsible
     * for and can perform actions for this person.
     *
     * @param person person to get managed members for
     * @return all managed and active members for the person
     */
    List<Person> getManagedActiveMembersOfPerson(Person person);

    /**
     * Check the role of the given person and return a {@link Page} of all managed and active {@link Person}s for the
     * {@link Pageable} request. Managed members are all persons for which a privileged person are responsible
     * for and can perform actions for this person.
     *
     * @param person                    person to get managed members for
     * @param personPageableSearchQuery search query containing pageable and an optional query for firstname/lastname
     * @return all managed and inactive members for the person
     */
    Page<Person> getManagedInactiveMembersOfPerson(Person person, PageableSearchQuery personPageableSearchQuery);

    /**
     * Check the role of the given person and return a {@link Page} of all managed and active {@link Person}s for the
     * {@link Pageable} request. Managed members are all persons for which a privileged person are responsible
     * for and can perform actions for this person.
     *
     * @param person              person to get managed members for
     * @param departmentId        departmentId to get managed members for
     * @param pageableSearchQuery searchQuery to restrict the result set
     * @return all managed and active members for the person
     */
    Page<Person> getManagedMembersOfPersonAndDepartment(Person person, Long departmentId, PageableSearchQuery pageableSearchQuery);

    /**
     * Check the role of the given person and return a {@link Page} of all managed and inactive {@link Person}s for the
     * {@link Pageable} request. Managed members are all persons for which a privileged person are responsible
     * for and can perform actions for this person.
     *
     * @param person              person to get managed members for
     * @param departmentId        departmentId to get managed members for
     * @param pageableSearchQuery search query containing pageable and an optional query for firstname/lastname
     * @return all managed and inactive members for the person
     */
    Page<Person> getManagedInactiveMembersOfPersonAndDepartment(Person person, Long departmentId, PageableSearchQuery pageableSearchQuery);

    /**
     * Get all distinct managed members of the department head.
     * Managed members are all persons for which the department head are responsible for and can
     * perform actions for this person.
     *
     * @param departmentHead to know all the members of the department
     * @return all managed members of the department head
     */
    List<Person> getManagedMembersOfDepartmentHead(Person departmentHead);

    /**
     * Check if the given secondStageAuthority is responsible for the department that the given person is assigned to.
     *
     * @param secondStageAuthority to be checked if he is responsible for a department that the given person is
     *                             assigned to
     * @param person               to be checked if he is assigned to a department that has the given secondStageAuthority
     * @return {@code true} if the given secondStageAuthority is responsible for a department that the given person is
     * assigned to, else {@code false}
     */
    boolean isSecondStageAuthorityAllowedToManagePerson(Person secondStageAuthority, Person person);

    /**
     * Get all distinct managed members of the second stage authority.
     * Managed members are all persons for which the second stage authority are responsible for and can
     * perform actions for this person.
     *
     * @param secondStageAuthority to know all the members of the department
     * @return all managed members of the second stage authority
     */
    List<Person> getManagedMembersForSecondStageAuthority(Person secondStageAuthority);

    /**
     * Check if the given signed in user is allowed to access the data of the given person.
     *
     * @param signedInUser to check the permissions
     * @param person       which data should be accessed
     * @return {@code true} if the given user may access the data of the given person, else {@code false}
     */
    boolean isSignedInUserAllowedToAccessPersonData(Person signedInUser, Person person);

    /**
     * Check if the given {@link Person} is allowed to access the data of the {@link Department}.
     *
     * @param person     to check the permissions
     * @param department which data should be accessed
     * @return {@code true} if the given user may access the data of the given person, else {@code false}
     */
    boolean isPersonAllowedToManageDepartment(Person person, Department department);

    /**
     * Returns the number of departments
     *
     * @return number of departments
     */
    long getNumberOfDepartments();

    /**
     * Get all department names for the given persons as a map.
     *
     * @param persons
     * @return a map of personId mapped to department names
     */
    Map<PersonId, List<String>> getDepartmentNamesByMembers(List<Person> persons);

    /**
     * Checks whether two persons are in the same department or not or one person of both is
     * {@linkplain org.synyx.urlaubsverwaltung.person.Role#DEPARTMENT_HEAD} or {@linkplain org.synyx.urlaubsverwaltung.person.Role#SECOND_STAGE_AUTHORITY}
     * of the other person.
     *
     * @param person      a person
     * @param otherPerson another person
     * @return {@code true} when the persons have a department match, {@code false} otherwise
     */
    boolean hasDepartmentMatch(Person person, Person otherPerson);
}
