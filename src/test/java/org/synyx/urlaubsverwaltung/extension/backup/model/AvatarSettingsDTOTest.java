package org.synyx.urlaubsverwaltung.extension.backup.model;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.person.settings.AvatarSettings;

import static org.assertj.core.api.Assertions.assertThat;

class AvatarSettingsDTOTest {

    @Test
    void happyPathAvatarSettingsToDTO() {
        AvatarSettings avatarSettings = new AvatarSettings();
        avatarSettings.setGravatarEnabled(true);
        AvatarSettingsDTO avatarSettingsDTO = AvatarSettingsDTO.of(avatarSettings);

        assertThat(avatarSettingsDTO).isNotNull();
        assertThat(avatarSettingsDTO.gravatarEnabled()).isEqualTo(avatarSettings.isGravatarEnabled());
    }

    @Test
    void happyPathDTOToAvatarSettings() {
        AvatarSettingsDTO avatarSettingsDTO = new AvatarSettingsDTO(true);
        AvatarSettings avatarSettings = avatarSettingsDTO.toAvatarSettings();

        assertThat(avatarSettings).isNotNull();
        assertThat(avatarSettings.isGravatarEnabled()).isEqualTo(avatarSettingsDTO.gravatarEnabled());
    }
}
