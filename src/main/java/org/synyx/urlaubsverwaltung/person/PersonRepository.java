package org.synyx.urlaubsverwaltung.person;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    Page<Person> findByPermissionsNotContaining(Role permission, Pageable pageable);

    List<Person> findByPermissionsContainingOrderByFirstNameAscLastNameAsc(Role permission);

    Page<Person> findByPermissionsContaining(Role permission, Pageable pageable);

    List<Person> findByPermissionsContainingAndPermissionsNotContainingOrderByFirstNameAscLastNameAsc(Role permissionContaining, Role permissionNotContaining);

    List<Person> findByPermissionsNotContainingAndNotificationsContainingOrderByFirstNameAscLastNameAsc(Role permissionNotContaining, MailNotification mailNotification);
}
