package org.synyx.urlaubsverwaltung.application.dao;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.person.Person;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


/**
 * Repository for {@link Application} entities.
 */
public interface ApplicationRepository extends CrudRepository<Application, Integer> {

    List<Application> findByStatusIn(List<ApplicationStatus> statuses);

    List<Application> findByStatusInAndStartDate(List<ApplicationStatus> statuses, LocalDate startDate);

    List<Application> findByStatusInAndEndDateGreaterThanEqual(List<ApplicationStatus> statuses, LocalDate since);

    List<Application> findByStatusInAndPersonIn(List<ApplicationStatus> statuses, List<Person> persons);

    List<Application> findByStatusInAndPersonInAndEndDateIsGreaterThanEqual(List<ApplicationStatus> statuses, List<Person> persons, LocalDate sinceStartDate);

    @Query(
        "select x from Application x "
            + "where x.status = ?3 and ((x.startDate between ?1 and ?2) or (x.endDate between ?1 and ?2) "
            + "or (x.startDate < ?1 and x.endDate > ?2)) "
            + "order by x.startDate"
    )
    List<Application> getApplicationsForACertainTimeAndState(LocalDate startDate, LocalDate endDate, ApplicationStatus status);


    @Query(
        "select x from Application x "
            + "where x.person = ?3 and ((x.startDate between ?1 and ?2) or (x.endDate between ?1 and ?2) "
            + "or (x.startDate < ?1 and x.endDate > ?2)) "
            + "order by x.startDate"
    )
    List<Application> getApplicationsForACertainTimeAndPerson(LocalDate startDate, LocalDate endDate, Person person);


    @Query(
        "select x from Application x "
            + "where x.person = ?3 and x.status = ?4 and ((x.startDate between ?1 and ?2) or (x.endDate between ?1 and ?2) "
            + "or (x.startDate < ?1 and x.endDate > ?2)) "
            + "order by x.startDate"
    )
    List<Application> getApplicationsForACertainTimeAndPersonAndState(LocalDate startDate, LocalDate endDate, Person person,
                                                                      ApplicationStatus status);


    @Query(
        "SELECT SUM(application.hours) FROM Application application WHERE application.person = :person "
            + "AND application.vacationType.category = 'OVERTIME' "
            + "AND (application.status = 'WAITING' OR application.status = 'ALLOWED')"
    )
    BigDecimal calculateTotalOvertimeReductionOfPerson(@Param("person") Person person);

    List<Application> findByHolidayReplacementAndEndDateIsGreaterThanEqualAndStatusIn(Person holidayReplacement, LocalDate date, List<ApplicationStatus> status);
}
