package org.synyx.urlaubsverwaltung.calendarintegration;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

interface AbsenceMappingRepository extends CrudRepository<AbsenceMapping, Long> {

    Optional<AbsenceMapping> findAbsenceMappingByAbsenceIdAndAbsenceMappingType(Long id, AbsenceMappingType absenceType);

    @Modifying
    void deleteByEventId(String eventId);
}
