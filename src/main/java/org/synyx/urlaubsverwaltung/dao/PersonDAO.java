package org.synyx.urlaubsverwaltung.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.synyx.urlaubsverwaltung.domain.Person;

/**
 * @author johannes
 */
public interface PersonDAO extends JpaRepository<Person, Integer> {

}
