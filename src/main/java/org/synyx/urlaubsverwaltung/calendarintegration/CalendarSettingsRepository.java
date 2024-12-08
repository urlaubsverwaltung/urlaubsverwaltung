package org.synyx.urlaubsverwaltung.calendarintegration;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

interface CalendarSettingsRepository extends CrudRepository<CalendarSettings, Long> {
    List<CalendarSettings> findAll();
}
