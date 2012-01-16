package org.synyx.urlaubsverwaltung.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.synyx.urlaubsverwaltung.domain.Person;

import java.util.List;


/**
 * @author  Johannes Reuter
 * @author  Aljona Murygina
 */
public interface PersonDAO extends JpaRepository<Person, Integer> {

    // get Person by login name (from LDAP account)
    @Query("select x from Person x where x.loginName = ?")
    Person getPersonByLogin(String loginName);


    @Query("select x from Person x where x.active = true order by x.lastName")
    List<Person> getPersonsOrderedByLastName();


    @Query("select x from Person x where x.active = false order by x.lastName")
    List<Person> getInactivePersons();


    @Query("select x from Person x where x.id != ?1 order by x.lastName")
    List<Person> getAllPersonsExceptOne(Integer id);
}
