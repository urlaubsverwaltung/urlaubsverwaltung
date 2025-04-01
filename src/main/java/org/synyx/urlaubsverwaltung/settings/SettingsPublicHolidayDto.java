package org.synyx.urlaubsverwaltung.settings;

import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeSettings;

import java.util.Objects;

public class SettingsPublicHolidayDto {

    private Long id;
    private WorkingTimeSettings workingTimeSettings;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SettingsPublicHolidayDto that = (SettingsPublicHolidayDto) o;
        return Objects.equals(id, that.id)
            && Objects.equals(workingTimeSettings, that.workingTimeSettings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, workingTimeSettings);
    }
}
