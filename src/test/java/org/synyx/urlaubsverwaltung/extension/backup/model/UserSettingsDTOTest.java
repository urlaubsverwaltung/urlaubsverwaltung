package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.user.UserSettingsEntity;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class UserSettingsDTOTest {

    @Test
    void happyPath() {
        UserSettingsDTO dto = new UserSettingsDTO(ThemeDTO.SYSTEM, Locale.GERMAN, Locale.ENGLISH, new UserNotificationSettingsDTO(false), new UserPaginationSettingsDTO(20));

        final UserSettingsEntity entity = dto.toUserSettingsEntity(1L);

        assertThat(entity.getPersonId()).isEqualTo(1L);
        assertThat(entity.getTheme()).isEqualTo(dto.theme().toTheme());
        assertThat(entity.getLocale()).isEqualTo(dto.locale());
        assertThat(entity.getLocaleBrowserSpecific()).isEqualTo(dto.browserSpecific());
    }

}
