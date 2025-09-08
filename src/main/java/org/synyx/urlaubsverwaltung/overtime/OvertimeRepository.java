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
interface OvertimeRepository extends CrudRepository<OvertimeEntity, Long> {

    List<OvertimeEntity> findByPerson(Person person);

    @Query("""
        SELECT SUM(overtime.duration)
        FROM overtime overtime
        WHERE overtime.person = :person
        """)
    Optional<Double> calculateTotalHoursForPerson(@Param("person") Person person);

    List<OvertimeEntity> findByPersonAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqual(Person person, LocalDate start, LocalDate end);

    List<OvertimeEntity> findByPersonIsInAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqual(Collection<Person> persons, LocalDate start, LocalDate end);

    List<OvertimeEntity> findByPersonIsInAndStartDateIsLessThanEqual(Collection<Person> persons, LocalDate until);

    List<OvertimeEntity> findByPersonAndStartDateIsBefore(Person person, LocalDate before);

    List<OvertimeEntity> findAllByPersonId(Long personId);

    Optional<OvertimeEntity> findByPersonIdAndStartDateAndEndDateAndExternalIsTrue(Long personId, LocalDate start, LocalDate end);

    @Modifying
    void deleteByPerson(Person personId);
}
