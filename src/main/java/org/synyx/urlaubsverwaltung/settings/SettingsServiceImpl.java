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
    public void save(Settings settings) {
        settingsRepository.save(settings);
        LOG.info("Updated settings: {}", settings);
    }

    @Override
    public Settings getSettings() {
        return settingsRepository.findById(1)
            .orElseThrow(() -> new IllegalStateException("No settings in database found."));
    }
}
