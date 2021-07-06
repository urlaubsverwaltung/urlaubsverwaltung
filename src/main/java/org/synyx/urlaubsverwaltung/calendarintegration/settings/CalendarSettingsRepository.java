package org.synyx.urlaubsverwaltung.calendarintegration.settings;

import org.springframework.data.repository.CrudRepository;

interface CalendarSettingsRepository extends CrudRepository<CalendarSettingsEntity, Long> {

    CalendarSettingsEntity findFirstBy();
}
