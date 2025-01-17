package org.synyx.urlaubsverwaltung.user;

import org.springframework.stereotype.Service;

@Service
public class UserSettingsImportService {

    private final UserSettingsRepository userSettingsRepository;

    UserSettingsImportService(UserSettingsRepository userSettingsRepository) {
        this.userSettingsRepository = userSettingsRepository;
    }

    public void deleteAll() {
        userSettingsRepository.deleteAll();
    }

    public void importUserSettings(UserSettingsEntity userSettingsEntity) {
        userSettingsRepository.save(userSettingsEntity);
    }
}
