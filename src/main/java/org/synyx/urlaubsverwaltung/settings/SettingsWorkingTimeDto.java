package org.synyx.urlaubsverwaltung.settings;

import org.synyx.urlaubsverwaltung.absence.TimeSettings;
import org.synyx.urlaubsverwaltung.overtime.OvertimeSettings;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeSettings;

import java.util.Objects;

public class SettingsWorkingTimeDto {

    private Long id;
    private WorkingTimeSettings workingTimeSettings;
    private TimeSettings timeSettings;
    private OvertimeSettings overtimeSettings;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public WorkingTimeSettings getWorkingTimeSettings() {
        return workingTimeSettings;
    }

    public void setWorkingTimeSettings(WorkingTimeSettings workingTimeSettings) {
        this.workingTimeSettings = workingTimeSettings;
    }

    public TimeSettings getTimeSettings() {
        return timeSettings;
    }

    public void setTimeSettings(TimeSettings timeSettings) {
        this.timeSettings = timeSettings;
    }

    public OvertimeSettings getOvertimeSettings() {
        return overtimeSettings;
    }

    public void setOvertimeSettings(OvertimeSettings overtimeSettings) {
        this.overtimeSettings = overtimeSettings;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SettingsWorkingTimeDto that = (SettingsWorkingTimeDto) o;
        return Objects.equals(id, that.id)
            && Objects.equals(workingTimeSettings, that.workingTimeSettings)
            && Objects.equals(timeSettings, that.timeSettings)
            && Objects.equals(overtimeSettings, that.overtimeSettings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, workingTimeSettings, timeSettings, overtimeSettings);
    }
}
