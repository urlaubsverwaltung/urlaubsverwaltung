package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.calendar.TimeSettings;

import static org.assertj.core.api.Assertions.assertThat;

class TimeSettingsDTOTest {

    @Test
    void happyPathTimeSettingsToDTO() {
        final TimeSettings timeSettings = new TimeSettings();
        timeSettings.setTimeZoneId("Europe/Berlin");
        timeSettings.setWorkDayBeginHour(8);
        timeSettings.setWorkDayBeginMinute(0);
        timeSettings.setWorkDayEndHour(16);
        timeSettings.setWorkDayEndMinute(0);

        final TimeSettingsDTO dto = TimeSettingsDTO.of(timeSettings);
        assertThat(dto).isEqualTo(new TimeSettingsDTO("Europe/Berlin", 8, 0, 16, 0));
    }

    @Test
    void happyPathDTOToTimeSettings() {
        final TimeSettingsDTO dto = new TimeSettingsDTO("Europe/Berlin", 8, 0, 16, 0);

        final TimeSettings timeSettings = dto.toTimeSettings();
        assertThat(timeSettings.getTimeZoneId()).isEqualTo("Europe/Berlin");
        assertThat(timeSettings.getWorkDayBeginHour()).isEqualTo(8);
        assertThat(timeSettings.getWorkDayBeginMinute()).isZero();
        assertThat(timeSettings.getWorkDayEndHour()).isEqualTo(16);
        assertThat(timeSettings.getWorkDayEndMinute()).isZero();
    }
}
