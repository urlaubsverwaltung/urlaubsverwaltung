package org.synyx.urlaubsverwaltung.application.settings;

import com.fasterxml.jackson.databind.ObjectMapper;

class ApplicationSettingsDtoMapper {

    private  ApplicationSettingsDtoMapper() {
    }

    public static ApplicationSettingsDto mapToApplicationSettingsDto(ApplicationSettingsEntity applicationSettingsEntity) {
        return new ObjectMapper().convertValue(applicationSettingsEntity, ApplicationSettingsDto.class);
    }

    public static ApplicationSettingsEntity mapToApplicationSettingsEntity(ApplicationSettingsDto applicationSettingsDto) {
        return new ObjectMapper().convertValue(applicationSettingsDto, ApplicationSettingsEntity.class);
    }
}
