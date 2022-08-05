package org.synyx.urlaubsverwaltung.person;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("select p from Person p where :permission not member of p.permissions and (p.firstName like %:query% or p.lastName like %:query%)")
    Page<Person> findByPermissionsNotContainingAndByNiceNameContainingIgnoreCase(@Param("permission") Role role, @Param("query") String query, Pageable pageable);

    List<Person> findByPermissionsContainingOrderByFirstNameAscLastNameAsc(Role permission);

    @Query("select p from Person p where :permission member of p.permissions and (p.firstName like %:query% or p.lastName like %:query%)")
    Page<Person> findByPermissionsContainingAndNiceNameContainingIgnoreCase(@Param("permission") Role permission, @Param("query") String nameQuery, Pageable pageable);

    List<Person> findByPermissionsContainingAndPermissionsNotContainingOrderByFirstNameAscLastNameAsc(Role permissionContaining, Role permissionNotContaining);

    List<Person> findByPermissionsNotContainingAndNotificationsContainingOrderByFirstNameAscLastNameAsc(Role permissionNotContaining, MailNotification mailNotification);
}
