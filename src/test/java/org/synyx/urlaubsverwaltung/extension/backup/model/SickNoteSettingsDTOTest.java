package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.sicknote.settings.SickNoteSettings;

import static org.assertj.core.api.Assertions.assertThat;

class SickNoteSettingsDTOTest {

    @Test
    void happyPathSickNoteSettingsToDTO() {
        final SickNoteSettings sickNoteSettings = new SickNoteSettings();
        sickNoteSettings.setMaximumSickPayDays(20);
        sickNoteSettings.setDaysBeforeEndOfSickPayNotification(10);
        sickNoteSettings.setUserIsAllowedToSubmitSickNotes(false);
        sickNoteSettings.setMissingAubNotificationWorkDay(5);

        final SickNoteSettingsDTO expected = new SickNoteSettingsDTO(20, 10, false, 5);

        final SickNoteSettingsDTO dto = SickNoteSettingsDTO.of(sickNoteSettings);

        assertThat(dto).isEqualTo(expected);
    }

    @Test
    void happyPathDTOToSickNoteSettings() {
        final SickNoteSettingsDTO dto = new SickNoteSettingsDTO(15, 7, true, 4);

        final SickNoteSettings sickNoteSettings = dto.toSickNoteSettings();

        assertThat(sickNoteSettings.getMaximumSickPayDays()).isEqualTo(15);
        assertThat(sickNoteSettings.getDaysBeforeEndOfSickPayNotification()).isEqualTo(7);
        assertThat(sickNoteSettings.getUserIsAllowedToSubmitSickNotes()).isTrue();
        assertThat(sickNoteSettings.getMissingAubNotificationWorkDay()).isEqualTo(4);
    }

    @Test
    void ensureBackupWithoutMissingAubNotificationWorkDayKeepsTheDefault() {
        final SickNoteSettingsDTO dto = new SickNoteSettingsDTO(15, 7, true, null);

        final SickNoteSettings sickNoteSettings = dto.toSickNoteSettings();

        assertThat(sickNoteSettings.getMissingAubNotificationWorkDay()).isEqualTo(3);
    }

}
