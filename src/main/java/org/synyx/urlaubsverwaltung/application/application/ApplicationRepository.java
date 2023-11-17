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

interface ApplicationRepository extends CrudRepository<ApplicationEntity, Long> {

    List<ApplicationEntity> findByStatusIn(List<ApplicationStatus> statuses);

    List<ApplicationEntity> findByStatusInAndStartDateBetweenAndUpcomingApplicationsReminderSendIsNull(List<ApplicationStatus> statuses, LocalDate from, LocalDate to);

    List<ApplicationEntity> findByStatusInAndStartDateBetweenAndHolidayReplacementsIsNotEmptyAndUpcomingHolidayReplacementNotificationSendIsNull(List<ApplicationStatus> statuses, LocalDate from, LocalDate to);

    List<ApplicationEntity> findByStatusInAndEndDateGreaterThanEqual(List<ApplicationStatus> statuses, LocalDate since);

    List<ApplicationEntity> findByStatusInAndPersonIn(List<ApplicationStatus> statuses, List<Person> persons);

    List<ApplicationEntity> findByStatusInAndPersonInAndEndDateIsGreaterThanEqual(List<ApplicationStatus> statuses, List<Person> persons, LocalDate sinceStartDate);

    List<ApplicationEntity> findByStatusInAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqual(List<ApplicationStatus> statuses, LocalDate start, LocalDate end);

    List<ApplicationEntity> findByStatusInAndPersonInAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqual(List<ApplicationStatus> statuses, List<Person> persons, LocalDate start, LocalDate end);

    List<ApplicationEntity> findByPersonInAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqualAndStatusIn(List<Person> persons, LocalDate start, LocalDate end, List<ApplicationStatus> statuses);

    @Query(
        "select x from application x "
            + "where x.status = ?3 "
            + "and ((x.startDate between ?1 and ?2) or (x.endDate between ?1 and ?2) or (x.startDate < ?1 and x.endDate > ?2)) "
            + "order by x.startDate"
    )
    List<ApplicationEntity> getApplicationsForACertainTimeAndState(LocalDate startDate, LocalDate endDate, ApplicationStatus status);

    @Query(
        "select x from application x "
            + "where x.person = ?3 "
            + "and ((x.startDate between ?1 and ?2) or (x.endDate between ?1 and ?2) or (x.startDate < ?1 and x.endDate > ?2)) "
            + "order by x.startDate"
    )
    List<ApplicationEntity> getApplicationsForACertainTimeAndPerson(LocalDate startDate, LocalDate endDate, Person person);

    List<ApplicationEntity> findByStatusInAndPersonAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqualAndVacationTypeCategory(List<ApplicationStatus> statuses, Person person, LocalDate start, LocalDate end, VacationCategory vacationCategory);

    @Query(
        "SELECT SUM(a.hours) FROM application a WHERE a.person = :person "
            + "AND a.vacationType.category = 'OVERTIME' "
            + "AND (a.status = 'WAITING' OR a.status = 'TEMPORARY_ALLOWED' OR a.status = 'ALLOWED' OR a.status = 'ALLOWED_CANCELLATION_REQUESTED')"
    )
    BigDecimal calculateTotalOvertimeReductionOfPerson(@Param("person") Person person);

    List<ApplicationEntity> findByPersonInAndVacationTypeCategoryAndStatusInAndStartDateIsLessThanEqual(
        Collection<Person> persons, VacationCategory category, List<ApplicationStatus> statuses, LocalDate until);

    List<ApplicationEntity> findByHolidayReplacements_PersonAndEndDateIsGreaterThanEqualAndStatusIn(Person person, LocalDate date, List<ApplicationStatus> status);

    List<ApplicationEntity> findByBoss(Person person);

    List<ApplicationEntity> findByCanceller(Person person);

    List<ApplicationEntity> findByApplier(Person person);

    @Modifying
    List<ApplicationEntity> deleteByPerson(Person person);

    List<ApplicationEntity> findAllByHolidayReplacements_Person(Person person);
}
