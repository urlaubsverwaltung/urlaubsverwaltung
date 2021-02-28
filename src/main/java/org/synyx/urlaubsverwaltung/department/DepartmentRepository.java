package org.synyx.urlaubsverwaltung.department;

import org.springframework.data.jpa.repository.JpaRepository;
import org.synyx.urlaubsverwaltung.person.Person;

import java.util.List;


/**
 * Repository for {@link DepartmentEntity} entities.
 */
interface DepartmentRepository extends JpaRepository<DepartmentEntity, Integer> {

    List<DepartmentEntity> findByDepartmentHeads(Person person);

    List<DepartmentEntity> findBySecondStageAuthorities(Person person);

    List<DepartmentEntity> findByMembersPerson(Person person);
}
