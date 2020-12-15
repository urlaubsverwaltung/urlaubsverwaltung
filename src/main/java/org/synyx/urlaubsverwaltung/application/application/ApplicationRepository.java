package org.synyx.urlaubsverwaltung.application.application;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.synyx.urlaubsverwaltung.person.Person;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository for {@link Application} entities.
 */
interface ApplicationRepository extends CrudRepository<Application, Integer> {

    List<Application> findByStatusIn(List<ApplicationStatus> statuses);

    List<Application> findByStatusInAndStartDateBetweenAndUpcomingApplicationsReminderSendIsNull(List<ApplicationStatus> statuses, LocalDate from, LocalDate to);

    List<Application> findByStatusInAndStartDateBetweenAndHolidayReplacementsIsNotEmptyAndUpcomingHolidayReplacementNotificationSendIsNull(List<ApplicationStatus> statuses, LocalDate from, LocalDate to);

    List<Application> findByStatusInAndEndDateGreaterThanEqual(List<ApplicationStatus> statuses, LocalDate since);

    List<Application> findByStatusInAndPersonIn(List<ApplicationStatus> statuses, List<Person> persons);

    List<Application> findByStatusInAndPersonInAndEndDateIsGreaterThanEqual(List<ApplicationStatus> statuses, List<Person> persons, LocalDate sinceStartDate);

    List<Application> findByStatusInAndPersonInAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqual(List<ApplicationStatus> statuses, List<Person> persons, LocalDate start, LocalDate end);

    @Query("""
            SELECT x FROM Application x
            WHERE x.status = ?3
                AND ((x.startDate between ?1 and ?2) or (x.endDate between ?1 and ?2) or (x.startDate < ?1 and x.endDate > ?2))
            ORDER BY x.startDate
            """)
    List<Application> getApplicationsForACertainTimeAndState(LocalDate startDate, LocalDate endDate, ApplicationStatus status);

    @Query("""
            SELECT x FROM Application x
            WHERE x.person = ?3
                AND ((x.startDate between ?1 and ?2) or (x.endDate between ?1 and ?2) or (x.startDate < ?1 and x.endDate > ?2))
            ORDER BY x.startDate
            """)
    List<Application> getApplicationsForACertainTimeAndPerson(LocalDate startDate, LocalDate endDate, Person person);

    @Query("""
            SELECT x FROM Application x
            WHERE x.person = ?3
                AND x.status = ?4
                AND ((x.startDate between ?1 and ?2) or (x.endDate between ?1 and ?2) or (x.startDate < ?1 and x.endDate > ?2))
            ORDER BY x.startDate
            """)
    List<Application> getApplicationsForACertainTimeAndPersonAndState(LocalDate startDate, LocalDate endDate, Person person, ApplicationStatus status);

    @Query("""
            SELECT SUM(application.hours) FROM Application application
            WHERE application.person = :person
                AND application.vacationType.category = 'OVERTIME'
                AND (application.status = 'WAITING' OR application.status = 'ALLOWED')
            """)
    BigDecimal calculateTotalOvertimeReductionOfPerson(@Param("person") Person person);

    List<Application> findByHolidayReplacements_PersonAndEndDateIsGreaterThanEqualAndStatusIn(Person person, LocalDate date, List<ApplicationStatus> status);
}
