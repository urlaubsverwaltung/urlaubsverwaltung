package org.synyx.urlaubsverwaltung.settings;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.overtime.OvertimeProperties;
import org.synyx.urlaubsverwaltung.overtime.OvertimeSettingsActivatedEvent;
import org.synyx.urlaubsverwaltung.overtime.OvertimeSettingsDeactivatedEvent;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Implementation for {@link org.synyx.urlaubsverwaltung.settings.SettingsService}.
 */
@Service
@EnableConfigurationProperties(OvertimeProperties.class)
public class SettingsServiceImpl implements SettingsService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final SettingsRepository settingsRepository;
    private final OvertimeProperties overtimeProperties;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final SettingsCache settingsCache;

    @Autowired
    public SettingsServiceImpl(
        SettingsRepository settingsRepository,
        OvertimeProperties overtimeProperties,
        ApplicationEventPublisher applicationEventPublisher,
        SettingsCache settingsCache
    ) {
        this.settingsRepository = settingsRepository;
        this.overtimeProperties = overtimeProperties;
        this.applicationEventPublisher = applicationEventPublisher;
        this.settingsCache = settingsCache;
    }

    @Override
    public Settings save(Settings settings) {

        // first, so that nothing below can leave settings modified by the caller in the cache
        settingsCache.invalidate();

        // not from the cache - the caller may have modified the instance it read from there
        final boolean previousOvertimeActive = loadSettings().getOvertimeSettings().isOvertimeActive();

        final Settings savedSettings = settingsRepository.save(settings);
        settingsCache.invalidate();
        LOG.info("Updated settings: {}", savedSettings);

        publishOvertimeSettingsChangeEvent(previousOvertimeActive, savedSettings);

        return savedSettings;
    }

    private void publishOvertimeSettingsChangeEvent(boolean previousOvertimeActive, Settings savedSettings) {
        final boolean currentOvertimeActive = savedSettings.getOvertimeSettings().isOvertimeActive();
        if (!previousOvertimeActive && currentOvertimeActive) {
            applicationEventPublisher.publishEvent(OvertimeSettingsActivatedEvent.of());
        } else if (previousOvertimeActive && !currentOvertimeActive) {
            applicationEventPublisher.publishEvent(OvertimeSettingsDeactivatedEvent.of());
        }
    }

    @Override
    public Settings getSettings() {
        return settingsCache.get(this::loadSettings);
    }

    @Override
    public void insertDefaultSettings() {

        final long count = settingsRepository.count();

        if (count == 0) {
            final Settings settings = new Settings();

            settings.getOvertimeSettings().setOvertimeSyncActive(overtimeProperties.isSyncActive());

            final Settings savedSettings = settingsRepository.save(settings);
            settingsCache.invalidate();
            applicationEventPublisher.publishEvent(new InitialDefaultSettingsSavedEvent());
            LOG.info("Saved initial settings {}", savedSettings);
        }
    }

    private Settings loadSettings() {
        return settingsRepository.findAll().stream().findFirst()
            .orElseThrow(() -> new IllegalStateException("No settings found in database!"));
    }
}
