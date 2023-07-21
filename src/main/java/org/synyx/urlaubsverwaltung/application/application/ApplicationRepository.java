package org.synyx.urlaubsverwaltung.application.application;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory;
import org.synyx.urlaubsverwaltung.person.Person;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
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

    List<Application> findByPersonInAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqualAndStatusIn(List<Person> persons, LocalDate start, LocalDate end, List<ApplicationStatus> statuses);

    @Query(
        "select x from Application x "
            + "where x.status = ?3 "
            + "and ((x.startDate between ?1 and ?2) or (x.endDate between ?1 and ?2) or (x.startDate < ?1 and x.endDate > ?2)) "
            + "order by x.startDate"
    )
    List<Application> getApplicationsForACertainTimeAndState(LocalDate startDate, LocalDate endDate, ApplicationStatus status);

    @Query(
        "select x from Application x "
            + "where x.person = ?3 "
            + "and ((x.startDate between ?1 and ?2) or (x.endDate between ?1 and ?2) or (x.startDate < ?1 and x.endDate > ?2)) "
            + "order by x.startDate"
    )
    List<Application> getApplicationsForACertainTimeAndPerson(LocalDate startDate, LocalDate endDate, Person person);

    List<Application> findByStatusInAndPersonAndStartDateBetweenAndVacationTypeCategory(List<ApplicationStatus> statuses, Person person, LocalDate start, LocalDate end, VacationCategory vacationCategory);

    List<Application> findByStatusInAndPersonAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqualAndVacationTypeCategory(List<ApplicationStatus> statuses, Person person, LocalDate start, LocalDate end, VacationCategory vacationCategory);

    @Query(
        "select x from Application x "
            + "where x.person = ?3 "
            + "and x.status = ?4 "
            + "and ((x.startDate between ?1 and ?2) or (x.endDate between ?1 and ?2) or (x.startDate < ?1 and x.endDate > ?2)) "
            + "order by x.startDate"
    )
    List<Application> getApplicationsForACertainTimeAndPersonAndState(LocalDate startDate, LocalDate endDate, Person person, ApplicationStatus status);

    @Query(
        "SELECT SUM(application.hours) FROM Application application WHERE application.person = :person "
            + "AND application.vacationType.category = 'OVERTIME' "
            + "AND (application.status = 'WAITING' OR application.status = 'TEMPORARY_ALLOWED' OR application.status = 'ALLOWED' OR application.status = 'ALLOWED_CANCELLATION_REQUESTED')"
    )
    BigDecimal calculateTotalOvertimeReductionOfPerson(@Param("person") Person person);

    List<Application> findByPersonInAndVacationTypeCategoryAndStatusInAndStartDateIsLessThanEqual(
        Collection<Person> persons, VacationCategory category, List<ApplicationStatus> statuses, LocalDate until);

    @Query(
        "SELECT SUM(application.hours) FROM Application application WHERE application.person = :person "
            + "AND application.startDate < :date "
            + "AND application.vacationType.category = 'OVERTIME' "
            + "AND (application.status = 'WAITING' OR application.status = 'TEMPORARY_ALLOWED' OR application.status = 'ALLOWED' OR application.status = 'ALLOWED_CANCELLATION_REQUESTED')"
    )
    BigDecimal calculateTotalOvertimeReductionOfPersonBefore(@Param("person") Person person, @Param("date") LocalDate before);

    List<Application> findByHolidayReplacements_PersonAndEndDateIsGreaterThanEqualAndStatusIn(Person person, LocalDate date, List<ApplicationStatus> status);

    List<Application> findByBoss(Person person);

    List<Application> findByCanceller(Person person);

    List<Application> findByApplier(Person person);

    @Modifying
    List<Application> deleteByPerson(Person person);

    List<Application> findAllByHolidayReplacements_Person(Person person);
}
