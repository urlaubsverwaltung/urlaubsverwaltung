package org.synyx.urlaubsverwaltung.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.synyx.urlaubsverwaltung.domain.Person;

import java.util.List;


/**
 * @author  johannes
 */
public interface PersonDAO extends JpaRepository<Person, Integer> {

    @Query("select x from person x where x.restUrlaub > 0")
    List<Person> getPersonsWithResturlaub();
}
