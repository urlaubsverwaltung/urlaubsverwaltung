package org.synyx.urlaubsverwaltung.department;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
interface DepartmentMembershipRepository extends JpaRepository<DepartmentMembershipEntity, Long> {

    List<DepartmentMembershipEntity> findAllByDepartmentId(Long departmentId);

    List<DepartmentMembershipEntity> findAllByDepartmentIdIsInAndValidToIsNull(Collection<Long> departmentIds);

    List<DepartmentMembershipEntity> findAllByPersonIdIsInAndValidToIsNull(Collection<Long> personIds);

    @Query("SELECT d from department_membership d " +
        "WHERE " +
        "  (     d.validTo IS NULL   AND YEAR(d.validFrom) <= :year) OR" +
        "  (YEAR(d.validTo) >= :year AND YEAR(d.validFrom) <= :year)")
    List<DepartmentMembershipEntity> findAllActiveInYear(int year);
}
