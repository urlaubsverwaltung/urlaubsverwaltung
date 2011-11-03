package org.synyx.urlaubsverwaltung.dao;

import org.joda.time.DateMidnight;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.synyx.urlaubsverwaltung.domain.Antrag;
import org.synyx.urlaubsverwaltung.domain.Person;
import org.synyx.urlaubsverwaltung.domain.State;

import java.util.List;


/**
 * @author  johannes
 */
public interface AntragDAO extends JpaRepository<Antrag, Integer> {

    // get List<Antrag> by certain state (e.g. 'wartend')
    @Query("select x from antrag x where x.state = ?")
    List<Antrag> getAllRequestsByState(State state);


    // get List<Antrag> by certain person
    @Query("select x from antrag x where x.person = ?")
    List<Antrag> getAllRequestsForPerson(Person person);


    // get List<Antrag> for a certain time (between startDate and endDate)
    @Query("select x from antrag x where x.startDate >= ? and x.endDate <= ?")
    List<Antrag> getAllRequestsForACertainTime(DateMidnight startDate, DateMidnight endDate);
}
