package org.synyx.urlaubsverwaltung.dao;

import org.joda.time.DateMidnight;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.synyx.urlaubsverwaltung.domain.Application;
import org.synyx.urlaubsverwaltung.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.domain.Person;

import java.util.List;


/**
 * @author  Johannes Reuter
 * @author  Aljona Murygina
 */
public interface ApplicationDAO extends JpaRepository<Application, Integer> {

    // get List<Application> by certain state (e.g. waiting)
    @Query("select x from Application x where x.status = ?1")
    List<Application> getAllApplicationsByState(ApplicationStatus state);


    // get List<Application> by certain person
    @Query("select x from Application x where x.person = ?1")
    List<Application> getAllApplicationsForPerson(Person person);


    // get List<Application> for a certain time (between startDate and endDate)
    @Query("select x from Application x where x.startDate >= ?1 and x.endDate <= ?2")
    List<Application> getAllApplicationsForACertainTime(DateMidnight startDate, DateMidnight endDate);
}
