package org.synyx.urlaubsverwaltung.department;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


/**
 * Repository for {@link DepartmentEntity} entities.
 */
interface DepartmentRepository extends JpaRepository<DepartmentEntity, Long> {

    Optional<DepartmentEntity> findFirstByName(String departmentName);
}
