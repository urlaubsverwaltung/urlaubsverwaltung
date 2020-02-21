package org.synyx.urlaubsverwaltung.calendar;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
interface CompanyCalendarAccessibleRepository extends CrudRepository<CompanyCalendarAccessible, Long> {

    List<CompanyCalendarAccessible> findAll();
}
