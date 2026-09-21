package org.synyx.urlaubsverwaltung.settings;

/**
 * Provides access to {@link org.synyx.urlaubsverwaltung.settings.Settings}.
 */
public interface SettingsService {

    /**
     * Persists the given settings.
     *
     * @param settings to be persisted
     * @return saved settings
     */
    Settings save(Settings settings);

    /**
     * @return settings for the application
     */
    Settings getSettings();

    void insertDefaultSettings();
}
