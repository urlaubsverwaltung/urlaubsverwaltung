package org.synyx.urlaubsverwaltung.absence;

import org.springframework.data.repository.CrudRepository;


interface AbsenceMappingRepository extends CrudRepository<AbsenceMapping, Integer> {

    AbsenceMapping findAbsenceMappingByAbsenceIdAndAbsenceType(Integer id, AbsenceType absenceType);
}
