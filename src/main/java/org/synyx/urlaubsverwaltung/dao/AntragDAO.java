package org.synyx.urlaubsverwaltung.dao;

import org.joda.time.DateMidnight;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.synyx.urlaubsverwaltung.domain.Antrag;
import org.synyx.urlaubsverwaltung.domain.AntragStatus;
import org.synyx.urlaubsverwaltung.domain.Person;

import java.util.List;


/**
 * @author  johannes
 */
public interface AntragDAO extends JpaRepository<Antrag, Integer> {

    // get List<Antrag> by certain state (e.g. 'wartend')
    @Query("select x from Antrag x where x.status = ?")
    List<Antrag> getAllRequestsByState(AntragStatus state);


    // get List<Antrag> by certain person
    @Query("select x from Antrag x where x.person = ?")
    List<Antrag> getAllRequestsForPerson(Person person);


    // get List<Antrag> for a certain time (between startDate and endDate)
    @Query("select x from Antrag x where x.startDate >= ?1 and x.endDate <= ?2")
    List<Antrag> getAllRequestsForACertainTime(DateMidnight startDate, DateMidnight endDate);
}
