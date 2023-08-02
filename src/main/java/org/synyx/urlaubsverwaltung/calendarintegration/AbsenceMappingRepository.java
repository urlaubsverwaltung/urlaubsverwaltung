package org.synyx.urlaubsverwaltung.calendarintegration;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

@Deprecated(since = "4.26.0", forRemoval = true)
interface AbsenceMappingRepository extends CrudRepository<AbsenceMapping, Long> {

    Optional<AbsenceMapping> findAbsenceMappingByAbsenceIdAndAbsenceMappingType(Long id, AbsenceMappingType absenceType);
}
