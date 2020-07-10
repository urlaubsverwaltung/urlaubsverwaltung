package org.synyx.urlaubsverwaltung.application.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.synyx.urlaubsverwaltung.application.domain.VacationType;


public interface VacationTypeRepository extends JpaRepository<VacationType, Integer> {
}
