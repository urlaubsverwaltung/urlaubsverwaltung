package org.synyx.urlaubsverwaltung.core.person;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


/**
 * Repository for {@link Person} entities.
 *
 * @author  Johannes Reuter
 * @author  Aljona Murygina
 */
public interface PersonDAO extends JpaRepository<Person, Integer> {

    Person findByLoginName(String loginName);

    @Query("select x from Person x where x.active = false order by x.firstName")
    List<Person> findInactive();

}
