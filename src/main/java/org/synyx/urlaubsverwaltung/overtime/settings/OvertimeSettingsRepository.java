package org.synyx.urlaubsverwaltung.overtime.settings;

import org.springframework.data.repository.CrudRepository;

interface OvertimeSettingsRepository extends CrudRepository<OvertimeSettingsEntity, Long> {

    OvertimeSettingsEntity findFirstBy();
}
