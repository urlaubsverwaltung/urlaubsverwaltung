package org.synyx.urlaubsverwaltung.core.settings;

/**
 * Provides access to {@link org.synyx.urlaubsverwaltung.core.settings.Settings}.
 */
public interface SettingsService {

    /**
     * Persists the given settings.
     *
     * @param  settings  to be persisted
     */
    void save(Settings settings);


    /**
     * @return  settings for the application
     */
    Settings getSettings();
}
