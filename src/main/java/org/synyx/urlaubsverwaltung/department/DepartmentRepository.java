package org.synyx.urlaubsverwaltung.department;

import org.springframework.data.jpa.repository.JpaRepository;
import org.synyx.urlaubsverwaltung.person.Person;

import java.util.List;
import java.util.Optional;


/**
 * Repository for {@link DepartmentEntity} entities.
 */
interface DepartmentRepository extends JpaRepository<DepartmentEntity, Long> {

    List<DepartmentEntity> findByDepartmentHeadsOrSecondStageAuthorities(Person departmentHead, Person secondStageAuthority);

    List<DepartmentEntity> findByDepartmentHeads(Person person);

    List<DepartmentEntity> findBySecondStageAuthorities(Person person);

    List<DepartmentEntity> findByMembersPerson(Person person);

    List<DepartmentEntity> findDistinctByMembersPersonIn(List<Person> person);

    Optional<DepartmentEntity> findFirstByName(String departmentName);
}
