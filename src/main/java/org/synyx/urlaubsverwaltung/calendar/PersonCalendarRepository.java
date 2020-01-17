package org.synyx.urlaubsverwaltung.calendar;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
interface PersonCalendarRepository extends CrudRepository<PersonCalendar, Long> {

    PersonCalendar findBySecret(String secret);
}
