package org.synyx.urlaubsverwaltung.core.sync.absence;

import org.springframework.data.repository.CrudRepository;


/**
 * Daniel Hammann - <hammann@synyx.de>.
 */
public interface AbsenceMappingDAO extends CrudRepository<AbsenceMapping, Integer> {

    AbsenceMapping findAbsenceMappingByAbsenceIdAndAbsenceType(Integer id, AbsenceType absenceType);
}
