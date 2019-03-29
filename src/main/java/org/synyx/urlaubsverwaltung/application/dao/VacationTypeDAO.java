package org.synyx.urlaubsverwaltung.application.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.synyx.urlaubsverwaltung.application.domain.VacationType;


public interface VacationTypeDAO extends JpaRepository<VacationType, Integer> {
}
