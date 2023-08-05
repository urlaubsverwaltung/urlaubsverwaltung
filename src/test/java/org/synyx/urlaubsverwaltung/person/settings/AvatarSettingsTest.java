package org.synyx.urlaubsverwaltung.person.settings;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AvatarSettingsTest {

    @Test
    void ensureHasDefaultValues() {
        final AvatarSettings avatarSettings = new AvatarSettings();
        assertThat(avatarSettings.isGravatarEnabled()).isFalse();
    }
}
