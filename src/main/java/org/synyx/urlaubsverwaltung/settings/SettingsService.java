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
     * Returns the settings of the application. The settings are cached in memory, so the returned
     * instance is shared: modify it only to pass it to {@link #save(Settings)} right away, which
     * drops the cached settings.
     *
     * @return settings for the application
     */
    Settings getSettings();

    void insertDefaultSettings();
}
