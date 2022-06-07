package org.synyx.urlaubsverwaltung.settings;

import org.synyx.urlaubsverwaltung.absence.TimeSettings;
import org.synyx.urlaubsverwaltung.account.AccountSettings;
import org.synyx.urlaubsverwaltung.application.settings.ApplicationSettings;
import org.synyx.urlaubsverwaltung.overtime.OvertimeSettings;
import org.synyx.urlaubsverwaltung.sicknote.settings.SickNoteSettings;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeSettings;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Objects;

/**
 * Represents the settings / business rules for the application.
 */
@Entity
public class Settings {

    @Id
    @GeneratedValue
    private Integer id;

    private ApplicationSettings applicationSettings;
    private AccountSettings accountSettings;
    private WorkingTimeSettings workingTimeSettings;
    private OvertimeSettings overtimeSettings;
    private TimeSettings timeSettings;
    private SickNoteSettings sickNoteSettings;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Settings that = (Settings) o;
        return null != this.getId() && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
