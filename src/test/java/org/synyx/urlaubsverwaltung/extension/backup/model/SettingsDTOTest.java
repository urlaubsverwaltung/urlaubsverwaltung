package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.settings.Settings;

import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;

class SettingsDTOTest {

    @Test
    void happyPathToSettings() {
        final ApplicationSettingsDTO applicationSettings = new ApplicationSettingsDTO(1, 2, true, true, 3, true, 4, true, 5);
        final AccountSettingsDTO accountSettings = new AccountSettingsDTO(25, 30, 31, Month.DECEMBER, true);
        final WorkingTimeSettingsDTO workingTimeSettings = new WorkingTimeSettingsDTO(DayLengthDTO.FULL, DayLengthDTO.FULL, DayLengthDTO.FULL, DayLengthDTO.FULL, DayLengthDTO.FULL, DayLengthDTO.ZERO, DayLengthDTO.ZERO, DayLengthDTO.MORNING, DayLengthDTO.MORNING, FederalStateDTO.GERMANY_BADEN_WUERTTEMBERG);
        final OverTimeSettingsDTO overTimeSettings = new OverTimeSettingsDTO(true, true, true, 100, 10, 5);
        final TimeSettingsDTO timeSettings = new TimeSettingsDTO("UTC", 9, 17);
        final SickNoteSettingsDTO sickNoteSettings = new SickNoteSettingsDTO(30, 5, true);
        final AvatarSettingsDTO avatarSettings = new AvatarSettingsDTO(false);

        final SettingsDTO dto = new SettingsDTO(1L, applicationSettings, accountSettings, workingTimeSettings, overTimeSettings, timeSettings, sickNoteSettings, avatarSettings);

        final Settings settings = dto.toSettings();

        assertThat(settings.getApplicationSettings()).usingRecursiveComparison().isEqualTo(applicationSettings.toApplicationSettings());
        assertThat(settings.getAccountSettings()).usingRecursiveComparison().isEqualTo(accountSettings.toAccountSettings());
        assertThat(settings.getWorkingTimeSettings()).usingRecursiveComparison().isEqualTo(workingTimeSettings.toWorkingTimeSettings());
        assertThat(settings.getOvertimeSettings()).usingRecursiveComparison().isEqualTo(overTimeSettings.toOverTimeSettings());
        assertThat(settings.getTimeSettings()).usingRecursiveComparison().isEqualTo(timeSettings.toTimeSettings());
        assertThat(settings.getSickNoteSettings()).usingRecursiveComparison().isEqualTo(sickNoteSettings.toSickNoteSettings());
        assertThat(settings.getAvatarSettings()).usingRecursiveComparison().isEqualTo(avatarSettings.toAvatarSettings());
    }

}
