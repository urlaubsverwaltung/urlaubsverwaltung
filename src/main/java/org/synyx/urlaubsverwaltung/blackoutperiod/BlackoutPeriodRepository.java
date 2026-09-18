package org.synyx.urlaubsverwaltung.blackoutperiod;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for {@link BlackoutPeriodEntity} entities.
 */
interface BlackoutPeriodRepository extends JpaRepository<BlackoutPeriodEntity, Long> {
}
