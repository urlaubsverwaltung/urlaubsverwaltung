package org.synyx.urlaubsverwaltung.calendar;

import org.jspecify.annotations.NonNull;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
interface CompanyCalendarAccessibleRepository extends CrudRepository<CompanyCalendarAccessible, Long> {

    @Override
    @NonNull List<CompanyCalendarAccessible> findAll();
}
