package org.synyx.urlaubsverwaltung.calendar.workingtime;

import org.springframework.data.jpa.repository.JpaRepository;


/**
 * Repository for accessing {@link WorkingTime} entities.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public interface WorkingTimeDAO extends JpaRepository<WorkingTime, Integer> {
}
