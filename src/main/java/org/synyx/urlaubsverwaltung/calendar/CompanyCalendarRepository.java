package org.synyx.urlaubsverwaltung.calendar;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.synyx.urlaubsverwaltung.person.Person;

import java.util.Optional;

@Repository
interface CompanyCalendarRepository extends JpaRepository<CompanyCalendar, Long> {

    Optional<CompanyCalendar> findByPerson(Person person);

    Optional<CompanyCalendar> findBySecretAndPerson(String secret, Person person);

    @Modifying
    void deleteByPerson(Person person);
}
