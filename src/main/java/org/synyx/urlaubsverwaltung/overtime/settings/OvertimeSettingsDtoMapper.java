package org.synyx.urlaubsverwaltung.overtime.settings;

import com.fasterxml.jackson.databind.ObjectMapper;

public class OvertimeSettingsDtoMapper {

    public static OvertimeSettingsDto mapToOvertimeSettingsDto(OvertimeSettingsEntity overtimeSettingsEntity) {
        return new ObjectMapper().convertValue(overtimeSettingsEntity, OvertimeSettingsDto.class);
    }

    public static OvertimeSettingsEntity mapToOvertimeSettingsEntiy(OvertimeSettingsDto overtimeSettingsDto) {
        return new ObjectMapper().convertValue(overtimeSettingsDto, OvertimeSettingsEntity.class);
    }

    public static OvertimeSettings mapToOvertimeSettingsModel(OvertimeSettingsEntity overtimeSettingsEntity) {
        return new ObjectMapper().convertValue(overtimeSettingsEntity, OvertimeSettings.class);
    }

    public static OvertimeSettingsEntity mapToOvertimeSettingsEntiy(OvertimeSettings overtimeSettings) {
        return new ObjectMapper().convertValue(overtimeSettings, OvertimeSettingsEntity.class);
    }
}
