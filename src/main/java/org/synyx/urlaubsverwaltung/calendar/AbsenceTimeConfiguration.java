package org.synyx.urlaubsverwaltung.calendar;

import java.time.Duration;
import java.time.LocalTime;

/**
 * Time configuration for half day absences (start and end time for morning resp. noon).
 */
public class AbsenceTimeConfiguration {

    private final TimeSettings timeSettings;

    public AbsenceTimeConfiguration(TimeSettings timeSettings) {
        this.timeSettings = timeSettings;
    }

    LocalTime getMorningStartTime() {
        return LocalTime.of(timeSettings.getWorkDayBeginHour(), timeSettings.getWorkDayBeginMinute());
    }

    LocalTime getMorningEndTime() {
        return getNoonStartTime();
    }

    LocalTime getNoonStartTime() {
        final LocalTime startTime = LocalTime.of(timeSettings.getWorkDayBeginHour(), timeSettings.getWorkDayBeginMinute());
        final LocalTime endTime = LocalTime.of(timeSettings.getWorkDayEndHour(), timeSettings.getWorkDayEndMinute());
        final Duration duration = Duration.between(startTime, endTime).dividedBy(2);
        return startTime.plus(duration);
    }

    LocalTime getNoonEndTime() {
        return LocalTime.of(timeSettings.getWorkDayEndHour(), timeSettings.getWorkDayEndMinute());
    }

    String getTimeZoneId() {
        return timeSettings.getTimeZoneId();
    }
}
