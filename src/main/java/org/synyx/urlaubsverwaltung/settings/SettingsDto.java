package org.synyx.urlaubsverwaltung.settings;

import org.synyx.urlaubsverwaltung.absence.TimeSettings;
import org.synyx.urlaubsverwaltung.account.AccountSettings;
import org.synyx.urlaubsverwaltung.application.settings.ApplicationSettings;
import org.synyx.urlaubsverwaltung.calendarintegration.CalendarSettings;
import org.synyx.urlaubsverwaltung.overtime.OvertimeSettings;
import org.synyx.urlaubsverwaltung.sicknote.settings.SickNoteSettings;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeSettings;

import java.util.Objects;

public class SettingsDto {

    private Integer id;
    private AbsenceTypeSettingsDto absenceTypeSettings;

    private ApplicationSettings applicationSettings;
    private AccountSettings accountSettings;
    private WorkingTimeSettings workingTimeSettings;
    private OvertimeSettings overtimeSettings;
    private TimeSettings timeSettings;
    private SickNoteSettings sickNoteSettings;
    private CalendarSettings calendarSettings;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public AbsenceTypeSettingsDto getAbsenceTypeSettings() {
        return absenceTypeSettings;
    }

    public void setAbsenceTypeSettings(AbsenceTypeSettingsDto absenceTypeSettings) {
        this.absenceTypeSettings = absenceTypeSettings;
    }

    public ApplicationSettings getApplicationSettings() {
        return applicationSettings;
    }

    public void setApplicationSettings(ApplicationSettings applicationSettings) {
        this.applicationSettings = applicationSettings;
    }

    public AccountSettings getAccountSettings() {
        return accountSettings;
    }

    public void setAccountSettings(AccountSettings accountSettings) {
        this.accountSettings = accountSettings;
    }

    public WorkingTimeSettings getWorkingTimeSettings() {
        return workingTimeSettings;
    }

    public void setWorkingTimeSettings(WorkingTimeSettings workingTimeSettings) {
        this.workingTimeSettings = workingTimeSettings;
    }

    public OvertimeSettings getOvertimeSettings() {
        return overtimeSettings;
    }

    public void setOvertimeSettings(OvertimeSettings overtimeSettings) {
        this.overtimeSettings = overtimeSettings;
    }

    public TimeSettings getTimeSettings() {
        return timeSettings;
    }

    public void setTimeSettings(TimeSettings timeSettings) {
        this.timeSettings = timeSettings;
    }

    public SickNoteSettings getSickNoteSettings() {
        return sickNoteSettings;
    }

    public void setSickNoteSettings(SickNoteSettings sickNoteSettings) {
        this.sickNoteSettings = sickNoteSettings;
    }

    public CalendarSettings getCalendarSettings() {
        return calendarSettings;
    }

    public void setCalendarSettings(CalendarSettings calendarSettings) {
        this.calendarSettings = calendarSettings;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SettingsDto that = (SettingsDto) o;
        return Objects.equals(id, that.id)
            && Objects.equals(absenceTypeSettings, that.absenceTypeSettings)
            && Objects.equals(applicationSettings, that.applicationSettings)
            && Objects.equals(accountSettings, that.accountSettings)
            && Objects.equals(workingTimeSettings, that.workingTimeSettings)
            && Objects.equals(overtimeSettings, that.overtimeSettings)
            && Objects.equals(timeSettings, that.timeSettings)
            && Objects.equals(sickNoteSettings, that.sickNoteSettings)
            && Objects.equals(calendarSettings, that.calendarSettings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, absenceTypeSettings, applicationSettings, accountSettings, workingTimeSettings,
            overtimeSettings, timeSettings, sickNoteSettings, calendarSettings);
    }
}
