package org.synyx.urlaubsverwaltung.core.application.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.core.person.Person;

import java.math.BigDecimal;

import java.util.Date;
import java.util.List;


/**
 * Repository for {@link Application} entities.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public interface ApplicationDAO extends JpaRepository<Application, Integer> {

    @Query("select x from Application x where x.status = ?1")
    List<Application> getApplicationsForACertainState(ApplicationStatus status);


    @Query(
        "select x from Application x "
        + "where x.status = ?3 and ((x.startDate between ?1 and ?2) or (x.endDate between ?1 and ?2) "
        + "or (x.startDate < ?1 and x.endDate > ?2)) "
        + "order by x.startDate"
    )
    List<Application> getApplicationsForACertainTimeAndState(Date startDate, Date endDate, ApplicationStatus status);


    @Query(
        "select x from Application x "
        + "where x.person = ?3 and ((x.startDate between ?1 and ?2) or (x.endDate between ?1 and ?2) "
        + "or (x.startDate < ?1 and x.endDate > ?2)) "
        + "order by x.startDate"
    )
    List<Application> getApplicationsForACertainTimeAndPerson(Date startDate, Date endDate, Person person);


    @Query(
        "select x from Application x "
        + "where x.person = ?3 and x.status = ?4 and ((x.startDate between ?1 and ?2) or (x.endDate between ?1 and ?2) "
        + "or (x.startDate < ?1 and x.endDate > ?2)) "
        + "order by x.startDate"
    )
    List<Application> getApplicationsForACertainTimeAndPersonAndState(Date startDate, Date endDate, Person person,
        ApplicationStatus status);


    @Query(
        "SELECT SUM(application.hours) FROM Application application WHERE application.person = :person "
        + "AND application.vacationType = 'OVERTIME' "
        + "AND (application.status = 'WAITING' OR application.status = 'ALLOWED')"
    )
    BigDecimal calculateTotalOvertimeOfPerson(@Param("person") Person person);
}
