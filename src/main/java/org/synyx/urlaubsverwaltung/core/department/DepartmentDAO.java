package org.synyx.urlaubsverwaltung.core.department;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


/**
 * Repository for {@link Department} entities.
 *
 * @author  Daniel Hammann - <hammann@synyx.de>
 */
public interface DepartmentDAO extends JpaRepository<Department, Integer> {

    @Query("SELECT d FROM Department d, in (d.departmentHeads) person WHERE person.id = ?1")
    List<Department> getManagedDepartments(Integer personId);
}
