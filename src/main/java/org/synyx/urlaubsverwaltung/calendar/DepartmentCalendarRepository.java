package org.synyx.urlaubsverwaltung.calendar;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
interface DepartmentCalendarRepository extends CrudRepository<DepartmentCalendar, Long> {

    DepartmentCalendar findBySecret(String secret);
}
