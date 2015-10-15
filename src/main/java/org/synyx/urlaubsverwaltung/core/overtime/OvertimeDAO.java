package org.synyx.urlaubsverwaltung.core.overtime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.synyx.urlaubsverwaltung.core.person.Person;

import java.math.BigDecimal;

import java.util.List;


/**
 * Allows access to overtime records.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 * @since  2.11.0
 */
public interface OvertimeDAO extends JpaRepository<Overtime, Integer> {

    List<Overtime> findByPerson(Person person);


    @Query("SELECT SUM(overtime.hours) FROM Overtime overtime WHERE overtime.person = ?1")
    BigDecimal calculateTotalHoursForPerson(Person person);
}
