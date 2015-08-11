package org.synyx.urlaubsverwaltung.core.department;

import org.springframework.data.jpa.repository.JpaRepository;


/**
 * Repository for {@link Department} entities.
 *
 * @author  Daniel Hammann - <hammann@synyx.de>
 */
public interface DepartmentDAO extends JpaRepository<Department, Integer> {
}
