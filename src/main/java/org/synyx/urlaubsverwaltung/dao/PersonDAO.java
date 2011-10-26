package org.synyx.urlaubsverwaltung.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.synyx.urlaubsverwaltung.domain.Person;


/**
 * @author  johannes
 */
public interface PersonDAO extends JpaRepository<Person, Integer> {

    @Query("select x from holiday x where x.person = ?")
    Integer readVacationDays(Person person);
}
