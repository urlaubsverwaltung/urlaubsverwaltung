package org.synyx.urlaubsverwaltung.core.person;

import org.springframework.data.jpa.repository.JpaRepository;


/**
 * Repository for {@link Person} entities.
 *
 * @author  Johannes Reuter
 * @author  Aljona Murygina
 */
public interface PersonDAO extends JpaRepository<Person, Integer> {

    Person findByLoginName(String loginName);
}
