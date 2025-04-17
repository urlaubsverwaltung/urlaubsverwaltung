package org.synyx.urlaubsverwaltung.settings;

import org.synyx.urlaubsverwaltung.publicholiday.PublicHolidaysSettings;

import java.util.Objects;

public class SettingsPublicHolidayDto {

    private Long id;
    private PublicHolidaysSettings publicHolidaysSettings;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PublicHolidaysSettings getPublicHolidaysSettings() {
        return publicHolidaysSettings;
    }

    public void setPublicHolidaysSettings(PublicHolidaysSettings publicHolidaysSettings) {
        this.publicHolidaysSettings = publicHolidaysSettings;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SettingsPublicHolidayDto that = (SettingsPublicHolidayDto) o;
        return Objects.equals(id, that.id)
            && Objects.equals(publicHolidaysSettings, that.publicHolidaysSettings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, publicHolidaysSettings);
    }
}
