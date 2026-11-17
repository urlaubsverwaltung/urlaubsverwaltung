package org.synyx.urlaubsverwaltung.calendarintegration;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

interface AbsenceMappingRepository extends CrudRepository<AbsenceMapping, Long> {

    Optional<AbsenceMapping> findAbsenceMappingByAbsenceIdAndAbsenceMappingType(Long id, AbsenceMappingType absenceType);

    List<AbsenceMapping> findAllByOrderByIdAsc();

    // the calendar sync deletes from async event listeners, which have no transaction of their own
    @Modifying
    @Transactional
    void deleteByEventId(String eventId);
}
