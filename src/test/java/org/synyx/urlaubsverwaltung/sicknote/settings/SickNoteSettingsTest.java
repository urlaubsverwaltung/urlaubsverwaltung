package org.synyx.urlaubsverwaltung.sicknote.settings;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SickNoteSettingsTest {

    @Test
    void ensureDefaultValues() {

        final SickNoteSettings settings = new SickNoteSettings();
        assertThat(settings.getMaximumSickPayDays()).isEqualTo(42);
        assertThat(settings.getDaysBeforeEndOfSickPayNotification()).isEqualTo(7);
    }
}
