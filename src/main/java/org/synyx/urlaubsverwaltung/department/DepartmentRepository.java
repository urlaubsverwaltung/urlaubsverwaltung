package org.synyx.urlaubsverwaltung.department;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.synyx.urlaubsverwaltung.person.Person;

import java.util.List;


/**
 * Repository for {@link Department} entities.
 */
public interface DepartmentRepository extends JpaRepository<Department, Integer> {

    @Query("SELECT d FROM Department d, in (d.departmentHeads) person WHERE person = ?1")
    List<Department> getManagedDepartments(Person person);


    @Query("SELECT d FROM Department d, in (d.secondStageAuthorities) person WHERE person = ?1")
    List<Department> getDepartmentsForSecondStageAuthority(Person person);


    @Query("SELECT d FROM Department d, in (d.members) person WHERE person = ?1")
    List<Department> getAssignedDepartments(Person person);
}
