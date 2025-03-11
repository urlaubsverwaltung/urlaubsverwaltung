package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.synyx.urlaubsverwaltung.calendar.TimeSettings;

public record TimeSettingsDTO(
    String timeZoneId,
    Integer workDayBeginHour,
    Integer workDayBeginMinute,
    Integer workDayEndHour,
    Integer workDayEndMinute
) {

    public static TimeSettingsDTO of(TimeSettings timeSettings) {
        return new TimeSettingsDTO(
            timeSettings.getTimeZoneId(),
            timeSettings.getWorkDayBeginHour(),
            timeSettings.getWorkDayBeginMinute(),
            timeSettings.getWorkDayEndHour(),
            timeSettings.getWorkDayEndMinute()
        );
    }

    public TimeSettings toTimeSettings() {
        final TimeSettings timeSettings = new TimeSettings();
        timeSettings.setTimeZoneId(timeZoneId);
        timeSettings.setWorkDayBeginHour(workDayBeginHour);
        timeSettings.setWorkDayBeginMinute(workDayBeginMinute);
        timeSettings.setWorkDayEndHour(workDayEndHour);
        timeSettings.setWorkDayEndMinute(workDayEndMinute);
        return timeSettings;
    }
}
