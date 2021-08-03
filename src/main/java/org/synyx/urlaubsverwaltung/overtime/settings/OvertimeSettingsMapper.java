package org.synyx.urlaubsverwaltung.overtime.settings;

import com.fasterxml.jackson.databind.ObjectMapper;

public class OvertimeSettingsMapper {

    public static OvertimeSettingsDto mapToOvertimeSettingsDto(OvertimeSettingsEntity overtimeSettingsEntity) {
        return new ObjectMapper().convertValue(overtimeSettingsEntity, OvertimeSettingsDto.class);
    }

    public static OvertimeSettingsEntity mapToOvertimeSettingsEntity(OvertimeSettingsDto overtimeSettingsDto) {
        return new ObjectMapper().convertValue(overtimeSettingsDto, OvertimeSettingsEntity.class);
    }

    public static OvertimeSettings mapToOvertimeSettingsModel(OvertimeSettingsEntity overtimeSettingsEntity) {
        return new ObjectMapper().convertValue(overtimeSettingsEntity, OvertimeSettings.class);
    }

    public static OvertimeSettingsEntity mapToOvertimeSettingsEntity(OvertimeSettings overtimeSettings) {
        return new ObjectMapper().convertValue(overtimeSettings, OvertimeSettingsEntity.class);
    }
}
