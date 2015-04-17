package org.synyx.urlaubsverwaltung.core.settings;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;


/**
 * Implementation for {@link org.synyx.urlaubsverwaltung.core.settings.SettingsService}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Service
public class SettingsServiceImpl implements SettingsService {

    private final SettingsDAO settingsDAO;

    @Autowired
    public SettingsServiceImpl(SettingsDAO settingsDAO) {

        this.settingsDAO = settingsDAO;
    }

    @Override
    public void save(Settings settings) {

        settingsDAO.save(settings);
    }


    @Override
    public Settings getSettings() {

        // TODO: Maybe fixed in future for different settings (based on date,...)
        return settingsDAO.findOne(1);
    }
}
