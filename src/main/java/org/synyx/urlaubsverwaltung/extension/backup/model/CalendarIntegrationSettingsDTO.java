package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.synyx.urlaubsverwaltung.calendarintegration.CalendarSettings;

public record CalendarIntegrationSettingsDTO(Long id, String provider,
                                             GoogleCalendarSettingsDTO googleCalendarSettings) {

    public CalendarSettings toCalendarSettings() {
        CalendarSettings calendarSettings = new CalendarSettings();
        calendarSettings.setId(null); // we don't import the id, database will generate a new one!
        calendarSettings.setProvider(this.provider);
        calendarSettings.setGoogleCalendarSettings(this.googleCalendarSettings != null ? this.googleCalendarSettings.toGoogleCalendarSettings() : null);
        return calendarSettings;
    }
}
