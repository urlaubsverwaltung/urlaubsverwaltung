package org.synyx.urlaubsverwaltung.calendar;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
interface PersonCalendarRepository extends CrudRepository<PersonCalendar, Long> {
}
