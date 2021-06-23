package org.synyx.urlaubsverwaltung.overtime.settings;

import com.fasterxml.jackson.databind.ObjectMapper;

public class OvertimeSettingsDtoMapper {

    public static OvertimeSettingsDto mapToOvertimeSettingsDto(OvertimeSettingsEntity overtimeSettingsEntity) {
        return new ObjectMapper().convertValue(overtimeSettingsEntity, OvertimeSettingsDto.class);
    }

    public static OvertimeSettingsEntity mapToOvertimeSettingsEntiy(OvertimeSettingsDto overtimeSettingsDto) {
        return new ObjectMapper().convertValue(overtimeSettingsDto, OvertimeSettingsEntity.class);
    }
}
