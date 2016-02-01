package org.synyx.urlaubsverwaltung.core.application.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import org.synyx.urlaubsverwaltung.core.application.domain.VacationType;


public interface VacationTypeDAO extends JpaRepository<VacationType, Integer> {
}
