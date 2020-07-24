package org.synyx.urlaubsverwaltung.person;

import org.springframework.data.jpa.repository.JpaRepository;


/**
 * Repository for {@link Person} entities.
 */
interface PersonRepository extends JpaRepository<Person, Integer> {

    Person findByUsername(String username);
}
