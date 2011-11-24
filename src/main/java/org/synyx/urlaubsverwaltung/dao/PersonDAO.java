package org.synyx.urlaubsverwaltung.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.synyx.urlaubsverwaltung.domain.Person;


/**
 * @author  johannes
 */
public interface PersonDAO extends JpaRepository<Person, Integer> {

    // get List<Antrag> by certain state (e.g. 'wartend')
    @Query("select from Person where loginName = ?1")
    Person getPersonByLogin(String loginName);
}
