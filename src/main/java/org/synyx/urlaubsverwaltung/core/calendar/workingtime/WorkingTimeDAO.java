package org.synyx.urlaubsverwaltung.core.calendar.workingtime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.synyx.urlaubsverwaltung.core.person.Person;

import java.util.Date;
import java.util.List;


/**
 * Repository for accessing {@link WorkingTime} entities.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public interface WorkingTimeDAO extends JpaRepository<WorkingTime, Integer> {

    @Query("SELECT x FROM WorkingTime x WHERE x.person = ?1 ORDER BY x.validFrom")
    List<WorkingTime> findByPerson(Person person);


    @Query("SELECT x FROM WorkingTime x WHERE x.person = ?1 AND x.validFrom = ?2")
    WorkingTime findByPersonAndValidityDate(Person person, Date date);


    @Query(
        "SELECT x FROM WorkingTime x WHERE x.person = ?1 AND x.validFrom = (SELECT MAX(w.validFrom) from WorkingTime w WHERE w.person = ?1 AND w.validFrom <= ?2)"
    )
    WorkingTime findByPersonAndValidityDateEqualsOrMinorDate(Person person, Date date);


    @Query(
        "SELECT x FROM WorkingTime x WHERE x.person = ?1 AND x.validFrom = (SELECT MAX(w.validFrom) from WorkingTime w WHERE w.person = ?1)"
    )
    WorkingTime findLastOneByPerson(Person person);
}
