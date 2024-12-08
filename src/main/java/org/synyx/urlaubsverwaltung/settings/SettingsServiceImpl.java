package org.synyx.urlaubsverwaltung.settings;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Implementation for {@link org.synyx.urlaubsverwaltung.settings.SettingsService}.
 */
@Service
public class SettingsServiceImpl implements SettingsService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final SettingsRepository settingsRepository;

    @Autowired
    public SettingsServiceImpl(SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    @Override
    public Settings save(Settings settings) {
        final Settings savedSettings = settingsRepository.save(settings);
        LOG.info("Updated settings: {}", savedSettings);
        return savedSettings;
    }

    @Override
    public Settings getSettings() {
        return settingsRepository.findAll().stream().findFirst()
            .orElseThrow(() -> new IllegalStateException("No settings found in database!"));
    }

    @Override
    public void insertDefaultSettings() {

        final long count = settingsRepository.count();

        if (count == 0) {
            final Settings settings = new Settings();
            final Settings savedSettings = settingsRepository.save(settings);
            LOG.info("Saved initial settings {}", savedSettings);
        }
    }
}
