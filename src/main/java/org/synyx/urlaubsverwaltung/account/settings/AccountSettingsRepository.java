package org.synyx.urlaubsverwaltung.account.settings;

import org.springframework.data.repository.CrudRepository;

interface AccountSettingsRepository extends CrudRepository<AccountSettingsEntity, Long> {

    AccountSettingsEntity findFirstBy();
}
