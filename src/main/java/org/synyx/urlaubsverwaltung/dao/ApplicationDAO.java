package org.synyx.urlaubsverwaltung.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.synyx.urlaubsverwaltung.domain.Application;
import org.synyx.urlaubsverwaltung.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.domain.DayLength;
import org.synyx.urlaubsverwaltung.domain.Person;

import java.util.Date;
import java.util.List;


/**
 * @author  Johannes Reuter
 * @author  Aljona Murygina
 */
public interface ApplicationDAO extends JpaRepository<Application, Integer> {

    // get List<Application> by certain state (e.g. waiting)
    @Query("select x from Application x where x.status = ?1 order by x.startDate")
    List<Application> getApplicationsByState(ApplicationStatus state);


    // get List<Application> by certain person
    @Query("select x from Application x where x.person = ?1 order by x.startDate")
    List<Application> getApplicationsByPerson(Person person);


    // get List<Application> for a certain time (between startDate and endDate)
    @Query(
        "select x from Application x where (x.startDate between ?1 and ?2) or (x.endDate between ?1 and ?2) or (x.startDate < ?1 and x.endDate > ?2) order by x.startDate"
    )
    List<Application> getApplicationsForACertainTime(Date startDate, Date endDate);


    // get List<Application> by certain person for a certain year
    @Query(
        "select x from Application x where x.person = ?1 and ((x.startDate between ?2 and ?3) or (x.endDate between ?2 and ?3)) order by x.startDate"
    )
    List<Application> getApplicationsByPersonAndYear(Person person, Date firstDayOfYear, Date lastDayOfYear);


    // get List<Application> by certain person for a certain year, get only the not cancelled applications
    @Query(
        "select x from Application x where x.status != ?1 and x.person = ?2 and ((x.startDate between ?3 and ?4) or (x.endDate between ?3 and ?4)) order by x.startDate"
    )
    List<Application> getNotCancelledApplicationsByPersonAndYear(ApplicationStatus state, Person person,
        Date firstDayOfYear, Date lastDayOfYear);


    // get List<Application> for a certain time (between startDate and endDate)for the given person and the given day
    // length, get only the not cancelled applications!
    @Query(
        "select x from Application x where ((x.startDate between ?1 and ?2) or (x.endDate between ?1 and ?2) or (x.startDate < ?1 and x.endDate > ?2)) "
        + "and x.person = ?3 and x.status != 3 and x.howLong = ?4 order by x.startDate"
    )
    List<Application> getApplicationsByPeriodAndDayLength(Date startDate, Date endDate, Person person,
        DayLength length);


    // get List<Application> for a certain time (between startDate and endDate)for the given person and the given day
    // length, get only the not cancelled applications!
    @Query(
        "select x from Application x where ((x.startDate between ?1 and ?2) or (x.endDate between ?1 and ?2) or (x.startDate < ?1 and x.endDate > ?2)) "
        + "and x.person = ?3 and x.status != 3 order by x.startDate"
    )
    List<Application> getApplicationsByPeriodForEveryDayLength(Date startDate, Date endDate, Person person);
}
