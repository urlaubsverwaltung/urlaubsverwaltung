package org.synyx.urlaubsverwaltung.settings;

import org.springframework.stereotype.Service;

@Service
public class SettingsImportService {

    private final SettingsRepository settingsRepository;
    private final SettingsCache settingsCache;

    public SettingsImportService(SettingsRepository settingsRepository, SettingsCache settingsCache) {
        this.settingsRepository = settingsRepository;
        this.settingsCache = settingsCache;
    }

    public void deleteAll() {
        settingsRepository.deleteAll();
        settingsCache.invalidate();
    }

    public void importSettings(Settings settings) {
        settingsRepository.save(settings);
        settingsCache.invalidate();
    }
}
