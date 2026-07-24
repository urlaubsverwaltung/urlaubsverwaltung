package org.synyx.urlaubsverwaltung.person;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
interface PersonActivePeriodRepository extends JpaRepository<PersonActivePeriodEntity, Long> {

    List<PersonActivePeriodEntity> findAllByPersonIdOrderByValidFromAsc(Long personId);

    Optional<PersonActivePeriodEntity> findByPersonIdAndValidToIsNull(Long personId);

    @Query("SELECT p FROM person_active_period p " +
        "WHERE p.personId IN :personIds " +
        "AND p.validFrom < :to " +
        "AND (p.validTo IS NULL OR p.validTo > :from)")
    List<PersonActivePeriodEntity> findAllByPersonIdIsInAndOverlapping(Collection<Long> personIds, Instant from, Instant to);
}
