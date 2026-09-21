package org.synyx.urlaubsverwaltung.settings;

import org.springframework.stereotype.Service;

@Service
public class SettingsImportService {

    private final SettingsRepository settingsRepository;

    public SettingsImportService(SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    public void deleteAll() {
        settingsRepository.deleteAll();
    }

    public void importSettings(Settings settings) {
        settingsRepository.save(settings);
    }
}
