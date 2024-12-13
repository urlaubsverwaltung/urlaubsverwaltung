package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.calendarintegration.GoogleCalendarSettings;

import static org.assertj.core.api.Assertions.assertThat;

class GoogleCalendarSettingsDTOTest {

    @Test
    void happyPathDTOToGoogleCalendarSettings() {
        GoogleCalendarSettingsDTO dto = new GoogleCalendarSettingsDTO("calendarId", "clientId", "clientSecret", "refreshToken");

        GoogleCalendarSettings googleCalendarSettings = dto.toGoogleCalendarSettings();

        assertThat(googleCalendarSettings.getCalendarId()).isEqualTo(dto.calendarId());
        assertThat(googleCalendarSettings.getClientId()).isEqualTo(dto.clientId());
        assertThat(googleCalendarSettings.getClientSecret()).isEqualTo(dto.clientSecret());
        assertThat(googleCalendarSettings.getRefreshToken()).isEqualTo(dto.refreshToken());
    }

    @Test
    void happyPathGoogleCalendarSettingsToDTO() {
        GoogleCalendarSettings googleCalendarSettings = new GoogleCalendarSettings();
        googleCalendarSettings.setCalendarId("calendarId");
        googleCalendarSettings.setClientId("clientId");
        googleCalendarSettings.setClientSecret("clientSecret");
        googleCalendarSettings.setRefreshToken("refreshToken");

        GoogleCalendarSettingsDTO dto = GoogleCalendarSettingsDTO.of(googleCalendarSettings);

        assertThat(dto.calendarId()).isEqualTo(googleCalendarSettings.getCalendarId());
        assertThat(dto.clientId()).isEqualTo(googleCalendarSettings.getClientId());
        assertThat(dto.clientSecret()).isEqualTo(googleCalendarSettings.getClientSecret());
        assertThat(dto.refreshToken()).isEqualTo(googleCalendarSettings.getRefreshToken());
    }


    @Test
    void googleCalendarSettingsToDTOHandlesNullValue() {
        assertThat(GoogleCalendarSettingsDTO.of(null)).isNull();
    }
}
