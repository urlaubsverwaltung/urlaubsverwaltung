package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeSettings;

public record WorkingTimeSettingsDTO(
    DayLengthDTO monday,
    DayLengthDTO tuesday,
    DayLengthDTO wednesday,
    DayLengthDTO thursday,
    DayLengthDTO friday,
    DayLengthDTO saturday,
    DayLengthDTO sunday
) {

    public static WorkingTimeSettingsDTO of(WorkingTimeSettings workingTimeSettings) {
        return new WorkingTimeSettingsDTO(
            DayLengthDTO.valueOf(workingTimeSettings.getMonday().name()),
            DayLengthDTO.valueOf(workingTimeSettings.getTuesday().name()),
            DayLengthDTO.valueOf(workingTimeSettings.getWednesday().name()),
            DayLengthDTO.valueOf(workingTimeSettings.getThursday().name()),
            DayLengthDTO.valueOf(workingTimeSettings.getFriday().name()),
            DayLengthDTO.valueOf(workingTimeSettings.getSaturday().name()),
            DayLengthDTO.valueOf(workingTimeSettings.getSunday().name())
        );
    }

    public WorkingTimeSettings toWorkingTimeSettings() {
        WorkingTimeSettings workingTimeSettings = new WorkingTimeSettings();
        workingTimeSettings.setMonday(DayLength.valueOf(monday().name()));
        workingTimeSettings.setTuesday(DayLength.valueOf(tuesday().name()));
        workingTimeSettings.setWednesday(DayLength.valueOf(wednesday().name()));
        workingTimeSettings.setThursday(DayLength.valueOf(thursday().name()));
        workingTimeSettings.setFriday(DayLength.valueOf(friday().name()));
        workingTimeSettings.setSaturday(DayLength.valueOf(saturday().name()));
        workingTimeSettings.setSunday(DayLength.valueOf(sunday().name()));
        return workingTimeSettings;
    }
}
