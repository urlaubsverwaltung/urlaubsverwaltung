package org.synyx.urlaubsverwaltung.calendar;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.Role;

import java.util.Collection;
import java.util.Optional;

@Repository
interface CompanyCalendarRepository extends JpaRepository<CompanyCalendar, Long> {

    Optional<CompanyCalendar> findByPerson(Person person);

    Optional<CompanyCalendar> findBySecretAndPerson(String secret, Person person);

    @Modifying
    void deleteByPerson(Person person);

    @Modifying
    @Query("DELETE FROM CompanyCalendar c WHERE c.person.id NOT IN (SELECT p.id FROM Person p JOIN p.permissions r WHERE r IN :roles)")
    void deleteByPersonWithoutOneOfRoles(Collection<Role> roles);
}
