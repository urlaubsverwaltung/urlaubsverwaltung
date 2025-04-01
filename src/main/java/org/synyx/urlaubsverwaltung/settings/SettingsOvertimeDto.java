package org.synyx.urlaubsverwaltung.settings;

import org.synyx.urlaubsverwaltung.overtime.OvertimeSettings;

import java.util.Objects;

public class SettingsOvertimeDto {

    private Long id;
    private OvertimeSettings overtimeSettings;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
        SettingsOvertimeDto that = (SettingsOvertimeDto) o;
        return Objects.equals(id, that.id)
            && Objects.equals(overtimeSettings, that.overtimeSettings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, overtimeSettings);
    }
}
