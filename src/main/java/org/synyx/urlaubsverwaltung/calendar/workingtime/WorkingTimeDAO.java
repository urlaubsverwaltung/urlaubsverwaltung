package org.synyx.urlaubsverwaltung.calendar.workingtime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.synyx.urlaubsverwaltung.person.Person;


/**
 * Repository for accessing {@link WorkingTime} entities.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public interface WorkingTimeDAO extends JpaRepository<WorkingTime, Integer> {

    @Query("SELECT x FROM WorkingTime x WHERE x.person = ?1")
    WorkingTime findByPerson(Person person);
}
