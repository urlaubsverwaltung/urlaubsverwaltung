package org.synyx.urlaubsverwaltung.absence;

import java.time.Duration;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;

/**
 * Time configuration for half day absences (start and end time for morning resp. noon).
 */
public class AbsenceTimeConfiguration {

    private final TimeSettings timeSettings;

    public AbsenceTimeConfiguration(TimeSettings timeSettings) {
        this.timeSettings = timeSettings;
    }

    public LocalTime getMorningStartTime() {
        return LocalTime.of(timeSettings.getWorkDayBeginHour(), timeSettings.getWorkDayBeginMinute());
    }

    public LocalTime getMorningEndTime() {
        return getNoonStartTime();
    }

    public LocalTime getNoonStartTime() {
        final LocalTime startTime = LocalTime.of(timeSettings.getWorkDayBeginHour(), timeSettings.getWorkDayBeginMinute());
        final LocalTime endTime = LocalTime.of(timeSettings.getWorkDayEndHour(), timeSettings.getWorkDayEndMinute());
        final Duration duration = Duration.between(startTime, endTime).dividedBy(2);
        return startTime.plus(duration);
    }

    public LocalTime getNoonEndTime() {
        return LocalTime.of(timeSettings.getWorkDayEndHour(), timeSettings.getWorkDayEndMinute());
    }

    public Integer getMorningStartHour() {
        return timeSettings.getWorkDayBeginHour();
    }

    public Integer getMorningEndHour() {
        int halfWorkDay = (timeSettings.getWorkDayEndHour() - timeSettings.getWorkDayBeginHour()) / 2;
        return timeSettings.getWorkDayBeginHour() + halfWorkDay;
    }

    public Integer getNoonStartHour() {
        return getMorningEndHour();
    }

    public Integer getNoonEndHour() {
        return timeSettings.getWorkDayEndHour();
    }

    public long getMorningStartAsMillis() {
        return TimeUnit.HOURS.toMillis(getMorningStartHour());
    }

    public long getMorningEndAsMillis() {
        return TimeUnit.HOURS.toMillis(getMorningEndHour());
    }

    public long getNoonStartAsMillis() {
        return TimeUnit.HOURS.toMillis(getNoonStartHour());
    }

    public long getNoonEndAsMillis() {
        return TimeUnit.HOURS.toMillis(getNoonEndHour());
    }

    public String getTimeZoneId() {
        return timeSettings.getTimeZoneId();
    }

    public void setTimeZoneId(String timeZoneId) {
        timeSettings.setTimeZoneId(timeZoneId);
    }
}
