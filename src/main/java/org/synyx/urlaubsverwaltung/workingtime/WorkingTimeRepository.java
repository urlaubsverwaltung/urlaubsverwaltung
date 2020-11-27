package org.synyx.urlaubsverwaltung.workingtime;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.LocalDate;
import java.util.List;


/**
 * Repository for accessing {@link WorkingTime} entities.
 */
interface WorkingTimeRepository extends CrudRepository<WorkingTime, Integer> {

    List<WorkingTime> findByPersonOrderByValidFromDesc(Person person);

    @Query("SELECT x FROM WorkingTime x WHERE x.person = ?1 AND x.validFrom = ?2")
    WorkingTime findByPersonAndValidityDate(Person person, LocalDate date);

    @Query("SELECT x FROM WorkingTime x WHERE x.person IN (:persons)" +
        "  AND x.validFrom >= (SELECT MAX(w.validFrom) from WorkingTime w WHERE w.person IN (:persons) AND w.validFrom <= :start)" +
        "  AND x.validFrom <= :end")
    List<WorkingTime> findByPersonInAndValidFromForDateInterval(@Param("persons") List<Person> persons,
                                                                @Param("start") LocalDate start,
                                                                @Param("end") LocalDate end);

    @Query(
        "SELECT x FROM WorkingTime x WHERE x.person = ?1 "
            + "AND x.validFrom = (SELECT MAX(w.validFrom) from WorkingTime w WHERE w.person = ?1 AND w.validFrom <= ?2)"
    )
    WorkingTime findByPersonAndValidityDateEqualsOrMinorDate(Person person, LocalDate date);
}
