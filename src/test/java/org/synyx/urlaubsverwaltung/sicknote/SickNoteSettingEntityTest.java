package org.synyx.urlaubsverwaltung.sicknote;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.sicknote.settings.SickNoteSettingsEntity;

import static org.assertj.core.api.Assertions.assertThat;

class SickNoteSettingEntityTest {

    @Test
    void ensureDefaultValues() {

        final SickNoteSettingsEntity settings = new SickNoteSettingsEntity();
        assertThat(settings.getMaximumSickPayDays()).isEqualTo(42);
        assertThat(settings.getDaysBeforeEndOfSickPayNotification()).isEqualTo(7);
    }
}
