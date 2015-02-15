package org.synyx.urlaubsverwaltung.core.settings;

/**
 * Provides access to {@link org.synyx.urlaubsverwaltung.core.settings.Settings}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public interface SettingsService {

    /**
     * @return  settings for the application
     */
    Settings getSettings();
}
