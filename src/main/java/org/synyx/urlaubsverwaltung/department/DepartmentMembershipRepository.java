package org.synyx.urlaubsverwaltung.department;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
interface DepartmentMembershipRepository extends JpaRepository<DepartmentMembershipEntity, Long> {

    List<DepartmentMembershipEntity> findAllByDepartmentId(Long departmentId);

    List<DepartmentMembershipEntity> findAllByDepartmentIdIsInAndValidToIsNull(Collection<Long> departmentId);
}
