package org.synyx.urlaubsverwaltung.sicknote.settings;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
interface SickNoteSettingsRepository extends CrudRepository<SickNoteSettingsEntity, Long> {

    SickNoteSettingsEntity findFirstBy();
}
