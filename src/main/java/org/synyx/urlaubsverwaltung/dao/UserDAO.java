
package org.synyx.urlaubsverwaltung.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.synyx.urlaubsverwaltung.domain.Person;


/**
 * @author  aljona
 */
public interface UserDAO extends JpaRepository<Person, Integer> {

    @Query("select x from holiday x where x.user = ?")
    Integer readVacationDays(Person user);
}
