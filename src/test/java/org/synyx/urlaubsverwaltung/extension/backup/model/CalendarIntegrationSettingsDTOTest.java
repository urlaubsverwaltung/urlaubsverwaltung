package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.calendarintegration.CalendarSettings;

import static org.assertj.core.api.Assertions.assertThat;

class CalendarIntegrationSettingsDTOTest {

    @Test
    void happyPathWithGoogleCalendarSettings() {
        GoogleCalendarSettingsDTO googleSettingsDTO = new GoogleCalendarSettingsDTO("testClientId", "testClientSecret", "testRefreshToken", "testCalendarId");
        CalendarIntegrationSettingsDTO dto = new CalendarIntegrationSettingsDTO(1L, "Google", googleSettingsDTO);
        CalendarSettings settings = dto.toCalendarSettings();

        assertThat(settings).isNotNull();
        assertThat(settings.getId()).isNull();
        assertThat(settings.getProvider()).isEqualTo(dto.provider());
        assertThat(settings.getGoogleCalendarSettings()).isNotNull();
        assertThat(settings.getGoogleCalendarSettings().getClientId()).isEqualTo(googleSettingsDTO.clientId());
        assertThat(settings.getGoogleCalendarSettings().getClientSecret()).isEqualTo(googleSettingsDTO.clientSecret());
        assertThat(settings.getGoogleCalendarSettings().getRefreshToken()).isEqualTo(googleSettingsDTO.refreshToken());
        assertThat(settings.getGoogleCalendarSettings().getCalendarId()).isEqualTo(googleSettingsDTO.calendarId());
    }

    @Test
    void happyPathWithoutGoogleCalendarSettings() {
        CalendarIntegrationSettingsDTO dto = new CalendarIntegrationSettingsDTO(1L, "MyDummy", null);
        CalendarSettings settings = dto.toCalendarSettings();

        assertThat(settings).isNotNull();
        assertThat(settings.getId()).isNull();
        assertThat(settings.getProvider()).isEqualTo(dto.provider());
        assertThat(settings.getGoogleCalendarSettings()).isNull();
    }

}
