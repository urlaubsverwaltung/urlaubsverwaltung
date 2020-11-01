package org.synyx.urlaubsverwaltung.settings;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Unit test for {@link org.synyx.urlaubsverwaltung.settings.Settings}.
 */
class SettingsTest {

    @Test
    void ensureDefaultValues() {

        Settings settings = new Settings();

        assertThat(settings.getAbsenceSettings()).isNotNull();
        assertThat(settings.getWorkingTimeSettings()).isNotNull();
        assertThat(settings.getCalendarSettings()).isNotNull();
    }
}
