package org.synyx.urlaubsverwaltung.workingtime.settings;

import com.fasterxml.jackson.databind.ObjectMapper;

class WorkingTimeSettingsDtoMapper {

    private WorkingTimeSettingsDtoMapper() {
    }

    static WorkingTimeSettingsDto mapToWorkingTimeSettingsDto(WorkingTimeSettingsEntity workingTimeSettingsEntity) {

        return new ObjectMapper().convertValue(workingTimeSettingsEntity, WorkingTimeSettingsDto.class);
    }

    static WorkingTimeSettingsEntity mapToWorkingTimeSettingsEntity(WorkingTimeSettingsDto workingTimeSettingsDto) {

        return new ObjectMapper().convertValue(workingTimeSettingsDto, WorkingTimeSettingsEntity.class);
    }
}
