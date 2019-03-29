package org.synyx.urlaubsverwaltung.calendarintegration.absence;

import org.springframework.data.repository.CrudRepository;


public interface AbsenceMappingDAO extends CrudRepository<AbsenceMapping, Integer> {

    AbsenceMapping findAbsenceMappingByAbsenceIdAndAbsenceType(Integer id, AbsenceType absenceType);
}
