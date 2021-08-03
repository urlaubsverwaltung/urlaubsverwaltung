package org.synyx.urlaubsverwaltung.workingtime.settings;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

class WorkingTimeSettingsMapper {

    private WorkingTimeSettingsMapper() {
    }

    static WorkingTimeSettingsDto mapToWorkingTimeSettingsDto(WorkingTimeSettingsEntity workingTimeSettingsEntity) {
        return new ObjectMapper().convertValue(workingTimeSettingsEntity, WorkingTimeSettingsDto.class);
    }

    static WorkingTimeSettingsEntity mapToWorkingTimeSettingsEntity(WorkingTimeSettingsDto workingTimeSettingsDto) {
        return new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).convertValue(workingTimeSettingsDto, WorkingTimeSettingsEntity.class);
    }

    public static WorkingTimeSettings mapToWorkingTimeSettingsModel(WorkingTimeSettingsEntity workingTimeSettingsEntity) {
        return new ObjectMapper().convertValue(workingTimeSettingsEntity, WorkingTimeSettings.class);
    }
}
