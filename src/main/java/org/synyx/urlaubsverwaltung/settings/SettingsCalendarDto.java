package org.synyx.urlaubsverwaltung.settings;

import org.synyx.urlaubsverwaltung.calendar.TimeSettings;

import java.util.Objects;

public class SettingsCalendarDto {

    private Long id;
    private TimeSettings timeSettings;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TimeSettings getTimeSettings() {
        return timeSettings;
    }

    public void setTimeSettings(TimeSettings timeSettings) {
        this.timeSettings = timeSettings;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SettingsCalendarDto that = (SettingsCalendarDto) o;
        return Objects.equals(id, that.id)
            && Objects.equals(timeSettings, that.timeSettings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, timeSettings);
    }
}
