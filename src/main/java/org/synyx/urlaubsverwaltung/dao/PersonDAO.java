package org.synyx.urlaubsverwaltung.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.synyx.urlaubsverwaltung.domain.Person;


/**
 * @author  Johannes Reuter
 * @author  Aljona Murygina
 */
public interface PersonDAO extends JpaRepository<Person, Integer> {

    // get Person by login name (from LDAP account)
    @Query("select from Person where loginName = ?1")
    Person getPersonByLogin(String loginName);
}
