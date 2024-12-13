package org.synyx.urlaubsverwaltung.extension.backup.restore;

import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.extension.backup.model.SettingsDTO;
import org.synyx.urlaubsverwaltung.settings.SettingsImportService;

@Service
@ConditionalOnBackupRestoreEnabled
class SettingsRestoreService {

    private final SettingsImportService settingsImportService;

    SettingsRestoreService(SettingsImportService settingsImportService) {
        this.settingsImportService = settingsImportService;
    }

    void restore(SettingsDTO settingsToRestore) {
        settingsImportService.importSettings(settingsToRestore.toSettings());
    }
}
