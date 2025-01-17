package org.synyx.urlaubsverwaltung.extension.backup.backup;

import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.extension.backup.model.AccountSettingsDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.ApplicationSettingsDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.AvatarSettingsDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.OverTimeSettingsDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.SettingsDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.SickNoteSettingsDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.TimeSettingsDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.WorkingTimeSettingsDTO;
import org.synyx.urlaubsverwaltung.settings.Settings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

@Service
@ConditionalOnBackupCreateEnabled
class SettingsDataCollectionService {

    private final SettingsService settingsService;

    SettingsDataCollectionService(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    SettingsDTO collectSettings() {
        final Settings settings = settingsService.getSettings();

        return new SettingsDTO(settings.getId(),
            ApplicationSettingsDTO.of(settings.getApplicationSettings()),
            AccountSettingsDTO.of(settings.getAccountSettings()),
            WorkingTimeSettingsDTO.of(settings.getWorkingTimeSettings()),
            OverTimeSettingsDTO.of(settings.getOvertimeSettings()),
            TimeSettingsDTO.of(settings.getTimeSettings()),
            SickNoteSettingsDTO.of(settings.getSickNoteSettings()),
            AvatarSettingsDTO.of(settings.getAvatarSettings())
        );
    }
}
