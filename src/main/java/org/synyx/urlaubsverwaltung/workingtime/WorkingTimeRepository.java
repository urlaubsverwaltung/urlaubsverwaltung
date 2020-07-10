package org.synyx.urlaubsverwaltung.workingtime;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.LocalDate;
import java.util.List;


/**
 * Repository for accessing {@link WorkingTime} entities.
 */
interface WorkingTimeRepository extends CrudRepository<WorkingTime, Integer> {

    @Query("SELECT x FROM WorkingTime x WHERE x.person = ?1 ORDER BY x.validFrom")
    List<WorkingTime> findByPerson(Person person);

    @Query("SELECT x FROM WorkingTime x WHERE x.person = ?1 AND x.validFrom = ?2")
    WorkingTime findByPersonAndValidityDate(Person person, LocalDate date);

    @Query(
        "SELECT x FROM WorkingTime x WHERE x.person = ?1 "
            + "AND x.validFrom = (SELECT MAX(w.validFrom) from WorkingTime w WHERE w.person = ?1 AND w.validFrom <= ?2)"
    )
    WorkingTime findByPersonAndValidityDateEqualsOrMinorDate(Person person, LocalDate date);

    @Query(
        "SELECT x FROM WorkingTime x WHERE x.person = ?1 "
            + "AND x.validFrom = (SELECT MAX(w.validFrom) from WorkingTime w WHERE w.person = ?1)"
    )
    WorkingTime findLastOneByPerson(Person person);
}
