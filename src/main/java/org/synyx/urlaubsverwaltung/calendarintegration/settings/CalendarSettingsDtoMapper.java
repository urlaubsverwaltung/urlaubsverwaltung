package org.synyx.urlaubsverwaltung.calendarintegration.settings;

import com.fasterxml.jackson.databind.ObjectMapper;

class CalendarSettingsDtoMapper {

    private CalendarSettingsDtoMapper() {
    }

    public static CalendarSettingsDto mapToCalendarSettingsDto(CalendarSettingsEntity settingsEntity) {
        return new ObjectMapper().convertValue(settingsEntity, CalendarSettingsDto.class);
    }

    public static CalendarSettingsEntity mapToCalendarSettingsEntity(CalendarSettingsDto calendarSettingsDto) {
        return new ObjectMapper().convertValue(calendarSettingsDto, CalendarSettingsEntity.class);
    }
}
