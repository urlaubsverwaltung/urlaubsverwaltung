package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.synyx.urlaubsverwaltung.absence.TimeSettings;

public record TimeSettingsDTO(
    String timeZoneId,
    Integer workDayBeginHour,
    Integer workDayEndHour
) {

    public static TimeSettingsDTO of(TimeSettings timeSettings) {
        return new TimeSettingsDTO(
            timeSettings.getTimeZoneId(),
            timeSettings.getWorkDayBeginHour(),
            timeSettings.getWorkDayEndHour()
        );
    }

    public TimeSettings toTimeSettings() {
        TimeSettings timeSettings = new TimeSettings();
        timeSettings.setTimeZoneId(timeZoneId);
        timeSettings.setWorkDayBeginHour(workDayBeginHour);
        timeSettings.setWorkDayEndHour(workDayEndHour);
        return timeSettings;
    }
}
