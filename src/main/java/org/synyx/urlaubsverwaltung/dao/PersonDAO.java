package org.synyx.urlaubsverwaltung.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.synyx.urlaubsverwaltung.domain.Person;

import java.util.List;
import org.synyx.urlaubsverwaltung.domain.Role;


/**
 * @author  Johannes Reuter
 * @author  Aljona Murygina
 */
public interface PersonDAO extends JpaRepository<Person, Integer> {

    // get Person by login name (from LDAP account)
    @Query("select x from Person x where x.loginName = ?")
    Person getPersonByLogin(String loginName);
    
    // TODO: must be removed or changed
    // get Person by Role
//    @Query("select Person_id from Person_permissions x where x.permissions = ?")
//    List<Integer> getPersonsByRole(Role role);


    @Query("select x from Person x where x.active = true order by x.firstName")
    List<Person> getPersonsOrderedByLastName();


    @Query("select x from Person x where x.active = false order by x.firstName")
    List<Person> getInactivePersons();


    @Query("select x from Person x where x.id != ?1 and x.active = true order by x.lastName")
    List<Person> getAllPersonsExceptOne(Integer id);
}
