package org.synyx.urlaubsverwaltung.workingtime.settings;

import org.springframework.data.repository.CrudRepository;

interface WorkingTimeSettingsRepository extends CrudRepository<WorkingTimeSettingsEntity, Long> {

    WorkingTimeSettingsEntity findFirstBy();
}
