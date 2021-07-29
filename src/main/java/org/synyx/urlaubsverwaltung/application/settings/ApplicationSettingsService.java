package org.synyx.urlaubsverwaltung.application.settings;

import org.springframework.stereotype.Service;

@Service
public class ApplicationSettingsService {

    private final ApplicationSettingsRepository applicationSettingsRepository;

    public ApplicationSettingsService(ApplicationSettingsRepository applicationSettingsRepository) {
        this.applicationSettingsRepository = applicationSettingsRepository;
    }

    public ApplicationSettingsEntity getSettings(){
        return applicationSettingsRepository.findFirstBy();
    }

    public ApplicationSettingsDto getSettingsDto() {
        return ApplicationSettingsDtoMapper.mapToApplicationSettingsDto(applicationSettingsRepository.findFirstBy());
    }

    public void save(ApplicationSettingsDto applicationSettingsDto) {
        applicationSettingsRepository.save(ApplicationSettingsDtoMapper.mapToApplicationSettingsEntity(applicationSettingsDto));
    }
}
