package org.synyx.urlaubsverwaltung.blackoutperiod;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for {@link BlackoutPeriodEntity} entities.
 */
interface BlackoutPeriodRepository extends JpaRepository<BlackoutPeriodEntity, Long> {

    /**
     * Finds all blackout periods overlapping the given date range, together with their department and vacation type
     * ids in the same statement.
     *
     * @param from start of the date range, inclusive
     * @param to   end of the date range, inclusive
     * @return the overlapping blackout periods, in no particular order
     */
    @EntityGraph(attributePaths = {"departmentIds", "vacationTypeIds"})
    @Query("select b from blackout_period b where b.startDate <= :to and b.endDate >= :from")
    List<BlackoutPeriodEntity> findOverlapping(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
