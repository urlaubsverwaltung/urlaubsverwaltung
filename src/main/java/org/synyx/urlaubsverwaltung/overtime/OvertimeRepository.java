package org.synyx.urlaubsverwaltung.overtime;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.synyx.urlaubsverwaltung.person.Person;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Allows access to overtime records.
 *
 * @since 2.11.0
 */
interface OvertimeRepository extends CrudRepository<Overtime, Integer> {

    List<Overtime> findByPerson(Person person);

    @Query("SELECT SUM(overtime.hours) FROM Overtime overtime WHERE overtime.person = :person")
    BigDecimal calculateTotalHoursForPerson(@Param("person") Person person);

    @Query(
        "SELECT overtime FROM Overtime overtime WHERE overtime.person = :person "
            + "AND ((overtime.startDate BETWEEN :start AND :end) "
            + "OR (overtime.endDate BETWEEN :start AND :end) "
            + "OR (overtime.startDate < :start and overtime.endDate > :end)) "
            + "ORDER BY overtime.startDate desc"
    )
    List<Overtime> findByPersonAndPeriod(@Param("person") Person person, @Param("start") LocalDate start, @Param("end") LocalDate end);
}
