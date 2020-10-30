package org.synyx.urlaubsverwaltung.settings;

import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Entity;


/**
 * Represents the settings / business rules for the application.
 */
@Entity
public class Settings extends AbstractPersistable<Integer> {

    private AbsenceSettings absenceSettings;
    private WorkingTimeSettings workingTimeSettings;

    @Deprecated(since = "4.0.0", forRemoval = true)
    private CalendarSettings calendarSettings;
    private TimeSettings timeSettings;

    public AbsenceSettings getAbsenceSettings() {
        if (absenceSettings == null) {
            absenceSettings = new AbsenceSettings();
        }

        return absenceSettings;
    }

    public void setAbsenceSettings(AbsenceSettings absenceSettings) {
        this.absenceSettings = absenceSettings;
    }

    public WorkingTimeSettings getWorkingTimeSettings() {
        if (workingTimeSettings == null) {
            workingTimeSettings = new WorkingTimeSettings();
        }

        return workingTimeSettings;
    }

    public void setWorkingTimeSettings(WorkingTimeSettings workingTimeSettings) {
        this.workingTimeSettings = workingTimeSettings;
    }

    public CalendarSettings getCalendarSettings() {

        if (calendarSettings == null) {
            calendarSettings = new CalendarSettings();
        }

        return calendarSettings;
    }

    @Deprecated(since = "4.0.0", forRemoval = true)
    public void setCalendarSettings(CalendarSettings calendarSettings) {

        this.calendarSettings = calendarSettings;
    }

    public TimeSettings getTimeSettings() {

        if (timeSettings == null) {
            timeSettings = new TimeSettings();
        }

        return timeSettings;
    }

    public void setTimeSettings(TimeSettings timeSettings) {
        this.timeSettings = timeSettings;
    }

    @Override
    public void setId(Integer id) { // NOSONAR - make it public instead of protected
        super.setId(id);
    }
}
