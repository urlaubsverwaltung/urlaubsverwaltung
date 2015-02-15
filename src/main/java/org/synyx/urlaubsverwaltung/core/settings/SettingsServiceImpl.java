package org.synyx.urlaubsverwaltung.core.settings;

import org.springframework.stereotype.Service;


/**
 * Implementation for {@link org.synyx.urlaubsverwaltung.core.settings.SettingsService}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Service
public class SettingsServiceImpl implements SettingsService {

    @Override
    public Settings getSettings() {

        return new Settings();
    }
}
