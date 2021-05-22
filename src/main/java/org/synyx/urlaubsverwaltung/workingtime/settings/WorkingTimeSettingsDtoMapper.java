package org.synyx.urlaubsverwaltung.workingtime.settings;

class WorkingTimeSettingsDtoMapper {

    static WorkingTimeSettingsDto mapToWorkingTimeSettingsDto(WorkingTimeSettingsEntity workingTimeSettingsEntity) {

        WorkingTimeSettingsDto workingTimeSettingsDto = new WorkingTimeSettingsDto();
        workingTimeSettingsDto.setId(workingTimeSettingsEntity.getId());
        workingTimeSettingsDto.setMonday(workingTimeSettingsEntity.getMonday());
        workingTimeSettingsDto.setTuesday(workingTimeSettingsEntity.getTuesday());
        workingTimeSettingsDto.setWednesday(workingTimeSettingsEntity.getWednesday());
        workingTimeSettingsDto.setThursday(workingTimeSettingsEntity.getThursday());
        workingTimeSettingsDto.setFriday(workingTimeSettingsEntity.getFriday());
        workingTimeSettingsDto.setSaturday(workingTimeSettingsEntity.getSaturday());
        workingTimeSettingsDto.setSunday(workingTimeSettingsEntity.getSunday());
        workingTimeSettingsDto.setWorkingDurationForChristmasEve(workingTimeSettingsEntity.getWorkingDurationForChristmasEve());
        workingTimeSettingsDto.setWorkingDurationForNewYearsEve(workingTimeSettingsEntity.getWorkingDurationForNewYearsEve());
        workingTimeSettingsDto.setFederalState(workingTimeSettingsEntity.getFederalState());

        return workingTimeSettingsDto;
    }

    public static WorkingTimeSettingsEntity mapToWorkingTimeSettingsEntity(WorkingTimeSettingsDto workingTimeSettingsDto) {

        WorkingTimeSettingsEntity workingTimeSettingsEntity = new WorkingTimeSettingsEntity();
        workingTimeSettingsEntity.setId(workingTimeSettingsDto.getId());
        workingTimeSettingsEntity.setMonday(workingTimeSettingsDto.getMonday());
        workingTimeSettingsEntity.setTuesday(workingTimeSettingsDto.getTuesday());
        workingTimeSettingsEntity.setWednesday(workingTimeSettingsDto.getWednesday());
        workingTimeSettingsEntity.setThursday(workingTimeSettingsDto.getThursday());
        workingTimeSettingsEntity.setFriday(workingTimeSettingsDto.getFriday());
        workingTimeSettingsEntity.setSaturday(workingTimeSettingsDto.getSaturday());
        workingTimeSettingsEntity.setSunday(workingTimeSettingsDto.getSunday());
        workingTimeSettingsEntity.setWorkingDurationForChristmasEve(workingTimeSettingsDto.getWorkingDurationForChristmasEve());
        workingTimeSettingsEntity.setWorkingDurationForNewYearsEve(workingTimeSettingsDto.getWorkingDurationForNewYearsEve());
        workingTimeSettingsEntity.setFederalState(workingTimeSettingsDto.getFederalState());

        return workingTimeSettingsEntity;
    }
}
