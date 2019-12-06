package org.synyx.urlaubsverwaltung.calendarintegration.absence;

import org.synyx.urlaubsverwaltung.settings.CalendarSettings;

import java.util.concurrent.TimeUnit;


/**
 * Time configuration for half day absences (start and end time for morning resp. noon).
 */
public class AbsenceTimeConfiguration {

    private final CalendarSettings calendarSettings;

    public AbsenceTimeConfiguration(CalendarSettings calendarSettings) {

        this.calendarSettings = calendarSettings;
    }

    public Integer getMorningStart() {

        return calendarSettings.getWorkDayBeginHour();
    }


    public Integer getMorningEnd() {

        int halfWorkDay = (calendarSettings.getWorkDayEndHour() - calendarSettings.getWorkDayBeginHour()) / 2;

        return calendarSettings.getWorkDayBeginHour() + halfWorkDay;
    }


    public Integer getNoonStart() {

        return getMorningEnd();
    }


    public Integer getNoonEnd() {

        return calendarSettings.getWorkDayEndHour();
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
}
