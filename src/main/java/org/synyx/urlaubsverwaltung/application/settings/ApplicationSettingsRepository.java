package org.synyx.urlaubsverwaltung.application.settings;

import org.springframework.data.repository.CrudRepository;

interface ApplicationSettingsRepository extends CrudRepository<ApplicationSettingsEntity, Long> {

    ApplicationSettingsEntity findFirstBy();
}
