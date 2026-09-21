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

    @Autowired
    public SettingsServiceImpl(
        SettingsRepository settingsRepository,
        OvertimeProperties overtimeProperties,
        ApplicationEventPublisher applicationEventPublisher
    ) {
        this.settingsRepository = settingsRepository;
        this.overtimeProperties = overtimeProperties;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public Settings save(Settings settings) {

        final boolean previousOvertimeActive = getSettings().getOvertimeSettings().isOvertimeActive();

        final Settings savedSettings = settingsRepository.save(settings);
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
        return settingsRepository.findAll().stream().findFirst()
            .orElseThrow(() -> new IllegalStateException("No settings found in database!"));
    }

    @Override
    public void insertDefaultSettings() {

        final long count = settingsRepository.count();

        if (count == 0) {
            final Settings settings = new Settings();

            settings.getOvertimeSettings().setOvertimeSyncActive(overtimeProperties.isSyncActive());

            final Settings savedSettings = settingsRepository.save(settings);
            applicationEventPublisher.publishEvent(new InitialDefaultSettingsSavedEvent());
            LOG.info("Saved initial settings {}", savedSettings);
        }
    }
}
