package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.synyx.urlaubsverwaltung.calendarintegration.GoogleCalendarSettings;

/**
 * Google oauth2 settings for calendar integration
 *
 * @param clientId     oauth2 client id
 * @param clientSecret oauth2 client secret
 * @param refreshToken oauth2 refresh token
 * @param calendarId   id of the google calendar with which the application is integrated
 */
public record GoogleCalendarSettingsDTO(String clientId, String clientSecret, String refreshToken, String calendarId) {

    public static GoogleCalendarSettingsDTO of(GoogleCalendarSettings googleCalendarSettings) {
        if (googleCalendarSettings == null) {
            return null;
        }

        return new GoogleCalendarSettingsDTO(googleCalendarSettings.getClientId(), googleCalendarSettings.getClientSecret(), googleCalendarSettings.getRefreshToken(), googleCalendarSettings.getCalendarId());
    }

    public GoogleCalendarSettings toGoogleCalendarSettings() {

        GoogleCalendarSettings googleCalendarSettings = new GoogleCalendarSettings();

        googleCalendarSettings.setClientId(this.clientId);
        googleCalendarSettings.setClientSecret(this.clientSecret);
        googleCalendarSettings.setRefreshToken(this.refreshToken);
        googleCalendarSettings.setCalendarId(this.calendarId);

        return googleCalendarSettings;
    }
}
