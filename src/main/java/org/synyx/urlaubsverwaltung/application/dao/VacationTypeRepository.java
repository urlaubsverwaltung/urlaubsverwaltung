package org.synyx.urlaubsverwaltung.application.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.synyx.urlaubsverwaltung.application.domain.VacationType;

import java.util.List;

public interface VacationTypeRepository extends JpaRepository<VacationType, Integer> {

    List<VacationType> findByActiveIsTrue();
}
