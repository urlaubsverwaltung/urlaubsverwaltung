package org.synyx.urlaubsverwaltung.absence;

import java.util.concurrent.TimeUnit;

/**
 * Time configuration for half day absences (start and end time for morning resp. noon).
 */
public class AbsenceTimeConfiguration {

    private final TimeSettings timeSettings;

    public AbsenceTimeConfiguration(TimeSettings timeSettings) {
        this.timeSettings = timeSettings;
    }

    public Integer getMorningStart() {
        return timeSettings.getWorkDayBeginHour();
    }

    public Integer getMorningEnd() {
        int halfWorkDay = (timeSettings.getWorkDayEndHour() - timeSettings.getWorkDayBeginHour()) / 2;
        return timeSettings.getWorkDayBeginHour() + halfWorkDay;
    }

    public Integer getNoonStart() {
        return getMorningEnd();
    }

    public Integer getNoonEnd() {
        return timeSettings.getWorkDayEndHour();
    }

    public long getMorningStartAsMillis() {
        return TimeUnit.HOURS.toMillis(getMorningStart());
    }

    public long getMorningEndAsMillis() {
        return TimeUnit.HOURS.toMillis(getMorningEnd());
    }

    public long getNoonStartAsMillis() {
        return TimeUnit.HOURS.toMillis(getNoonStart());
    }

    public long getNoonEndAsMillis() {
        return TimeUnit.HOURS.toMillis(getNoonEnd());
    }

    public String getTimeZoneId() {
        return timeSettings.getTimeZoneId();
    }

    public void setTimeZoneId(String timeZoneId) {
        timeSettings.setTimeZoneId(timeZoneId);
    }
}
