package org.synyx.urlaubsverwaltung.absence.settings;

import org.springframework.data.repository.CrudRepository;

interface TimeSettingsRepository extends CrudRepository<TimeSettingsEntity, Long> {

    TimeSettingsEntity findFirstBy();
}
