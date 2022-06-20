package org.synyx.urlaubsverwaltung.person;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link Person} entities.
 */
interface PersonRepository extends JpaRepository<Person, Integer> {

    Optional<Person> findByUsername(String username);

    Optional<Person> findByEmail(String email);

    int countByPermissionsNotContaining(Role permission);

    List<Person> findByPermissionsNotContainingOrderByFirstNameAscLastNameAsc(Role permission);

    List<Person> findByPermissionsContainingOrderByFirstNameAscLastNameAsc(Role permission);

    List<Person> findByPermissionsContainingAndPermissionsNotContainingOrderByFirstNameAscLastNameAsc(Role permissionContaining, Role permissionNotContaining);
}
