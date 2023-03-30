package org.synyx.urlaubsverwaltung.overtime;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Allows access to overtime records.
 *
 * @since 2.11.0
 */
interface OvertimeRepository extends CrudRepository<Overtime, Integer> {

    List<Overtime> findByPerson(Person person);

    @Query("SELECT SUM(overtime.duration) FROM Overtime overtime WHERE overtime.person = :person")
    Optional<Double> calculateTotalHoursForPerson(@Param("person") Person person);

    @Query("SELECT o.person as person, SUM(o.duration) as durationDouble FROM Overtime o WHERE o.person IN :persons GROUP BY o.person")
    List<OvertimeDurationSum> calculateTotalHoursForPersons(@Param("persons") Collection<Person> persons);

    List<Overtime> findByPersonAndStartDateBetweenOrderByStartDateDesc(Person person, LocalDate start, LocalDate end);

    List<Overtime> findByPersonIsInAndStartDateBetweenOrderByStartDateDesc(Collection<Person> person, LocalDate start, LocalDate end);

    List<Overtime> findByPersonAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqual(Person person, LocalDate start, LocalDate end);

    List<Overtime> findByPersonIsInAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqual(Collection<Person> persons, LocalDate start, LocalDate end);

    List<Overtime> findByPersonIsInAndStartDateIsLessThanEqual(Collection<Person> persons, LocalDate until);

    List<Overtime> findByPersonAndStartDateIsBefore(Person person, LocalDate before);

    @Modifying
    void deleteByPerson(Person personId);
}
