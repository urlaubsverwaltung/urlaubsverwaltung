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
interface WorkingTimeRepository extends CrudRepository<WorkingTimeEntity, Integer> {

    List<WorkingTimeEntity> findByPersonOrderByValidFromDesc(Person person);

    @Query("SELECT x FROM working_time x WHERE x.person = ?1 AND x.validFrom = ?2")
    WorkingTimeEntity findByPersonAndValidityDate(Person person, LocalDate date);

    @Query("SELECT x FROM working_time x WHERE x.person IN (:persons)" +
        "  AND x.validFrom >= (SELECT MAX(w.validFrom) from working_time w WHERE w.person IN (:persons) AND w.validFrom <= :start)" +
        "  AND x.validFrom <= :end")
    List<WorkingTimeEntity> findByPersonInAndValidFromForDateInterval(@Param("persons") List<Person> persons,
                                                                @Param("start") LocalDate start,
                                                                @Param("end") LocalDate end);

    @Query(
        "SELECT x FROM working_time x WHERE x.person = ?1 "
            + "AND x.validFrom = (SELECT MAX(w.validFrom) from working_time w WHERE w.person = ?1 AND w.validFrom <= ?2)"
    )
    WorkingTimeEntity findByPersonAndValidityDateEqualsOrMinorDate(Person person, LocalDate date);
}
