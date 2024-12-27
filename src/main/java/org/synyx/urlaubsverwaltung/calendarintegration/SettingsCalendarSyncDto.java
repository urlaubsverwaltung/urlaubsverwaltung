package org.synyx.urlaubsverwaltung.calendarintegration;

import java.util.Objects;

public class SettingsCalendarSyncDto {

    private Long id;
    private CalendarSettings calendarSettings;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
        SettingsCalendarSyncDto that = (SettingsCalendarSyncDto) o;
        return Objects.equals(id, that.id) && Objects.equals(calendarSettings, that.calendarSettings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, calendarSettings);
    }
}
