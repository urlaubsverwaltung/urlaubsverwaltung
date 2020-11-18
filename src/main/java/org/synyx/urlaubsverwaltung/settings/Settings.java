package org.synyx.urlaubsverwaltung.settings;

import org.springframework.data.jpa.domain.AbstractPersistable;
import org.synyx.urlaubsverwaltung.application.ApplicationSettings;
import org.synyx.urlaubsverwaltung.absence.TimeSettings;
import org.synyx.urlaubsverwaltung.account.AccountSettings;
import org.synyx.urlaubsverwaltung.calendarintegration.CalendarSettings;
import org.synyx.urlaubsverwaltung.overtime.OvertimeSettings;
import org.synyx.urlaubsverwaltung.sicknote.SickNoteSettings;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeSettings;

import javax.persistence.Entity;


/**
 * Represents the settings / business rules for the application.
 */
@Entity
public class Settings extends AbstractPersistable<Integer> {

    private ApplicationSettings applicationSettings;
    private AccountSettings accountSettings;
    private WorkingTimeSettings workingTimeSettings;
    private OvertimeSettings overtimeSettings;
    private TimeSettings timeSettings;
    private SickNoteSettings sickNoteSettings;

    @Deprecated(since = "4.0.0", forRemoval = true)
    private CalendarSettings calendarSettings;

    public ApplicationSettings getApplicationSettings() {
        if (applicationSettings == null) {
            applicationSettings = new ApplicationSettings();
        }

        return applicationSettings;
    }

    public void setApplicationSettings(ApplicationSettings applicationSettings) {
        this.applicationSettings = applicationSettings;
    }

    public AccountSettings getAccountSettings() {
        if (accountSettings == null) {
            accountSettings = new AccountSettings();
        }

        return accountSettings;
    }

    public void setAccountSettings(AccountSettings accountSettings) {
        this.accountSettings = accountSettings;
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

    public OvertimeSettings getOvertimeSettings() {
        if (overtimeSettings == null) {
            overtimeSettings = new OvertimeSettings();
        }

        return overtimeSettings;
    }

    public void setOvertimeSettings(OvertimeSettings overtimeSettings) {
        this.overtimeSettings = overtimeSettings;
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

    public SickNoteSettings getSickNoteSettings() {

        if (sickNoteSettings == null) {
            sickNoteSettings = new SickNoteSettings();
        }

        return sickNoteSettings;
    }

    public void setSickNoteSettings(SickNoteSettings sickNoteSettings) {
        this.sickNoteSettings = sickNoteSettings;
    }

    @Override
    public void setId(Integer id) { // NOSONAR - make it public instead of protected
        super.setId(id);
    }
}
