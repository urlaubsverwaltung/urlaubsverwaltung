package org.synyx.urlaubsverwaltung.calendar;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
interface CompanyCalendarRepository extends CrudRepository<CompanyCalendar, Long> {
}
