package org.synyx.urlaubsverwaltung.calendar;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.synyx.urlaubsverwaltung.person.Person;

import java.util.Optional;

@Repository
interface PersonCalendarRepository extends CrudRepository<PersonCalendar, Long> {

    Optional<PersonCalendar> findByPerson(Person person);

    Optional<PersonCalendar> findBySecret(String secret);

    @Modifying
    void deleteByPerson(Person person);
}
