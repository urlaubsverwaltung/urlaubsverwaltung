package org.synyx.urlaubsverwaltung.sicknote;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for {@link SickNote} entities.
 */
interface SickNoteRepository extends CrudRepository<SickNote, Integer> {

    @Query(
        "SELECT x FROM SickNote x WHERE x.person = ?1 AND "
            + "((x.startDate BETWEEN ?2 AND ?3) OR (x.endDate BETWEEN ?2 AND ?3) "
            + "OR (x.startDate < ?2 and x.endDate > ?3)) "
            + "ORDER BY x.startDate"
    )
    List<SickNote> findByPersonAndPeriod(Person person, LocalDate startDate, LocalDate endDate);

    @Query(
        "SELECT x FROM SickNote x WHERE ((x.startDate BETWEEN ?1 AND ?2) OR (x.endDate BETWEEN ?1 AND ?2) "
            + "OR (x.startDate < ?1 and x.endDate > ?2)) "
            + "ORDER BY x.startDate"
    )
    List<SickNote> findByPeriod(LocalDate startDate, LocalDate endDate);

    // NOTE: Following methods are to create statistic
    @Query("SELECT x FROM SickNote x WHERE (YEAR(x.startDate) = ?1 OR YEAR(x.endDate) = ?1) AND x.status = 'ACTIVE'")
    List<SickNote> findAllActiveByYear(int year);

    @Query("SELECT COUNT(DISTINCT x.person) FROM SickNote x WHERE YEAR(x.startDate) = ?1 OR YEAR(x.endDate) = ?1 AND x.status = 'ACTIVE'")
    Long findNumberOfPersonsWithMinimumOneSickNote(int year);

    // NOTE: Only needed to send email after certain duration of a sick note
    @Query(value = "SELECT x " +
        "FROM SickNote x " +
        "WHERE DATEDIFF(x.endDate, x.startDate) > ?1 " +
        "AND x.endDate <= ?2 " +
        "AND x.status = 'ACTIVE' " +
        "AND (x.endOfSickPayNotificationSend IS NULL OR x.lastEdited > x.endOfSickPayNotificationSend)"
    )
    List<SickNote> findSickNotesToNotifyForSickPayEnd(int limit, LocalDate endDate);

    List<SickNote> findByStatusIn(List<SickNoteStatus> openSickNoteStatuses);

    List<SickNote> findByStatusInAndPersonInAndEndDateIsGreaterThanEqual(List<SickNoteStatus> openSickNoteStatuses,
                                                                         List<Person> persons, LocalDate sinceStartDate);

    List<SickNote> findByStatusInAndPersonInAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqual(List<SickNoteStatus> sickNoteStatus, List<Person> persons, LocalDate startDate, LocalDate endDate);
}
