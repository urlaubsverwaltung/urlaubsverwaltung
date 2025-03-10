package org.synyx.urlaubsverwaltung.calendar;

import java.time.Duration;
import java.time.LocalTime;

/**
 * Time configuration for half day absences (start and end time for morning resp. noon).
 */
public record CalendarAbsenceConfiguration(TimeSettings timeSettings) {

    LocalTime morningStartTime() {
        return LocalTime.of(timeSettings.getWorkDayBeginHour(), timeSettings.getWorkDayBeginMinute());
    }

    LocalTime morningEndTime() {
        return noonStartTime();
    }

    LocalTime noonStartTime() {
        final LocalTime startTime = LocalTime.of(timeSettings.getWorkDayBeginHour(), timeSettings.getWorkDayBeginMinute());
        final LocalTime endTime = LocalTime.of(timeSettings.getWorkDayEndHour(), timeSettings.getWorkDayEndMinute());
        final Duration duration = Duration.between(startTime, endTime).dividedBy(2);
        return startTime.plus(duration);
    }

    LocalTime noonEndTime() {
        return LocalTime.of(timeSettings.getWorkDayEndHour(), timeSettings.getWorkDayEndMinute());
    }

    String timeZoneId() {
        return timeSettings.getTimeZoneId();
    }
}
