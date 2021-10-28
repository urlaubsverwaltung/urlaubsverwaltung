package org.synyx.urlaubsverwaltung.calendarintegration;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

interface AbsenceMappingRepository extends CrudRepository<AbsenceMapping, Integer> {

    Optional<AbsenceMapping> findAbsenceMappingByAbsenceIdAndAbsenceMappingType(Integer id, AbsenceMappingType absenceType);
}
