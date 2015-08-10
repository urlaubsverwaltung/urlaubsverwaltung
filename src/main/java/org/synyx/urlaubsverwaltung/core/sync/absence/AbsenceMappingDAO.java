package org.synyx.urlaubsverwaltung.core.sync.absence;

import org.springframework.data.jpa.repository.JpaRepository;


/**
 * Daniel Hammann - <hammann@synyx.de>.
 */
public interface AbsenceMappingDAO extends JpaRepository<AbsenceMapping, Integer> {

    AbsenceMapping findAbsenceMappingByAbsenceIdAndAbsenceType(Integer id, AbsenceType absenceType);
}
