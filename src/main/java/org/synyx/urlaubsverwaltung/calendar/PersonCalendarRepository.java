package org.synyx.urlaubsverwaltung.calendar;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.synyx.urlaubsverwaltung.person.Person;

import java.util.Optional;

@Repository
interface PersonCalendarRepository extends CrudRepository<PersonCalendar, Long> {

    PersonCalendar findByPerson(Person person);

    PersonCalendar findBySecret(String secret);

    void deleteByPerson(Person person);
}
