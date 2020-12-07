package org.synyx.urlaubsverwaltung.absence;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

interface AbsenceMappingRepository extends CrudRepository<AbsenceMapping, Integer> {

    Optional<AbsenceMapping> findAbsenceMappingByAbsenceIdAndAbsenceType(Integer id, AbsenceType absenceType);
}
