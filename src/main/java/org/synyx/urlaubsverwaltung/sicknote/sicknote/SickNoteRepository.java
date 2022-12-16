package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.Role;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for {@link SickNoteEntity} entities.
 */
interface SickNoteRepository extends CrudRepository<SickNoteEntity, Integer> {

    @Query(
        "SELECT x FROM SickNoteEntity x WHERE x.person = ?1 AND "
            + "((x.startDate BETWEEN ?2 AND ?3) OR (x.endDate BETWEEN ?2 AND ?3) "
            + "OR (x.startDate < ?2 and x.endDate > ?3)) "
            + "ORDER BY x.startDate"
    )
    List<SickNoteEntity> findByPersonAndPeriod(Person person, LocalDate startDate, LocalDate endDate);

    @Query("SELECT COUNT(DISTINCT x.person) FROM SickNoteEntity x WHERE YEAR(x.startDate) = ?1 OR YEAR(x.endDate) = ?1 AND x.status = 'ACTIVE'")
    Long findNumberOfPersonsWithMinimumOneSickNote(int year);

    // NOTE: Only needed to send email after certain duration of a sick note
    @Query("SELECT x " +
        "FROM SickNoteEntity x " +
        "WHERE DATEDIFF(x.endDate, x.startDate) + 1 > ?1 " +
        "AND ?3 >= ADDDATE(x.startDate, (?1 - ?2 - 1)) " +
        "AND x.status = 'ACTIVE' " +
        "AND (x.endOfSickPayNotificationSend IS NULL OR x.lastEdited > x.endOfSickPayNotificationSend)"
    )
    List<SickNoteEntity> findSickNotesToNotifyForSickPayEnd(int maximumSickPayDays, int daysBeforeEndOfSickPayNotification, LocalDate today);

    List<SickNoteEntity> findByStatusInAndEndDateGreaterThanEqual(List<SickNoteStatus> openSickNoteStatuses, LocalDate since);

    List<SickNoteEntity> findByStatusInAndPersonInAndEndDateIsGreaterThanEqual(List<SickNoteStatus> openSickNoteStatuses, List<Person> persons, LocalDate sinceStartDate);

    List<SickNoteEntity> findByPersonPermissionsIsInAndStatusInAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqual(List<Role> roles, List<SickNoteStatus> sickNoteStatus, LocalDate startDate, LocalDate endDate);

    List<SickNoteEntity> findByStatusInAndPersonInAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqual(List<SickNoteStatus> sickNoteStatus, List<Person> persons, LocalDate startDate, LocalDate endDate);

    List<SickNoteEntity> findByStatusInAndPersonInAndPersonPermissionsInAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqual(List<SickNoteStatus> sickNoteStatus, List<Person> persons, List<Role> roles, LocalDate startDate, LocalDate endDate);

    @Modifying
    List<SickNoteEntity> deleteByPerson(Person person);

    List<SickNoteEntity> findByApplier(Person applier);
}
