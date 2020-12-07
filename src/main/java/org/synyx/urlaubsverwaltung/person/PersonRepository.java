package org.synyx.urlaubsverwaltung.person;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for {@link Person} entities.
 */
interface PersonRepository extends JpaRepository<Person, Integer> {

    Optional<Person> findByUsername(String username);
}
