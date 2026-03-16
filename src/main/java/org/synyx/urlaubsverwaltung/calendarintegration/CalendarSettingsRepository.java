package org.synyx.urlaubsverwaltung.calendarintegration;

import org.jspecify.annotations.NonNull;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

interface CalendarSettingsRepository extends CrudRepository<CalendarSettings, Long> {

    @Override
    @NonNull List<CalendarSettings> findAll();
}
