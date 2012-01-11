package org.synyx.urlaubsverwaltung.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.synyx.urlaubsverwaltung.domain.Application;
import org.synyx.urlaubsverwaltung.domain.ApplicationStatus;
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
    @Query("select x from Application x where x.startDate >= ?1 and x.endDate <= ?2 order by x.startDate")
    List<Application> getApplicationsForACertainTime(Date startDate, Date endDate);


    // get List<Application> for a certain time (between startDate and endDate)
    @Query(
        "select x from Application x where (x.startDate >= ?1 and x.endDate <= ?2) or (x.startDate <= ?1 and x.endDate >= ?2) and x.person = ?3 order by x.startDate"
    )
    List<Application> getApplicationsByPersonForACertainTime(Date startDate, Date endDate, Person person);


    // get List<Application> by certain person
    @Query(
        "select x from Application x where x.person = ?1 and (x.startDate between ?2 and ?3) or (x.endDate between ?2 and ?3) order by x.startDate"
    )
    List<Application> getApplicationsByPersonAndYear(Person person, Date firstDayOfYear, Date lastDayOfYear);
}
