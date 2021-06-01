package org.synyx.urlaubsverwaltung.absence.settings;

class TimeSettingsDtoMapper {

    static TimeSettingsDto mapToWorkingTimeSettingsDto(TimeSettingsEntity timeSettingsEntity) {

        TimeSettingsDto timeSettingsDto = new TimeSettingsDto();
        timeSettingsDto.setId(timeSettingsEntity.getId());
        timeSettingsDto.setTimeZoneId(timeSettingsEntity.getTimeZoneId());
        timeSettingsDto.setWorkDayBeginHour(timeSettingsEntity.getWorkDayBeginHour());
        timeSettingsDto.setWorkDayEndHour(timeSettingsEntity.getWorkDayEndHour());

        return timeSettingsDto;
    }

    static TimeSettingsEntity mapToWorkingTimeSettingsEntity(TimeSettingsDto timeSettingsDto) {

        TimeSettingsEntity workingTimeSettingsEntity = new TimeSettingsEntity();
        workingTimeSettingsEntity.setId(timeSettingsDto.getId());
        workingTimeSettingsEntity.setTimeZoneId(timeSettingsDto.getTimeZoneId());
        workingTimeSettingsEntity.setWorkDayBeginHour(timeSettingsDto.getWorkDayBeginHour());
        workingTimeSettingsEntity.setWorkDayEndHour(timeSettingsDto.getWorkDayEndHour());

        return workingTimeSettingsEntity;
    }
}
